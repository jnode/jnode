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

import org.jnode.vm.VmAddress;
import org.jnode.vm.annotation.KernelSpace;

/**
 * Stub implementation of an Address type. Needs commenting.
 * 
 * @author Daniel Frampton
 */
public final class Address implements UnboxedObject {
    
    final long v;
    
    /**
     * Constructor used during the bootimage creation.
     * @param v
     */
    Address(long v) {
        this.v = v;
    }

    public static Address fromInt(int address) {
        return new Address(address);
    }

    public static Address fromLong(long address) {
        return new Address(address);
    }

    public static Address fromIntSignExtend(int address) {
        return new Address(address);
    }

    public static Address fromIntZeroExtend(int address) {
        return new Address(0xFFFFFFFFL & address);
    }
    
    /**
     * Temporary method to easy the transition from VmAddress to Address.
     * @param address
     * @return The address
     */
    public static Address fromAddress(VmAddress address) {
        return null;
    }

    public static Address zero() {
        return new Address(0);
    }

    /**
     * Size of an address in bytes (typically 4 or 8)
     * @return
     */
    public static int size() {
        throw new RuntimeException("Not supported at buildtime");
    }

    public static Address max() {
        return new Address(0xFFFFFFFFFFFFFFFFL);
    }

    public ObjectReference toObjectReference() {
        return null;
    }

    public VmAddress toAddress() {
        return null;
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

    public Address add(int v) {
        return new Address(this.v + v);
    }

    public Address add(Word offset) {
        return new Address(this.v + offset.v);
    }

    public Address add(Offset offset) {
        return new Address(this.v + offset.v);
    }

    public Address add(Extent extent) {
        return new Address(this.v + extent.v);
    }

    public Address sub(Word offset) {
        return new Address(this.v - offset.v);
    }

    public Address sub(Extent extent) {
        return new Address(this.v - extent.v);
    }

    public Address sub(Offset offset) {
        return new Address(this.v - offset.v);
    }

    public Address sub(int v) {
        return new Address(this.v - v);
    }

    public Offset diff(Address addr2) {
        return new Address(this.v - addr2.v).toWord().toOffset();
    }

    public boolean isZero() {
        return EQ(zero());
    }

    public boolean isMax() {
        return EQ(max());
    }

    public boolean LT(Address addr2) {
        if (this.v >= 0 && addr2.v >= 0) return (this.v < addr2.v);
        if (this.v < 0 && addr2.v < 0) return (this.v < addr2.v);
        if (this.v < 0) return true;
        return false;
    }

    public boolean LE(Address addr2) {
        return (this.v == addr2.v) || LT(addr2);
    }

    public boolean GT(Address addr2) {
        return addr2.LT(this);
    }

    public boolean GE(Address addr2) {
        return addr2.LE(this);
    }

    public boolean EQ(Address addr2) {
        return (this.v == addr2.v);
    }

    public boolean NE(Address addr2) {
        return !EQ(addr2);
    }

    /**
     * Loads a reference from the memory location pointed to by the current
     * instance.
     * 
     * @return the read value
     */
    public ObjectReference loadObjectReference() {
        return null;
    }

    /**
     * Loads a reference from the memory location pointed to by the current
     * instance.
     * 
     * @param offset
     *            the offset to the value.
     * @return the read value
     */
    public ObjectReference loadObjectReference(Offset offset) {
        return null;
    }

    /**
     * Loads a byte from the memory location pointed to by the current instance.
     * 
     * @return the read value
     */
    public byte loadByte() {
        return (byte) 0;
    }

    /**
     * Loads a byte from the memory location pointed to by the current instance.
     * 
     * @param offset
     *            the offset to the value.
     * @return the read value
     */
    public byte loadByte(Offset offset) {
        return (byte) 0;
    }

    /**
     * Loads a char from the memory location pointed to by the current instance.
     * 
     * @return the read value
     */
    public char loadChar() {
        return (char) 0;
    }

    /**
     * Loads a char from the memory location pointed to by the current instance.
     * 
     * @param offset
     *            the offset to the value.
     * @return the read value
     */
    public char loadChar(Offset offset) {
        return (char) 0;
    }

    /**
     * Loads a short from the memory location pointed to by the current
     * instance.
     * 
     * @return the read value
     */
    public short loadShort() {
        return (short) 0;
    }

    /**
     * Loads a short from the memory location pointed to by the current
     * instance.
     * 
     * @param offset
     *            the offset to the value.
     * @return the read value
     */
    public short loadShort(Offset offset) {
        return (short) 0;
    }

    /**
     * Loads a float from the memory location pointed to by the current
     * instance.
     * 
     * @return the read value
     */
    public float loadFloat() {
        return 0.0f;
    }

    /**
     * Loads a float from the memory location pointed to by the current
     * instance.
     * 
     * @param offset
     *            the offset to the value.
     * @return the read value
     */
    public float loadFloat(Offset offset) {
        return 0.0f;
    }

    /**
     * Loads an int from the memory location pointed to by the current instance.
     * 
     * @return the read value
     */
    public int loadInt() {
        return 0;
    }

    /**
     * Loads an int from the memory location pointed to by the current instance.
     * 
     * @param offset
     *            the offset to the value.
     * @return the read value
     */
    @KernelSpace
    public int loadInt(Offset offset) {
        return 0;
    }

    /**
     * Loads a long from the memory location pointed to by the current instance.
     * 
     * @return the read value
     */
    public long loadLong() {
        return 0L;
    }

    /**
     * Loads a long from the memory location pointed to by the current instance.
     * 
     * @param offset
     *            the offset to the value.
     * @return the read value
     */
    public long loadLong(Offset offset) {
        return 0L;
    }

    /**
     * Loads a double from the memory location pointed to by the current
     * instance.
     * 
     * @return the read value
     */
    public double loadDouble() {
        return 0;
    }

    /**
     * Loads a double from the memory location pointed to by the current
     * instance.
     * 
     * @param offset
     *            the offset to the value.
     * @return the read value
     */
    public double loadDouble(Offset offset) {
        return 0;
    }

    /**
     * Loads an address value from the memory location pointed to by the current
     * instance.
     * 
     * @return the read address value.
     */
    public Address loadAddress() {
        return null;
    }

    /**
     * Loads an address value from the memory location pointed to by the current
     * instance.
     * 
     * @param offset
     *            the offset to the value.
     * @return the read address value.
     */
    @KernelSpace
    public Address loadAddress(Offset offset) {
        return null;
    }

    /**
     * Loads a word value from the memory location pointed to by the current
     * instance.
     * 
     * @return the read word value.
     */
    @KernelSpace
    public Word loadWord() {
        return null;
    }

    /**
     * Loads a word value from the memory location pointed to by the current
     * instance.
     * 
     * @param offset
     *            the offset to the value.
     * @return the read word value.
     */
    public Word loadWord(Offset offset) {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @return the old value to be passed to an attempt call.
     */
    public Word prepareWord() {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @param offset
     *            the offset to the value.
     * @return the old value to be passed to an attempt call.
     */
    public Word prepareWord(Offset offset) {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @return the old value to be passed to an attempt call.
     */
    public ObjectReference prepareObjectReference() {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @param offset
     *            the offset to the value.
     * @return the old value to be passed to an attempt call.
     */
    public ObjectReference prepareObjectReference(Offset offset) {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @return the old value to be passed to an attempt call.
     */
    public Address prepareAddress() {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @param offset
     *            the offset to the value.
     * @return the old value to be passed to an attempt call.
     */
    public Address prepareAddress(Offset offset) {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @return the old value to be passed to an attempt call.
     */
    public int prepareInt() {
        return 0;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @param offset
     *            the offset to the value.
     * @return the old value to be passed to an attempt call.
     */
    public int prepareInt(Offset offset) {
        return 0;
    }

    /**
     * Attempt an atomic store operation. This must be associated with a related
     * call to prepare.
     * 
     * @param old
     *            the old value.
     * @param value
     *            the new value.
     * @return true if the attempt was successful.
     */
    public boolean attempt(int old, int value) {
        return false;
    }

    /**
     * Attempt an atomic store operation. This must be associated with a related
     * call to prepare.
     * 
     * @param old
     *            the old value.
     * @param value
     *            the new value.
     * @param offset
     *            the offset to the value.
     * @return true if the attempt was successful.
     */
    public boolean attempt(int old, int value, Offset offset) {
        return false;
    }

    /**
     * Attempt an atomic store operation. This must be associated with a related
     * call to prepare.
     * 
     * @param old
     *            the old value.
     * @param value
     *            the new value.
     * @return true if the attempt was successful.
     */
    public boolean attempt(Word old, Word value) {
        return false;
    }

    /**
     * Attempt an atomic store operation. This must be associated with a related
     * call to prepare.
     * 
     * @param old
     *            the old value.
     * @param value
     *            the new value.
     * @param offset
     *            the offset to the value.
     * @return true if the attempt was successful.
     */
    public boolean attempt(Word old, Word value, Offset offset) {
        return false;
    }

    /**
     * Attempt an atomic store operation. This must be associated with a related
     * call to prepare.
     * 
     * @param old
     *            the old value.
     * @param value
     *            the new value.
     * @return true if the attempt was successful.
     */
    public boolean attempt(ObjectReference old, ObjectReference value) {
        return false;
    }

    /**
     * Attempt an atomic store operation. This must be associated with a related
     * call to prepare.
     * 
     * @param old
     *            the old value.
     * @param value
     *            the new value.
     * @param offset
     *            the offset to the value.
     * @return true if the attempt was successful.
     */
    public boolean attempt(ObjectReference old, ObjectReference value,
            Offset offset) {
        return false;
    }

    /**
     * Attempt an atomic store operation. This must be associated with a related
     * call to prepare.
     * 
     * @param old
     *            the old value.
     * @param value
     *            the new value.
     * @return true if the attempt was successful.
     */
    public boolean attempt(Address old, Address value) {
        return false;
    }

    /**
     * Attempt an atomic store operation. This must be associated with a related
     * call to prepare.
     * 
     * @param old
     *            the old value.
     * @param value
     *            the new value.
     * @param offset
     *            the offset to the value.
     * @return true if the attempt was successful.
     */
    public boolean attempt(Address old, Address value, Offset offset) {
        return false;
    }

    /**
     * Stores the address value in the memory location pointed to by the current
     * instance.
     * 
     * @param ref
     *            The address value to store.
     */
    public void store(ObjectReference ref) {
    }

    /**
     * Stores the address value in the memory location pointed to by the current
     * instance.
     * 
     * @param ref
     *            The address value to store.
     * @param offset
     *            the offset to the value.
     */
    public void store(ObjectReference ref, Offset offset) {
    }

    /**
     * Stores the address value in the memory location pointed to by the current
     * instance.
     * 
     * @param address
     *            The address value to store.
     */
    public void store(Address address) {
    }

    /**
     * Stores the address value in the memory location pointed to by the current
     * instance.
     * 
     * @param address
     *            The address value to store.
     * @param offset
     *            the offset to the value.
     */
    public void store(Address address, Offset offset) {
    }

    /**
     * Stores the float value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The float value to store.
     */
    public void store(float value) {
    }

    /**
     * Stores the float value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The float value to store.
     * @param offset
     *            the offset to the value.
     */
    public void store(float value, Offset offset) {
    }

    /**
     * Stores the word value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The word value to store.
     */
    public void store(Word value) {
    }

    /**
     * Stores the word value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The word value to store.
     * @param offset
     *            the offset to the value.
     */
    public void store(Word value, Offset offset) {
    }

    /**
     * Stores the byte value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The byte value to store.
     */
    public void store(byte value) {
    }

    /**
     * Stores the byte value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The byte value to store.
     * @param offset
     *            the offset to the value.
     */
    public void store(byte value, Offset offset) {
    }

    /**
     * Stores an int value in memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The int value to store.
     */
    public void store(int value) {
    }

    /**
     * Stores an int value in memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The int value to store.
     * @param offset
     *            the offset to the value.
     */
    public void store(int value, Offset offset) {
    }

    /**
     * Stores a double value in memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The double value to store.
     */
    public void store(double value) {
    }

    /**
     * Stores a double value in memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The double value to store.
     * @param offset
     *            the offset to the value.
     */
    public void store(double value, Offset offset) {
    }

    /**
     * Stores a double value in memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The double value to store.
     */
    public void store(long value) {
    }

    /**
     * Stores a double value in memory location pointed to by the current
     * instance.
     * 
     * @param offset
     *            the offset to the value.
     * @param value
     *            The double value to store.
     */
    public void store(long value, Offset offset) {
    }

    /**
     * Stores a char value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            the char value to store.
     */
    public void store(char value) {
    }

    /**
     * Stores a char value in the memory location pointed to by the current
     * instance.
     * 
     * @param offset
     *            the offset to the value.
     * @param value
     *            the char value to store.
     */
    public void store(char value, Offset offset) {
    }

    /**
     * Stores a short value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            the short value to store.
     */
    public void store(short value) {
    }

    /**
     * Stores a short value in the memory location pointed to by the current
     * instance.
     * 
     * @param offset
     *            the offset to the value.
     * @param value
     *            the short value to store.
     */
    public void store(short value, Offset offset) {
    }
    
    /**
     * Performs an atomic add bewteen the word in the memory location pointed to by the
     * current instance and the given word.
     * The result is stored in the memory location pointed to by the current instance.
     * 
     * @param value
     */
    public void atomicAdd(Word value) {    	
    }

    /**
     * Performs an atomic and bewteen the word in the memory location pointed to by the
     * current instance and the given word.
     * The result is stored in the memory location pointed to by the current instance.
     * 
     * @param value
     */
    public void atomicAnd(Word value) {    	
    }

    /**
     * Performs an atomic or bewteen the word in the memory location pointed to by the
     * current instance and the given word.
     * The result is stored in the memory location pointed to by the current instance.
     * 
     * @param value
     */
    public void atomicOr(Word value) {    	
    }

    /**
     * Performs an atomic or bewteen the word in the memory location pointed to by the
     * current instance and the given word.
     * The result is stored in the memory location pointed to by the current instance.
     * 
     * @param value
     */
    public void atomicSub(Word value) {    	
    }
}
