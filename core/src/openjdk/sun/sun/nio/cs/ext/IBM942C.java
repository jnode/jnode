/*
 * Copyright 2003-2004 Sun Microsystems, Inc.  All Rights Reserved.
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



package sun.nio.cs.ext;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharacterCodingException;
import sun.nio.cs.HistoricallyNamedCharset;

public class IBM942C extends Charset implements HistoricallyNamedCharset
{

    public IBM942C() {
	super("x-IBM942C", ExtendedCharsets.aliasesFor("x-IBM942C"));
    }

    public String historicalName() {
	return "Cp942C";
    }

    public boolean contains(Charset cs) {
	return ((cs.name().equals("US-ASCII"))
		|| (cs instanceof IBM942C));
    }

    public CharsetDecoder newDecoder() {
	return new Decoder(this);
    }

    public CharsetEncoder newEncoder() {
	return new Encoder(this);
    }

    private static class Decoder extends IBM942.Decoder {
        protected static final String singleByteToChar;

	static {
	  String indexs = "";
	  for (char c = '\0'; c < '\u0080'; ++c) indexs += c;
	      singleByteToChar = indexs +
				 IBM942.Decoder.singleByteToChar.substring(indexs.length());
	}

	public Decoder(Charset cs) {
	    super(cs, singleByteToChar);
	}
    }

    private static class Encoder extends IBM942.Encoder {

   protected static final short index1[];
   protected static final String index2a;
   protected static final int shift = 5;

	static {

	    String indexs = "";
	    for (char c = '\0'; c < '\u0080'; ++c) indexs += c;
		index2a = IBM942.Encoder.index2a + indexs;

	    int o = IBM942.Encoder.index2a.length() + 15000;
	    index1 = new short[IBM942.Encoder.index1.length];
	    System.arraycopy(IBM942.Encoder.index1, 0, index1, 0, IBM942.Encoder.index1.length);

	    for (int i = 0; i * (1<<shift) < 128; ++i) {
		index1[i] = (short)(o + i * (1<<shift));
	    }
	}

	public Encoder(Charset cs) {
	    super(cs, index1, index2a);
	}
    }
}
