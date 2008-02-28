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
 * To be commented
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Daniel Frampton
 */
public final class Offset implements UnboxedObject {

    final long v;
    
    /**
     * Constructor used during the bootimage creation.
     * @param v
     */
    Offset(long v) {
        this.v = v;
    }
    
    /**
     * @deprecated
     */
    public static Offset fromInt(int address) {
        return new Offset(address);
    }

    public static Offset fromIntSignExtend(int address) {
        return new Offset(address);
    }

    public static Offset fromIntZeroExtend(int address) {
        return new Offset(0xFFFFFFFFL & address);
    }

    /**
     * Size of an address in bytes (typically 4 or 8)
     * @return
     */
    public static int size() {
        throw new RuntimeException("Not supported at buildtime");
    }

    public static Offset zero() {
        return new Offset(0);
    }

    public static Offset max() {
        return new Offset(0xFFFFFFFFFFFFFFFFL);
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

    public Offset add(int byteSize) {
        return new Offset(this.v + byteSize);
    }

    public Offset add(Word byteSize) {
        return new Offset(this.v + byteSize.v);
    }

    public Offset add(Extent byteSize) {
        return new Offset(this.v + byteSize.v);
    }

    public Offset sub(int byteSize) {
        return new Offset(this.v - byteSize);
    }

    public Offset sub(Offset off2) {
        return new Offset(this.v + off2.v);
    }

    public boolean EQ(Offset off2) {
        return (this.v == off2.v);
    }

    public boolean NE(Offset off2) {
        return (this.v != off2.v);
    }

    public boolean sLT(Offset off2) {
        return (this.v < off2.v);
    }

    public boolean sLE(Offset off2) {
        return (this.v <= off2.v);
    }

    public boolean sGT(Offset off2) {
        return (this.v > off2.v);
    }

    public boolean sGE(Offset off2) {
        return (this.v >= off2.v);
    }

    public boolean isZero() {
        return EQ(zero());
    }

    public boolean isMax() {
        return EQ(max());
    }
}
