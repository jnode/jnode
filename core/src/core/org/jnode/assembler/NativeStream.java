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
 
package org.jnode.assembler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.jnode.vm.CpuID;
import org.jnode.vm.classmgr.VmType;

public abstract class NativeStream {

    /**
     * Remove all data and references.
     */
    public abstract void clear();

    /**
     * Returns the base address.
     *
     * @return long
     */
    public abstract long getBaseAddr();

    /**
     * Get the length in bytes of valid data
     *
     * @return The length
     */
    public abstract int getLength();

    /**
     * Return the actual bytes. This array may be longer then getLength()
     *
     * @return The actual bytes
     */
    public abstract byte[] getBytes();

    /**
     * Gets the identification of the CPU for which this stream will produce data.
     */
    public abstract CpuID getCPUID();

    /**
     * Write my contents to the given stream.
     *
     * @param os
     * @throws IOException
     */
    public abstract void writeTo(OutputStream os) throws IOException;

    /**
     * Start a new object and write its header.
     * An ObjectInfo object is returned, on which the <code>markEnd</code>
     * mehod must be called after all data has been written into the object.
     *
     * @param cls
     * @return The object info for the started object
     * @see ObjectInfo
     */
    public abstract ObjectInfo startObject(VmType<?> cls);

    /**
     * Gets an objectref for a given object.
     *
     * @param key
     * @return ObjectRef
     */
    public abstract ObjectRef getObjectRef(Object key);

    /**
     * Gets all references of objects as instanceof ObjectRef
     *
     * @return Collection
     */
    public abstract Collection<? extends ObjectRef> getObjectRefs();

    /**
     * Gets all unresolved references of objects as instanceof ObjectRef
     *
     * @return Collection
     */
    public abstract Collection<?> getUnresolvedObjectRefs();

    /**
     * Are there unresolved references?
     *
     * @return True if there are unsolved references, false otherwise
     */
    public abstract boolean hasUnresolvedObjectRefs();

    /**
     * Resolve the given object to the current position.
     *
     * @param label
     * @return ObjectRef
     */
    public abstract ObjectRef setObjectRef(Object label);

    /**
     * Write a reference to the given object
     * @param object
     */
    //public abstract void writeObjectRef(Object object);

    /**
     * Write a 64-bit integer
     *
     * @param value
     */
    public abstract void set64(int offset, long value);

    /**
     * Write a 32-bit integer
     *
     * @param value
     */
    public abstract void set32(int offset, int value);

    /**
     * Write a 16-bit short
     *
     * @param value
     */
    public abstract void set16(int offset, int value);

    /**
     * Write a 8-bit byte
     *
     * @param value
     */
    public abstract void set8(int offset, int value);

    /**
     * Write a word with the processor specific word size.
     *
     * @param word
     */
    public abstract void setWord(int offset, long word);

    /**
     * Write a 64-bit integer
     *
     * @param value
     */
    public abstract void write64(long value);

    /**
     * Write a 32-bit integer
     *
     * @param value
     */
    public abstract void write32(int value);

    /**
     * Write a 16-bit short
     *
     * @param value
     */
    public abstract void write16(int value);

    /**
     * Write a 8-bit byte
     *
     * @param value
     */
    public abstract void write8(int value);

    /**
     * Write a word with the processor specific word size.
     *
     * @param word
     */
    public abstract void writeWord(long word);

    /**
     * Write a series of bytes
     *
     * @param data
     * @param ofs
     * @param len
     */
    public abstract void write(byte[] data, int ofs, int len);

    /**
     * Write a reference to the given object
     *
     * @param object
     */
    public abstract void writeObjectRef(Object object);

    /**
     * A wrapper for constructing objects in the native stream.
     */
    public abstract static class ObjectInfo {

        /**
         * Mark the current location as the end of this object end fixup the objectheader.
         */
        public abstract void markEnd();
    }

    public abstract static class ObjectRef {
        private Object object;

        /**
         * Create a new instance
         *
         * @param object
         */
        public ObjectRef(Object object) {
            this.object = object;
        }

        /**
         * Gets the object i'm referring to.
         *
         * @return Object
         */
        public Object getObject() {
            return object;
        }

        /**
         * Gets the offset of my object within this stream.
         * This offset is only valid if <code>isResolved() == true</code>.
         *
         * @return long
         * @throws UnresolvedObjectRefException
         */
        public abstract int getOffset()
            throws UnresolvedObjectRefException;

        /**
         * Has my object been resolved?
         *
         * @return boolean
         */
        public abstract boolean isResolved();

        /**
         * Link this objectref to the given objectref. That is, the offset of this
         * objectref will be set to the offset of the given objectref.
         *
         * @param objectRef
         * @throws UnresolvedObjectRefException The given objectref is not resolved.
         */
        public abstract void link(ObjectRef objectRef)
            throws UnresolvedObjectRefException;

        /**
         * Add an offset that needs to be resolved by this objectref.
         *
         * @param offset
         * @param patchSize
         */
        public abstract void addUnresolvedLink(int offset, int patchSize);

        public String toString() {
            return object.toString();
        }
    }
}
