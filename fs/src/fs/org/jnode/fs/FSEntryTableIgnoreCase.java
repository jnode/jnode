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
 
package org.jnode.fs;

import java.util.List;

import org.jnode.fs.spi.AbstractFileSystem;
import org.jnode.fs.spi.FSEntryTable;

/**
 * A child class of FSEntryTable that ignore the case of entry names.
 * 
 * @author Fabien DUMINY
 */
public class FSEntryTableIgnoreCase extends FSEntryTable {
    
    /**
     * Construct a FSEntryTableIgnoreCase from a list of FSEntry
     * 
     * @param fs
     * @param entryList
     */
    public FSEntryTableIgnoreCase(AbstractFileSystem<?> fs, List<FSEntry> entryList) {
        super(fs, entryList);
    }

    /**
     * To Ignore case, we convert all entry names to upper case
     */
    protected String normalizeName(String name) {
        if (name == null) {
            return null;
        }

        return name.toUpperCase();
    }
}
