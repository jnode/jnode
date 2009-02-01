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
 
package org.jnode.fs.jifs;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jnode.fs.FSEntry;

/**
 * @author Andreas H\u00e4nel
 */
public class JIFSDirIterator implements Iterator<FSEntry> {

    private Iterator<FSEntry> it;

    public JIFSDirIterator(Collection<FSEntry> entries) {
        it = entries.iterator();
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        if (it == null) {
            return false;
        } else {
            return it.hasNext();
        }
    }

    /**
     * @see java.util.Iterator#next()
     */
    public FSEntry next() throws NoSuchElementException {
        if (hasNext()) {
            FSEntry current = it.next();
            if (current instanceof JIFSFile)
                ((JIFSFile) current).refresh();
            if (current instanceof JIFSDirectory)
                ((JIFSDirectory) current).refresh();

            return current;
        } else {
            throw new NoSuchElementException("no more FSEntries to iterate..");
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
