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

import java.lang.reflect.Field;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginUtils;
import org.jnode.shell.CommandLine;

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
    
    public static String getLocalizedHelp(String messageKey)
    {
    	return PluginUtils.getLocalizedMessage(Help.class, 
    					BUNDLE_NAME, messageKey);
    }

    public static Info getInfo(Class clazz) throws HelpException {
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
     * @param info
     *            the command information
     */
    public abstract void help(Info info);

    /**
     * Shows the usage line for a command
     * 
     * @param info
     *            the command information
     */
    public abstract void usage(Info info);

    /**
     * Shows the description of a single argument. Used as a callback in
     * {@link Argument#describe(Help)}.
     */
    public abstract void describeArgument(Argument arg);

    /**
     * Shows the description of a single parameter. Used as a callback in
     * {@link Parameter#describe(Help)}.
     */
    public abstract void describeParameter(Parameter param);

    /**
     * Here is where all the command description goes. Example code: <br>
     * 
     * <pre>
     * 
     *   public class DdCommand { public static CommandShell.Info commandShellInfo = new CommandShell.Info( &quot;dd&quot;, // Internal command name &quot;Copies blocks of data between block and character devices&quot;, // short description of the command new Parameter[]{ new Parameter(&quot;if&quot;, &quot;input file&quot;, new FileArgument(&quot;infilename&quot;, &quot;location of the input file / device&quot;), Parameter.MANDATORY), new Parameter(&quot;of&quot;, &quot;output file&quot;, new FileArgument(&quot;outfilename&quot;, &quot;location of the output file / device&quot;), Parameter.MANDATORY), new Parameter(&quot;count&quot;, &quot;count of blocks to transfer&quot;, new Argument(&quot;blockCount&quot;, &quot;count of blocks to transfer&quot;), Parameter.MANDATORY), new Parameter(&quot;bs&quot;, &quot;size of the blocks to transfer&quot;, new Argument(&quot;blocksize&quot;, &quot;example: 512, 42K, 300M, 2G. default: 1&quot;), Parameter.OPTIONAL) } );
     *  
     *   ... }
     *  
     * </pre>
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

        public void usage() {
            try {
                Help.getHelp().usage(this);
            } catch (HelpException ex) {
                ex.printStackTrace();
            }
        }

        public void help() throws HelpException {
            Help.getHelp().help(this);
        }

        public String complete(CommandLine partial) throws CompletionException {
            //System.out.println("completing \"" + partial + "\"");
            String max = "";
            boolean foundCompletion = false;
            for (Syntax syntax : syntaxes) {
                try {
                    final String s = syntax.complete(partial
                            .getRemainder());
                    foundCompletion = true;
                    if (s.length() > max.length()) {
                        max = s;
                    }
                } catch (CompletionException ex) {
                    // this syntax is not fitting
                    // following debug output is for testing the "intelligent"
                    // delegation mechanism
                    // System.err.println("Syntax \"" +
                    // syntaxes[i].getDescription() + "\" threw "
                    // + ex.toString());
                }
            }
            if ((max.length() > 0) || foundCompletion) {
                return max;
            } else {
                System.out.println();
                usage();
                throw new CompletionException("Invalid command syntax");
            }
        }

        public ParsedArguments parse(String... args) throws SyntaxErrorException {
            for (Syntax s : syntaxes) {
                try {
                    return s.parse(args);
                } catch (SyntaxErrorException ex) {
                    s.clearArguments();
                    // debug output to debug syntax finding mechanism
                    //System.err.println(ex.toString());
                    //ex.printStackTrace(System.out);
                }
            }

            // no fitting Syntax found? trow an error
            throw new SyntaxErrorException("No matching syntax found");
        }
    }

}
