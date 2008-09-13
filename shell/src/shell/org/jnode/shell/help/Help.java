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
import java.lang.reflect.Field;
import java.util.TreeSet;

import javax.naming.NamingException;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginUtils;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * @author qades
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public abstract class Help {
    public static final String BUNDLE_NAME = "messages"; // must be in our package

    public static final Class<Help> NAME = Help.class;

    public static final String INFO_FIELD_NAME = "HELP_INFO";

    public static Help getHelp() throws HelpException {
        try {
            return InitialNaming.lookup(NAME);
        } catch (NamingException ex) {
            throw new HelpException("Help application not found");
        }
    }

    public static String getLocalizedHelp(String messageKey) {
        return PluginUtils.getLocalizedMessage(Help.class, 
                BUNDLE_NAME, messageKey);
    }

    public static Info getInfo(Class<?> clazz) throws HelpException {
        try {
            Field helpInfo = clazz.getField(INFO_FIELD_NAME);
            return (Help.Info) helpInfo.get(null); // static access
        } catch (NoSuchFieldException ex) {
            throw new HelpException("Command information not found");
        } catch (IllegalAccessException ex) {
            throw new HelpException("Command information not accessible");
        }
    }

    /**
     * Shows the help page for a command
     * 
     * @param info the command info
     * @param command a command name or alias which appears in the help
     * @param out the destination for help output.
     */
    public abstract void help(Info info, String command, PrintWriter out);

    /**
     * Shows the help page for a command
     * 
     * @param syntaxes the command's syntax bundle
     * @param bundle the command's argument bundle
     * @param out the destination for help output.
     */
    public abstract void help(SyntaxBundle syntaxes, ArgumentBundle bundle, PrintWriter out);

    /**
     * Shows the usage line for a command
     * 
     * @param info the command information
     * @param out the destination for help output.
     */
    public abstract void usage(Info info, PrintWriter out);

    /**
     * Shows the usage line for a command
     * 
     * @param syntaxes the command's syntax bundle
     * @param bundle the command's argument bundle
     * @param out the destination for help output.
     */
    public abstract void usage(SyntaxBundle syntaxes, ArgumentBundle bundle, PrintWriter out);

    /**
     * Shows the description of a single argument. Used as a callback in
     * {@link Argument#describe(Help)}.
     */
    public abstract void describeArgument(Argument arg, PrintWriter out);

    /**
     * Shows the description of a single argument. Used as a callback in
     * {@link Argument#describe(Help)}.
     */
    public abstract void describeArgument(org.jnode.shell.syntax.Argument<?> arg, PrintWriter out);

    /**
     * Shows the description of a single FlagArgument. Used as a callback in
     * {@link Argument#describe(Help)}.
     */
    public abstract void describeOption(FlagArgument arg, 
            TreeSet<String> flagTokens, PrintWriter out);

    /**
     * Shows the description of a single parameter. Used as a callback in
     * {@link Parameter#describe(Help)}.
     */
    public abstract void describeParameter(Parameter param, PrintWriter out);

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
                Help.getHelp().usage(this, out);
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
            Help.getHelp().help(this, command, out);
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
