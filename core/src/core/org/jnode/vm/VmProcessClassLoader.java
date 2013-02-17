/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm;

import java.util.HashSet;

/**
 * @author epr
 */
public class VmProcessClassLoader extends ClassLoader {

    /**
     * Set of classname strings to skip loading via a parent classloader
     */
    private final HashSet<String> skipClassNames;

    /**
     * Create a new instance
     *
     * @param parent
     */
    public VmProcessClassLoader(ClassLoader parent) {
        super(parent);
        skipClassNames = new HashSet<String>();
        skipClassNames.add("java.lang.System");
        skipClassNames.add("org.jnode.vm.VmProcess");
    }

    /**
     * Create a new instance using the system classloader as parent.
     */
    public VmProcessClassLoader() {
        this(ClassLoader.getSystemClassLoader());
    }

    public boolean skipParentLoader(String name) {
        name = name.replace('/', '.');
        return skipClassNames.contains(name);
    }

}
