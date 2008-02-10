/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.argument.AliasArgument;
import org.jnode.shell.help.argument.FileArgument;

/**
 * This class represents the command line as command name and a sequence of
 * argument strings. It also can carry the i/o stream environment for launching
 * the command.
 * 
 * TODO This class needs to be "syntax agnostic".
 * 
 * @author crawley@jnode.org
 */
public class CommandLine implements Completable, Iterable<String> {

    public static final Closeable DEFAULT_STDIN = new StreamMarker("STDIN");

    public static final Closeable DEFAULT_STDOUT = new StreamMarker("STDOUT");

    public static final Closeable DEFAULT_STDERR = new StreamMarker("STDERR");

    public static final Closeable DEVNULL = new StreamMarker("DEVNULL");

    public static final int LITERAL = 0;

    public static final int STRING = 1;

    public static final int CLOSED = 2;

    public static final int SPECIAL = 4;

    public static final char ESCAPE_CHAR = '\\';

    public static final char FULL_ESCAPE_CHAR = '\'';

    public static final char QUOTE_CHAR = '"';

    public static final char SPACE_CHAR = ' ';

    public static final char SEND_OUTPUT_TO_CHAR = '>';

    public static final char COMMENT_CHAR = '#';

    private static final char ESCAPE_B = '\b';

    private static final char B = 'b';

    private static final char ESCAPE_N = '\n';

    private static final char N = 'n';

    private static final char ESCAPE_R = '\r';

    private static final char R = 'r';

    private static final char ESCAPE_T = '\t';

    private static final char T = 't';

    private static final String[] NO_ARGS = new String[0];
    private static final Token[] NO_TOKENS = new Token[0];

    private final Help.Info defaultParameter = new Help.Info("file",
            "default parameter for command line completion", new Parameter(
                    new FileArgument("file", "a file", Argument.MULTI),
                    Parameter.OPTIONAL));

    private final Argument defaultArg = new AliasArgument("command",
            "the command to be called");

    private final String commandName;
    private final Token commandToken;

    private final String[] arguments;
    private final Token[] argumentTokens;

    private Closeable[] streams;

    private boolean argumentAnticipated = false;

    /**
     * Create a new instance using Tokens instead of Strings.
     * 
     * @param commandToken the command name token or <code>null</code>.
     * @param argumentTokens the argument token list or <code>null</code>.
     * @param streams the io stream array or <code>null</code>.
     */
    public CommandLine(Token commandToken, Token[] argumentTokens,
            Closeable[] streams) {
        this.commandToken = commandToken;
        this.commandName = (commandToken == null) ? null : commandToken.token;
        this.argumentTokens = (argumentTokens == null || argumentTokens.length == 0) ? NO_TOKENS
                : argumentTokens.clone();
        this.arguments = prepareArguments(this.argumentTokens);
        this.streams = setupStreams(streams);
    }

    /**
     * Create a new instance encapsulating a command name, argument list and io
     * stream array. If 'arguments' is <code>null</code>, a zero length
     * String array is substituted. If 'streams' is <code>null</code> , an
     * array of length 3 is substituted. A non-null 'streams' argument must have
     * a length of at least 3.
     * 
     * @param commandName the command name or <code>null</code>.
     * @param arguments the argument list or <code>null</code>.
     * @param streams the io stream array or <code>null</code>.
     */
    public CommandLine(String commandName, String[] arguments,
            Closeable[] streams) {
        this.commandName = commandName;
        this.arguments = (arguments == null || arguments.length == 0) ? NO_ARGS
                : arguments.clone();
        this.commandToken = null;
        this.argumentTokens = null;
        this.streams = setupStreams(streams);
    }

    /**
     * Create a new instance. Equivalent to CommandLine(commandName, arguments,
     * null);
     * 
     * @param commandName the command name or <code>null</code>.
     * @param arguments the argument list or <code>null</code>.
     */
    public CommandLine(String commandName, String[] arguments) {
        this(commandName, arguments, null);
    }

    /**
     * Create a new instance. Equivalent to CommandLine(null, arguments, null);
     * 
     * @param arguments the argument list or <code>null</code>.
     * @deprecated It is a bad idea to leave out the command name.
     */
    public CommandLine(String[] arguments) {
        this(null, arguments, null);
    }

    private Closeable[] setupStreams(Closeable[] streams) {
        if (streams == null) {
            return new Closeable[] { DEFAULT_STDIN, DEFAULT_STDOUT,
                    DEFAULT_STDERR };
        } else if (streams.length < 3) {
            throw new IllegalArgumentException("streams.length < 3");
        } else {
            return streams.clone();
        }
    }

    private String[] prepareArguments(Token[] argumentTokens) {
        String[] arguments = new String[argumentTokens.length];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = argumentTokens[i].token;
        }
        return arguments;
    }

    /**
     * This method returns an Iterator for the arguments represented as Strings.
     */
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            private int pos = 0;

            public boolean hasNext() {
                return pos < arguments.length;
            }

            public String next() throws NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return arguments[pos++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * This method returns an Iterator for the arguments represented as Tokens
     */
    public Iterator<Token> tokenIterator() throws NoTokensAvailableException {
        if (argumentTokens == null) {
            throw new NoTokensAvailableException(
                    "No tokens available in the CommandLine");
        }
        return new Iterator<Token>() {
            private int pos = 0;

            public boolean hasNext() {
                return pos < argumentTokens.length;
            }

            public Token next() throws NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return argumentTokens[pos++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * Get the command name
     * 
     * @return the command name
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Get the command name in token form
     * 
     * @return the command token
     */
    public Token getCommandToken() {
        return commandToken;
    }

    /**
     * Get the arguments as String[].
     * 
     * @return the arguments as String[]
     */
    public String[] getArguments() {
        return arguments.clone();
    }

    /**
     * Get the arguments as String[].
     * 
     * @return the arguments as String[]
     * @deprecated this method name is wrong.
     */
    public String[] toStringArray() {
        return arguments.clone();
    }

    /**
     * Returns the entire command line as a string.
     * 
     * @return the entire command line
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(escape(commandName));
        for (String argument : arguments) {
            sb.append(' ');
            sb.append(escape(argument));
        }
        return sb.toString();
    }

    /**
     * Gets the remaining number of parts
     * 
     * @return the remaining number of parts
     */
    public int getLength() {
        return arguments.length;
    }

    public boolean isArgumentAnticipated() {
        return argumentAnticipated;
    }

    public void setArgumentAnticipated(boolean newValue) {
        argumentAnticipated = newValue;
    }

    /**
     * The Token class is a light-weight representation for tokens that make up
     * a command line.
     */
    public static class Token {
        /**
         * This field holds the "cooked" representation of command line token.
         * By the time we reach the CommandLine, all shell meta-characters
         * should have been processed so that the value of the field represents
         * a command name or argument.
         */
        public final String token;

        /**
         * This field represents the type of the token. The meaning is
         * interpreter specific.
         */
        public final int tokenType;

        /**
         * This field denotes the character offset of the first character of
         * this token in the source character sequence passed to the
         * interpreter.
         */
        public final int start;

        /**
         * This field denotes the character offset + 1 for the last character of
         * this token in the source character sequence passed to the
         * interpreter.
         */
        public final int end;

        public Token(String token, int type, int start, int end) {
            this.token = token;
            this.tokenType = type;
            this.start = start;
            this.end = end;
        }
    }

    // escape and unescape methods

    private static final Escape[] escapes = {
            // plain escaped
            new Escape(ESCAPE_CHAR, ESCAPE_CHAR), new Escape(ESCAPE_B, B),
            new Escape(ESCAPE_N, N), new Escape(ESCAPE_R, R),
            new Escape(ESCAPE_T, T),
            new Escape(FULL_ESCAPE_CHAR, FULL_ESCAPE_CHAR) };

    /**
     * Escape a single command line argument for the Shell. Same as calling
     * escape(arg, <code>false</code>)
     * 
     * @param arg the unescaped argument
     * @return the escaped argument
     */
    public String escape(String arg) {
        return doEscape(arg, false); // don't force quotation
    }

    /**
     * Escape a single command line argument for the Shell.
     * 
     * @param arg the unescaped argument
     * @param forceQuote if <code>true</code>, forces the argument to be
     *        returned in quotes even if not necessary
     * @return the escaped argument
     * @deprecated This method does not belong here. Escaping is an interpretter
     *             concern, and this class needs to be interpretter specific.
     */
    public static String doEscape(String arg, boolean forceQuote) {
        int length = arg.length();
        if (length == 0) {
            return "" + QUOTE_CHAR + QUOTE_CHAR;
        }
        StringBuilder sb = new StringBuilder(length);

        // insert escape sequences
        for (int i = 0; i < arg.length(); i++) {
            char c = arg.charAt(i);
            for (int j = 0; j < escapes.length; j++) {
                if (escapes[j].plain == c) {
                    sb.append(ESCAPE_CHAR);
                    c = escapes[j].escaped;
                    break;
                }
            }
            forceQuote |= (c == SPACE_CHAR || c == QUOTE_CHAR);
            sb.append(c);
        }

        if (forceQuote) {
            sb.insert(0, FULL_ESCAPE_CHAR);
            sb.append(FULL_ESCAPE_CHAR);
        }
        return sb.toString();
    }

    public String escape(String arg, boolean forceQuote) {
        return CommandLine.doEscape(arg, forceQuote);
    }

    private static class Escape {
        final char plain;

        final char escaped;

        Escape(char plain, char escaped) {
            this.plain = plain;
            this.escaped = escaped;
        }
    }

    /**
     * Get the IO stream context for executing the command. The result is
     * guaranteed to be non-null and to have at least 3 entries.
     * 
     * @return stream context as described above.
     */
    public Closeable[] getStreams() {
        return streams.clone();
    }

    /**
     * Set the IO stream context for executing the command.
     * 
     * @param the new stream context.
     */
    public void setStreams(Closeable[] streams) {
        this.streams = streams.clone();
    }

    public void complete(CompletionInfo completion, CommandShell shell)
            throws CompletionException {
        String cmd = (commandName == null) ? "" : commandName.trim();
        String result = null;
        if (!cmd.equals("") && (arguments.length > 0 || argumentAnticipated)) {
            try {
                // get command's help info
                CommandInfo cmdClass = shell.getCommandClass(cmd);

                Help.Info info;
                try {
                    info = Help.getInfo(cmdClass.getCommandClass());
                } catch (HelpException ex) {
                    // assuming default syntax; i.e. multiple file arguments
                    info = defaultParameter;
                }

                // perform completion of the command arguments based on the
                // command's
                // help info / syntax ... if any.
                result = info.complete(this);

            } catch (ClassNotFoundException ex) {
                throw new CompletionException("Command class not found", ex);
            }
        } else {
            // do completion on the command name
            result = defaultArg.complete(cmd);
        }
        completion.setCompleted(result);
    }
}
