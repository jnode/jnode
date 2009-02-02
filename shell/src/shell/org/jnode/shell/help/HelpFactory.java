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
 
package org.jnode.shell.help;

import java.io.PrintWriter;
import java.util.TreeSet;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginUtils;
import org.jnode.shell.CommandInfo;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * The HelpFactory class is the base class for the Help factory classes, and provides
 * a static method for getting the default factory.  Other methods in this class are
 * here for historical reasons, and should be avoided where possible.
 * 
 * @author qades
 * @author Fabien DUMINY (fduminy@jnode.org)
 * @author crawley@jnode.org
 */
public abstract class HelpFactory {
    public static final String BUNDLE_NAME = "messages"; // must be in our package

    public static final Class<HelpFactory> NAME = HelpFactory.class;

    public static final String INFO_FIELD_NAME = "HELP_INFO";

    public static HelpFactory getHelpFactory() throws HelpException {
        try {
            return InitialNaming.lookup(NAME);
        } catch (NamingException ex) {
            throw new HelpException("Help factory not found");
        }
    }

    public static String getLocalizedHelp(String messageKey) {
        return PluginUtils.getLocalizedMessage(HelpFactory.class, 
                BUNDLE_NAME, messageKey);
    }
    
    /**
     * Obtain a CommanHelp object for a given command alias and its resolved CommandInfo.
     * 
     * @param alias
     * @param cmdInfo
     * @return
     * @throws HelpException
     */
    public abstract Help getHelp(
            String alias, CommandInfo cmdInfo) throws HelpException;
    
    // FIXME ... the remaining API methods are historical, and should not be used outside of
    // the help package and its implementation packages.

    /**
     * Shows the help page for a command
     * 
     * @param syntaxes the command's syntax bundle
     * @param bundle the command's argument bundle
     * @param out the destination for help output.
     */
    protected abstract void help(SyntaxBundle syntaxes, ArgumentBundle bundle, PrintWriter out);

    /**
     * Shows the usage line for a command
     * 
     * @param syntaxes the command's syntax bundle
     * @param bundle the command's argument bundle
     * @param out the destination for help output.
     */
    protected abstract void usage(SyntaxBundle syntaxes, ArgumentBundle bundle, PrintWriter out);

    /**
     * Shows the description of a single argument. Used as a callback in
     * {@link Argument#describe(HelpFactory)}.
     */
    protected abstract void describeArgument(org.jnode.shell.syntax.Argument<?> arg, PrintWriter out);

    /**
     * Shows the description of a single FlagArgument. Used as a callback in
     * {@link Argument#describe(HelpFactory)}.
     */
    protected abstract void describeOption(FlagArgument arg, 
            TreeSet<String> flagTokens, PrintWriter out);

}
