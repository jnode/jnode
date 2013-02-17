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
 
package org.jnode.vm.classmgr;

import java.util.Iterator;

/**
 * @author Levente S\u00e1ntha
 */
public class VmStaticsIterator extends VmStaticsBase implements Iterator<VmType> {
    private VmStatics statics;
    private VmType next;
    private byte[] types;
    private int size;
    private int current;

    public VmStaticsIterator(VmStatics statics) {
        if (statics == null) throw new IllegalArgumentException();
        this.statics = statics;
        this.types = this.statics.getAllocator().getTypes();
        this.size = types.length;
        this.current = 0;
    }

    public boolean hasNext() {
        while (current < size) {
            if (types[current] == TYPE_CLASS) {
                try {
                    next = statics.getTypeEntry(current);
                    if (next != null) {
                        current++;
                        return true;
                    }
                } catch (NullPointerException npe) {
                    //todo fix this
                    //apparently if the VmType object was garbage collected then
                    //statics.getTypeEntry(current); will throw an NPE
                }
            }
            current++;
        }
        next = null;
        return false;
    }

    public VmType next() {
        return next;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
