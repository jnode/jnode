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

import java.util.NoSuchElementException;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.io.CommandIOMarker;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This class represents the command line as command name and a sequence of
 * argument strings. It also can carry the i/o stream environment for launching
 * the command.
 * 
 * TODO This class needs to be fully "shell and command syntax agnostic".
 * TODO Get rid of API methods using a String argument representation.
 * 
 * @author crawley@jnode.org
 */
@SuppressWarnings("deprecation")
public class CommandLine implements Completable, Iterable<String> {

    public static final CommandIO DEFAULT_STDIN = new CommandIOMarker("STDIN");

    public static final CommandIO DEFAULT_STDOUT = new CommandIOMarker("STDOUT");

    public static final CommandIO DEFAULT_STDERR = new CommandIOMarker("STDERR");

    public static final CommandIO DEVNULL = new CommandIOMarker("DEVNULL");

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

    @SuppressWarnings("deprecation")
    private final Help.Info defaultInfo = new Help.Info("file",
            "default parameter for command line completion", 
            new Parameter(
                    new org.jnode.shell.help.argument.FileArgument(
                            "file", "a file", org.jnode.shell.help.Argument.MULTI),
                            org.jnode.shell.help.Parameter.OPTIONAL));

    private final org.jnode.shell.help.Argument defaultArg = 
        new org.jnode.shell.help.argument.AliasArgument("command",
            "the command to be called");

//  private final Syntax defaultSyntax = new RepeatSyntax(new ArgumentSyntax("argument"));
//  private final ArgumentBundle defaultArguments = new ArgumentBundle(
//  new FileArgument("argument", org.jnode.shell.syntax.Argument.MULTIPLE));

    private final Token commandToken;

    private final Token[] argumentTokens;

    private CommandIO[] ios;

    private boolean argumentAnticipated = false;

    /**
     * Create a new instance using Tokens instead of Strings.
     * 
     * @param commandToken the command name token or <code>null</code>.
     * @param argumentTokens the argument token list or <code>null</code>.
     * @param ios the io stream array or <code>null</code>.
     */
    public CommandLine(Token commandToken, Token[] argumentTokens,
            CommandIO[] ios) {
        this.commandToken = commandToken;
        this.argumentTokens = (argumentTokens == null || argumentTokens.length == 0) ? NO_TOKENS
                : argumentTokens.clone();
        this.ios = setupStreams(ios);
    }

    /**
     * Create a new instance encapsulating a command name, argument list and io
     * stream array. If 'arguments' is <code>null</code>, a zero length
     * String array is substituted. If 'streams' is <code>null</code> , an
     * array of length 4 is substituted. A non-null 'streams' argument must have
     * a length of at least 4.
     * 
     * @param commandName the command name or <code>null</code>.
     * @param arguments the argument list or <code>null</code>.
     * @param ios the io stream array or <code>null</code>.
     */
    public CommandLine(String commandName, String[] arguments, CommandIO[] ios) {
        this.commandToken = commandName == null ? null : new Token(commandName);
        if (arguments == null || arguments.length == 0) {
            this.argumentTokens = NO_TOKENS;
        } else {
            int len = arguments.length;
            argumentTokens = new Token[len];
            for (int i = 0; i < len; i++) {
                this.argumentTokens[i] = new Token(arguments[i]);
            }
        }
        this.ios = setupStreams(ios);
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
        this(null, arguments, null /* FIXME */);
    }

    private CommandIO[] setupStreams(CommandIO[] ios) {
        if (ios == null) {
            ios = new CommandIO[4];
            ios[Command.STD_IN] = DEFAULT_STDIN;
            ios[Command.STD_OUT] = DEFAULT_STDOUT;
            ios[Command.STD_ERR] = DEFAULT_STDERR;
            ios[Command.SHELL_ERR] = DEFAULT_STDERR;
            return ios;
        } else if (ios.length < 4) {
            throw new IllegalArgumentException("streams.length < 4");
        } else {
            return ios.clone();
        }
    }

    /**
     * This method returns an Iterator for the arguments represented as Strings.
     * 
     * @deprecated
     */
    public SymbolSource<String> iterator() {
        final boolean whitespaceAfterLast = this.argumentAnticipated;

        return new SymbolSource<String>() {
            private int pos = 0;

            public boolean hasNext() {
                return pos < argumentTokens.length;
            }

            public String next() throws NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return argumentTokens[pos++].token;
            }

            public String peek() throws NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return argumentTokens[pos].token;
            }

            public String last() throws NoSuchElementException {
                if (pos <= 0) {
                    throw new NoSuchElementException();
                }
                return argumentTokens[pos - 1].token;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void seek(int pos) throws NoSuchElementException {
                if (pos >= 0 && pos <= argumentTokens.length) {
                    this.pos = pos;
                } else {
                    throw new NoSuchElementException("pos out of range");
                }
            }

            public int tell() {
                return pos;
            }

            public boolean whitespaceAfterLast() {
                return whitespaceAfterLast;
            }

        };
    }

    /**
     * This method returns an Iterator for the arguments represented as Tokens
     */
    public SymbolSource<Token> tokenIterator() throws NoTokensAvailableException {
        if (argumentTokens == null) {
            throw new NoTokensAvailableException("No tokens available in the CommandLine");
        }

        final boolean whitespaceAfterLast = this.argumentAnticipated;

        return new SymbolSource<Token>() {
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

            public Token peek() throws NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return argumentTokens[pos];
            }

            public Token last() throws NoSuchElementException {
                if (pos <= 0) {
                    throw new NoSuchElementException();
                }
                return argumentTokens[pos - 1];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void seek(int pos) throws NoSuchElementException {
                if (pos >= 0 && pos <= argumentTokens.length) {
                    this.pos = pos;
                } else {
                    throw new NoSuchElementException("pos out of range");
                }
            }

            public int tell() {
                return pos;
            }

            public boolean whitespaceAfterLast() {
                return whitespaceAfterLast;
            }

        };
    }

    /**
     * Get the command name
     * 
     * @return the command name
     */
    public String getCommandName() {
        return commandToken == null ? null : commandToken.token;
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
        int len = argumentTokens.length;
        if (len == 0) {
            return NO_ARGS;
        }
        String[] arguments = new String[len];
        for (int i = 0; i < len; i++) {
            arguments[i] = argumentTokens[i].token;
        }
        return arguments;
    }

    /**
     * Get the arguments as String[].
     * 
     * @return the arguments as String[]
     * @deprecated this method name is wrong.
     */
    public String[] toStringArray() {
        return getArguments();
    }

    /**
     * Returns the entire command line as a string.
     * 
     * @return the entire command line
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(escape(commandToken.token));
        for (Token arg : argumentTokens) {
            sb.append(' ');
            sb.append(escape(arg.token));
        }
        return sb.toString();
    }

    /**
     * Gets the remaining number of parts
     * 
     * @return the remaining number of parts
     */
    public int getLength() {
        return argumentTokens.length;
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
         * interpreter specific.  The value -1 indicates that no token type is 
         * available.
         */
        public final int tokenType;

        /**
         * This field denotes the character offset of the first character of
         * this token in the source character sequence passed to the
         * interpreter.  The value -1 indicates that no source start position is 
         * available.
         */
        public final int start;

        /**
         * This field denotes the character offset + 1 for the last character of
         * this token in the source character sequence passed to the
         * interpreter.  The value -1 indicates that no source end position is 
         * available.
         */
        public final int end;

        public Token(String value, int type, int start, int end) {
            this.token = value;
            this.tokenType = type;
            this.start = start;
            this.end = end;
        }

        public Token(String token) {
            this(token, -1, -1, -1);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + end;
            result = prime * result + start;
            result = prime * result + ((token == null) ? 0 : token.hashCode());
            result = prime * result + tokenType;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Token other = (Token) obj;
            if (end != other.end)
                return false;
            if (start != other.start)
                return false;
            if (token == null) {
                if (other.token != null)
                    return false;
            } else if (!token.equals(other.token))
                return false;
            if (tokenType != other.tokenType)
                return false;
            return true;
        } 

        public String toString() {
            return "Token{'" + token + "'," + start + "," + end + "," + tokenType + "}";
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
     * @deprecated This method does not belong here. Escaping is an interpreter
     *             concern, and this class needs to be interpreter specific.
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
     * guaranteed to be non-null and to have at least 4 entries.
     * 
     * @return the stream context as described above.
     */
    public CommandIO[] getStreams() {
        return ios.clone();
    }

    /**
     * Set the IO stream context for executing the command.
     * 
     * @param ios the command's new stream context.
     */
    public void setStreams(CommandIO[] ios) {
        if (ios.length < 4) {
            throw new IllegalArgumentException("need >= 4 CommandIO objects");
        }
        this.ios = ios.clone();
    }

    /**
     * Perform command line argument parsing in preparation to invoking a command.
     * This locates the command's class and a suitable command line syntax, then
     * parses against the Syntax, binding the command arguments to Argument objects
     * in an ArgumentBundle object obtained from the Command object.
     * 
     * @param shell the context for resolving command aliases and locating syntaxes
     * @return a CompandInfo which includes the command instance to which the arguments have been bound
     * @throws CommandSyntaxException if the chosen syntax doesn't match the command
     * line arguments.
     */
    public CommandInfo parseCommandLine(CommandShell shell) throws ShellException {
        String cmd = (commandToken == null) ? "" : commandToken.token.trim();
        if (cmd.equals("")) {
            throw new ShellFailureException("no command name");
        }
        try {
            // Get command's argument bundle and syntax
            CommandInfo cmdInfo = shell.getCommandInfo(cmd);
            Command command = cmdInfo.createCommandInstance();

            // Get the command's argument bundle, or the default one.
            ArgumentBundle bundle = (command == null) ? null :
                command.getArgumentBundle();
            if (bundle != null) {
                // Get a syntax for the alias, or a default one.
                SyntaxBundle syntaxes = shell.getSyntaxManager().getSyntaxBundle(cmd);

                // Do a full parse to bind the command line argument tokens to corresponding
                // command arguments
                bundle.parse(this, syntaxes);
            }
            return cmdInfo;
        } catch (ClassNotFoundException ex) {
            throw new ShellException("Command class not found", ex);
        } catch (InstantiationException ex) {
            throw new ShellException("Command class cannot be instantiated", ex);
        } catch (IllegalAccessException ex) {
            throw new ShellException("Command class cannot be instantiated", ex);
        }
    }

    public void complete(CompletionInfo completion, CommandShell shell) throws CompletionException {
        String cmd = (commandToken == null) ? "" : commandToken.token.trim();
        if (!cmd.equals("") && (argumentTokens.length > 0 || argumentAnticipated)) {
            CommandInfo cmdClass;
            try {
                cmdClass = shell.getCommandInfo(cmd);
            } catch (ClassNotFoundException ex) {
                throw new CompletionException("Command class not found", ex);
            }

            Command command;
            try {
                command = cmdClass.createCommandInstance();
            } catch (Throwable ex) {
                throw new CompletionException("Problem creating a command instance", ex);
            }

            // Get the command's argument bundle, or the default one.
            ArgumentBundle bundle = (command == null) ? null : command.getArgumentBundle();

            // Get a syntax for the alias, or a default one.
            SyntaxBundle syntaxes = shell.getSyntaxManager().getSyntaxBundle(cmd);

            try {
                // Try new-style completion if we have a Syntax
                if (bundle != null) {
                    bundle.complete(this, syntaxes, completion);
                } else {
                    // Otherwise, try old-style completion using the command's INFO
                    try {
                        Help.Info info = HelpFactory.getInfo(cmdClass.getCommandClass());
                        info.complete(completion, this, shell.getOut());
                    } catch (HelpException ex) {
                        // And fall back to old-style completion with an 'info' that
                        // specifies a sequence of 'file' names.
                        // FIXME ...
                        defaultInfo.complete(completion, this, shell.getOut());
                    }
                }
            } catch (CommandSyntaxException ex) {
                throw new CompletionException("Command syntax problem", ex);
            }
        } else {
            // do completion on the command name
            defaultArg.complete(completion, cmd);
            completion.setCompletionStart(commandToken == null ? 0 : commandToken.start);
        }
    }

    public CommandInfo getCommandInfo(CommandShell shell) {
        String cmd = (commandToken == null) ? "" : commandToken.token.trim();
        if (cmd.equals("")) {
            return null;
        }
        try {
            return shell.getCommandInfo(cmd);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
