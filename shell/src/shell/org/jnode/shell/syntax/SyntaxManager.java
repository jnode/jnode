/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.shell.syntax;

import java.util.Collection;
import org.jnode.shell.syntax.ArgumentSpecLoader.ArgumentSpec;

/**
 * A SyntaxManager manages the association between a command "alias" and 
 * the Syntax that specifies its argument syntax.  The manager can also
 * record a set of argument specs for non-native commands.
 * 
 * @author crawley@jnode.org
 */
public interface SyntaxManager {

    /**
     * Name of the system alias manager (in the InitialNaming namespace)
     */
    public static final Class<SyntaxManager> NAME = SyntaxManager.class;

    public static final String SYNTAXES_EP_NAME = "org.jnode.shell.syntaxes";

    /**
     * Add a syntax bundle, using the alias name embedded in the bundle.
     * 
     * @param bundle The syntax to be added
     */
    public abstract void add(SyntaxBundle bundle);

    /**
     * Add the argument specs for a non-native command; i.e. one which does
     * not define and register its own arguments.
     *
     * @param argSpecs the specs for the arguments
     * @param alias the alias
     */
    public abstract void add(String alias, ArgumentSpec<?>[] argSpecs);
    
    /**
     * Remove the syntaxBundle and argumentBundle (if one exists) for an alias
     * 
     * @param alias The alias
     */
    public abstract SyntaxBundle remove(String alias);

    /**
     * Gets the syntax bundle for a given alias
     * 
     * @param alias the alias
     * @return The syntax for the given alias, or <code>null</code>
     */
    public abstract SyntaxBundle getSyntaxBundle(String alias);
    
    /**
     * Gets the argument bundle for a given alias if one exists.
     *
     * @param alias an alias that corresponds to a particular bundle
     * @return The arguments for the given alias, or <code>null</code>
     */
    public abstract ArgumentBundle getArgumentBundle(String alias);
    
    /**
     * Get the current set of keys known to the SyntaxManager.
     * 
     * @return the aliases which have syntaxes defined.
     */
    public abstract Collection<String> getKeys();

    /**
     * Create a new syntax manager that has this syntax manager as its parent.
     */
    public SyntaxManager createSyntaxManager();

}
