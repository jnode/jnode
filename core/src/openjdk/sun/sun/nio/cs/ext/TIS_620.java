
/*
 * Copyright 2002-2004 Sun Microsystems, Inc.  All Rights Reserved.
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


import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import sun.nio.cs.StandardCharsets;
import sun.nio.cs.SingleByteDecoder;
import sun.nio.cs.SingleByteEncoder;
import sun.nio.cs.HistoricallyNamedCharset;

public class TIS_620
    extends Charset
    implements HistoricallyNamedCharset
{

    public TIS_620() {
	super("TIS-620", ExtendedCharsets.aliasesFor("TIS-620"));
    }

    public String historicalName() {
	return "TIS620";
    }

    public boolean contains(Charset cs) {
	return ((cs.name().equals("US-ASCII"))
		|| (cs instanceof TIS_620));
    }

    public CharsetDecoder newDecoder() {
	return new Decoder(this);
    }

    public CharsetEncoder newEncoder() {
	return new Encoder(this);
    }


    /**
     * These accessors are temporarily supplied while sun.io
     * converters co-exist with the sun.nio.cs.{ext} charset coders
     * These facilitate sharing of conversion tables between the
     * two co-existing implementations. When sun.io converters
     * are made extinct these will be unncessary and should be removed
     */

    public String getDecoderSingleByteMappings() {
	return Decoder.byteToCharTable;

    }

    public short[] getEncoderIndex1() {
	return Encoder.index1;

    }
    public String getEncoderIndex2() {
	return Encoder.index2;

    }

    private static class Decoder extends SingleByteDecoder {
	    public Decoder(Charset cs) {
		super(cs, byteToCharTable);
	}

	private final static String byteToCharTable =

	    "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD" + 	// 0x80 - 0x87
	    "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD" + 	// 0x88 - 0x8F
	    "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD" + 	// 0x90 - 0x97
	    "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD" + 	// 0x98 - 0x9F
	    "\u00A0\u0E01\u0E02\u0E03\u0E04\u0E05\u0E06\u0E07" + 	// 0xA0 - 0xA7
	    "\u0E08\u0E09\u0E0A\u0E0B\u0E0C\u0E0D\u0E0E\u0E0F" + 	// 0xA8 - 0xAF
	    "\u0E10\u0E11\u0E12\u0E13\u0E14\u0E15\u0E16\u0E17" + 	// 0xB0 - 0xB7
	    "\u0E18\u0E19\u0E1A\u0E1B\u0E1C\u0E1D\u0E1E\u0E1F" + 	// 0xB8 - 0xBF
	    "\u0E20\u0E21\u0E22\u0E23\u0E24\u0E25\u0E26\u0E27" + 	// 0xC0 - 0xC7
	    "\u0E28\u0E29\u0E2A\u0E2B\u0E2C\u0E2D\u0E2E\u0E2F" + 	// 0xC8 - 0xCF
	    "\u0E30\u0E31\u0E32\u0E33\u0E34\u0E35\u0E36\u0E37" + 	// 0xD0 - 0xD7
	    "\u0E38\u0E39\u0E3A\uFFFD\uFFFD\uFFFD\uFFFD\u0E3F" + 	// 0xD8 - 0xDF
	    "\u0E40\u0E41\u0E42\u0E43\u0E44\u0E45\u0E46\u0E47" + 	// 0xE0 - 0xE7
	    "\u0E48\u0E49\u0E4A\u0E4B\u0E4C\u0E4D\u0E4E\u0E4F" + 	// 0xE8 - 0xEF
	    "\u0E50\u0E51\u0E52\u0E53\u0E54\u0E55\u0E56\u0E57" + 	// 0xF0 - 0xF7
	    "\u0E58\u0E59\u0E5A\u0E5B\uFFFD\uFFFD\uFFFD\uFFFD" + 	// 0xF8 - 0xFF
	    "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007" + 	// 0x00 - 0x07
	    "\b\t\n\u000B\f\r\u000E\u000F" + 	// 0x08 - 0x0F
	    "\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017" + 	// 0x10 - 0x17
	    "\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F" + 	// 0x18 - 0x1F
	    "\u0020\u0021\"\u0023\u0024\u0025\u0026\'" + 	// 0x20 - 0x27
	    "\u0028\u0029\u002A\u002B\u002C\u002D\u002E\u002F" + 	// 0x28 - 0x2F
	    "\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037" + 	// 0x30 - 0x37
	    "\u0038\u0039\u003A\u003B\u003C\u003D\u003E\u003F" + 	// 0x38 - 0x3F
	    "\u0040\u0041\u0042\u0043\u0044\u0045\u0046\u0047" + 	// 0x40 - 0x47
	    "\u0048\u0049\u004A\u004B\u004C\u004D\u004E\u004F" + 	// 0x48 - 0x4F
	    "\u0050\u0051\u0052\u0053\u0054\u0055\u0056\u0057" + 	// 0x50 - 0x57
	    "\u0058\u0059\u005A\u005B\\\u005D\u005E\u005F" + 	// 0x58 - 0x5F
	    "\u0060\u0061\u0062\u0063\u0064\u0065\u0066\u0067" + 	// 0x60 - 0x67
	    "\u0068\u0069\u006A\u006B\u006C\u006D\u006E\u006F" + 	// 0x68 - 0x6F
	    "\u0070\u0071\u0072\u0073\u0074\u0075\u0076\u0077" + 	// 0x70 - 0x77
	    "\u0078\u0079\u007A\u007B\u007C\u007D\u007E\u007F"; 	// 0x78 - 0x7F
    }

    private static class Encoder extends SingleByteEncoder {
	    public Encoder(Charset cs) {
		super(cs, index1, index2, 0xFF00, 0x00FF, 8);
	    }

	    private final static String index2 =

		"\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007" + 
		"\b\t\n\u000B\f\r\u000E\u000F" + 
		"\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017" + 
		"\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F" + 
		"\u0020\u0021\"\u0023\u0024\u0025\u0026\'" + 
		"\u0028\u0029\u002A\u002B\u002C\u002D\u002E\u002F" + 
		"\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037" + 
		"\u0038\u0039\u003A\u003B\u003C\u003D\u003E\u003F" + 
		"\u0040\u0041\u0042\u0043\u0044\u0045\u0046\u0047" + 
		"\u0048\u0049\u004A\u004B\u004C\u004D\u004E\u004F" + 
		"\u0050\u0051\u0052\u0053\u0054\u0055\u0056\u0057" + 
		"\u0058\u0059\u005A\u005B\\\u005D\u005E\u005F" + 
		"\u0060\u0061\u0062\u0063\u0064\u0065\u0066\u0067" + 
		"\u0068\u0069\u006A\u006B\u006C\u006D\u006E\u006F" + 
		"\u0070\u0071\u0072\u0073\u0074\u0075\u0076\u0077" + 
		"\u0078\u0079\u007A\u007B\u007C\u007D\u007E\u007F" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u00A0\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7" + 
		"\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF" + 
		"\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7" + 
		"\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF" + 
		"\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7" + 
		"\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF" + 
		"\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7" + 
		"\u00D8\u00D9\u00DA\u0000\u0000\u0000\u0000\u00DF" + 
		"\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7" + 
		"\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF" + 
		"\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7" + 
		"\u00F8\u00F9\u00FA\u00FB\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
		"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000"; 

	    private final static short index1[] = {
		0, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 416, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
		161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 161, 
	};
    }
}
