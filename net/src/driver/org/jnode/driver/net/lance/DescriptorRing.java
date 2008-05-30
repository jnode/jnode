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

package org.jnode.driver.net.lance;

import org.jnode.system.MemoryResource;

/**
 * @author Chris Cole
 */
public class DescriptorRing {
    protected final MemoryResource mem;
    protected final int offset;
    protected final int length;
    protected int currentDescriptor;

    public DescriptorRing(MemoryResource mem, int offset, int length) {
        this.mem = mem;
        this.offset = offset;
        this.length = length;
    }

    public int getSize() {
        return length * Descriptor.MESSAGE_DESCRIPTOR_SIZE;
    }

    public int getLength() {
        return length;
    }

    public int getAddressAs32() {
        return mem.getAddress().add(offset).toInt();
    }

}
