/*
 * $Id: AliasManager.java 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.shell.syntax;

import java.util.Collection;

/**
 * A SyntaxManager manages the association between a command "alias" and 
 * the Syntax that specifies its argument syntax.  
 * 
 * @author crawley@jnode.org
 */
public interface SyntaxManager {

    /**
     * Name of the system alias manager (in the InitialNaming namespace)
     */
    public static final Class<SyntaxManager> NAME = SyntaxManager.class;

    public static final String ALIASES_EP_NAME = "org.jnode.shell.syntaxes";

    /**
     * Add a syntax bundle
     * 
     * @param bundle The syntax to be added
     */
    public abstract void add(SyntaxBundle bundle);

    /**
     * Remove the syntaxBundle for an alias
     * 
     * @param alias The alias
     */
    public abstract SyntaxBundle remove(String alias);

    /**
     * Gets the syntax bundle for a given alias
     * 
     * @param alias The alias
     * @return The syntax for the given alias, or <code>null</code>
     */
    public abstract SyntaxBundle getSyntaxBundle(String alias);
    
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
