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
 
package java.io;

/**
 * @see java.io.ObjectOutputStream
 * @author Levente S\u00e1ntha
 */
class NativeObjectOutputStream {
    /**
     * @see java.io.ObjectOutputStream#floatsToBytes(float[], int, byte[], int, int)
     */
    private static void floatsToBytes(float[] src, int srcpos, byte[] dst, int dstpos, int nfloats) {
        if(src == null) throw new NullPointerException();
        if(dst == null) throw new NullPointerException();
        int srcend = srcpos + nfloats;
        while( srcpos < srcend ){
            int ival = Float.floatToIntBits(src[srcpos ++]);
            dst[dstpos ++ ] = (byte) ((ival >> 24) & 0xff);
            dst[dstpos ++ ] = (byte) ((ival >> 16) & 0xff);
            dst[dstpos ++ ] = (byte) ((ival >>  8) & 0xff);
            dst[dstpos ++ ] = (byte) ((ival      ) & 0xff);
        }
    }
    /**
     * @see java.io.ObjectOutputStream#doublesToBytes(double[], int, byte[], int, int)
     */
    private static void doublesToBytes(double[] src, int srcpos, byte[] dst, int dstpos, int ndoubles) {
        if(src == null) throw new NullPointerException();
        if(dst == null) throw new NullPointerException();
        int srcend = srcpos + ndoubles;
        while( srcpos < srcend ){
            long lval = Double.doubleToLongBits(src[srcpos ++]);
            dst[dstpos ++ ] = (byte) ((lval >> 56) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >> 48) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >> 40) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >> 32) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >> 24) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >> 16) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >>  8) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval      ) & 0xff);
        }
    }
}
