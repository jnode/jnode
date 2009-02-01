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
 
package org.jnode.fs.jfat;

import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CharacterCodingException;


/**
 * @author gvt
 */
public class CodePageEncoder {
    private final Charset cs;
    private final CharsetEncoder encoder;
    private boolean lossy;

    protected CodePageEncoder(Charset cs) {
        this.cs = cs;
        this.encoder = cs.newEncoder();
        reset();
    }

    public void reset() {
        encoder.reset();
        lossy = false;
    }

    public boolean isLossy() {
        return lossy;
    }

    private ByteBuffer encode(CharBuffer in, boolean map, byte replacement)
        throws CharacterCodingException {
        int n = (int) (in.remaining() * encoder.averageBytesPerChar());
        ByteBuffer out = ByteBuffer.allocate(n);

        if (n == 0) {
            return out;
        }
        
        reset();

        for (;;) {
            CoderResult cr;

            if (in.hasRemaining()) {
                cr = encoder.encode(in, out, true);
            } else {
                cr = encoder.flush(out);
            }

            if (cr.isUnderflow()) {
                break;
            }

            if (cr.isOverflow()) {
                n *= 2;
                ByteBuffer o = ByteBuffer.allocate(n);
                out.flip();
                o.put(out);
                out = o;
                continue;
            }

            if (map & cr.isUnmappable()) {
                lossy = true;
                in.get();
                out.put(replacement);
                continue;
            }
            cr.throwException();
        }
        out.flip();
        return out;
    }

    public byte[] encode(String s, byte replacement) throws CharacterCodingException {
        ByteBuffer out = encode(CharBuffer.wrap(s), true, replacement);
        byte[] b = new byte[out.remaining()];
        out.get(b);
        return b;
    }

    public byte[] encode(String s) throws CharacterCodingException {
        ByteBuffer out = encode(CharBuffer.wrap(s), false, (byte) '?');
        byte[] b = new byte[out.remaining()];
        out.get(b);
        return b;
    }
}
