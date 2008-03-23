/*
 * $Id: ThreadCommandInvoker.java 3374 2007-08-02 18:15:27Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007 JNode.org
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.CommandSyntaxException;

/**
 * This interpreter simply parses the command line into a command name and
 * arguments, with simple quoting and escaping.
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

    public String getName() {
        return "default";
    }

    public int interpret(CommandShell shell, String line) throws ShellException {
        LinkedList<CommandLine.Token> tokens =
                new LinkedList<CommandLine.Token>();
        Tokenizer tokenizer = new Tokenizer(line);
        while (tokenizer.hasNext()) {
            tokens.add(tokenizer.next());
        }
        int nosTokens = tokens.size();
        if (nosTokens == 0) {
            return 0;
        }
        CommandLine cmd;
        if (nosTokens == 1) {
            cmd = new CommandLine(tokens.get(0), null, null);
        } else {
            CommandLine.Token commandToken = tokens.removeFirst();
            CommandLine.Token[] argTokens =
                    new CommandLine.Token[nosTokens - 1];
            cmd = new CommandLine(
                    commandToken, tokens.toArray(argTokens),null);
        }
        shell.addCommandToHistory(line);
        try {
            Command command = cmd.parseCommandLine(shell);
            return shell.invoke(cmd, command);
        } catch (CommandSyntaxException ex) {
            throw new ShellException("Command arguments don't match syntax", ex);
        }
    }

    public Completable parsePartial(CommandShell shell, String line)
            throws ShellSyntaxException {
        Tokenizer tokenizer = new Tokenizer(line);
        if (!tokenizer.hasNext()) {
            return new CommandLine("", null);
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
         */
        public Tokenizer(String line, int flags) {
            tokenize(line, flags);
        }

        public Tokenizer(String line) {
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

        private void tokenize(String s, int flags)
                throws IllegalArgumentException {
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
                            throw new IllegalArgumentException(
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
