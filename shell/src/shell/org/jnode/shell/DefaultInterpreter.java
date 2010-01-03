/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.HelpFactory;

/**
 * This interpreter simply parses the command line into a command name and
 * arguments, with simple quoting and escaping.  This class also provides
 * infrastructure that is reused in the RedirectingInterpreter class.
 * 
 * @author crawley@jnode.org
 */
public class DefaultInterpreter implements CommandInterpreter {

    public static final Factory FACTORY = new Factory() {
        public CommandInterpreter create() {
            return new DefaultInterpreter();
        }

        public String getName() {
            return "default";
        }
    };

    static final String[] NO_ARGS = new String[0];
    
    private static final Logger LOG = Logger.getLogger(DefaultInterpreter.class);

    public static final int REDIRECTS_FLAG = 0x01;

    // Token types.
    public static final int LITERAL = 0;
    public static final int STRING = 1;
    public static final int CLOSED = 2;
    public static final int SPECIAL = 4;

    // Recognized meta-characters
    public static final char ESCAPE_CHAR = '\\';
    public static final char FULL_ESCAPE_CHAR = '\'';
    public static final char QUOTE_CHAR = '"';
    public static final char SPACE_CHAR = ' ';
    public static final char SEND_OUTPUT_TO_CHAR = '>';
    public static final char GET_INPUT_FROM_CHAR = '<';
    public static final char PIPE_CHAR = '|';
    public static final char COMMENT_CHAR = '#';

    // Recognized '\' escapes
    private static final char ESCAPE_B = '\b';
    private static final char B = 'b';
    private static final char ESCAPE_N = '\n';
    private static final char N = 'n';
    private static final char ESCAPE_R = '\r';
    private static final char R = 'r';
    private static final char ESCAPE_T = '\t';
    private static final char T = 't';

    @Override
    public String getName() {
        return "default";
    }
    
    /**
     * {@inheritDoc}
     * 
     * The default interpreter treats a command script as a sequence of commands. 
     * Commands are expected to consist of exactly one line.  Any line whose first non-whitespace 
     * character is '#' will be ignored.  Command line arguments from the script are not supported,
     * and will result in a {@link ShellException} being thrown.
     */
    @Override
    public int interpret(CommandShell shell, Reader reader, boolean script, String alias, String[] args) 
        throws ShellException {
        if (args != null && args.length > 0) {
            throw new ShellInvocationException(
                    "The " + getName() + " interpreter does not support script file arguments");
        }
        try {
            BufferedReader br = new BufferedReader(reader);
            String line;
            int rc = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0 && !line.startsWith("#")) {
                    rc = interpret(shell, line);
                }
                if (!script) {
                    break;
                }
            }
            return rc;
        } catch (IOException ex) {
            throw new ShellInvocationException("Problem reading command: " + ex.getMessage(), ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
    
    @Override
    public Completable parsePartial(CommandShell shell, String line) throws ShellException {
        CommandLine res = doParseCommandLine(line);
        return res == null ? new CommandLine("", null) : res;
    }
    
    @Override
    public boolean help(CommandShell shell, String line, PrintWriter pw) throws ShellException {
        CommandLine cmd = doParseCommandLine(line);
        CommandInfo cmdInfo = cmd.getCommandInfo(shell);
        if (cmdInfo != null) {
            try {
                Help help = HelpFactory.getHelpFactory().getHelp(cmd.getCommandName(), cmdInfo);
                help.usage(pw);
                return true;
            } catch (HelpException ex) {
                LOG.info("Unexpected error while getting help for alias / class '" + 
                        cmd.getCommandName() + "': " + ex.getMessage(), ex);
            }
        }
        return false;
    }
    
    protected int interpret(CommandShell shell, String line) 
        throws ShellException {
        CommandLine cmd = doParseCommandLine(line);
        if (cmd == null) {
            return 0;
        }
        return shell.invoke(cmd, null, null);
    }

    private CommandLine doParseCommandLine(String line) throws ShellException {
        Tokenizer tokenizer = new Tokenizer(line);
        if (!tokenizer.hasNext()) {
            return null;
        }
        CommandLine.Token commandToken = tokenizer.next();
        LinkedList<CommandLine.Token> tokenList =
                new LinkedList<CommandLine.Token>();
        while (tokenizer.hasNext()) {
            tokenList.add(tokenizer.next());
        }
        CommandLine.Token[] argTokens =
                tokenList.toArray(new CommandLine.Token[tokenList.size()]);
        CommandLine res = new CommandLine(commandToken, argTokens, null);
        res.setArgumentAnticipated(tokenizer.whitespaceAfterLast());
        return res;
    }

    @Override
    public String escapeWord(String word) {
        return escapeWord(word, false);
    }

    protected String escapeWord(String word, boolean escapeRedirects) {
        final int len = word.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char ch = word.charAt(i);
            switch (ch) {
                case ESCAPE_B:
                    sb.append(ESCAPE_CHAR).append(B); 
                    break;
                case ESCAPE_N:
                    sb.append(ESCAPE_CHAR).append(N); 
                    break;
                case ESCAPE_R:
                    sb.append(ESCAPE_CHAR).append(R); 
                    break;
                case ESCAPE_T:
                    sb.append(ESCAPE_CHAR).append(T); 
                    break;
                case ESCAPE_CHAR:
                    sb.append(ESCAPE_CHAR).append(ESCAPE_CHAR); 
                    break;
                case FULL_ESCAPE_CHAR:
                    sb.append(ESCAPE_CHAR).append(FULL_ESCAPE_CHAR); 
                    break;
                case QUOTE_CHAR:
                    sb.append(ESCAPE_CHAR).append(QUOTE_CHAR); 
                    break;
                case COMMENT_CHAR:
                    sb.append(ESCAPE_CHAR).append(COMMENT_CHAR); 
                    break;
                case SPACE_CHAR:
                    sb.append(ESCAPE_CHAR).append(SPACE_CHAR); 
                    break;
                case PIPE_CHAR:
                    if (escapeRedirects) {
                        sb.append(ESCAPE_CHAR).append(PIPE_CHAR); 
                    } else {
                        sb.append(PIPE_CHAR);
                    }
                    break;
                case SEND_OUTPUT_TO_CHAR:
                    if (escapeRedirects) {
                        sb.append(ESCAPE_CHAR).append(SEND_OUTPUT_TO_CHAR); 
                    } else {
                        sb.append(SEND_OUTPUT_TO_CHAR);
                    }
                    break;
                case GET_INPUT_FROM_CHAR:
                    if (escapeRedirects) {
                        sb.append(ESCAPE_CHAR).append(GET_INPUT_FROM_CHAR); 
                    } else {
                        sb.append(GET_INPUT_FROM_CHAR);
                    }
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Get and expand the default command prompt.
     */
    public String getPrompt(CommandShell shell, boolean continuation) {
        String prompt = shell.getProperty(CommandShell.PROMPT_PROPERTY_NAME);
        final StringBuffer result = new StringBuffer();
        boolean commandMode = false;
        StringReader reader = new StringReader(prompt);
        int i;
        try {
            while ((i = reader.read()) != -1) {
                char c = (char) i;
                if (commandMode) {
                    switch (c) {
                        case 'P':
                            result.append(new File(System.getProperty(CommandShell.DIRECTORY_PROPERTY_NAME, "")));
                            break;
                        case 'G':
                            result.append("> ");
                            break;
                        case 'D':
                            final Date now = new Date();
                            DateFormat.getDateTimeInstance().format(now, result, null);
                            break;
                        default:
                            result.append(c);
                    }
                    commandMode = false;
                } else {
                    switch (c) {
                        case '$':
                            commandMode = true;
                            break;
                        default:
                            result.append(c);
                    }
                }
            }
        } catch (IOException ex) {
            // A StringReader shouldn't give an IOException unless we close it ... which we don't!
            LOG.error("Impossible", ex);
        }
        return result.toString();
    }
    
    @Override
    public boolean supportsMultiline() {
        return false;
    }

    /**
     * A simple command line tokenizer for the 'built-in' interpreters. It
     * understands quoting, some '\' escapes, and (depending on constructor
     * flags) certain "special" symbols.
     */
    protected static class Tokenizer implements SymbolSource<CommandLine.Token> {
        private int pos = 0;
        private final ArrayList<CommandLine.Token> tokens =
                new ArrayList<Token>(8);
        private boolean whiteSpaceAfterLast;

        /**
         * Instantiate a command line tokenizer for a given input String.
         * 
         * @param line the input String.
         * @param flags flags controlling the tokenization.
         * @throws ShellException 
         */
        public Tokenizer(String line, int flags) throws ShellSyntaxException {
            tokenize(line, flags);
        }

        public Tokenizer(String line) throws ShellSyntaxException {
            this(line, 0);
        }

        /**
         * Returns if there are no more tokens to return.
         * 
         * @return <code>true</code> if there is another token;
         *         <code>false</code> otherwise
         */
        public boolean hasNext() {
            return pos < tokens.size();
        }

        /**
         * Extract the next token string and return it.
         * 
         * @return the next token
         */
        public CommandLine.Token next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return tokens.get(pos++);
        }

        private void tokenize(String s, int flags) throws ShellSyntaxException {
            int pos = 0;

            while (true) {
                // Skip spaces before start of token
                whiteSpaceAfterLast = false;
                while (pos < s.length() && s.charAt(pos) == SPACE_CHAR) {
                    pos++;
                    whiteSpaceAfterLast = true;
                }
                if (pos >= s.length()) {
                    break;
                }

                // Parse a token
                boolean inFullEscape = false;
                boolean inQuote = false;
                int type = LITERAL;
                int start = pos;
                StringBuilder token = new StringBuilder(5);
                char currentChar;
                boolean finished = false;

                while (!finished && pos < s.length()) {
                    currentChar = s.charAt(pos++);

                    switch (currentChar) {
                        case ESCAPE_CHAR:
                            if (pos >= s.length()) {
                                throw new ShellSyntaxException(
                                    "escape char ('\\') not followed by a character");
                            }
                            char ch;
                            switch (ch = s.charAt(pos++)) {
                                case N:
                                    token.append(ESCAPE_N);
                                    break;
                                case B:
                                    token.append(ESCAPE_B);
                                    break;
                                case R:
                                    token.append(ESCAPE_R);
                                    break;
                                case T:
                                    token.append(ESCAPE_T);
                                    break;
                                default:
                                    token.append(ch);
                            }
                            break;

                        case FULL_ESCAPE_CHAR:
                            if (inQuote) {
                                token.append(currentChar);
                            } else {
                                inFullEscape = !inFullEscape; // just a toggle
                                type = STRING;
                                if (!inFullEscape) {
                                    type |= CLOSED;
                                }
                            }
                            break;
                        case QUOTE_CHAR:
                            if (inFullEscape) {
                                token.append(currentChar);
                            } else {
                                inQuote = !inQuote;
                                type = STRING;
                                if (!inQuote) {
                                    type |= CLOSED;
                                }
                            }
                            break;
                        case SPACE_CHAR:
                            if (inFullEscape || inQuote) {
                                token.append(currentChar);
                            } else {
                                if (token.length() != 0) { // don't return an empty
                                    // token
                                    finished = true;
                                    pos--; // to return trailing space as empty
                                    // last
                                    // token
                                }
                            }
                            break;
                        case COMMENT_CHAR:
                            if (inFullEscape || inQuote) {
                                token.append(currentChar);
                            } else {
                                finished = true;
                                pos = s.length(); // ignore EVERYTHING
                            }
                            break;
                        case GET_INPUT_FROM_CHAR:
                        case SEND_OUTPUT_TO_CHAR:
                        case PIPE_CHAR:
                            if (inFullEscape || inQuote ||
                                    (flags & REDIRECTS_FLAG) == 0) {
                                token.append(currentChar);
                            } else {
                                finished = true;
                                if (token.length() == 0) {
                                    token.append(currentChar);
                                    type = SPECIAL;
                                } else {
                                    pos--; // the special character terminates the
                                    // literal.
                                }
                            }
                            break;
                        default:
                            token.append(currentChar);
                    }
                }
                tokens.add(new CommandLine.Token(token.toString(), type, start, pos));
            }
        }

        /**
         * This operation it not supported.
         */
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        /**
         * Return the Token returned by the last successful call to next().
         * 
         * @return the last token.
         */
        public CommandLine.Token last() {
            return tokens.get(pos - 1);
        }

        public Token peek() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return tokens.get(pos + 1);
        }

        public void seek(int pos) throws NoSuchElementException {
            if (pos < 0 || pos > tokens.size()) {
                throw new NoSuchElementException();
            }
            this.pos = pos;
        }

        public int tell() {
            return pos;
        }

        public boolean whitespaceAfterLast() {
            return whiteSpaceAfterLast;
        }
    }
}
