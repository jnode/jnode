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
 
package org.mmtk.vm;

import org.jnode.vm.VmMagic;
import org.jnode.vm.annotation.Inline;
import org.mmtk.utility.scan.MMType;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * $Id$
 * 
 * @author <a href="http://cs.anu.edu.au/~Steve.Blackburn">Steve Blackburn</a>
 * @author Perry Cheng
 * @version $Revision$
 * @date $Date$
 */
public class ObjectModel {
    /**
     * Copy an object using a plan's allocCopy to get space and install the
     * forwarding pointer. On entry, <code>from</code> must have been reserved
     * for copying by the caller. This method calls the plan's
     * <code>getStatusForCopy()</code> method to establish a new status word
     * for the copied object and <code>postCopy()</code> to allow the plan to
     * perform any post copy actions.
     * 
     * @param from
     *            the address of the object to be copied
     * @return the address of the new object
     */
    public static ObjectReference copy(ObjectReference from) {
        return null;
    }

    /**
     * Allocate an array object, using the given array as an example of the
     * required type.
     * 
     * @param array
     *            an array of the type to be allocated
     * @param allocator
     *            which allocation scheme/area JMTk should allocation the memory
     *            from.
     * @param length
     *            the number of elements in the array to be allocated
     * @return the initialzed array object
     */
    public static Object cloneArray(Object[] array, int allocator, int length) {
        return null;
    }

    /**
     * Return the size required to copy an object
     * 
     * @param object
     *            The object whose size is to be queried
     * @return The size required to copy <code>obj</code>
     */
    public static int getSizeWhenCopied(ObjectReference object) {
        return 0;
    }

    /**
     * Return the size used by an object
     * 
     * @param object
     *            The object whose size is to be queried
     * @return The size of <code>obj</code>
     */
    public static int getCurrentSize(ObjectReference object) {
        return 0;
    }

    /**
     * Return the next object in the heap under contiguous allocation
     */
    public static ObjectReference getNextObject(ObjectReference object) {
        return null;
    }

    /**
     * Return an object reference from knowledge of the low order word
     */
    public static ObjectReference getObjectFromStartAddress(Address start) {
        return null;
    }

    /**
     * Get the type descriptor for an object.
     * 
     * @param ref
     *            address of the object
     * @return byte array with the type descriptor
     */
    public static byte[] getTypeDescriptor(ObjectReference ref) {
        return null;
    }

    /**
     * Gets the length of the given array.
     * 
     * @param object
     * @return
     */
    @Inline
    public static int getArrayLength(ObjectReference object) {
        return 0;
    }

    /**
     * Tests a bit available for memory manager use in an object.
     * 
     * @param object
     *            the address of the object
     * @param idx
     *            the index of the bit
     */
    public static boolean testAvailableBit(ObjectReference object, int idx) {
        return false;
    }

    /**
     * Sets a bit available for memory manager use in an object.
     * 
     * @param object
     *            the address of the object
     * @param idx
     *            the index of the bit
     * @param flag
     *            <code>true</code> to set the bit to 1, <code>false</code>
     *            to set it to 0
     */
    public static void setAvailableBit(ObjectReference object, int idx,
            boolean flag) {
    }

    /**
     * Attempts to set the bits available for memory manager use in an object.
     * The attempt will only be successful if the current value of the bits
     * matches <code>oldVal</code>. The comparison with the current value and
     * setting are atomic with respect to other allocators.
     * 
     * @param object
     *            the address of the object
     * @param oldVal
     *            the required current value of the bits
     * @param newVal
     *            the desired new value of the bits
     * @return <code>true</code> if the bits were set, <code>false</code>
     *         otherwise
     */
    public static boolean attemptAvailableBits(ObjectReference object,
            Word oldVal, Word newVal) {
        return false;
    }

    /**
     * Gets the value of bits available for memory manager use in an object, in
     * preparation for setting those bits.
     * 
     * @param object
     *            the address of the object
     * @return the value of the bits
     */
    public static Word prepareAvailableBits(ObjectReference object) {
        return null;
    }

    /**
     * Sets the bits available for memory manager use in an object.
     * 
     * @param object
     *            the address of the object
     * @param val
     *            the new value of the bits
     */
    public static void writeAvailableBitsWord(ObjectReference object, Word val) {
    }

    /**
     * Read the bits available for memory manager use in an object.
     * 
     * @param object
     *            the address of the object
     * @return the value of the bits
     */
    public static Word readAvailableBitsWord(ObjectReference object) {
        return null;
    }

    /**
     * Gets the offset of the memory management header from the object reference
     * address. XXX The object model / memory manager interface should be
     * improved so that the memory manager does not need to know this.
     * 
     * @return the offset, relative the object reference address
     */
    /* AJG: Should this be a variable rather than method? */
    public static Offset GC_HEADER_OFFSET() {
        return Offset.zero();
    }

    /**
     * Returns the lowest address of the storage associated with an object.
     * 
     * @param object
     *            the reference address of the object
     * @return the lowest address of the object
     */
    public static Address objectStartRef(ObjectReference object) {
        return null;
    }

    /**
     * Returns an address guaranteed to be inside the storage assocatied with
     * and object.
     * 
     * @param object
     *            the reference address of the object
     * @return an address inside the object
     */
    public static Address refToAddress(ObjectReference object) {
        return object.toAddress();
    }

    /**
     * Checks if a reference of the given type in another object is inherently
     * acyclic. The type is given as a TIB.
     * 
     * @return <code>true</code> if a reference of the type is inherently
     *         acyclic
     */
    public static boolean isAcyclic(ObjectReference typeRef) {
        return false;
    }

    /**
     * Return the type object for a give object
     * 
     * @param object
     *            The object whose type is required
     * @return The type object for <code>object</code>
     */
    public static MMType getObjectType(ObjectReference object) {
        return (MMType) VmMagic.getObjectType(object).getMmType();
    }
}
