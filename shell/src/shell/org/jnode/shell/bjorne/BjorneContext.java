/*
 * $Id: Command.java 3772 2008-02-10 15:02:53Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jnode.shell.bjorne;

import static org.jnode.shell.bjorne.BjorneInterpreter.REDIR_CLOBBER;
import static org.jnode.shell.bjorne.BjorneInterpreter.REDIR_DGREAT;
import static org.jnode.shell.bjorne.BjorneInterpreter.REDIR_DLESS;
import static org.jnode.shell.bjorne.BjorneInterpreter.REDIR_DLESSDASH;
import static org.jnode.shell.bjorne.BjorneInterpreter.REDIR_GREAT;
import static org.jnode.shell.bjorne.BjorneInterpreter.REDIR_GREATAND;
import static org.jnode.shell.bjorne.BjorneInterpreter.REDIR_LESS;
import static org.jnode.shell.bjorne.BjorneInterpreter.REDIR_LESSAND;
import static org.jnode.shell.bjorne.BjorneInterpreter.REDIR_LESSGREAT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandThread;
import org.jnode.shell.PathnamePattern;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellFailureException;
import org.jnode.shell.ShellSyntaxException;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.io.CommandIOMarker;
import org.jnode.shell.io.CommandInput;
import org.jnode.shell.io.CommandOutput;

/**
 * This class holds the shell variable and stream state for a bjorne shell
 * context. A parent context persists between calls to the shell's
 * <code>interpret</code> method to hold the global shell variables. Others
 * are created as required to hold the (umm) lexically scoped state for
 * individual commands, pipelines, subshells and function calls.
 * 
 * @author crawley@jnode.org
 */
public class BjorneContext {
    
    public static final CommandIOMarker PIPE_IN = new CommandIOMarker("PIPEIN");
    
    public static final CommandIOMarker PIPE_OUT = new CommandIOMarker("PIPEOUT");

    private static final int NONE = 0;

    private static final int PREHASH = 1;

    private static final int HASH = 2;

    private static final int DHASH = 3;

    private static final int PERCENT = 4;

    private static final int DPERCENT = 5;

    private static final int HYPHEN = 6;

    private static final int COLONHYPHEN = 7;

    private static final int EQUALS = 8;

    private static final int COLONEQUALS = 9;

    private static final int PLUS = 10;

    private static final int COLONPLUS = 11;

    private static final int QUERY = 12;

    private static final int COLONQUERY = 13;

    private final BjorneInterpreter interpreter;

    private Map<String, VariableSlot> variables;

    private String command = "";

    private List<String> args = new ArrayList<String>();

    private int lastReturnCode;

    private int shellPid;

    private int lastAsyncPid;

    private boolean tildeExpansion = true;
    
    private boolean globbing = true;

    private String options = "";

    private StreamHolder[] holders;

    private boolean echoExpansions;

    public BjorneContext(BjorneInterpreter interpreter, StreamHolder[] holders) {
        this.interpreter = interpreter;
        this.holders = holders;
        this.variables = new HashMap<String, VariableSlot>();
    }

    public BjorneContext(BjorneInterpreter interpreter) {
        this(interpreter, new StreamHolder[] {
            new StreamHolder(CommandLine.DEFAULT_STDIN, false),
            new StreamHolder(CommandLine.DEFAULT_STDOUT, false),
            new StreamHolder(CommandLine.DEFAULT_STDERR, false),
            new StreamHolder(CommandLine.DEFAULT_STDERR, false)});
    }

    /**
     * Create a copy of a context with the same initial variable bindings and
     * streams. Stream ownership is not transferred.
     * 
     * @param parent the context that gives us our initial state.
     */
    public BjorneContext(BjorneContext parent) {
        this.interpreter = parent.interpreter;
        this.holders = copyStreamHolders(parent.holders);
        this.variables = copyVariables(parent.variables);
        this.globbing = parent.globbing;
        this.tildeExpansion = parent.tildeExpansion;
    }
    
    public boolean isTildeExpansion() {
        return tildeExpansion;
    }

    public void setTildeExpansion(boolean tildeExpansion) {
        this.tildeExpansion = tildeExpansion;
    }

    public boolean isGlobbing() {
        return globbing;
    }

    public void setGlobbing(boolean globbing) {
        this.globbing = globbing;
    }

    /**
     * Create a deep copy of some variable bindings
     */
    private Map<String, VariableSlot> copyVariables(
            Map<String, VariableSlot> variables) {
        Map<String, VariableSlot> res = new HashMap<String, VariableSlot>(
                variables.size());
        for (Map.Entry<String, VariableSlot> entry : variables.entrySet()) {
            res.put(entry.getKey(), new VariableSlot(entry.getValue()));
        }
        return res;
    }

    /**
     * Create a copy of some stream holders without passing ownership.
     */
    public static StreamHolder[] copyStreamHolders(StreamHolder[] holders) {
        StreamHolder[] res = new StreamHolder[holders.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new StreamHolder(holders[i]);
        }
        return res;
    }
    
    StreamHolder[] getCopyOfHolders() {
        return copyStreamHolders(holders);
    }
    
    /**
     * This method implements 'NAME=VALUE'. If variable NAME does not exist, it
     * is created as an unexported shell variable.
     * 
     * @param name the name of the variable to be set
     * @param value a non-null value for the variable
     */
    public void setVariable(String name, String value) {
        value.length(); // Check that the value is non-null.
        VariableSlot var = variables.get(name);
        if (var == null) {
            variables.put(name, new VariableSlot(value, false));
        } else {
            var.value = value;
        }
    }

    /**
     * Test if the variable is currently set here in this context.
     * 
     * @param name the name of the variable to be tested
     * @return <code>true</code> if the variable is set.
     */
    public boolean isVariableSet(String name) {
        return variables.get(name) != null;
    }

    /**
     * This method implements 'unset NAME'
     * 
     * @param name the name of the variable to be unset
     */
    public void unsetVariableValue(String name) {
        variables.remove(name);
    }

    /**
     * This method implements 'export NAME' or 'unexport NAME'.
     * 
     * @param name the name of the variable to be exported / unexported
     */
    public void setExported(String name, boolean exported) {
        VariableSlot var = variables.get(name);
        if (var == null) {
            if (exported) {
                variables.put(name, new VariableSlot("", exported));
            }
        } else {
            var.exported = exported;
        }
    }

    /**
     * Perform expand-and-split processing on an array of word tokens. The resulting
     * wordTokens are assembled into a CommandLine.  
     * 
     * @param tokens the tokens to be expanded and split into words
     * @return the command line
     * @throws ShellException
     */
    public CommandLine expandAndSplit(Iterable<BjorneToken> tokens) throws ShellException {
        List<BjorneToken> wordTokens = new LinkedList<BjorneToken>();
        for (BjorneToken token : tokens) {
            String word = token.getText();
            CharSequence expanded = expand(word);
            if (expanded == word) {
                splitAndAppend(token, wordTokens);
            } else {
                BjorneToken newToken = token.remake(expanded);
                if (newToken != null) {
                    splitAndAppend(newToken, wordTokens);
                }
            }
        }
        return makeCommandLine(wordTokens);
    }
    
    public CommandLine expandAndSplit(BjorneToken[] tokens) throws ShellException {
        return expandAndSplit(Arrays.asList(tokens));
    }

    /**
     * Perform expand-and-split processing on a sequence of characters.  This method is only 
     * used in tests at the moment, and probably should be removed.  (It does not set token
     *  attributes properly ...)
     * 
     * @param text the characters to be split
     * @return the command line 
     * @throws ShellException
     */
    public List<BjorneToken> expandAndSplit(CharSequence text) throws ShellException {
        return split(expand(text));
    }

    private CommandLine makeCommandLine(List<BjorneToken> wordTokens) {
        if (globbing || tildeExpansion) {
            List<BjorneToken> globbedWordTokens = new LinkedList<BjorneToken>();
            for (BjorneToken wordToken : wordTokens) {
                if (tildeExpansion) {
                    wordToken = tildeExpand(wordToken);
                }
                if (globbing) {
                    globAndAppend(wordToken, globbedWordTokens);
                } else {
                    globbedWordTokens.add(wordToken);
                }
            }
            wordTokens = globbedWordTokens;
        }
        int nosWords = wordTokens.size();
        if (nosWords == 0) {
            return new CommandLine(null, null);
        } else {
            BjorneToken alias = wordTokens.remove(0);
            BjorneToken[] args = wordTokens.toArray(new BjorneToken[nosWords - 1]);
            return new CommandLine(alias, args, null);
        }
    }
    
    private BjorneToken tildeExpand(BjorneToken wordToken) {
        String word = wordToken.getText();
        if (word.startsWith("~")) {
            int slashPos = word.indexOf(File.separatorChar);
            String name = (slashPos >= 0) ? word.substring(1, slashPos) : "";
            // FIXME ... support "~username" when we have some kind of user info / management.
            String home = (name.length() == 0) ? System.getProperty("user.home", "") : "";
            if (home.length() == 0) {
                return wordToken;
            } 
            String expansion = (slashPos == -1) ?
                home : (home + word.substring(slashPos));
            return wordToken.remake(expansion);
        } else {
            return wordToken;
        }
    }
    
    private void globAndAppend(BjorneToken wordToken, List<BjorneToken> globbedWordTokens) {
        // Try to deal with the 'not-a-pattern' case quickly and cheaply.
        String word = wordToken.getText();
        if (!PathnamePattern.isPattern(word)) {
            globbedWordTokens.add(wordToken);
            return;
        }
        PathnamePattern pattern = PathnamePattern.compile(word);
        LinkedList<String> paths = pattern.expand(new File("."));
        // If it doesn't match anything, a pattern 'expands' to itself.
        if (paths.isEmpty()) {
            globbedWordTokens.add(wordToken);
        } else {
            for (String path : paths) {
                globbedWordTokens.add(wordToken.remake(path));
            }
        }
    }

    /**
     * Split a character sequence into word tokens, dealing with and removing any
     * non-literal
     * 
     * @param text the characters to be split
     * @return the destination for the tokens.
     * @throws ShellException
     */
    public List<BjorneToken> split(CharSequence text) throws ShellException {
        List<BjorneToken> wordTokens = new LinkedList<BjorneToken>();
        splitAndAppend(new BjorneToken(BjorneToken.TOK_WORD, text.toString(), -1, -1), wordTokens);
        return wordTokens;
    }
    
    /**
     * Split a token into a series of word tokens, dealing with and removing any
     * non-literal quotes.  The resulting tokens are appended to a supplied list.
     * 
     * @param token the token to be split
     * @param wordTokens the destination for the tokens.
     * @throws ShellException
     */
    private void splitAndAppend(BjorneToken token, List<BjorneToken> wordTokens)
        throws ShellException {
        String text = token.getText();
        StringBuffer sb = null;
        int len = text.length();
        int quote = 0;
        for (int i = 0; i < len; i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '"':
                case '\'':
                    if (quote == 0) {
                        quote = ch;
                        if (sb == null) {
                            sb = new StringBuffer();
                        }
                    } else if (quote == ch) {
                        quote = 0;
                    } else {
                        sb = accumulate(sb, ch);
                    }
                    break;
                case ' ':
                case '\t':
                    if (quote == 0) {
                        if (sb != null) {
                            wordTokens.add(token.remake(sb));
                            sb = null;
                        }
                    } else {
                        sb = accumulate(sb, ch);
                    }
                    break;
                case '\\':
                    if (i + 1 < len) {
                        ch = text.charAt(++i);
                    }
                    sb = accumulate(sb, ch);
                    break;
                default:
                    sb = accumulate(sb, ch);
                    break;
            }
        }
        if (sb != null) {
            wordTokens.add(token.remake(sb));
        }
    }

    private StringBuffer runBacktickCommand(String commandLine) throws ShellException {
        StringWriter capture = new StringWriter();
        interpreter.interpret(interpreter.getShell(), commandLine, capture, false);
        StringBuffer output = capture.getBuffer();
        while (output.length() > 0 && output.charAt(output.length() - 1) == '\n') {
            output.setLength(output.length() - 1);
        }
        return output;
    }

    private StringBuffer accumulate(StringBuffer sb, char ch) {
        if (sb == null) {
            sb = new StringBuffer();
        }
        sb.append(ch);
        return sb;
    }

    /**
     * Perform '$' expansion and backtick substitution. Any quotes and escapes should be preserved (?!?!?)
     * 
     * @param text the characters to be expanded
     * @return the result of the expansion.
     * @throws ShellException
     */
    public CharSequence expand(CharSequence text) throws ShellException {
        CharIterator ci = new CharIterator(text);
        StringBuffer sb = new StringBuffer(text.length());
        char quote = 0;
        int backtickStart = -1;
        int ch = ci.nextCh();
        while (ch != -1) {
            switch (ch) {
                case '"':
                case '\'':
                    if (quote == 0) {
                        quote = (char) ch;
                    } else if (quote == ch) {
                        quote = 0;
                    }
                    sb.append((char) ch);
                    break;
                case '`':
                    if (backtickStart == -1) {
                        backtickStart = sb.length();
                    } else {
                        StringBuffer tmp = runBacktickCommand(sb.substring(backtickStart));
                        sb.replace(backtickStart, sb.length(), tmp.toString());
                        backtickStart = -1;
                    }
                    break;
                case ' ':
                case '\t':
                    sb.append(' ');
                    while ((ch = ci.peekCh()) == ' ' || ch == '\t') {
                        ci.nextCh();
                    }
                    break;
                case '\\':
                    sb.append((char) ch);
                    if ((ch = ci.nextCh()) != -1) {
                        sb.append((char) ch);
                    }
                    break;
                case '$':
                    if (quote == '\'') {
                        sb.append('$');
                    } else {
                        sb.append(dollarExpansion(ci, quote));
                    }
                    break;

                default:
                    sb.append((char) ch);
                    break;
            }
            ch = ci.nextCh();
        }
        if (backtickStart != -1) {
            throw new ShellFailureException("unmatched '`'");
        }
        return sb;
    }

    private String dollarExpansion(CharIterator ci, char quote) throws ShellSyntaxException {
        int ch = ci.nextCh();
        switch (ch) {
            case -1:
                return "$";
            case '{':
                return dollarBraceExpansion(ci);
            case '(':
                return dollarParenExpansion(ci);
            case '$':
            case '#':
            case '@':
            case '*':
            case '?':
            case '!':
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return specialVariable(ch);
            default:
                StringBuffer sb = new StringBuffer().append((char) ch);
                ch = ci.peekCh();
                while ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= 'a' && ch <= 'z') || ch == '_') {
                    sb.append((char) ch);
                    ci.nextCh();
                    ch = ci.peekCh();
                }
                VariableSlot var = variables.get(sb.toString());
                return (var != null) ? var.value : "";
        }
    }

    private String dollarBraceExpansion(CharIterator ci) throws ShellSyntaxException {
        // Scan to the '}' that matches the '${'
        StringBuffer sb = new StringBuffer();
        int braceLevel = 1;
        int ch = ci.nextCh();
        int quote = 0;
    LOOP: 
        while (ch != -1) {
            switch (ch) {
                case '}':
                    if (quote == 0) {
                        braceLevel--;
                        if (braceLevel == 0) {
                            break LOOP;
                        }
                    }
                    break;
                case '{':
                    if (quote == 0) {
                        braceLevel++;
                    }
                    break;
                case '\\':
                    sb.append((char) ch);
                    ch = ci.nextCh();
                    break;
                case '"':
                case '\'':
                    if (quote == 0) {
                        quote = ch;
                    } else if (quote == ch) {
                        quote = 0;
                    }
                    break;
                default:
                    // Nothing to do
                    break;
            }
            if (ch != -1) {
                sb.append((char) ch);
            }
            ch = ci.nextCh();
        }

        // Deal with case where the braces are empty ...
        if (sb.length() == 0) {
            return "";
        }

        // Extract the parameter name, noting a leading '#' operator
        int operator = NONE;
        int i;
    LOOP: 
        for (i = 0; i < sb.length(); i++) {
            char ch2 = sb.charAt(i);
            switch (ch2) {
                case '#':
                    if (i == 0) {
                        operator = PREHASH;
                    } else {
                        break LOOP;
                    }
                    break;
                case '%':
                case ':':
                case '=':
                case '?':
                case '+':
                case '-':
                    break LOOP;
                default:
                    // Include this in the parameter name for now.
                    break;
            }
        }

        String parameter = sb.substring(operator == NONE ? 0 : 1, i);
        String word = null;

        if (i < sb.length()) {
            // Work out what the operator is ...
            char opch = sb.charAt(i);
            char opch2 = (i + 1 < sb.length()) ? sb.charAt(i + 1) : (char) 0;
            switch (opch) {
                case '#':
                    operator = (opch2 == '#') ? DHASH : HASH;
                    break;
                case '%':
                    operator = (opch2 == '%') ? DPERCENT : PERCENT;
                    break;
                case ':':
                    switch (opch2) {
                        case '=':
                            operator = COLONEQUALS;
                            break;
                        case '+':
                            operator = COLONPLUS;
                            break;
                        case '?':
                            operator = COLONQUERY;
                            break;
                        case '-':
                            operator = COLONHYPHEN;
                            break;
                        default:
                            throw new ShellSyntaxException("bad substitution");
                    }
                    break;
                case '=':
                    operator = EQUALS;
                    break;
                case '?':
                    operator = QUERY;
                    break;
                case '+':
                    operator = PLUS;
                    break;
                case '-':
                    operator = HYPHEN;
                    break;
                default:
                    throw new ShellFailureException("bad state");
            }
            // Adjust for two-character operators
            switch (operator) {
                case EQUALS:
                case QUERY:
                case PLUS:
                case HYPHEN:
                case HASH:
                case PERCENT:
                    break;
                default:
                    i++;
                    break;
            }
            // Extract the word
            if (i >= sb.length()) {
                throw new ShellSyntaxException("bad substitution");
            }
            word = sb.substring(i);
        }
        String value = variable(parameter);
        switch (operator) {
            case NONE:
                return (value != null) ? value : "";
            case PREHASH:
                return (value != null) ? Integer.toString(value.length()) : "0";
            default:
                throw new ShellFailureException("not implemented");
        }
    }

    private String variable(String parameter) throws ShellSyntaxException {
        if (parameter.length() == 1) {
            String tmp = specialVariable(parameter.charAt(0));
            if (tmp != null) {
                return tmp;
            }
        }
        if (BjorneToken.isName(parameter)) {
            VariableSlot var = variables.get(parameter);
            return (var != null) ? var.value : null;
        } else {
            try {
                int argNo = Integer.parseInt(parameter);
                return argVariable(argNo);
            } catch (NumberFormatException ex) {
                throw new ShellSyntaxException("bad substitution");
            }
        }
    }

    private String specialVariable(int ch) {
        switch (ch) {
            case '$':
                return Integer.toString(shellPid);
            case '#':
                return Integer.toString(args.size());
            case '@':
            case '*':
                throw new ShellFailureException("not implemented");
            case '?':
                return Integer.toString(lastReturnCode);
            case '!':
                return Integer.toString(lastAsyncPid);
            case '-':
                return options;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return argVariable(ch - '0');
            default:
                return null;
        }
    }

    private String argVariable(int argNo) {
        if (argNo == 0) {
            return command;
        } else if (argNo <= args.size()) {
            return args.get(argNo);
        } else {
            return "";
        }
    }

    public boolean isSet(String name) {
        return variables.get(name) != null;
    }

    private String dollarParenExpansion(CharIterator ci) {
        throw new ShellFailureException("not implemented");
    }

    int execute(CommandLine command, CommandIO[] streams) throws ShellException {
        if (echoExpansions) {
            StringBuilder sb = new StringBuilder();
            sb.append(" + ").append(command.getCommandName());
            for (String arg : command.getArguments()) {
                sb.append(" ").append(arg);
            }
            streams[2].getPrintWriter().println(sb);
        }
        lastReturnCode = interpreter.executeCommand(command, this, streams);
        return lastReturnCode;
    }

    PrintStream resolvePrintStream(CommandIO commandIOIF) {
        return interpreter.resolvePrintStream(commandIOIF);
    }

    InputStream resolveInputStream(CommandIO stream) {
        return interpreter.resolveInputStream(stream);
    }

    CommandIO getStream(int index) {
        if (index < 0) {
            throw new ShellFailureException("negative stream index");
        } else if (index < holders.length) {
            return holders[index].stream;
        } else {
            return null;
        }
    }
    
    void setStream(int index, CommandIO stream, boolean mine) {
        if (index < 0 || index >= holders.length) {
            throw new ShellFailureException("negative stream index");
        } else {
            holders[index].setStream(stream, mine);
        }
    }

    public boolean isNoClobber() {
        return isVariableSet("NOCLOBBER");
    }

    void setEchoExpansions(boolean echoExpansions) {
        this.echoExpansions = echoExpansions;
    }

    boolean isEchoExpansions() {
        return this.echoExpansions;
    }

    final int getLastAsyncPid() {
        return lastAsyncPid;
    }

    final int getLastReturnCode() {
        return lastReturnCode;
    }
    
    final void setLastReturnCode(int rc) {
        lastReturnCode = rc;
    }

    final int getShellPid() {
        return shellPid;
    }

    void performAssignments(BjorneToken[] assignments) throws ShellException {
        if (assignments != null) {
            for (int i = 0; i < assignments.length; i++) {
                String assignment = assignments[i].getText();
                int pos = assignment.indexOf('=');
                if (pos <= 0) {
                    throw new ShellFailureException("misplaced '=' in assignment");
                }
                String name = assignment.substring(0, pos);
                String value = expand(assignment.substring(pos + 1)).toString();
                this.setVariable(name, value);
            }
        }
    }

    /**
     * Evaluate the redirections for this command.
     * 
     * @param redirects the redirection nodes to be evaluated
     * @return an array representing the mapping of logical fds to
     *         input/outputStreamTuple streams for this command.
     * @throws ShellException
     */
    StreamHolder[] evaluateRedirections(RedirectionNode[] redirects) throws ShellException {
        return evaluateRedirections(redirects, copyStreamHolders(holders));
    }
    
    /**
     * Evaluate the redirections for this command.
     * 
     * @param redirects the redirection nodes to be evaluated
     * @param holders the initial stream state which we will mutate
     * @return the stream state after redirections
     * @throws ShellException
     */
    StreamHolder[] evaluateRedirections(
            RedirectionNode[] redirects, StreamHolder[] holders) throws ShellException {
        if (redirects == null) {
            return holders;
        }
        boolean ok = false;
        try {
            for (int i = 0; i < redirects.length; i++) {
                RedirectionNode redir = redirects[i];
                // Work out which fd to redirect ...
                int fd;
                BjorneToken io = redir.getIo();
                if (io == null) {
                    switch (redir.getRedirectionType()) {
                        case REDIR_DLESS:
                        case REDIR_DLESSDASH:
                        case REDIR_LESS:
                        case REDIR_LESSAND:
                        case REDIR_LESSGREAT:
                            fd = 0;
                            break;
                        default:
                            fd = 1;
                            break;
                    }
                } else {
                    try {
                        fd = Integer.parseInt(io.getText());
                    } catch (NumberFormatException ex) {
                        throw new ShellFailureException("Invalid &fd number");
                    }
                }
                // If necessary, grow the fd table.
                if (fd >= holders.length) {
                    StreamHolder[] tmp = new StreamHolder[fd + 1];
                    System.arraycopy(holders, 0, tmp, 0, fd + 1);
                    holders = tmp;
                }

                StreamHolder stream;
                switch (redir.getRedirectionType()) {
                    case REDIR_DLESS:
                        throw new UnsupportedOperationException("<<");
                    case REDIR_DLESSDASH:
                        throw new UnsupportedOperationException("<<-");

                    case REDIR_GREAT:
                        try {
                            File file = new File(redir.getArg().getText());
                            if (isNoClobber() && file.exists()) {
                                throw new ShellException("File already exists");
                            }
                            CommandOutput tmp = new CommandOutput(new FileOutputStream(file));
                            stream = new StreamHolder(tmp, true);
                        } catch (IOException ex) {
                            throw new ShellException("Cannot open input file", ex);
                        }
                        break;

                    case REDIR_CLOBBER:
                    case REDIR_DGREAT:
                        try {
                            FileOutputStream tmp = new FileOutputStream(redir.getArg().getText(), 
                                    redir.getRedirectionType() == REDIR_DGREAT);
                            stream = new StreamHolder(new CommandOutput(tmp), true);
                        } catch (IOException ex) {
                            throw new ShellException("Cannot open input file", ex);
                        }
                        break;

                    case REDIR_LESS:
                        try {
                            File file = new File(redir.getArg().getText());
                            CommandInput tmp = new CommandInput(new FileInputStream(file));
                            stream = new StreamHolder(tmp, true);
                        } catch (IOException ex) {
                            throw new ShellException("Cannot open input file", ex);
                        }
                        break;

                    case REDIR_LESSAND:
                        try {
                            int fromFd = Integer.parseInt(redir.getArg().getText());
                            stream = (fromFd >= holders.length) ? null : new StreamHolder(holders[fromFd]);
                        } catch (NumberFormatException ex) {
                            throw new ShellException("Invalid fd after >&");
                        }
                        break;

                    case REDIR_GREATAND:
                        try {
                            int fromFd = Integer.parseInt(redir.getArg().getText());
                            stream = (fromFd >= holders.length) ? null : new StreamHolder(holders[fromFd]);
                        } catch (NumberFormatException ex) {
                            throw new ShellException("Invalid fd after >&");
                        }
                        break;

                    case REDIR_LESSGREAT:
                        throw new UnsupportedOperationException("<>");
                    default:
                        throw new ShellFailureException("unknown redirection type");
                }
                holders[fd] = stream;
            }
            ok = true;
        } finally {
            if (!ok) {
                for (StreamHolder holder : holders) {
                    holder.close();
                }
            }
        }
        return holders;
    }

    public CommandThread fork(CommandLine command, CommandIO[] ios)  throws ShellException {
        return interpreter.fork(command, ios);
    }

    public boolean patternMatch(CharSequence expandedWord, CharSequence pat) {
        // TODO Auto-generated method stub
        return false;
    }

    private static class VariableSlot {
        public String value;

        public boolean exported;

        public VariableSlot(String value, boolean exported) {
            if (value == null) {
                throw new ShellFailureException("null value");
            }
            this.value = value;
            this.exported = exported;
        }

        public VariableSlot(VariableSlot other) {
            this.value = other.value;
            this.exported = other.exported;
        }
    }

    public static class StreamHolder {
        private CommandIO stream;
        private boolean isMine;

        public StreamHolder(CommandIO stream, boolean isMine) {
            this.stream = stream;
            this.isMine = isMine;
        }

        public StreamHolder(StreamHolder other) {
            this.stream = other.stream;
            this.isMine = false;
        }
        
        public CommandIO getStream() {
            return stream;
        }

        public void setStream(CommandIO stream, boolean isMine) {
            close();
            this.stream = stream;
            this.isMine = isMine;
        }

        public void close() {
            if (isMine) {
                try {
                    isMine = false; // just in case we call close twice
                    stream.close();
                } catch (IOException ex) {
                    // FIXME - should we squash or report this?
                }
            }
        }
        
        public boolean isMine() {
            return isMine;
        }
    }

    private static class CharIterator {
        private CharSequence str;
        private int pos, start, limit;

        public CharIterator(CharSequence str) {
            this.str = str;
            this.start = pos = 0;
            this.limit = str.length();
        }

        public CharIterator(CharSequence str, int start, int limit) {
            this.str = str;
            this.start = pos = start;
            this.limit = limit;
        }

        public int nextCh() {
            return (pos >= limit) ? -1 : str.charAt(pos++);
        }

        public int peekCh() {
            return (pos >= limit) ? -1 : str.charAt(pos);
        }

        public int lastCh() {
            return (pos > start) ? str.charAt(pos - 1) : -1;
        }
    }
}
