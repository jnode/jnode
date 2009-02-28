/*
 * $Id$
 *
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
 
package org.jnode.apps.vmware.disk;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class RandomAccessFileReader extends Reader {
    private final RandomAccessFile raf;
    private final boolean mustClose;

    public RandomAccessFileReader(RandomAccessFile raf, boolean mustClose) {
        this.raf = raf;
        this.mustClose = mustClose;
    }

    @Override
    public void close() throws IOException {
        if (mustClose) {
            raf.close();
        }
    }

    @Override
    public int read(char[] buf, int offset, int count) throws IOException {
        int nbRead = 0;
        for (int i = offset; i < (offset + count); i++) {
            buf[i] = raf.readChar();
        }
        return nbRead;
    }
}
