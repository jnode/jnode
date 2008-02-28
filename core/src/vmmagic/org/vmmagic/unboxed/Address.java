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

import org.jnode.vm.VmAddress;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;

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

    @Uninterruptible
    public Address add(int v) {
        return new Address(this.v + v);
    }

    @Uninterruptible
    public Address add(Word offset) {
        return new Address(this.v + offset.v);
    }

    @Uninterruptible
    public Address add(Offset offset) {
        return new Address(this.v + offset.v);
    }

    @Uninterruptible
    public Address add(Extent extent) {
        return new Address(this.v + extent.v);
    }

    @Uninterruptible
    public Address sub(Word offset) {
        return new Address(this.v - offset.v);
    }

    @Uninterruptible
    public Address sub(Extent extent) {
        return new Address(this.v - extent.v);
    }

    @Uninterruptible
    public Address sub(Offset offset) {
        return new Address(this.v - offset.v);
    }

    @Uninterruptible
    public Address sub(int v) {
        return new Address(this.v - v);
    }

    @Uninterruptible
    public Offset diff(Address addr2) {
        return new Address(this.v - addr2.v).toWord().toOffset();
    }

    @Uninterruptible
    public boolean isZero() {
        return EQ(zero());
    }

    @Uninterruptible
    public boolean isMax() {
        return EQ(max());
    }

    @Uninterruptible
    public boolean LT(Address addr2) {
        if (this.v >= 0 && addr2.v >= 0) return (this.v < addr2.v);
        if (this.v < 0 && addr2.v < 0) return (this.v < addr2.v);
        if (this.v < 0) return true;
        return false;
    }

    @Uninterruptible
    public boolean LE(Address addr2) {
        return (this.v == addr2.v) || LT(addr2);
    }

    @Uninterruptible
    public boolean GT(Address addr2) {
        return addr2.LT(this);
    }

    @Uninterruptible
    public boolean GE(Address addr2) {
        return addr2.LE(this);
    }

    @Uninterruptible
    public boolean EQ(Address addr2) {
        return (this.v == addr2.v);
    }

    @Uninterruptible
    public boolean NE(Address addr2) {
        return !EQ(addr2);
    }

    /**
     * Loads a reference from the memory location pointed to by the current
     * instance.
     * 
     * @return the read value
     */
    @Uninterruptible
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
    @Uninterruptible
    public ObjectReference loadObjectReference(Offset offset) {
        return null;
    }

    /**
     * Loads a byte from the memory location pointed to by the current instance.
     * 
     * @return the read value
     */
    @Uninterruptible
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
    @Uninterruptible
    public byte loadByte(Offset offset) {
        return (byte) 0;
    }

    /**
     * Loads a char from the memory location pointed to by the current instance.
     * 
     * @return the read value
     */
    @Uninterruptible
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
    @Uninterruptible
    public char loadChar(Offset offset) {
        return (char) 0;
    }

    /**
     * Loads a short from the memory location pointed to by the current
     * instance.
     * 
     * @return the read value
     */
    @Uninterruptible
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
    @Uninterruptible
    public short loadShort(Offset offset) {
        return (short) 0;
    }

    /**
     * Loads a float from the memory location pointed to by the current
     * instance.
     * 
     * @return the read value
     */
    @Uninterruptible
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
    @Uninterruptible
    public float loadFloat(Offset offset) {
        return 0.0f;
    }

    /**
     * Loads an int from the memory location pointed to by the current instance.
     * 
     * @return the read value
     */
    @Uninterruptible
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
    @Uninterruptible
    public int loadInt(Offset offset) {
        return 0;
    }

    /**
     * Loads a long from the memory location pointed to by the current instance.
     * 
     * @return the read value
     */
    @Uninterruptible
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
    @Uninterruptible
    public long loadLong(Offset offset) {
        return 0L;
    }

    /**
     * Loads a double from the memory location pointed to by the current
     * instance.
     * 
     * @return the read value
     */
    @Uninterruptible
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
    @Uninterruptible
    public double loadDouble(Offset offset) {
        return 0;
    }

    /**
     * Loads an address value from the memory location pointed to by the current
     * instance.
     * 
     * @return the read address value.
     */
    @Uninterruptible
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
    @Uninterruptible
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
    @Uninterruptible
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
    @Uninterruptible
    public Word loadWord(Offset offset) {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @return the old value to be passed to an attempt call.
     */
    @Uninterruptible
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
    @Uninterruptible
    public Word prepareWord(Offset offset) {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @return the old value to be passed to an attempt call.
     */
    @Uninterruptible
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
    @Uninterruptible
    public ObjectReference prepareObjectReference(Offset offset) {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @return the old value to be passed to an attempt call.
     */
    @Uninterruptible
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
    @Uninterruptible
    public Address prepareAddress(Offset offset) {
        return null;
    }

    /**
     * Prepare for an atomic store operation. This must be associated with a
     * related call to attempt.
     * 
     * @return the old value to be passed to an attempt call.
     */
    @Uninterruptible
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
    @Uninterruptible
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
    @Uninterruptible
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
    @Uninterruptible
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
    @Uninterruptible
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
    @Uninterruptible
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
    @KernelSpace
    @Uninterruptible
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
    @Uninterruptible
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
    @Uninterruptible
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
    @Uninterruptible
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
    @Uninterruptible
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
    @Uninterruptible
    public void store(ObjectReference ref, Offset offset) {
    }

    /**
     * Stores the address value in the memory location pointed to by the current
     * instance.
     * 
     * @param address
     *            The address value to store.
     */
    @Uninterruptible
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
    @Uninterruptible
    public void store(Address address, Offset offset) {
    }

    /**
     * Stores the float value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The float value to store.
     */
    @Uninterruptible
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
    @Uninterruptible
    public void store(float value, Offset offset) {
    }

    /**
     * Stores the word value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The word value to store.
     */
    @Uninterruptible
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
    @Uninterruptible
    public void store(Word value, Offset offset) {
    }

    /**
     * Stores the byte value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The byte value to store.
     */
    @Uninterruptible
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
    @Uninterruptible
    public void store(byte value, Offset offset) {
    }

    /**
     * Stores an int value in memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The int value to store.
     */
    @Uninterruptible
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
    @Uninterruptible
    public void store(int value, Offset offset) {
    }

    /**
     * Stores a double value in memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The double value to store.
     */
    @Uninterruptible
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
    @Uninterruptible
    public void store(double value, Offset offset) {
    }

    /**
     * Stores a double value in memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            The double value to store.
     */
    @Uninterruptible
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
    @Uninterruptible
    public void store(long value, Offset offset) {
    }

    /**
     * Stores a char value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            the char value to store.
     */
    @Uninterruptible
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
    @Uninterruptible
    public void store(char value, Offset offset) {
    }

    /**
     * Stores a short value in the memory location pointed to by the current
     * instance.
     * 
     * @param value
     *            the short value to store.
     */
    @Uninterruptible
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
    @Uninterruptible
    public void store(short value, Offset offset) {
    }
    
    /**
     * Performs an atomic add bewteen the word in the memory location pointed to by the
     * current instance and the given word.
     * The result is stored in the memory location pointed to by the current instance.
     * 
     * @param value
     */
    @Uninterruptible
    public void atomicAdd(Word value) {    	
    }

    /**
     * Performs an atomic and bewteen the word in the memory location pointed to by the
     * current instance and the given word.
     * The result is stored in the memory location pointed to by the current instance.
     * 
     * @param value
     */
    @Uninterruptible
    public void atomicAnd(Word value) {    	
    }

    /**
     * Performs an atomic or bewteen the word in the memory location pointed to by the
     * current instance and the given word.
     * The result is stored in the memory location pointed to by the current instance.
     * 
     * @param value
     */
    @Uninterruptible
    public void atomicOr(Word value) {    	
    }

    /**
     * Performs an atomic or bewteen the word in the memory location pointed to by the
     * current instance and the given word.
     * The result is stored in the memory location pointed to by the current instance.
     * 
     * @param value
     */
    @Uninterruptible
    public void atomicSub(Word value) {    	
    }
}
