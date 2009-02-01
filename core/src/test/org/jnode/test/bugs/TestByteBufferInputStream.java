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
 
package org.jnode.test.bugs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import junit.framework.TestCase;
import org.jnode.util.ByteBufferInputStream;

/**
 * @author Andrei DORE
 */
public class TestByteBufferInputStream extends TestCase {
    /**
     * That test show if the (ByteBuffer)InputStream.available is properly implemented
     * or not
     *
     * @throws IOException
     */
    public void testWrappedByBufferedInputStream() throws IOException {
        final int SIZE = 5000;

        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        for (int i = 0; i < SIZE; i++) {
            buffer.put((byte) 1);
        }

        buffer.rewind();

        ByteBufferInputStream input = new ByteBufferInputStream(buffer);

        BufferedInputStream bufferedInputStream = new BufferedInputStream(input, 2048);


        byte data[] = new byte[SIZE];

        assertEquals(SIZE, bufferedInputStream.read(data));
    }
}
