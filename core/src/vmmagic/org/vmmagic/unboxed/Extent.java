/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.vmmagic.unboxed;

/**
 * Commenting required
 * 
 * @author Daniel Frampton
 */
public final class Extent {

    /**
     * @deprecated
     */
    public static Extent fromInt(int extent) {
        return null;
    }

    public static Extent fromLong(long extent) {
        return null;
    }

    public static Extent fromIntSignExtend(int extent) {
        return null;
    }

    public static Extent fromIntZeroExtend(int extent) {
    	throw new Error("No magic");
        //return null;
    }

    public static Extent zero() {
        return null;
    }

    public static Extent one() {
        return null;
    }

    public static Extent max() {
        return null;
    }
    
    public boolean isZero() {
    	return false;
    }

    public int toInt() {
        return 0;
    }

    public long toLong() {
        return 0L;
    }

    public Word toWord() {
        return null;
    }

    public Extent add(int byteSize) {
        return null;
    }

    public Extent sub(int byteSize) {
        return null;
    }

    public Extent add(Extent byteSize) {
        return null;
    }

    public Extent sub(Extent byteSize) {
        return null;
    }

    public Extent add(Word byteSize) {
        return null;
    }

    public Extent sub(Word byteSize) {
        return null;
    }

    public boolean LT(Extent extent2) {
        return false;
    }

    public boolean LE(Extent extent2) {
        return false;
    }

    public boolean GT(Extent extent2) {
        return false;
    }

    public boolean GE(Extent extent2) {
        return false;
    }

    public boolean EQ(Extent extent2) {
        return false;
    }

    public boolean NE(Extent extent2) {
        return false;
    }
}

