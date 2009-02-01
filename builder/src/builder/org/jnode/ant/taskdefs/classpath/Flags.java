/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.ant.taskdefs.classpath;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class Flags {
    private int flags;
    private SortedSet<String> bugIDs = new TreeSet<String>();

    public Flags() {
        this(0);
    }

    public Flags(int flags) {
        this.flags = flags;
    }

    public void set(int flag) {
        flags |= flag;
    }

    public Flags mask(int mask) {
        return new Flags(this.flags & mask);
    }

    public boolean isSet(int flag) {
        return ((flags & flag) != 0);
    }

    public int asInt() {
        return flags;
    }

    public void addBugID(String id) {
        bugIDs.add(id);
    }

    public String[] getBugIDs() {
        return bugIDs.toArray(new String[bugIDs.size()]);
    }
}
