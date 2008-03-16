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

/**
 * @author crawley@jnode.org
 */
public interface SyntaxManager {

    /**
     * Name of the system alias manager (in the InitialNaming namespace)
     */
    public static final Class<SyntaxManager> NAME = SyntaxManager.class;

    public static final String ALIASES_EP_NAME = "org.jnode.shell.syntaxes";

    /**
     * Add a syntax for an alias
     * 
     * @param alias The alias
     * @param syntax The syntax to be added
     */
    public abstract void add(String alias, Syntax syntax);

    /**
     * Remove the syntax for an alias
     * 
     * @param alias The alias
     * @param syntax 
     */
    public abstract Syntax remove(String alias);

    /**
     * Gets the syntax for a given alias
     * 
     * @param alias 
     * @return The syntax the given alias
     * @throws ClassNotFoundException
     */
    public abstract Syntax getSyntax(String alias);

    /**
     * Create a new syntax manager that has this syntax manager as its parent.
     */
    public SyntaxManager createSyntaxManager();

}
