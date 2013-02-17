/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.system.resource;

import java.nio.ByteBuffer;

import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Offset;


/**
 * Block of memory resource.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface MemoryResource extends Resource {

    /**
     * Create a bytebuffer that has the same content as this resource.
     *
     * @return the byte buffer
     */
    public abstract ByteBuffer asByteBuffer();

    /**
     * Gets a 8-bit signed byte at the given memory address
     *
     * @param memPtr
     * @return byte
     */
    public abstract byte getByte(int memPtr);

    /**
     * Gets multiple 8-bit signed bytes from the given memory address
     *
     * @param memPtr
     * @param dst
     * @param dstOfs
     * @param length
     */
    public abstract void getBytes(int memPtr, byte[] dst, int dstOfs, int length);

    /**
     * Gets a 16-bit signed short at the given memory address
     *
     * @param memPtr
     * @return short
     */
    public abstract short getShort(int memPtr);

    /**
     * Gets multiple 16-bit signed bytes from the given memory address
     *
     * @param memPtr
     * @param dst
     * @param dstOfs
     * @param length
     */
    public abstract void getShorts(int memPtr, short[] dst, int dstOfs, int length);

    /**
     * Gets a 16-bit unsigned char at the given memory address
     *
     * @param memPtr
     * @return char
     */
    public abstract char getChar(int memPtr);

    /**
     * Gets multiple 16-bit unsigned chars from the given memory address
     *
     * @param memPtr
     * @param dst
     * @param dstOfs
     * @param length
     */
    public abstract void getChars(int memPtr, char[] dst, int dstOfs, int length);

    /**
     * Gets a 32-bit signed int at the given memory address
     *
     * @param memPtr
     * @return int
     */
    @KernelSpace
    @Uninterruptible
    public abstract int getInt(int memPtr);

    /**
     * Gets multiple 32-bit signed ints from the given memory address
     *
     * @param memPtr
     * @param dst
     * @param dstOfs
     * @param length
     */
    public abstract void getInts(int memPtr, int[] dst, int dstOfs, int length);

    /**
     * Gets a 64-bit signed long at the given memory address
     *
     * @param memPtr
     * @return long
     */
    public abstract long getLong(int memPtr);

    /**
     * Gets multiple 64-bit signed longs from the given memory address
     *
     * @param memPtr
     * @param dst
     * @param dstOfs
     * @param length
     */
    public abstract void getLongs(int memPtr, long[] dst, int dstOfs, int length);

    /**
     * Gets a float at the given memory address
     *
     * @param memPtr
     * @return float
     */
    public abstract float getFloat(int memPtr);

    /**
     * Gets multiple 32-bit floats from the given memory address
     *
     * @param memPtr
     * @param dst
     * @param dstOfs
     * @param length
     */
    public abstract void getFloats(int memPtr, float[] dst, int dstOfs, int length);

    /**
     * Gets a double at the given memory address
     *
     * @param memPtr
     * @return double
     */
    public abstract double getDouble(int memPtr);

    /**
     * Gets multiple 64-bit doubles from the given memory address
     *
     * @param memPtr
     * @param dst
     * @param dstOfs
     * @param length
     */
    public abstract void getDoubles(int memPtr, double[] dst, int dstOfs, int length);

    /**
     * Gets a object reference at the given memory address
     *
     * @param memPtr
     * @return Object
     */
    public abstract Object getObject(int memPtr);

    /**
     * Sets a byte at a given memory address
     *
     * @param memPtr
     * @param value
     */
    public abstract void setByte(int memPtr, byte value);

    /**
     * Sets a byte at a given memory address
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void setByte(int memPtr, byte value, int count);

    /**
     * Perform a bitwise AND of the byte at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void andByte(int memPtr, byte value, int count);

    /**
     * Perform a bitwise OR of the byte at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void orByte(int memPtr, byte value, int count);

    /**
     * Perform a bitwise XOR of the byte at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void xorByte(int memPtr, byte value, int count);

    /**
     * Sets multiple 8-bit signed bytes at the given memory address
     *
     * @param src
     * @param srcOfs
     * @param dstPtr
     * @param length
     */
    public abstract void setBytes(byte[] src, int srcOfs, int dstPtr, int length);

    /**
     * Sets a char at a given memory address
     *
     * @param memPtr
     * @param value
     */
    public abstract void setChar(int memPtr, char value);

    /**
     * Sets a char at a given memory address
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void setChar(int memPtr, char value, int count);

    /**
     * Perform a bitwise AND of the char at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void andChar(int memPtr, char value, int count);

    /**
     * Perform a bitwise OR of the char at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void orChar(int memPtr, char value, int count);

    /**
     * Perform a bitwise XOR of the char at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void xorChar(int memPtr, char value, int count);

    /**
     * Sets multiple 16-bit unsigned chars at the given memory address
     *
     * @param src
     * @param srcOfs
     * @param dstPtr
     * @param length
     */
    public abstract void setChars(char[] src, int srcOfs, int dstPtr, int length);

    /**
     * Sets a short at a given memory address
     *
     * @param memPtr
     * @param value
     */
    public abstract void setShort(int memPtr, short value);

    /**
     * Sets a short at a given memory address
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void setShort(int memPtr, short value, int count);

    /**
     * Perform a bitwise AND of the short at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void andShort(int memPtr, short value, int count);

    /**
     * Perform a bitwise OR of the short at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void orShort(int memPtr, short value, int count);

    /**
     * Perform a bitwise XOR of the short at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void xorShort(int memPtr, short value, int count);

    /**
     * Sets multiple 16-bit signed shorts at the given memory address
     *
     * @param src
     * @param srcOfs
     * @param dstPtr
     * @param length
     */
    public abstract void setShorts(short[] src, int srcOfs, int dstPtr, int length);

    /**
     * Sets an int at a given memory address
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void setInt24(int memPtr, int value, int count);

    /**
     * Perform a bitwise AND of the int at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     * @param memPtr
     * @param value
     * @param count
     */
    //public abstract void andInt24(int memPtr, int value, int count);

    /**
     * Perform a bitwise OR of the int at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     * @param memPtr
     * @param value
     * @param count
     */
    //public abstract void orInt24(int memPtr, int value, int count);

    /**
     * Perform a bitwise XOR of the int at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void xorInt24(int memPtr, int value, int count);

    /**
     * Sets an int at a given memory address
     *
     * @param memPtr
     * @param value
     */
    @KernelSpace
    @Uninterruptible
    public abstract void setInt(int memPtr, int value);

    /**
     * Sets an int at a given memory address
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void setInt(int memPtr, int value, int count);

    /**
     * Perform a bitwise AND of the int at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void andInt(int memPtr, int value, int count);

    /**
     * Perform a bitwise OR of the int at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void orInt(int memPtr, int value, int count);

    /**
     * Perform a bitwise XOR of the int at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void xorInt(int memPtr, int value, int count);

    /**
     * Sets multiple 32-bit signed ints at the given memory address
     *
     * @param src
     * @param srcOfs
     * @param dstPtr
     * @param length
     */
    public abstract void setInts(int[] src, int srcOfs, int dstPtr, int length);

    /**
     * Sets a float at a given memory address
     *
     * @param memPtr
     * @param value
     */
    public abstract void setFloat(int memPtr, float value);

    /**
     * Sets a float at a given memory address
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void setFloat(int memPtr, float value, int count);

    /**
     * Sets multiple 32-bit floats at the given memory address
     *
     * @param src
     * @param srcOfs
     * @param dstPtr
     * @param length
     */
    public abstract void setFloats(float[] src, int srcOfs, int dstPtr, int length);

    /**
     * Sets a long at a given memory address
     *
     * @param memPtr
     * @param value
     */
    public abstract void setLong(int memPtr, long value);

    /**
     * Sets a long at a given memory address
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void setLong(int memPtr, long value, int count);

    /**
     * Perform a bitwise AND of the long at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void andLong(int memPtr, long value, int count);

    /**
     * Perform a bitwise OR of the long at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void orLong(int memPtr, long value, int count);

    /**
     * Perform a bitwise XOR of the long at the given address and the given value.
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void xorLong(int memPtr, long value, int count);

    /**
     * Sets multiple 64-bit signed longs at the given memory address
     *
     * @param src
     * @param srcOfs
     * @param dstPtr
     * @param length
     */
    public abstract void setLongs(long[] src, int srcOfs, int dstPtr, int length);

    /**
     * Sets a double at a given memory address
     *
     * @param memPtr
     * @param value
     */
    public abstract void setDouble(int memPtr, double value);

    /**
     * Sets a double at a given memory address
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void setDouble(int memPtr, double value, int count);

    /**
     * Sets multiple 64-bit doubles at the given memory address
     *
     * @param src
     * @param srcOfs
     * @param dstPtr
     * @param length
     */
    public abstract void setDoubles(double[] src, int srcOfs, int dstPtr, int length);

    /**
     * Sets a Object at a given memory address
     *
     * @param memPtr
     * @param value
     */
    public abstract void setObject(int memPtr, Object value);

    /**
     * Sets a Object at a given memory address
     * While count is greater then 1, the address is incremented and the process repeats.
     *
     * @param memPtr
     * @param value
     * @param count
     */
    public abstract void setObject(int memPtr, Object value, int count);

    /**
     * Fill the memory at the given memory address with size times 0 bytes.
     * <p/>
     * memPtr must be VmObject.SLOT_SIZE aligned
     * <p/>
     * size % VmObject.SLOT_SIZE must be 0
     *
     * @param memPtr
     * @param size
     */
    public abstract void clear(int memPtr, int size);

    public abstract void copy(int srcMemPtr, int destMemPtr, int size);

    /**
     * Returns the size of this buffer in bytes.
     *
     * @return int
     */
    public abstract Extent getSize();

    /**
     * Gets the address of the first byte of this buffer
     *
     * @return The address of the first byte of this buffer
     */
    public abstract Address getAddress();

    /**
     * Get a memory resource for a portion of this memory resources.
     * The first area of this memory resource that fits the given size
     * and it not claimed by any child resource is returned.
     * If not large enought area if found, a ResourceNotFreeException is thrown.
     * A child resource is always releases when the parent is released.
     * A child resource can be released without releasing the parent.
     *
     * @param size  Length of the returned resource in bytes.
     * @param align Align of this boundary. Align must be a multiple of 2.
     * @return the child memory resource
     */
    public abstract MemoryResource claimChildResource(Extent size, int align)
        throws IndexOutOfBoundsException, ResourceNotFreeException;

    /**
     * Get a memory resource for a portion of this memory resources.
     * The first area of this memory resource that fits the given size
     * and it not claimed by any child resource is returned.
     * If not large enought area if found, a ResourceNotFreeException is thrown.
     * A child resource is always releases when the parent is released.
     * A child resource can be released without releasing the parent.
     *
     * @param size  Length of the returned resource in bytes.
     * @param align Align of this boundary. Align must be a multiple of 2.
     * @return the child memory resource
     */
    public MemoryResource claimChildResource(int size, int align)
        throws IndexOutOfBoundsException, ResourceNotFreeException;

    /**
     * Get a memory resource for a portion of this memory resources.
     * A child resource is always releases when the parent is released.
     * A child resource can be released without releasing the parent.
     *
     * @param offset        Offset relative to the start of this resource.
     * @param size          Length of the returned resource in bytes.
     * @param allowOverlaps If true, overlapping child resources will be allowed,
     * otherwise overlapping child resources will resulut in a ResourceNotFreeException.
     * @return the child memory resource
     */
    public abstract MemoryResource claimChildResource(Offset offset, Extent size, boolean allowOverlaps)
        throws IndexOutOfBoundsException, ResourceNotFreeException;

    /**
     * Get a memory resource for a portion of this memory resources.
     * A child resource is always releases when the parent is released.
     * A child resource can be released without releasing the parent.
     *
     * @param offset        Offset relative to the start of this resource.
     * @param size          Length of the returned resource in bytes.
     * @param allowOverlaps If true, overlapping child resources will be allowed,
     * otherwise overlapping child resources will resulut in a ResourceNotFreeException.
     * @return the child memory resource
     */
    public abstract MemoryResource claimChildResource(int offset, int size, boolean allowOverlaps)
        throws IndexOutOfBoundsException, ResourceNotFreeException;


    /**
     * Gets a multi media memory resource wrapping this given memory resource.
     *
     * @return The created instance. This will never be null.
     */
    public abstract MultiMediaMemoryResource asMultiMediaMemoryResource();

    /**
     * Gets the offset relative to my parent.
     * If this resource has no parent, the address of this buffer is returned.
     */
    public abstract Offset getOffset();
}

