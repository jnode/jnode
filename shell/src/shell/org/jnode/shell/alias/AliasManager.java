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
 
package org.jnode.shell.alias;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author epr
 */
public interface AliasManager {

	/**
	 * Name of the system alias manager (in the InitialNaming namespace)
	 */
	public static final Class<AliasManager> NAME = AliasManager.class;// "system/aliasmanager";

	public static final String ALIASES_EP_NAME = "org.jnode.shell.aliases";

	/**
	 * Add or update an alias to class name binding.  The supplied class name is not checked
	 * in any way, but the shell expects it to be the name of a loadable class which suitable
	 * for use as a "command".  
	 * 
	 * @param alias a new or existing alias name
	 * @param className a fully qualified Java class name.
	 */
	public abstract void add(String alias, String className);

	/**
	 * Remove an alias to class name binding.
	 * 
	 * @param alias
	 */
	public abstract void remove(String alias);

	/**
	 * Gets the class for a given alias.  If necessary, the alias manager will 
	 * attempt to load the class.
	 * 
	 * @param alias
	 * @return The class for the given alias
	 * @throws ClassNotFoundException
	 */
	public abstract Class<?> getAliasClass(String alias)
			throws ClassNotFoundException, NoSuchAliasException;

	/**
	 * Should the given alias be invoked in the context of the shell, instead of
	 * in its own context.
	 * 
	 * @param alias
	 */
	public abstract boolean isInternal(String alias)
			throws NoSuchAliasException;

	/**
	 * Gets the class name currently bound to a given alias
	 * 
	 * @param alias
	 * @return The class name for the given alias
	 */
	public abstract String getAliasClassName(String alias)
			throws NoSuchAliasException;

	/**
	 * Create a new alias manager that has this alias manager as parent.
	 */
	public AliasManager createAliasManager();

	/**
	 * Gets a collection of all aliases known to this alias manager.
	 */
	public Collection<String> aliases();

    /**
     * Gets an iterator to iterate over all aliases for the alias manager.
     * 
     * @return An iterator the returns instances of String.
     */
    public Iterator<String> aliasIterator();
}
