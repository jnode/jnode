/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.net.httpserver;

import java.io.*;
import java.net.*;
import com.sun.net.httpserver.*;
import com.sun.net.httpserver.spi.*;

class ChunkedInputStream extends LeftOverInputStream {
    ChunkedInputStream (ExchangeImpl t, InputStream src) {
        super (t, src);
    }

    private int remaining;

    /* true when a chunk header needs to be read */

    private boolean needToReadHeader = true;

    static char CR = '\r';
    static char LF = '\n';

    private int numeric (char[] arr, int nchars) throws IOException {
        assert arr.length >= nchars;
        int len = 0;
        for (int i=0; i<nchars; i++) {
            char c = arr[i];
            int val=0;
            if (c>='0' && c <='9') {
                val = c - '0';
            } else if (c>='a' && c<= 'f') {
                val = c - 'a' + 10;
            } else if (c>='A' && c<= 'F') {
                val = c - 'A' + 10;
            } else {
                throw new IOException ("invalid chunk length");
            }
            len = len * 16 + val;
        }
        return len;
    }

    /* read the chunk header line and return the chunk length
     * any chunk extensions are ignored
     */
    private int readChunkHeader () throws IOException {
        boolean gotCR = false;
        char c;
        char[] len_arr = new char [16];
        int len_size = 0;
        boolean end_of_len = false;

        while ((c=(char)in.read())!= -1) {
            if (len_size == len_arr.length -1) {
                throw new IOException ("invalid chunk header");
            }
            if (gotCR) {
                if (c == LF) {
                    int l = numeric (len_arr, len_size);
                    return l;
                } else {
                    gotCR = false;
                }
                if (!end_of_len) {
                    len_arr[len_size++] = c;
                }
            } else {
                if (c == CR) {
                    gotCR = true;
                } else if (c == ';') {
                    end_of_len = true;
                } else if (!end_of_len) {
                    len_arr[len_size++] = c;
                }
            }
        }
        throw new IOException ("end of stream reading chunk header");
    }

    protected int readImpl (byte[]b, int off, int len) throws IOException {
        if (eof) {
            return -1;
        }
        if (needToReadHeader) {
            remaining = readChunkHeader();
            if (remaining == 0) {
                eof = true;
                consumeCRLF();
                return -1;
            }
            needToReadHeader = false;
        }
        if (len > remaining) {
            len = remaining;
        }
        int n = in.read(b, off, len);
        if (n > -1) {
            remaining -= n;
        }
        if (remaining == 0) {
            needToReadHeader = true;
            consumeCRLF();
        }
        return n;
    }

    private void consumeCRLF () throws IOException {
        char c;
        c = (char)in.read(); /* CR */
        if (c != CR) {
            throw new IOException ("invalid chunk end");
        }
        c = (char)in.read(); /* LF */
        if (c != LF) {
            throw new IOException ("invalid chunk end");
        }
    }

    /**
     * returns the number of bytes available to read in the current chunk
     * which may be less than the real amount, but we'll live with that
     * limitation for the moment. It only affects potential efficiency
     * rather than correctness.
     */
    public int available () throws IOException {
        if (eof || closed) {
            return 0;
        }
        int n = in.available();
        return n > remaining? remaining: n;
    }

    /* called after the stream is closed to see if bytes
     * have been read from the underlying channel
     * and buffered internally
     */
    public boolean isDataBuffered () throws IOException {
        assert eof;
        return in.available() > 0;
    }

    public boolean markSupported () {return false;}

    public void mark (int l) {
    }

    public void reset () throws IOException {
        throw new IOException ("mark/reset not supported");
    }
}
