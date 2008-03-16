/*
 * $Id: DefaultAliasManager.java 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.test.shell.syntax;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;

/**
 * @author crawley@jnode.org
 */
public class TestAliasManager implements AliasManager {

    private final HashMap<String, Alias> aliases = new HashMap<String, Alias>();
    
    /**
     * Add an alias
     * 
     * @param alias
     * @param className
     */
    public void add(String alias, String className) {
        try {
                if (getAliasClassName(className) != null) {
                    className = getAliasClassName(className);
                }
            } catch (NoSuchAliasException e) {
            }
            aliases.put(alias, new Alias(alias, className, false));
        }

    /**
     * Remove an alias
     * 
     * @param alias
     */
    public void remove(String alias) {
        aliases.remove(alias);
    }

    /**
     * Gets the class of a given alias
     * 
     * @param alias
     * @return the class of the given alias
     * @throws ClassNotFoundException
     */
    public Class<?> getAliasClass(String alias) throws ClassNotFoundException,
            NoSuchAliasException {
        return getAlias(alias).getAliasClass();
    }

    /**
     * Should the given alias be invoked in the context of the shell, instead of
     * in its own context.
     * 
     * @param alias
     */
    public boolean isInternal(String alias) throws NoSuchAliasException {
        return getAlias(alias).isInternal();
    }

    /**
     * Gets the classname of a given alias
     * 
     * @param alias
     * @return the classname of the given alias
     */
    public String getAliasClassName(String alias) throws NoSuchAliasException {
        return getAlias(alias).getClassName();
    }

    /**
     * Create a new alias manager that has this alias manager as parent.
     */
    public AliasManager createAliasManager() {
        return new TestAliasManager();
    }

    /**
     * Gets a collection of all aliases.
     */
    public Collection<String> aliases() {
        return Collections.unmodifiableCollection(aliases.keySet());
    }

    /**
     * Gets an iterator to iterator over all aliases.
     * 
     * @return An iterator the returns instances of String.
     */
    public Iterator<String> aliasIterator() {
        return aliases().iterator();
    }

    /**
     * Gets the alias with the given name
     * 
     * @param alias
     */
    protected Alias getAlias(String alias) throws NoSuchAliasException {
        Alias res = aliases.get(alias);
        if (res == null) {
            throw new NoSuchAliasException(alias);
        }
        return res;
    }

    static class Alias {
        private final String alias;

        private final String className;

        private Class<?> aliasClass;

        private final boolean internal;

        public Alias(String alias, String className, boolean internal) {
            this.alias = alias;
            this.className = className;
            this.internal = internal;
        }

        /**
         * Gets the name of this alias
         */
        public String getAlias() {
            return alias;
        }

        /**
         * Gets the name of the class of this alias
         */
        public String getClassName() {
            return className;
        }

        /**
         * Gets the class of this alias
         */
        public Class<?> getAliasClass() throws ClassNotFoundException {
            if (aliasClass == null) {
                aliasClass = Thread.currentThread().getContextClassLoader()
                        .loadClass(className);
            }
            return aliasClass;
        }

        /**
         * Should this alias be executed in the context of the shell, instead of
         * in its own context.
         * 
         * @return
         */
        public final boolean isInternal() {
            return internal;
        }
    }
}
