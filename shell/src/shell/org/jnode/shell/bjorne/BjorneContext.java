/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.IncompleteCommandException;
import org.jnode.shell.PathnamePattern;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellFailureException;
import org.jnode.shell.ShellSyntaxException;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.io.CommandInput;
import org.jnode.shell.io.CommandOutput;
import org.jnode.shell.io.CommandIOHolder;

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
    
    private TreeMap<String, String> aliases;

    private String command = "";

    private List<String> args = new ArrayList<String>();

    private int lastReturnCode;

    private int shellPid;

    private int lastAsyncPid;

    private boolean tildeExpansion = true;
    
    private boolean globbing = true;

    private String options = "";

    private CommandIOHolder[] holders;

    private boolean echoExpansions;

    private BjorneContext parent;

    public BjorneContext(BjorneInterpreter interpreter, CommandIOHolder[] holders) {
        this.interpreter = interpreter;
        this.holders = holders;
        this.variables = new HashMap<String, VariableSlot>();
        this.aliases = new TreeMap<String, String>();
    }

    public BjorneContext(BjorneInterpreter interpreter) {
        this(interpreter, defaultStreamHolders());
    }
    
    private static CommandIOHolder[] defaultStreamHolders() {
        CommandIOHolder[] res = new CommandIOHolder[4];
        res[Command.STD_IN] = new CommandIOHolder(CommandLine.DEFAULT_STDIN, false);
        res[Command.STD_OUT] = new CommandIOHolder(CommandLine.DEFAULT_STDOUT, false);
        res[Command.STD_ERR] = new CommandIOHolder(CommandLine.DEFAULT_STDERR, false);
        res[Command.SHELL_ERR] = new CommandIOHolder(CommandLine.DEFAULT_STDERR, false);
        return res;
    }

    /**
     * Create a copy of a context with the same initial variable bindings and
     * streams. Stream ownership is not transferred.
     * 
     * @param parent the context that gives us our initial state.
     */
    public BjorneContext(BjorneContext parent) {
        this.parent = parent;
        this.interpreter = parent.interpreter;
        this.holders = copyStreamHolders(parent.holders);
        this.variables = copyVariables(parent.variables);
        this.aliases = new TreeMap<String, String>(parent.aliases);
        this.globbing = parent.globbing;
        this.tildeExpansion = parent.tildeExpansion;
        this.echoExpansions = parent.echoExpansions;
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
        return this.lastAsyncPid;
    }

    final int getLastReturnCode() {
        return this.lastReturnCode;
    }
    
    final void setLastReturnCode(int rc) {
        this.lastReturnCode = rc;
    }

    final int getShellPid() {
        return this.shellPid;
    }
    
    final BjorneContext getParent() {
        return this.parent;
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
    public static CommandIOHolder[] copyStreamHolders(CommandIOHolder[] holders) {
        CommandIOHolder[] res = new CommandIOHolder[holders.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new CommandIOHolder(holders[i]);
        }
        return res;
    }
    
    CommandIOHolder[] getCopyOfHolders() {
        return copyStreamHolders(holders);
    }
    
    CommandIOHolder[] getHolders() {
        return holders;
    }
    
    void setArgs(String[] args) {
        this.args = Arrays.asList(args.clone());
    }

    void setCommand(String command) {
        this.command = command;
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
            variables.put(name, new VariableSlot(name, value, false));
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
    boolean isVariableSet(String name) {
        return variables.get(name) != null;
    }

    /**
     * This method implements 'unset NAME'
     * 
     * @param name the name of the variable to be unset
     */
    void unsetVariableValue(String name) {
        variables.remove(name);
    }

    /**
     * This method implements 'export NAME' or 'unexport NAME'.
     * 
     * @param name the name of the variable to be exported / unexported
     */
    void setExported(String name, boolean exported) {
        VariableSlot var = variables.get(name);
        if (var == null) {
            if (exported) {
                variables.put(name, new VariableSlot(name, "", exported));
            }
        } else {
            var.exported = exported;
        }
    }
    
    /**
     * Get the complete alias map.
     * @return the alias map
     */
    TreeMap<String, String> getAliases() {
        return aliases;
    }
    
    /**
     * Lookup an alias
     * @param aliasName the (possible) alias name
     * @return the alias string or {@code null}
     */
    String getAlias(String aliasName) {
        return aliases.get(aliasName);
    }
    
    /**
     * Define an alias
     * @param aliasName the alias name
     * @param alias the alias.
     */
    void defineAlias(String aliasName, String alias) {
        aliases.put(aliasName, alias);
    }

    /**
     * Undefine an alias
     * @param aliasName the alias name
     */
    void undefineAlias(String aliasName) {
        aliases.remove(aliasName);
    }

    /**
     * Perform expand-and-split processing on a list of word tokens. The resulting
     * wordTokens are assembled into a CommandLine.  
     * 
     * @param tokens the tokens to be expanded and split into words
     * @return the command line
     * @throws ShellException
     */
    public CommandLine expandAndSplit(Iterable<BjorneToken> tokens) throws ShellException {
        List<BjorneToken> wordTokens = new LinkedList<BjorneToken>();
        expandAndSplit(tokens, wordTokens);
        return makeCommandLine(wordTokens);
    }
    
    /**
     * Perform expand-and-split processing on an array of word tokens. The resulting
     * wordTokens are assembled into a CommandLine.  
     * 
     * @param tokens the tokens to be expanded and split into words
     * @return the command line
     * @throws ShellException
     */
    public CommandLine expandAndSplit(BjorneToken[] tokens) throws ShellException {
        List<BjorneToken> wordTokens = new LinkedList<BjorneToken>();
        expandAndSplit(tokens, wordTokens);
        return makeCommandLine(wordTokens);
    }
    
    /**
     * Perform expand-and-split processing on a list of word tokens. The resulting
     * tokens are appended to wordTokens.  
     * 
     * @param tokens the tokens to be expanded and split into words
     * @param wordTokens append expanded/split tokens to this list
     * @throws ShellException
     */
    public void expandAndSplit(Iterable<BjorneToken> tokens, List<BjorneToken> wordTokens) 
        throws ShellException {
        for (BjorneToken token : tokens) {
            expandSplitAndAppend(token, wordTokens);
        }
    }
    
    /**
     * Perform expand-and-split processing on an array of word tokens. The resulting
     * tokens are appended to wordTokens.  
     * 
     * @param tokens the tokens to be expanded and split into words
     * @param wordTokens append expanded/split tokens to this list
     * @throws ShellException
     */
    public void expandAndSplit(BjorneToken[] tokens, List<BjorneToken> wordTokens) 
        throws ShellException {
        for (BjorneToken token : tokens) {
            expandSplitAndAppend(token, wordTokens);
        }
    }
    
    private void expandSplitAndAppend(BjorneToken token, List<BjorneToken> wordTokens) 
        throws ShellException {
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
        PathnamePattern pattern = PathnamePattern.compilePathPattern(word);
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
     * non-literal quotes.
     * 
     * @param text the characters to be split
     * @return the resulting list of tokens.
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
                        String tmp = dollarExpansion(ci, quote);
                        sb.append(tmp == null ? "" : tmp);
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

    private String dollarExpansion(CharIterator ci, char quote) throws ShellException {
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
                return specialVariable(ch, quote == '"');
            default:
                StringBuffer sb = new StringBuffer().append((char) ch);
                ch = ci.peekCh();
                while ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_') {
                    sb.append((char) ch);
                    ci.nextCh();
                    ch = ci.peekCh();
                }
                return variable(sb.toString());
        }
    }

    private String dollarBraceExpansion(CharIterator ci) throws ShellException {
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
        
        if (braceLevel > 0) {
            throw new ShellSyntaxException("unmatched '{'");
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
                    i++;
                    break;
                default:
                    i += 2;
                    break;
            }
            // Extract the word
            word = sb.substring(i);
        }
        String value = dollarExpansion(new CharIterator(parameter), '\000');
        switch (operator) {
            case NONE:
                return (value != null) ? value : "";
            case PREHASH:
                return (value != null) ? Integer.toString(value.length()) : "0";
            case HYPHEN:
                return (value == null) ? word : value;
            case COLONHYPHEN:
                return (value == null || value.length() == 0) ? word : value;
            case PLUS:
                return (value == null) ? "" : word;
            case COLONPLUS:
                return (value == null || value.length() == 0) ? "" : word;
            case QUERY:
                if (value == null) {
                    String msg = word.length() > 0 ? word : (parameter + " is unset");
                    resolvePrintStream(getIO(Command.STD_ERR)).println(msg);
                    throw new BjorneControlException(BjorneInterpreter.BRANCH_EXIT, 1);
                } else {
                    return value;
                }
            case COLONQUERY:
                if (value == null || value.length() == 0) {
                    String msg = word.length() > 0 ? word : (parameter + " is unset or null");
                    resolvePrintStream(getIO(Command.STD_ERR)).println(msg);
                    throw new BjorneControlException(BjorneInterpreter.BRANCH_EXIT, 1);
                } else {
                    return value;
                }
            case EQUALS:
                if (value == null) {
                    setVariable(parameter, word);
                    return word;
                } else {
                    return value;
                }
            case COLONEQUALS:
                if (value == null || value.length() == 0) {
                    setVariable(parameter, word);
                    return word;
                } else {
                    return value;
                }
            case HASH:
                return patternEdit(value, word, false, false);
            case DHASH:
                return patternEdit(value, word, false, true);
            case PERCENT:
                return patternEdit(value, word, true, false);
            case DPERCENT:
                return patternEdit(value, word, true, true);
            default:
                throw new ShellFailureException("not implemented");
        }
    }

    private String patternEdit(String value, String pattern, boolean suffix, boolean eager) {
        if (value == null || value.length() == 0) {
            return "";
        }
        if (pattern == null || pattern.length() == 0) {
            return value;
        }
        // FIXME ... this does not work for a suffix == true, eager == false.  We
        // translate '*' to '.*?', but that won't give us the shortest suffix because 
        // Patterns inherently match from left to right.
        int flags = (suffix ? PathnamePattern.ANCHOR_RIGHT : PathnamePattern.ANCHOR_LEFT) |
                (eager ? PathnamePattern.EAGER : 0);
        Pattern p = PathnamePattern.compilePosixShellPattern(pattern, 
                PathnamePattern.DEFAULT_FLAGS | flags);
        Matcher m = p.matcher(value);
        if (m.find()) {
            if (suffix) {
                return value.substring(0, m.start());
            } else {
                return value.substring(m.end());
            }
        } else {
            return value;
        }
    }
    
    @SuppressWarnings("unused")
    private String reverse(String str) {
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = str.length() - 1; i >= 0; i--) {
            sb.append(str.charAt(i));
        }
        return sb.toString();
    }


    private String variable(String parameter) throws ShellSyntaxException {
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

    private String specialVariable(int ch, boolean inDoubleQuotes) {
        switch (ch) {
            case '$':
                return Integer.toString(shellPid);
            case '#':
                return Integer.toString(args.size());
            case '@':
                return concatenateArgs(false, inDoubleQuotes);
            case '*':
                return concatenateArgs(true, inDoubleQuotes);
            case '?':
                return Integer.toString(lastReturnCode);
            case '!':
                return Integer.toString(lastAsyncPid);
            case '-':
                return options;
            default:
                return null;
        }
    }

    private String concatenateArgs(boolean isStar, boolean inDoubleQuotes) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (sb.length() > 0) {
                if (isStar || !inDoubleQuotes) {
                    sb.append(' ');
                } else {
                    sb.append("\" \"");
                }
            }
            sb.append(arg);
        }
        return sb.toString();
    }

    private String argVariable(int argNo) {
        if (argNo == 0) {
            return command;
        } else if (argNo <= args.size()) {
            return args.get(argNo - 1);
        } else {
            return null;
        }
    }

    public boolean isSet(String name) {
        return variables.get(name) != null;
    }

    private String dollarParenExpansion(CharIterator ci) {
        throw new ShellFailureException("not implemented");
    }

    int execute(CommandLine command, CommandIO[] streams) throws ShellException {
        if (isEchoExpansions()) {
            StringBuilder sb = new StringBuilder();
            sb.append(" + ").append(command.getCommandName());
            for (String arg : command.getArguments()) {
                sb.append(" ").append(interpreter.escapeWord(arg));
            }
            resolvePrintStream(streams[Command.STD_ERR]).println(sb);
        }
        Map<String, String> env = buildEnvFromExports();
        lastReturnCode = interpreter.executeCommand(command, this, streams, null, env);
        return lastReturnCode;
    }

    private Map<String, String> buildEnvFromExports() {
        HashMap<String, String> map = new HashMap<String, String>(variables.size());
        for (VariableSlot var : variables.values()) {
            if (var.exported) {
                map.put(var.name, var.value);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    PrintStream resolvePrintStream(CommandIO commandIOIF) {
        return interpreter.resolvePrintStream(commandIOIF);
    }

    InputStream resolveInputStream(CommandIO stream) {
        return interpreter.resolveInputStream(stream);
    }

    CommandIO getIO(int index) {
        if (index < 0) {
            throw new ShellFailureException("negative stream index");
        } else if (index < holders.length) {
            return holders[index].getIO();
        } else {
            return null;
        }
    }
    
    void setIO(int index, CommandIO io, boolean mine) {
        if (index < 0 || index >= holders.length) {
            throw new ShellFailureException("bad stream index");
        } else {
            holders[index].setIO(io, mine);
        }
    }
    
    void setIO(int index, CommandIOHolder holder) {
        if (index < 0 || index >= holders.length) {
            throw new ShellFailureException("bad stream index");
        } else {
            holders[index].setIO(holder);
        }
    }
    
    void closeIOs() {
        for (CommandIOHolder holder : holders) {
            holder.close();
        }
    }
    
    void flushIOs() {
        for (CommandIOHolder holder : holders) {
            holder.flush();
        }
    }


    CommandIO[] getIOs() {
        CommandIO[] io = new CommandIO[holders.length];
        int i = 0;
        for (CommandIOHolder holder : holders) {
            io[i++] = (holder == null) ? null : holder.getIO();
        }
        return io;
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
    CommandIOHolder[] evaluateRedirections(RedirectionNode[] redirects) throws ShellException {
        CommandIOHolder[] res = copyStreamHolders(holders);
        evaluateRedirections(redirects, res);
        return res;
    }
    
    /**
     * Evaluate the redirections for this command.
     * 
     * @param redirects the redirection nodes to be evaluated
     * @param holders the initial stream state which we will mutate
     * @return the stream state after redirections
     * @throws ShellException
     */
    void evaluateRedirections(
            RedirectionNode[] redirects, CommandIOHolder[] holders) throws ShellException {
        if (redirects == null) {
            return;
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
                    CommandIOHolder[] tmp = new CommandIOHolder[fd + 1];
                    System.arraycopy(holders, 0, tmp, 0, fd + 1);
                    holders = tmp;
                }

                CommandIOHolder stream;
                CommandInput in;
                CommandOutput out;
                switch (redir.getRedirectionType()) {
                    case REDIR_DLESS:
                    case REDIR_DLESSDASH:
                        String here = redir.getHereDocument();
                        if (redir.isHereDocumentExpandable()) {
                            here = expand(here).toString();
                        }
                        in = new CommandInput(new StringReader(here));
                        stream = new CommandIOHolder(in, true);
                        break;

                    case REDIR_GREAT:
                        try {
                            File file = new File(redir.getArg().getText());
                            if (isNoClobber() && file.exists()) {
                                throw new ShellException("File already exists");
                            }
                            out = new CommandOutput(new FileOutputStream(file));
                            stream = new CommandIOHolder(out, true);
                        } catch (IOException ex) {
                            throw new ShellException("Cannot open output file", ex);
                        }
                        break;

                    case REDIR_CLOBBER:
                    case REDIR_DGREAT:
                        try {
                            FileOutputStream tmp = new FileOutputStream(redir.getArg().getText(), 
                                    redir.getRedirectionType() == REDIR_DGREAT);
                            stream = new CommandIOHolder(new CommandOutput(tmp), true);
                        } catch (IOException ex) {
                            throw new ShellException("Cannot open output file", ex);
                        }
                        break;

                    case REDIR_LESS:
                        try {
                            File file = new File(redir.getArg().getText());
                            in = new CommandInput(new FileInputStream(file));
                            stream = new CommandIOHolder(in, true);
                        } catch (IOException ex) {
                            throw new ShellException("Cannot open input file", ex);
                        }
                        break;

                    case REDIR_LESSAND:
                        try {
                            int fromFd = Integer.parseInt(redir.getArg().getText());
                            stream = (fromFd >= holders.length) ? null :
                                    new CommandIOHolder(holders[fromFd]);
                        } catch (NumberFormatException ex) {
                            throw new ShellException("Invalid fd after <&");
                        }
                        break;

                    case REDIR_GREATAND:
                        try {
                            int fromFd = Integer.parseInt(redir.getArg().getText());
                            stream = (fromFd >= holders.length) ? null : 
                                    new CommandIOHolder(holders[fromFd]);
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
                for (CommandIOHolder holder : holders) {
                    holder.close();
                }
            }
        }
    }

    public boolean patternMatch(CharSequence text, CharSequence pat) {
        int flags = PathnamePattern.EAGER | PathnamePattern.DEFAULT_FLAGS;
        Pattern regex = PathnamePattern.compilePosixShellPattern(pat, flags);
        return regex.matcher(text).matches();
    }

    public String[] getArgs() {
        return args.toArray(new String[args.size()]);
    }

    public int nosArgs() {
        return args.size();
    }

    public CommandShell getShell() {
        return interpreter.getShell();
    }

    public String getName() {
        return interpreter.getUniqueName();
    }

    public BjorneToken[] substituteAliases(BjorneToken[] words) 
        throws IncompleteCommandException {
        String alias = aliases.get(words[0].getText());
        if (alias == null) {
            return words;
        }
        List<BjorneToken> list = new LinkedList<BjorneToken>(Arrays.asList(words));
        substituteAliases(list, 0, 0);
        return list.toArray(new BjorneToken[list.size()]);
    }
        
    private void substituteAliases(List<BjorneToken> list, int pos, int depth) 
        throws IncompleteCommandException {
        if (depth > 10) {
            throw new ShellFailureException("probable cycle detected in alias expansion");
        }
        String aliasName = list.get(pos).getText();
        String alias = aliases.get(aliasName);
        if (alias == null) {
            return;
        }
        BjorneTokenizer tokens = new BjorneTokenizer(alias);
        list.remove(pos);
        int i = 0;
        while (tokens.hasNext()) {
            list.add(pos + i, tokens.next());
            if (i == 0 && !aliasName.equals(list.get(pos + i).getText())) {
                substituteAliases(list, pos + i, depth + 1);
            }
            i++;
        }
        if (alias.endsWith(" ") && pos + i < list.size()) {
            substituteAliases(list, pos + i, depth + 1);
        }
    }
}
