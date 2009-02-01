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
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;

/**
 * @author gvt
 */
public class CodePageDecoder {
    private final Charset cs;
    private final CharsetDecoder decoder;

    protected CodePageDecoder(Charset cs) {
        this.cs = cs;
        this.decoder = cs.newDecoder();
        reset();
    }

    public void reset() {
        decoder.reset();
    }

    public CharBuffer decode(ByteBuffer in) throws CharacterCodingException {
        return decoder.decode(in);
    }

    public String decode(byte[] b, int offset, int length) throws CharacterCodingException {
        CharBuffer out = decode(ByteBuffer.wrap(b, offset, length));
        return out.toString();
    }

    public String decode(byte[] b) throws CharacterCodingException {
        return decode(b, 0, b.length);
    }
}
