
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
import sun.nio.cs.StandardCharsets;
import sun.nio.cs.SingleByteDecoder;
import sun.nio.cs.SingleByteEncoder;
import sun.nio.cs.HistoricallyNamedCharset;

public class IBM864
    extends Charset
    implements HistoricallyNamedCharset
{

    public IBM864() {
	super("IBM864", ExtendedCharsets.aliasesFor("IBM864"));
    }

    public String historicalName() {
	return "Cp864";
    }

    public boolean contains(Charset cs) {
	return (cs instanceof IBM864);
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

            "\u00B0\u00B7\u2219\u221A\u2592\u2500\u2502\u253C" + 	// 0x80 - 0x87
            "\u2524\u252C\u251C\u2534\u2510\u250C\u2514\u2518" + 	// 0x88 - 0x8F
            "\u03B2\u221E\u03C6\u00B1\u00BD\u00BC\u2248\u00AB" + 	// 0x90 - 0x97
            "\u00BB\uFEF7\uFEF8\uFFFD\uFFFD\uFEFB\uFEFC\uFFFD" + 	// 0x98 - 0x9F
            "\u00A0\u00AD\uFE82\u00A3\u00A4\uFE84\uFFFD\uFFFD" + 	// 0xA0 - 0xA7
            "\uFE8E\uFE8F\uFE95\uFE99\u060C\uFE9D\uFEA1\uFEA5" + 	// 0xA8 - 0xAF
            "\u0660\u0661\u0662\u0663\u0664\u0665\u0666\u0667" + 	// 0xB0 - 0xB7
            "\u0668\u0669\uFED1\u061B\uFEB1\uFEB5\uFEB9\u061F" + 	// 0xB8 - 0xBF
            "\u00A2\uFE80\uFE81\uFE83\uFE85\uFECA\uFE8B\uFE8D" + 	// 0xC0 - 0xC7
            "\uFE91\uFE93\uFE97\uFE9B\uFE9F\uFEA3\uFEA7\uFEA9" + 	// 0xC8 - 0xCF
            "\uFEAB\uFEAD\uFEAF\uFEB3\uFEB7\uFEBB\uFEBF\uFEC1" + 	// 0xD0 - 0xD7
            "\uFEC5\uFECB\uFECF\u00A6\u00AC\u00F7\u00D7\uFEC9" + 	// 0xD8 - 0xDF
            "\u0640\uFED3\uFED7\uFEDB\uFEDF\uFEE3\uFEE7\uFEEB" + 	// 0xE0 - 0xE7
            "\uFEED\uFEEF\uFEF3\uFEBD\uFECC\uFECE\uFECD\uFEE1" + 	// 0xE8 - 0xEF
            "\uFE7D\u0651\uFEE5\uFEE9\uFEEC\uFEF0\uFEF2\uFED0" + 	// 0xF0 - 0xF7
            "\uFED5\uFEF5\uFEF6\uFEDD\uFED9\uFEF1\u25A0\uFFFD" + 	// 0xF8 - 0xFF
            "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007" + 	// 0x00 - 0x07
            "\b\t\n\u000B\f\r\u000E\u000F" + 	// 0x08 - 0x0F
            "\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017" + 	// 0x10 - 0x17
            "\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F" + 	// 0x18 - 0x1F
            "\u0020\u0021\"\u0023\u0024\u066A\u0026\'" + 	// 0x20 - 0x27
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
            "\u0020\u0021\"\u0023\u0024\u0000\u0026\'" + 
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
            "\u00A0\u0000\u00C0\u00A3\u00A4\u0000\u00DB\u0000" + 
            "\u0000\u0000\u0000\u0097\u00DC\u00A1\u0000\u0000" + 
            "\u0080\u0093\u0000\u0000\u0000\u0000\u0000\u0081" + 
            "\u0000\u0000\u0000\u0098\u0095\u0094\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00DE" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00DD" + 
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
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0090\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0092\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u00AC\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u00BB\u0000\u0000" + 
            "\u0000\u00BF\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u00E0\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u00F1\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5" + 
            "\u00B6\u00B7\u00B8\u00B9\u0025\u0000\u0000\u0000" + 
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
            "\u0000\u0000\u0082\u0083\u0000\u0000\u0000\u0091" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u0096\u0000\u0000\u0000\u0000\u0000\u0000" + 
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
            "\u0000\u0085\u0000\u0086\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u008D\u0000\u0000" + 
            "\u0000\u008C\u0000\u0000\u0000\u008E\u0000\u0000" + 
            "\u0000\u008F\u0000\u0000\u0000\u008A\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0088\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0089\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u008B\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0087\u0000\u0000" + 
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
            "\u0000\u0000\u0000\u0084\u0000\u0000\u0000\u0000" + 
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
            "\u0000\u00FE\u0000\u0000\u0000\u0000\u0000\u0000" + 
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
            "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00F0" + 
            "\u0000\u0000\u00C1\u00C2\u00A2\u00C3\u00A5\u00C4" + 
            "\u0000\u0000\u0000\u0000\u0000\u00C6\u0000\u00C7" + 
            "\u00A8\u00A9\u0000\u00C8\u0000\u00C9\u0000\u00AA" + 
            "\u0000\u00CA\u0000\u00AB\u0000\u00CB\u0000\u00AD" + 
            "\u0000\u00CC\u0000\u00AE\u0000\u00CD\u0000\u00AF" + 
            "\u0000\u00CE\u0000\u00CF\u0000\u00D0\u0000\u00D1" + 
            "\u0000\u00D2\u0000\u00BC\u0000\u00D3\u0000\u00BD" + 
            "\u0000\u00D4\u0000\u00BE\u0000\u00D5\u0000\u00EB" + 
            "\u0000\u00D6\u0000\u00D7\u0000\u0000\u0000\u00D8" + 
            "\u0000\u0000\u0000\u00DF\u00C5\u00D9\u00EC\u00EE" + 
            "\u00ED\u00DA\u00F7\u00BA\u0000\u00E1\u0000\u00F8" + 
            "\u0000\u00E2\u0000\u00FC\u0000\u00E3\u0000\u00FB" + 
            "\u0000\u00E4\u0000\u00EF\u0000\u00E5\u0000\u00F2" + 
            "\u0000\u00E6\u0000\u00F3\u0000\u00E7\u00F4\u00E8" + 
            "\u0000\u00E9\u00F5\u00FD\u00F6\u00EA\u0000\u00F9" + 
            "\u00FA\u0099\u009A\u0000\u0000\u009D\u009E\u0000" + 
            "\u0000\u0000"; 

    private final static short index1[] = {
            0, 248, 248, 326, 248, 248, 570, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 801, 248, 248, 1057, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 
            248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 1218, 248, 

	};
    }
}
