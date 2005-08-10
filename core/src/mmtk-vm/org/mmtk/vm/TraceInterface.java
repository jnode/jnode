/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.mmtk.vm;

import org.jnode.vm.annotation.Inline;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * Class that supports scanning Objects or Arrays for references during tracing,
 * handling those references, and computing death times
 * 
 * @author <a href="http://www-ali.cs.umass.edu/~hertz">Matthew Hertz</a>
 * @version $Revision$
 * @date $Date$
 */
public final class TraceInterface {

    /***************************************************************************
     * 
     * Public Methods
     */

    /**
     * Returns if the VM is ready for a garbage collection.
     * 
     * @return True if the RVM is ready for GC, false otherwise.
     */
    public static final boolean gcEnabled() {
        return false;
    }

    /**
     * This adjusts the offset into an object to reflect what it would look like
     * if the fields were laid out in memory space immediately after the object
     * pointer.
     * 
     * @param isScalar
     *            If this is a pointer store to a scalar object
     * @param src
     *            The address of the source object
     * @param slot
     *            The address within <code>src</code> into which the update
     *            will be stored
     * @return The easy to understand offset of the slot
     */
    public static final Offset adjustSlotOffset(boolean isScalar,
            ObjectReference src, Address slot) {
        return null;
    }

    /**
     * This skips over the frames added by the tracing algorithm, outputs
     * information identifying the method the containts the "new" call
     * triggering the allocation, and returns the address of the first
     * non-trace, non-alloc stack frame.
     * 
     * @param typeRef
     *            The type reference (tib) of the object just allocated
     * @return The frame pointer address for the method that allocated the
     *         object
     */
    public static final Address skipOwnFramesAndDump(ObjectReference typeRef) {
        return null;
    }

    /***************************************************************************
     * 
     * Wrapper methods
     */

    @Inline
    public static void updateDeathTime(Object obj) {
    }

    public static void setDeathTime(ObjectReference ref, Word time_) {
    }

    public static void setLink(ObjectReference ref, ObjectReference link) {
    }

    @Inline
    public static void updateTime(Word time_) {
    }

    @Inline
    public static Word getOID(ObjectReference ref) {
        return null;
    }

    @Inline
    public static Word getDeathTime(ObjectReference ref) {
        return null;
    }

    public static ObjectReference getLink(ObjectReference ref) {
        return null;
    }

    @Inline
    public static Address getBootImageLink() {
        return null;
    }

    @Inline
    public static Word getOID() {
        return null;
    }

    @Inline
    public static void setOID(Word oid) {
    }

    @Inline
    public static final int getHeaderSize() {
        return 0;
    }

    @Inline
    public static final int getHeaderEndOffset() {
        return 0;
    }
}
