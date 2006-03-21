/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.vmmagic.unboxed;

/**
 * Commenting required
 * 
 * @author Daniel Frampton
 */
public final class Extent implements UnboxedObject {

    final long v;
    
    /**
     * Constructor used during the bootimage creation.
     * @param v
     */
    Extent(long v) {
        this.v = v;
    }
    
    /**
     * @deprecated
     */
    public static Extent fromInt(int extent) {
        return new Extent(extent);
    }

    public static Extent fromLong(long extent) {
        return new Extent(extent);
    }

    public static Extent fromIntSignExtend(int extent) {
        return new Extent(extent);
    }

    public static Extent fromIntZeroExtend(int extent) {
        return new Extent(0xFFFFFFFFL & extent);
    }

    /**
     * Size of an extent in bytes (typically 4 or 8)
     * @return
     */
    public static int size() {
        throw new RuntimeException("Not supported at buildtime");
    }

    public static Extent zero() {
        return new Extent(0);
    }

    public static Extent one() {
        return new Extent(1);
    }

    public static Extent max() {
        return new Extent(0xFFFFFFFFFFFFFFFFL);
    }
    
    public boolean isZero() {
    	return EQ(zero());
    }

    public int toInt() {
        return (int)v;
    }

    public long toLong() {
        return v;
    }

    public Word toWord() {
        return new Word(v);
    }

    public Extent add(int byteSize) {
        return new Extent(this.v + byteSize);
    }

    public Extent sub(int byteSize) {
        return new Extent(this.v - byteSize);
    }

    public Extent add(Extent byteSize) {
        return new Extent(this.v + byteSize.v);
    }

    public Extent sub(Extent byteSize) {
        return new Extent(this.v - byteSize.v);
    }

    public Extent add(Word byteSize) {
        return new Extent(this.v + byteSize.v);
    }

    public Extent sub(Word byteSize) {
        return new Extent(this.v - byteSize.v);
    }

    public boolean LT(Extent extent2) {
        if (this.v >= 0 && extent2.v >= 0) return (this.v < extent2.v);
        if (this.v < 0 && extent2.v < 0) return (this.v < extent2.v);
        if (this.v < 0) return true;
        return false;
    }

    public boolean LE(Extent extent2) {
        return (this.v == extent2.v) || LT(extent2);
    }

    public boolean GT(Extent extent2) {
        return extent2.LT(this);
    }

    public boolean GE(Extent extent2) {
        return extent2.LE(this);
    }

    public boolean EQ(Extent extent2) {
        return (this.v == extent2.v);
    }

    public boolean NE(Extent extent2) {
        return !EQ(extent2);
    }
}

