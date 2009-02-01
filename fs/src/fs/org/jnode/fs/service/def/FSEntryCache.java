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
 
package org.jnode.fs.service.def;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jnode.fs.FSEntry;

/**
 * @author epr
 */
@SuppressWarnings("serial")
final class FSEntryCache {

    /** The actual cache */
    private final Map<String, FSEntry> entries = new LinkedHashMap<String, FSEntry>() {
        @Override
        protected boolean removeEldestEntry(Entry<String, FSEntry> eldest) {
            return size() > 100;
        }
    };

    /**
     * Create a new instance
     * 
     * @param fsm
     */
    public FSEntryCache() {
    }

    /**
     * Gets a cached entry for a given path.
     * 
     * @param path must be an absolute path
     */
    public synchronized FSEntry getEntry(String path) {
        final FSEntry entry = entries.get(path);
        if (entry != null) {
            if (entry.isValid()) {
                return entry;
            } else {
                entries.remove(path);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Puts an entry in the cache. Any existing entry for the given path will be
     * removed.
     * 
     * @param path must be an absolute path
     * @param entry
     */
    public synchronized void setEntry(String path, FSEntry entry) {
        entries.put(path, entry);
    }

    /**
     * Remove any entry bound to the given path or a path below the given path.
     * 
     * @param rootPathStr must be an absolute path
     */
    public synchronized void removeEntries(String rootPathStr) {
        entries.remove(rootPathStr);
        final ArrayList<String> removePathList = new ArrayList<String>();
        for (String pathStr : entries.keySet()) {
            if (pathStr.startsWith(rootPathStr)) {
                removePathList.add(pathStr);
            }
        }
        for (String path : removePathList) {
            entries.remove(path);
        }
    }
}
