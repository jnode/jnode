/*
 * $Id$
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
        this.length = (int)resource.getSize();
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