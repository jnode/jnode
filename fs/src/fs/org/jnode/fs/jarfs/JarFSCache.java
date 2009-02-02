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
 
package org.jnode.fs.jarfs;

import java.util.Map;
import java.util.jar.JarEntry;

/**
 * TODO use JCache API
 * 
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 *
 */
public class JarFSCache {
    private Map<JarEntry, JarFSEntry> jarFSEntries =
            new java.util.Hashtable<JarEntry, JarFSEntry>();
    private Map<JarFSEntry, Map<String, JarFSEntry>> childEntries =
            new java.util.Hashtable<JarFSEntry, Map<String, JarFSEntry>>();

    public JarFSCache() {
    }

    public JarFSEntry get(JarEntry jarEntry) {
        return jarFSEntries.get(jarEntry);
    }

    public Object put(JarEntry jarEntry, JarFSEntry fsEntry) {
        return jarFSEntries.put(jarEntry, fsEntry);
    }

    public boolean isEmpty() {
        return (jarFSEntries.size() == 0);
    }

    public Map<String, JarFSEntry> getChildEntries(JarFSEntry entry) {
        Map<String, JarFSEntry> children = childEntries.get(entry);
        if (children == null) {
            children = new java.util.Hashtable<String, JarFSEntry>();
            for (JarFSEntry jarFSEntry : jarFSEntries.values()) {
                if (jarFSEntry.getParentFSEntry().equals(entry)) {
                    children.put(jarFSEntry.getName(), jarFSEntry);
                }
            }
            childEntries.put(entry, children);
        }

        return children;
    }
}
