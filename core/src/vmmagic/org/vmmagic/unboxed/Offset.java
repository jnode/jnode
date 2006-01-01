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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
