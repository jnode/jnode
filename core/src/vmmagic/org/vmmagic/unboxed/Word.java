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

import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;

/**
 * To be commented.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Daniel Frampton
 * @see Address
 */
public final class Word implements UnboxedObject {

    final long v;
    
    /**
     * Constructor used during the bootimage creation.
     * @param v
     */
    Word(long v) {
        this.v = v;
    }

    /**
     * @deprecated
     */
    public static Word fromInt(int val) {
        return new Word(val);
    }

    @Uninterruptible
    public static Word fromIntSignExtend(int val) {
        return new Word(val);
    }

    @Uninterruptible
    public static Word fromIntZeroExtend(int val) {
        return new Word(0xFFFFFFFFL & val);
    }

    /**
     * Size of an address in bytes (typically 4 or 8)
     * @return
     */
    @Uninterruptible
    public static int size() {
        throw new RuntimeException("Not supported at buildtime");
    }

    @Uninterruptible
    public static Word zero() {
        return new Word(0);
    }

    @KernelSpace
    @Uninterruptible
    public static Word one() {
        return new Word(1);
    }

    @Uninterruptible
    public static Word max() {
        return new Word(0xFFFFFFFFFFFFFFFFL);
    }

    @Uninterruptible
    public int toInt() {
        return (int)v;
    }

    @Uninterruptible
    public long toLong() {
        return v;
    }

    @Uninterruptible
    public Address toAddress() {
        return new Address(v);
    }

    @Uninterruptible
    public Offset toOffset() {
        return new Offset(v);
    }

    @Uninterruptible
    public Extent toExtent() {
        return new Extent(v);
    }

    @Uninterruptible
    public Word add(int w2) {
        return new Word(this.v + w2);
    }

    @KernelSpace
    @Uninterruptible
    public Word add(Word w2) {
        return new Word(this.v + w2.v);
    }

    @Uninterruptible
    public Word add(Offset w2) {
        return new Word(this.v + w2.v);
    }

    @Uninterruptible
    public Word add(Extent w2) {
        return new Word(this.v + w2.v);
    }

    @Uninterruptible
    public Word sub(int w2) {
        return new Word(this.v - w2);
    }

    @KernelSpace
    @Uninterruptible
    public Word sub(Word w2) {
        return new Word(this.v - w2.v);
    }

    @Uninterruptible
    public Word sub(Offset w2) {
        return new Word(this.v - w2.v);
    }

    @Uninterruptible
    public Word sub(Extent w2) {
        return new Word(this.v - w2.v);
    }

    @KernelSpace
    @Uninterruptible
    public boolean isZero() {
        return EQ(zero());
    }

    @Uninterruptible
    public boolean isMax() {
        return EQ(max());
    }

    @Uninterruptible
    public boolean LT(Word w2) {
        if (this.v >= 0 && w2.v >= 0) return (this.v < w2.v);
        if (this.v < 0 && w2.v < 0) return (this.v < w2.v);
        if (this.v < 0) return true;
        return false;
    }

    @Uninterruptible
    public boolean LE(Word w2) {
        return (this.v == w2.v) || LT(w2);
    }

    @Uninterruptible
    public boolean GT(Word w2) {
        return w2.LT(this);
    }

    @Uninterruptible
    public boolean GE(Word w2) {
        return w2.LE(this);
    }

    @Uninterruptible
    public boolean EQ(Word w2) {
        return (this.v == w2.v);
    }

    @Uninterruptible
    public boolean NE(Word w2) {
        return !EQ(w2);
    }

    @Uninterruptible
    public Word and(Word w2) {
        return new Word(this.v & w2.v);
    }

    @Uninterruptible
    public Word or(Word w2) {
        return new Word(this.v | w2.v);
    }

    @Uninterruptible
    public Word not() {
        return new Word(~this.v);
    }

    @Uninterruptible
    public Word xor(Word w2) {
        return new Word(this.v ^ w2.v);
    }

    @Uninterruptible
    public Word lsh(int amt) {
        return new Word(this.v << amt);
    }

    @Uninterruptible
    public Word rshl(int amt) {
        return new Word(this.v >>> amt);
    }

    @Uninterruptible
    public Word rsha(int amt) {
        return new Word(this.v >> amt);
    }
}
