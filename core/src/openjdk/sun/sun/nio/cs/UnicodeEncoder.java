/*
 * Copyright 2000-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.nio.cs;

import java.nio.*;
import java.nio.charset.*;

/**
 * Base class for different flavors of UTF-16 encoders
 */
public abstract class UnicodeEncoder extends CharsetEncoder {

    protected static final char BYTE_ORDER_MARK = '\uFEFF';
    protected static final char REVERSED_MARK = '\uFFFE';

    protected static final int BIG = 0;
    protected static final int LITTLE = 1;

    private int byteOrder;      /* Byte order in use */
    private boolean usesMark;	/* Write an initial BOM */
    private boolean needsMark;

    protected UnicodeEncoder(Charset cs, int bo, boolean m) {
	super(cs, 2.0f,
	      // Four bytes max if you need a BOM
	      m ? 4.0f : 2.0f,
	      // Replacement depends upon byte order
	      ((bo == BIG)
	       ? new byte[] { (byte)0xff, (byte)0xfd }
	       : new byte[] { (byte)0xfd, (byte)0xff }));
	usesMark = needsMark = m;
	byteOrder = bo;
    }

    private void put(char c, ByteBuffer dst) {
	if (byteOrder == BIG) {
	    dst.put((byte)(c >> 8));
	    dst.put((byte)(c & 0xff));
	} else {
	    dst.put((byte)(c & 0xff));
	    dst.put((byte)(c >> 8));
	}
    }

    private final Surrogate.Parser sgp = new Surrogate.Parser();

    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
	int mark = src.position();

	if (needsMark) {
	    if (dst.remaining() < 2)
		return CoderResult.OVERFLOW;
	    put(BYTE_ORDER_MARK, dst);
	    needsMark = false;
	}

	try {
	    while (src.hasRemaining()) {
		char c = src.get();
		if (!Surrogate.is(c)) {
		    if (dst.remaining() < 2)
			return CoderResult.OVERFLOW;
		    mark++;
		    put(c, dst);
		    continue;
		}
		int d = sgp.parse(c, src);
		if (d < 0)
		    return sgp.error();
		if (dst.remaining() < 4)
		    return CoderResult.OVERFLOW;
		mark += 2;
		put(Surrogate.high(d), dst);
		put(Surrogate.low(d), dst);
	    }
	    return CoderResult.UNDERFLOW;
	} finally {
	    src.position(mark);
	}
    }

    protected void implReset() {
	needsMark = usesMark;
    }

    public boolean canEncode(char c) {
	return ! Surrogate.is(c);
    }
}
