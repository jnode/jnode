/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.shell.alias;

import java.util.Iterator;

/**
 * @author epr
 */
public interface AliasManager {

	/**
	 * Name of the system alias manager (in the InitialNaming namespace)
	 */
	public static final Class NAME = AliasManager.class;// "system/aliasmanager";

	public static final String ALIASES_EP_NAME = "org.jnode.shell.aliases";

	/**
	 * Add an alias
	 * 
	 * @param alias
	 * @param className
	 */
	public abstract void add(String alias, String className);

	/**
	 * Remove an alias
	 * 
	 * @param alias
	 */
	public abstract void remove(String alias);

	/**
	 * Gets the class of a given alias
	 * 
	 * @param alias
	 * @return The class of the given alias
	 * @throws ClassNotFoundException
	 */
	public abstract Class getAliasClass(String alias)
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
	 * Gets the classname of a given alias
	 * 
	 * @param alias
	 * @return The classname of the given alias
	 */
	public abstract String getAliasClassName(String alias)
			throws NoSuchAliasException;

	/**
	 * Create a new alias manager that has this alias manager as parent.
	 */
	public AliasManager createAliasManager();

	/**
	 * Gets an iterator to iterate over all aliases.
	 * 
	 * @return An iterator the returns instances of String.
	 */
	public Iterator aliasIterator();

}
