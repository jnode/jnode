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
 
package org.mmtk.vm;

import org.jnode.vm.annotation.NoReadBarrier;
import org.jnode.vm.annotation.NoWriteBarrier;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/**
 * $Id$
 * 
 * @author <a href="http://cs.anu.edu.au/~Steve.Blackburn">Steve Blackburn</a>
 * @author Perry Cheng
 * 
 * @version $Revision$
 * @date $Date$
 */
public class Barriers {
    /**
     * Sets an element of a char array without invoking any write barrier. This
     * method is called by the Log method, as it will be used during garbage
     * collection and needs to manipulate character arrays without causing a
     * write barrier operation.
     * 
     * @param dst
     *            the destination array
     * @param index
     *            the index of the element to set
     * @param value
     *            the new value for the element
     */
    @NoWriteBarrier
    public static void setArrayNoBarrier(char[] dst, int index, char value) {
        dst[index] = value;
    }

    /**
     * Perform the actual write of the write barrier.
     * 
     * @param ref
     *            The object that has the reference field
     * @param slot
     *            The slot that holds the reference
     * @param target
     *            The value that the slot will be updated to
     * @param offset
     *            The offset from the ref (metaDataA)
     * @param locationMetadata
     *            An index of the FieldReference (metaDataB)
     * @param mode
     *            The context in which the write is occuring
     */
    public static void performWriteInBarrier(ObjectReference ref, Address slot,
            ObjectReference target, Offset offset, int locationMetadata,
            int mode) {
    }

    /**
     * Atomically write a reference field of an object or array and return the
     * old value of the reference field.
     * 
     * @param ref
     *            The object that has the reference field
     * @param slot
     *            The slot that holds the reference
     * @param target
     *            The value that the slot will be updated to
     * @param offset
     *            The offset from the ref (metaDataA)
     * @param locationMetadata
     *            An index of the FieldReference (metaDataB)
     * @param mode
     *            The context in which the write is occuring
     * @return The value that was replaced by the write.
     */
    public static ObjectReference performWriteInBarrierAtomic(
            ObjectReference ref, Address slot, ObjectReference target,
            Offset offset, int locationMetadata, int mode) {
        return null;
    }

    /**
     * Gets an element of a char array without invoking any read barrier or
     * performing bounds check.
     * 
     * @param src
     *            the source array
     * @param index
     *            the natural array index of the element to get
     * @return the new value of element
     */
    @NoReadBarrier
    public static char getArrayNoBarrier(char[] src, int index) {
        return src[index];
    }

    /**
     * Gets an element of a byte array without invoking any read barrier or
     * bounds check.
     * 
     * @param src
     *            the source array
     * @param index
     *            the natural array index of the element to get
     * @return the new value of element
     */
    @NoReadBarrier
    public static byte getArrayNoBarrier(byte[] src, int index) {
        return src[index];
    }

    /**
     * Gets an element of an int array without invoking any read barrier or
     * performing bounds checks.
     * 
     * @param src
     *            the source array
     * @param index
     *            the natural array index of the element to get
     * @return the new value of element
     */
    @NoReadBarrier
    public static int getArrayNoBarrier(int[] src, int index) {
        return src[index];
    }

    /**
     * Gets an element of an Object array without invoking any read barrier or
     * performing bounds checks.
     * 
     * @param src
     *            the source array
     * @param index
     *            the natural array index of the element to get
     * @return the new value of element
     */
    @NoReadBarrier
    public static Object getArrayNoBarrier(Object[] src, int index) {
        return src[index];
    }

    /**
     * Gets an element of an array of byte arrays without causing the potential
     * thread switch point that array accesses normally cause.
     * 
     * @param src
     *            the source array
     * @param index
     *            the index of the element to get
     * @return the new value of element
     */
    @NoReadBarrier
    public static byte[] getArrayNoBarrier(byte[][] src, int index) {
        return src[index];
    }
}
