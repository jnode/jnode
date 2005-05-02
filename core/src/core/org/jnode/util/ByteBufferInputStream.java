/*
 * $Id$
 */
package org.jnode.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
    private final ByteBuffer buf;
    
    public ByteBufferInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    /**
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        if (buf.remaining() > 0) {
            return buf.get() & 0xFF;
        } else {
            return -1;
        }
    }        
}