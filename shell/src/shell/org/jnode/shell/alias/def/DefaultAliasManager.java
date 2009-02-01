/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.shell.alias.def;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.util.BooleanUtils;

/**
 * @author epr
 */
public class DefaultAliasManager implements AliasManager,
        ExtensionPointListener {

    private final DefaultAliasManager parent;

    private final HashMap<String, Alias> aliases = new HashMap<String, Alias>();

    private final ExtensionPoint aliasesEP;

    /**
     * Create a new instance
     */
    public DefaultAliasManager(ExtensionPoint aliasesEP) {
        this.parent = null;
        this.aliasesEP = aliasesEP;
        aliasesEP.addListener(this);
        refreshAliases();
    }

    /**
     * Create a new instance
     */
    public DefaultAliasManager(DefaultAliasManager parent) {
        this.parent = parent;
        this.aliasesEP = null;
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
    }

    /**
     * Add an alias
     * 
     * @param alias
     * @param className
     */
    public void add(String alias, String className) {
        if (parent == null) {
            throw new UnsupportedOperationException(
                    "Cannot modify the system alias manager");
        } else {
            aliases.put(alias, new Alias(alias, className, false));
        }
    }

    /**
     * Remove an alias
     * 
     * @param alias
     */
    public void remove(String alias) {
        if (parent == null) {
            throw new UnsupportedOperationException(
                    "Cannot modify the system alias manager");
        } else {
            aliases.remove(alias);
        }
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
     * Gets the class name of a given alias
     * 
     * @param alias
     * @return the class name of the given alias
     */
    public String getAliasClassName(String alias) throws NoSuchAliasException {
        return getAlias(alias).getClassName();
    }

    /**
     * Create a new alias manager that has this alias manager as parent.
     */
    public AliasManager createAliasManager() {
        return new DefaultAliasManager(this);
    }

    /**
     * Gets a collection of all aliases.
     */
    public Collection<String> aliases() {
        if (parent == null) {
            return Collections.unmodifiableCollection(aliases.keySet());
        } else {
            final HashSet<String> all = new HashSet<String>();
            all.addAll(parent.aliases());
            all.addAll(aliases.keySet());
            return all;
        }
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
        final Alias a = (Alias) aliases.get(alias);
        if (a != null) {
            return a;
        } else if (parent != null) {
            return parent.getAlias(alias);
        } else {
            throw new NoSuchAliasException(alias);
        }
    }

    /**
     * Reload the alias list from the extension-point
     */
    protected void refreshAliases() {
        System.out.println("Refreshing alias list");
        if (aliasesEP != null) {
            aliases.clear();
            final Extension[] extensions = aliasesEP.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                final Extension ext = extensions[i];
                final ConfigurationElement[] elements = ext
                        .getConfigurationElements();
                for (int j = 0; j < elements.length; j++) {
                    createAlias(aliases, elements[j]);
                }
            }
        }
    }

    private void createAlias(Map<String, Alias> aliases, ConfigurationElement element) {
        final String name = element.getAttribute("name");
        final String className = element.getAttribute("class");
        final boolean internal = BooleanUtils.valueOf(element
                .getAttribute("internal"));
        if ((name != null) && (className != null)) {
            aliases.put(name, new Alias(name, className, internal));
        }
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint,
     *      org.jnode.plugin.Extension)
     */
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        refreshAliases();
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint,
     *      org.jnode.plugin.Extension)
     */
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        refreshAliases();
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
