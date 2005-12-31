/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.boot;

import java.io.IOException;
import java.io.InputStream;

import org.jnode.system.MemoryResource;


class MemoryResourceInputStream extends InputStream {
    
    private final MemoryResource resource;
    private int offset;
    private final int length;
    
    public MemoryResourceInputStream(MemoryResource resource) {
        this.resource = resource;
        this.length = resource.getSize().toInt();
    }     
    
    /**
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        if (offset < length) {
            return resource.getByte(offset++) & 0xFF;
        } else {
            return -1;
        }
    }
    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        if (offset < length) {
            len = Math.min(len, length - offset);
            resource.getBytes(offset, b, off, len);
            offset += len;
            return len;
        } else {
            return -1;
        }
    }
}
