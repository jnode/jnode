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

package org.jnode.plugin;

import java.util.Collection;
import java.util.Set;

import org.jnode.vm.classmgr.VmClassLoader;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class PluginClassLoader extends ClassLoader {

    /**
     * Default ctor
     */
    public PluginClassLoader() {
        // No code here
    }

    /**
     * Wrap this classloader around the given vmClassLoader.
     * Requires special permission.
     *
     * @param parent
     * @param vmClassLoader
     */
    protected PluginClassLoader(ClassLoader parent, VmClassLoader vmClassLoader) {
        super(parent, vmClassLoader);
    }

    /**
     * Gets the descriptor of the plugin in which this element was declared.
     *
     * @return The descriptor
     */
    public abstract PluginDescriptor getDeclaringPluginDescriptor();

    /**
     * Gets the names of the classes contained in this plugin.
     *
     * @return the set of contained classnames
     */
    public abstract Set<String> getClassNames();

    /**
     * Gets the names of the resources contained in this plugin.
     *
     * @return the set of contained resources
     */
    public abstract Collection<String> getResources();
    
    public String toString() {
        return getClass().getName() + "(" + getDeclaringPluginDescriptor().getId() + ")";
    }
}
