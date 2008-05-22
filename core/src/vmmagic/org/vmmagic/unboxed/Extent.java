/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
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
     * @return the extent size in bytes
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

