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
package org.jnode.shell.help;

import java.io.PrintWriter;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine;

/**
 * This is the interface for an object that outputs command help.  Different
 * implementations support different command syntax mechanisms, and (in the
 * future) will provide help in different output formats; e.g. plain text, 
 * HTML and so on.
 * 
 * @author crawley@jnode.org
 */
public interface Help {
    
    /**
     * Output complete help for the command.
     * 
     * @param pw the help information is written here
     */
    public void help(PrintWriter pw);
    
    /**
     * Output the usage message(s) for the command.
     * 
     * @param pw the help information is written here
     */
    public void usage(PrintWriter pw);
    
    /**
     * This class is here for historical reasons.  It is a key API class in the
     * 'old' JNode syntax mechanism.
     */
    public static class Info {

        private final String name;

        private final Syntax[] syntaxes;

        public Info(String name, Syntax... syntaxes) {
            this.name = name;
            this.syntaxes = syntaxes;
        }

        public Info(String name, String description, Parameter... params) {
            this(name, new Syntax(description, params));
        }

        /**
         * Gets the name of this command
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the syntaxes allowed for this command
         */
        public Syntax[] getSyntaxes() {
            return syntaxes;
        }

        public void usage(PrintWriter out) {
            try {
                HelpFactory.getHelpFactory().usage(this, out);
            } catch (HelpException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Prints the help message.
         * @param command command name or alias which appears in the help message
         * @throws HelpException
         */
        public void help(String command, PrintWriter out) throws HelpException {
            HelpFactory.getHelpFactory().help(this, command, out);
        }

        public String complete(CompletionInfo completion, CommandLine partial, 
                PrintWriter out) throws CompletionException {
            // The completion strategy is to try to complete each of the
            // syntaxes, and return the longest completion string.
            String max = "";
            boolean foundCompletion = false;
            for (Syntax syntax : syntaxes) {
                try {
                    syntax.complete(completion, partial);
                    foundCompletion = true;

                } catch (CompletionException ex) {
                    // just try the next syntax
                }
            }
            if (!foundCompletion) {
                out.println();
                usage(out);
                throw new CompletionException("Invalid command syntax");
            }
            return max;
        }

        /**
         * Parse the supplied command arguments against this object's syntax(es).
         * @param args the command arguments
         * @return the resulting binding of parameters/arguments to values.
         * @throws SyntaxErrorException
         * @deprecated use parse(CommandLine) instead.
         * 
         */
        public ParsedArguments parse(String... args) throws SyntaxErrorException {
            return parse(new CommandLine(args));
        }

        /**
         * Parse the supplied CommandLine against this object's syntax(es).
         * 
         * @param cmdLine the CommandLine
         * @return the resulting binding of parameters/arguments to values.
         * @throws SyntaxErrorException
         */
        public ParsedArguments parse(CommandLine cmdLine) throws SyntaxErrorException {
            for (int i = 0; i < syntaxes.length; i++) {
                Syntax s = syntaxes[i];
                // FIXME ... it appears that s.parse is a stateful operation.  If that is
                // the case, we should either synchronize on s for s.parse + s.clearArguments,
                // or move the s.clearArgument call into s.parse.
                try {
                    return s.parse(cmdLine);
                } catch (SyntaxErrorException ex) {
                    s.clearArguments();
                    if (syntaxes.length == 1) {
                        // If there was only one syntax, propagate the exception so that
                        // we can tell the user why the arguments didn't match.
                        throw ex;
                    }
                }
            }

            // There were no syntaxes, or we have tried more than one syntax and they 
            // all have failed.
            throw new SyntaxErrorException("No matching syntax found");
        }
    }
}
