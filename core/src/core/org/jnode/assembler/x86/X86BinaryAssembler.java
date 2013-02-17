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
 
package org.jnode.assembler.x86;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.jnode.assembler.BootImageNativeStream;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.X86Register.CRX;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR32;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.assembler.x86.X86Register.MMX;
import org.jnode.assembler.x86.X86Register.SR;
import org.jnode.assembler.x86.X86Register.XMM;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.X86CpuID;

import static org.jnode.assembler.x86.X86Register.CS;
import static org.jnode.assembler.x86.X86Register.DS;
import static org.jnode.assembler.x86.X86Register.ES;
import static org.jnode.assembler.x86.X86Register.FS;
import static org.jnode.assembler.x86.X86Register.GS;
import static org.jnode.assembler.x86.X86Register.SS;

/**
 * Implementation of AbstractX86Stream.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 * @author Patrik Reali (patrik_reali@users.sourceforge.net)
 */
public class X86BinaryAssembler extends X86Assembler implements X86Constants,
    BootImageNativeStream, X86Operation {


    static final class Key {

        private final Object key;

        public Key(Object key) {
            this.key = key;
        }

        /**
         * Test if this Key is equal to the supplied object.  The semantics
         * of equality depend on what the 'key' is.  If it is a {@link Label}, then two
         * Keys are equal if the Label values are equal.  Otherwise, two keys are equal if
         * the 'key' values refer to the same object.
         *
         * @param obj the object to test for equality.
         * @return Return {@code true} if obj is 'equal to' this, {@code false} otherwise.
         */
        public final boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Key)) {
                return false;
            }
            obj = ((Key) obj).key;
            if (this.key instanceof Label) {
                return key.equals(obj);
            } else {
                return (obj == this.key);
            }
        }

        /**
         * The hashcode is the hashcode for the Key's 'key' object.
         *
         * @return This Key instance's hashcode.
         */
        public final int hashCode() {
            return key.hashCode();
        }

    }

    public class X86ObjectInfo extends NativeStream.ObjectInfo {

        private int m_objptr;

        X86ObjectInfo() {
            m_objptr = getLength();
        }

        /**
         * Mark the current location as the end of this object end fixup the
         * objectheader.
         */
        public void markEnd() {
            if (!inObject) {
                throw new RuntimeException("inObject == false");
            }
            if (m_objptr == -1) {
                throw new RuntimeException("markEnd has already been called");
            }
            align(ObjectLayout.OBJECT_ALIGN);
            final int size = getLength() - m_objptr;
            if (isCode32()) {
                set32(m_objptr - (3 * 4), size);
            } else {
                set64(m_objptr - (3 * 8), size);
            }
            m_objptr = -1;
            inObject = false;
        }
    }

    public static final class UnresolvedOffset {
        private final int offset;

        private final int patchSize;

        public UnresolvedOffset(int offset, int patchSize) {
            if ((patchSize != 1) && (patchSize != 4) && (patchSize != 8)) {
                throw new IllegalArgumentException("PatchSize: " + patchSize);
            }
            this.offset = offset;
            this.patchSize = patchSize;
        }

        public final int getOffset() {
            return offset;
        }

        public final int getPatchSize() {
            return patchSize;
        }
    }

    /**
     * Represents an reference to an object/label.
     * The reference does not (yet) have to be resolved.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public class X86ObjectRef extends NativeStream.ObjectRef {

        private int dataOffset;

        private boolean isPublic;

        private boolean isRelJump;

        private LinkedList<UnresolvedOffset> unresolvedLinks; // Array of data_offsets where

        public X86ObjectRef(Object object) {
            super(object);
            this.dataOffset = -1;
            this.unresolvedLinks = null;
            this.isPublic = false;
            this.isRelJump = false;
        }

        public void addUnresolvedLink(int offset, int patchSize) {
            if (unresolvedLinks == null) {
                unresolvedLinks = new LinkedList<UnresolvedOffset>();
            }
            unresolvedLinks.add(new UnresolvedOffset(offset, patchSize));
        }

        /**
         * Gets the offset of the represented object/label in the
         * native stream.
         *
         * @see org.jnode.assembler.NativeStream.ObjectRef#getOffset()
         */
        public int getOffset() throws UnresolvedObjectRefException {
            if (!isResolved()) {
                throw new UnresolvedObjectRefException("Unresolved object: "
                    + this);
            }
            return dataOffset;
        }

        public int[] getUnresolvedOffsets() {
            final int cnt = unresolvedLinks.size();
            final int[] offsets = new int[cnt];
            int ofs = 0;
            for (UnresolvedOffset unrOfs : unresolvedLinks) {
                offsets[ofs++] = unrOfs.getOffset();
            }
            return offsets;
        }

        public boolean isPublic() {
            return isPublic;
        }

        public boolean isRelJump() {
            return isRelJump;
        }

        public boolean isResolved() {
            return (dataOffset != -1);
        }

        /**
         * Link this objectref to the given objectref. That is, the offset of
         * this objectref will be set to the offset of the given objectref.
         *
         * @param objectRef
         * @throws UnresolvedObjectRefException The given objectref is not resolved.
         */
        public void link(ObjectRef objectRef)
            throws UnresolvedObjectRefException {
            if (!objectRef.isResolved()) {
                throw new UnresolvedObjectRefException(objectRef.getObject()
                    .toString());
            }
            setOffset(objectRef.getOffset());
        }

        /**
         * Set the startoffset of referenced object/label and resolve
         * all unresolved references to it.
         *
         * @param offset
         */
        public void setOffset(int offset) {
            if (this.dataOffset != -1) {
                if (getObject().toString().isEmpty()) {
                    return;
                }
                throw new RuntimeException(
                    "Offset is already set. Duplicate labels? ("
                        + getObject() + ')');
            }
            if (offset < 0) {
                throw new IllegalArgumentException("Offset: " + offset);
            }
            this.dataOffset = offset;
            if (unresolvedLinks != null) {
                // Link all unresolved links
                for (UnresolvedOffset unrOfs : unresolvedLinks) {
                    final int addr = unrOfs.getOffset();
                    switch (unrOfs.getPatchSize()) {
                        case 1:
                            resolve8(addr, offset);
                            break;
                        case 4:
                            resolve32(addr, offset);
                            break;
                        case 8:
                            resolve64(addr, offset);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown patch size " + unrOfs.getPatchSize());
                    }
                }
                unresolvedLinks = null;
            }
        }

        /**
         * Resolve a 8-bit patch location.
         *
         * @param addr
         * @param offset
         */
        private final void resolve8(int addr, int offset) {
//            System.out.println("addr " + addr);
//            System.out.println("get8(addr) " + get8(addr));
//            System.out.println("offset " + offset);
            final int distance = offset - addr - 1; //get8(addr);
            if (!X86Utils.isByte(distance)) {
                throw new IllegalArgumentException("Jump out of byte-range (" + distance + ')');
            }
            if (isRelJump() && (distance == 0)) {
                // JMP
                if (get8(addr - 1) == 0xe9) {
                    set8(addr - 1, 0x90); // NOP
                    set8(addr, 0x90); // 1 NOP (overrides jmp offset)
                } else if (get8(addr - 2) == 0x0f) {
                    // Jcc
                    set8(addr - 2, 0x90);
                    set8(addr - 1, 0x90);
                    set8(addr, 0x90); // 1 NOP
                } else {
                    set8(addr, distance);
                }
            } else {
                set8(addr, distance);
            }
        }

        /**
         * Resolve a 32-bit patch location.
         *
         * @param addr
         * @param offset
         */
        private final void resolve32(int addr, int offset) {
            final int distance = offset - get32(addr);
            if (isRelJump() && (distance == 0)) {
                // JMP
                if (get8(addr - 1) == 0xe9) {
                    set8(addr - 1, 0x90); // NOP
                    set32(addr, 0x90909090); // 4 NOP's
                } else if (get8(addr - 2) == 0x0f) {
                    // Jcc
                    set8(addr - 2, 0x90);
                    set8(addr - 1, 0x90);
                    set32(addr, 0x90909090); // 4 NOP's
                } else {
                    set32(addr, distance);
                }
            } else {
                set32(addr, distance);
            }
        }

        /**
         * Resolve a 32-bit patch location.
         *
         * @param addr
         * @param offset
         */
        private final void resolve64(int addr, int offset) {
            final long distance = offset - get64(addr);
            if (isRelJump()) {
                throw new IllegalArgumentException(
                    "RelJump not supported for 64-bit");
            } else {
                set64(addr, distance);
            }
        }

        public void setPublic() {
            isPublic = true;
        }

        public void setRelJump() {
            isRelJump = true;
        }
    }

    private final int baseAddr;

    private int dataLength;

    private int growCount;

    private final int growSize;

    private final boolean haveCMOV;

    private final int initialObjectRefsCapacity;

    boolean inObject;

    private byte[] m_data;

    private int m_used;

    private Map<Key, X86ObjectRef> objectRefs; // Integer(labelnr),Integer(offset)

    private ObjectResolver resolver;

    private boolean byteValueEnabled = true;

    /**
     * Initialize this instance.
     *
     * @param cpuId
     * @param mode
     * @param baseAddr
     */
    public X86BinaryAssembler(X86CpuID cpuId, Mode mode, int baseAddr) {
        this(cpuId, mode, baseAddr, 1024, 128, 1024);
    }

    /**
     * Initialize this instance.
     *
     * @param cpuId
     * @param mode
     * @param baseAddr
     * @param initialObjectRefsCapacity
     * @param initialSize
     * @param growSize
     */
    public X86BinaryAssembler(X86CpuID cpuId, Mode mode, int baseAddr,
                              int initialObjectRefsCapacity, int initialSize, int growSize) {
        super(cpuId, mode);
        this.m_data = new byte[initialSize];
        this.m_used = 0;
        this.baseAddr = baseAddr;
        this.inObject = false;
        this.initialObjectRefsCapacity = initialObjectRefsCapacity;
        this.growSize = growSize;
        this.haveCMOV = cpuId.hasFeature(X86CpuID.FEAT_CMOV);
    }

    /**
     * Align on a given value
     *
     * @param value
     * @return The number of bytes needed to align.
     */
    public final int align(int value) {
        int count = 0;
        while ((getLength() % value) != 0) {
            write8(0x90); // Nop
            count++;
        }
        return count;
    }

    /**
     * Remove all data and references.
     */
    public final void clear() {
        //this.m_data = new byte[0];
        this.m_used = 0;
        this.objectRefs.clear();
    }

    /*
    // a possible growth strategy
    private final void ensureSize(int extra) {
        if (m_used + extra >= dataLength) {
            int newLen;
            byte[] newArr;
            if (growSize > dataLength) {
                newLen = growSize;
            } else {
                newLen = (dataLength << 1) + 3;
            }
            if (extra + dataLength > newLen) {
                newLen = dataLength + extra;
            }
            newArr = new byte[newLen];
            System.arraycopy(m_data, 0, newArr, 0, m_used);
            m_data = newArr;
            dataLength = newLen;
            growCount++;
            //System.out.println("Growing stream buffer to " + newLen);
        }
    }

    */


    private final void ensureSize(int extra) {
        if (m_used + extra >= dataLength) {
            int newLen;
            byte[] newArr;
            if (extra > growSize) {
                newLen = dataLength + extra;
            } else {
                newLen = dataLength + growSize;
            }
            newArr = new byte[newLen];
            System.arraycopy(m_data, 0, newArr, 0, m_used);
            m_data = newArr;
            dataLength = newLen;
            growCount++;
            // System.out.println("Growing stream buffer to " + newLen);
        }
    }

    /**
     * Allocate space and return the offset of the start of the allocated space.
     *
     * @see org.jnode.assembler.BootImageNativeStream#allocate(int)
     */
    public final int allocate(int size) {
        ensureSize(size);
        final int start = m_used;
        m_used += size;
        return start;
    }

    public final int get32(int offset) {
        int v1 = m_data[offset++];
        int v2 = m_data[offset++];
        int v3 = m_data[offset++];
        int v4 = m_data[offset];
        return (v1 & 0xFF) | ((v2 & 0xFF) << 8) | ((v3 & 0xFF) << 16)
            | ((v4 & 0xFF) << 24);
    }

    public final long get64(int offset) {
        long v1 = m_data[offset++];
        long v2 = m_data[offset++];
        long v3 = m_data[offset++];
        long v4 = m_data[offset++];
        long v5 = m_data[offset++];
        long v6 = m_data[offset++];
        long v7 = m_data[offset++];
        long v8 = m_data[offset];
        return (v1 & 0xFF) | ((v2 & 0xFF) << 8) | ((v3 & 0xFF) << 16)
            | ((v4 & 0xFF) << 24) | ((v5 & 0xFF) << 32)
            | ((v6 & 0xFF) << 40) | ((v7 & 0xFF) << 48)
            | ((v8 & 0xFF) << 56);
    }

    public final int get8(int offset) {
        return (m_data[offset] & 0xFF);
    }

    /**
     * Returns the base address.
     *
     * @return long
     */
    public final long getBaseAddr() {
        return baseAddr;
    }

    /**
     * Return the actual bytes. This array may be longer then getLength() *
     *
     * @return The actual bytes
     */
    public final byte[] getBytes() {
        return m_data;
    }

    /**
     * Get the length in bytes of valid data
     *
     * @return the length of valid data
     */
    public final int getLength() {
        return m_used;
    }

    /**
     * Gets an objectref for a given object.
     *
     * @param keyObj
     * @return ObjectRef
     */
    public final ObjectRef getObjectRef(Object keyObj) {
        if (keyObj == null) {
            throw new NullPointerException("Key cannot be null");
        }
        if (objectRefs == null) {
            objectRefs = new HashMap<Key, X86ObjectRef>(initialObjectRefsCapacity);
        }
        Key key = new Key(keyObj);
        X86ObjectRef ref = objectRefs.get(key);
        if (ref != null) {
            return ref;
        }
        ref = new X86ObjectRef(keyObj);
        objectRefs.put(key, ref);
        return ref;
    }

    /**
     * Gets all references of objects as instanceof ObjectRef
     *
     * @return Collection
     */
    public final Collection<X86ObjectRef> getObjectRefs() {
        if (objectRefs == null) {
            objectRefs = new HashMap<Key, X86ObjectRef>(initialObjectRefsCapacity);
        }
        return objectRefs.values();
    }

    public final int getObjectRefsCount() {
        if (objectRefs != null) {
            return objectRefs.size();
        } else {
            return 0;
        }
    }

    /**
     * @return ObjectResolver
     */
    public final ObjectResolver getResolver() {
        return resolver;
    }

    /**
     * Gets all unresolved references of objects as instanceof ObjectRef
     *
     * @return Collection
     */
    public final Collection<ObjectRef> getUnresolvedObjectRefs() {
        final Collection<X86ObjectRef> coll = getObjectRefs();
        final LinkedList<ObjectRef> result = new LinkedList<ObjectRef>();
        for (X86ObjectRef ref : coll) {
            if (!ref.isResolved()) {
                if (!(ref.getObject() instanceof Label)) {
                    result.add(ref);
                }
            }
        }
        System.out.println("getUnresolvedObjectsRefs: count=" + result.size());
        return result;
    }

    /**
     * Are there unresolved references?
     *
     * @return True if there are unresolved references, false otherwise
     */
    public final boolean hasUnresolvedObjectRefs() {
        final Collection<X86ObjectRef> coll = getObjectRefs();
        for (ObjectRef ref : coll) {
            if (!ref.isResolved()) {
                if (!(ref.getObject() instanceof Label)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Is logging enabled. This method will only return true on on debug like
     * implementations.
     *
     * @return boolean
     */
    public boolean isLogEnabled() {
        return false;
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#isTextStream()
     */
    public boolean isTextStream() {
        return false;
    }

    /**
     * Write a log message. This method is only implemented on debug like
     * implementations.
     *
     * @param msg
     */
    public void log(Object msg) {
        // Do nothing
    }

    public final void set16(int offset, int v16) {
        m_data[offset++] = (byte) (v16 & 0xFF);
        m_data[offset++] = (byte) ((v16 >> 8) & 0xFF);
    }

    public final void set32(int offset, int v32) {
        m_data[offset++] = (byte) (v32 & 0xFF);
        m_data[offset++] = (byte) ((v32 >> 8) & 0xFF);
        m_data[offset++] = (byte) ((v32 >> 16) & 0xFF);
        m_data[offset++] = (byte) ((v32 >> 24) & 0xFF);
    }

    public final void set64(int offset, long v64) {
        m_data[offset++] = (byte) (v64 & 0xFF);
        m_data[offset++] = (byte) ((v64 >> 8) & 0xFF);
        m_data[offset++] = (byte) ((v64 >> 16) & 0xFF);
        m_data[offset++] = (byte) ((v64 >> 24) & 0xFF);
        m_data[offset++] = (byte) ((v64 >> 32) & 0xFF);
        m_data[offset++] = (byte) ((v64 >> 40) & 0xFF);
        m_data[offset++] = (byte) ((v64 >> 48) & 0xFF);
        m_data[offset++] = (byte) ((v64 >> 56) & 0xFF);
    }

    public final void set8(int offset, int v8) {
        m_data[offset] = (byte) v8;
    }

    public final ObjectRef setObjectRef(Object label) {
        X86ObjectRef ref = (X86ObjectRef) getObjectRef(label);
        ref.setOffset(m_used);
        return ref;
    }

    /**
     * Sets the resolver.
     *
     * @param resolver The resolver to set
     */
    public void setResolver(ObjectResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Start a new object and write its header. An ObjectInfo object is
     * returned, on which the <code>markEnd</code> mehod must be called after
     * all data has been written into the object.
     *
     * @param cls
     * @return The info for the started object
     * @see ObjectInfo
     */
    public final ObjectInfo startObject(VmType<?> cls) {
        if (inObject) {
            throw new RuntimeException(
                "Cannot start an object within an object");
        }
        if (align(ObjectLayout.OBJECT_ALIGN) != 0) {
            throw new RuntimeException("Unaligned before startObject");
        }
        inObject = true;

        // The default header is 2 words long. The size fields add another
        // word, which adds up to 3 words which masy not be objectaligned.
        // Write some slack until it is aligned again
        int alignSlack = 0;
        final int threeWords = getWordSize() * 3;
        while (ObjectLayout.objectAlign(alignSlack + threeWords) != (alignSlack + threeWords)) {
            writeWord(0);
            alignSlack += getWordSize();
        }
        // System.out.println("alignSlack=" + alignSlack);

        writeWord(0); // Size
        writeWord(ObjectFlags.GC_DEFAULT_COLOR); // Flags
        if (cls == null) {
            throw new NullPointerException("cls==null");
        } else {
            final Object[] tib = ((VmClassType<?>) cls).getTIB();
            if (tib[0] == null) {
                throw new NullPointerException("tib[0]==null");
            }
            writeObjectRef(tib, 0, false);
        }
        return new X86ObjectInfo();
    }

    private void testDst(X86Register dst, int dstDisp) {
        if ((dst == X86Register.EBP) && (dstDisp == 0)) {
            throw new IllegalArgumentException("Write to [EBP+0]");
        }
    }

    /**
     * Dst register must have an 8-bits part. Valid for EAX, EBX, ECX, EDX,
     * runtimeexception for others.
     *
     * @param dst
     */
    private void testSuitableForBits8(X86Register dst) {
        if (!dst.isSuitableForBits8()) {
            throw new IllegalArgumentException("Register " + dst
                + " is not suitable for BITS8 datasize");
        }
    }

    /**
     * Remove count bytes from the end of the generated stream.
     *
     * @param count
     */
    public void trim(int count) {
        if ((count < 0) || (count > m_used)) {
            throw new IllegalArgumentException("Invalid count value " + count);
        }
        m_used -= count;
    }

    public final void write(byte[] data, int ofs, int len) {
        /*
           * if (!inObject) { throw new IllegalArgumentException("Cannot write out
           * of an object");
           */
        ensureSize(len);
        System.arraycopy(data, ofs, m_data, m_used, len);
        m_used += len;
    }

    public final void write16(int v16) {

        //if (!inObject) {
        //  throw new IllegalArgumentException("Cannot write out of an object");
        //}
        ensureSize(2);
        m_data[m_used++] = (byte) (v16 & 0xFF);
        m_data[m_used++] = (byte) ((v16 >> 8) & 0xFF);
    }

    public final void write32(int v32) {
        /*
           * if (!inObject) { throw new IllegalArgumentException("Cannot write out
           * of an object");
           */
        ensureSize(4);
        m_data[m_used++] = (byte) (v32 & 0xFF);
        m_data[m_used++] = (byte) ((v32 >> 8) & 0xFF);
        m_data[m_used++] = (byte) ((v32 >> 16) & 0xFF);
        m_data[m_used++] = (byte) ((v32 >> 24) & 0xFF);
    }

    public final void write64(long v64) {
        write32((int) (v64 & 0xFFFFFFFF)); // lsb
        write32((int) ((v64 >>> 32) & 0xFFFFFFFF)); // msb
    }

    /**
     * Write an 8-bit unsigned byte.
     *
     * @param v8
     */
    public final void write8(int v8) {
        ensureSize(1);
        m_data[m_used++] = (byte) (v8 & 0xFF);
    }

    /**
     * Create a ADC dstReg, imm32
     *
     * @param dstReg
     * @param imm32
     */
    public void writeADC(GPR dstReg, int imm32) {
        testSize(dstReg, mode.getSize());
        if (isByte(imm32)) {
            write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 2);
            write8(imm32);
        } else {
            write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 2);
            write32(imm32);
        }
    }

    public void writeADC(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
        if (isByte(imm32)) {
            write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 2);
            write8(imm32);
        } else {
            write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 2);
            write32(imm32);
        }
    }

    /**
     * Create a ADC [dstReg+dstDisp], <srcReg>
     *
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public final void writeADC(GPR dstReg, int dstDisp, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        write1bOpcodeModRM(0x11, srcReg.getSize(), dstReg, dstDisp, srcReg
            .getNr());
    }

    /**
     * Create a ADC dstReg, srcReg
     *
     * @param dstReg
     * @param srcReg
     */
    public void writeADC(GPR dstReg, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        write1bOpcodeModRR(0x11, dstReg.getSize(), dstReg, srcReg.getNr());
    }

    /**
     * Create a ADC dstReg, [srcReg+srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeADC(GPR dstReg, GPR srcReg, int srcDisp) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        write1bOpcodeModRM(0x13, dstReg.getSize(), srcReg, srcDisp, dstReg
            .getNr());
    }

    /**
     * Create an ADD dstReg, imm32
     *
     * @param dstReg
     * @param imm32
     */
    public void writeADD(GPR dstReg, int imm32) {
        testSize(dstReg, BITS32 | BITS64);
        if (isByte(imm32)) {
            write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 0);
            write8(imm32);
        } else {
            write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 0);
            write32(imm32);
        }
    }

    /**
     * Create a ADD [dstReg+dstDisp], imm32
     *
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeADD(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
        if (isByte(imm32)) {
            write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 0);
            write8(imm32);
        } else {
            write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 0);
            write32(imm32);
        }
    }

    public void writeADD(int operandSize, SR dstReg, int dstDisp, int imm32) {
        testOperandSize(operandSize, BITS32);
        writeSegPrefix(dstReg);
        write8(0x81);
        write8(0x05);
        write32(dstDisp);
        write32(imm32);
    }

    public void writeADD_MEM(X86Register.GPR reg, int memPtr) {
        testSize(reg, mode.getSize());
        if (code64) {
            throw new InvalidOpcodeException("Not implemented");
        }
        write8(0x03); // opcode
        write8((reg.getNr() << 3) | 5); // disp32
        write32(memPtr);
    }

    /**
     * Create a ADD [dstDisp], imm32
     *
     * @param operandSize
     * @param dstDisp
     * @param imm32
     */
    public void writeADD(int operandSize, int dstDisp, int imm32) {
        testOperandSize(operandSize, BITS8 | BITS16 | BITS32);
        switch (operandSize) {
            case BITS8:
                write1bOpcodeModMem(0x80, operandSize, dstDisp, 0);
                break;
            case BITS16:
                write8(OSIZE_PREFIX);
                write1bOpcodeModMem(0x81, operandSize, dstDisp, 0);
                break;
            case BITS32:
                write1bOpcodeModMem(0x81, operandSize, dstDisp, 0);
                break;
        }
        write32(imm32);
    }

    /**
     * Create a ADD [dstReg+dstDisp], srcReg
     *
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public final void writeADD(GPR dstReg, int dstDisp, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        write1bOpcodeModRM(0x01, srcReg.getSize(), dstReg, dstDisp, srcReg
            .getNr());
    }

    /**
     * Create a ADD dstReg, srcReg
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeADD(GPR dstReg, GPR srcReg) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        write1bOpcodeModRR(0x01, dstReg.getSize(), dstReg, srcReg.getNr());
    }

    /**
     * Create a ADD dstReg, [srcReg+srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public final void writeADD(GPR dstReg, GPR srcReg, int srcDisp) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        write1bOpcodeModRM(0x03, dstReg.getSize(), srcReg, srcDisp, dstReg
            .getNr());
    }

    /**
     * Create a AND dstReg, imm32
     *
     * @param dstReg
     * @param imm32
     */
    public final void writeAND(GPR dstReg, int imm32) {
        testSize(dstReg, BITS8 | BITS16 | BITS32 | BITS64);
        int size = dstReg.getSize();
        if ((size & (BITS32 | BITS64)) == size) {
            if (isByte(imm32)) {
                write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 4);
                write8(imm32);
            } else {
                write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 4);
                write32(imm32);
            }
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            if (isByte(imm32)) {
                write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 4);
                write8(imm32);
            } else {
                write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 4);
                write16(imm32);
            }
        } else if (size == BITS8) {
            write1bOpcodeModRR(0x80, dstReg.getSize(), dstReg, 4);
            write8(imm32);
        }
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeAND(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
        if (isByte(imm32)) {
            write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 4);
            write8(imm32);
        } else {
            write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 4);
            write32(imm32);
        }
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeAND(int operandSize, SR dstReg, int dstDisp, int imm32) {
        testOperandSize(operandSize, BITS32);
        writeSegPrefix(dstReg);
        write8(0x81);
        write8(0x25);
        write32(dstDisp);
        write32(imm32);
    }

    /**
     * @param operandSize
     * @param dstDisp
     * @param imm32
     */
    public void writeAND(int operandSize, int dstDisp, int imm32) {
        testOperandSize(operandSize, BITS8 | BITS16 | BITS32);
        switch (operandSize) {
            case BITS8:
                write1bOpcodeModMem(0x80, operandSize, dstDisp, 4);
                break;
            case BITS16:
                write8(OSIZE_PREFIX);
                write1bOpcodeModMem(0x81, operandSize, dstDisp, 4);
                break;
            case BITS32:
                write1bOpcodeModMem(0x81, operandSize, dstDisp, 4);
                break;
        }
        write8(imm32);
    }

    /**
     * Create a AND [dstReg+dstDisp], srcReg
     *
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public final void writeAND(GPR dstReg, int dstDisp, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, BITS8 | BITS16 | BITS32 | BITS64);
        int size = srcReg.getSize();
        if ((size & (BITS32 | BITS64)) == size) {
            write1bOpcodeModRM(0x21, size, dstReg, dstDisp, srcReg.getNr());
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            write1bOpcodeModRM(0x21, size, dstReg, dstDisp, srcReg.getNr());
        } else if (size == BITS8) {
            write1bOpcodeModRM(0x20, size, dstReg, dstDisp, srcReg.getNr());
        }
    }

    /**
     * Create a AND dstReg, srcReg
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeAND(GPR dstReg, GPR srcReg) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        write1bOpcodeModRR(0x21, dstReg.getSize(), dstReg, srcReg.getNr());
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeAND(GPR dstReg, GPR srcReg, int srcDisp) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        write1bOpcodeModRM(0x23, dstReg.getSize(), srcReg, srcDisp, dstReg
            .getNr());
    }

    public void writeArithSSEDOp(int operation, XMM dst, GPR src, int srcDisp) {
        final int opcode3 = sseOperationToOpcode3(operation);
        write3bOpcodeModRM(0xF2, 0x0F, opcode3, 0, src, srcDisp, dst.getNr());
    }

    public void writeArithSSEDOp(int operation, XMM dst, XMM src) {
        final int opcode3 = sseOperationToOpcode3(operation);
        write3bOpcodeModRR(0xF2, 0x0F, opcode3, 0, src, dst.getNr());
    }

    public void writeArithSSESOp(int operation, XMM dst, GPR src, int srcDisp) {
        final int opcode3 = sseOperationToOpcode3(operation);
        write3bOpcodeModRM(0xF3, 0x0F, opcode3, 0, src, srcDisp, dst.getNr());
    }

    public void writeArithSSESOp(int operation, XMM dst, XMM src) {
        final int opcode3 = sseOperationToOpcode3(operation);
        write3bOpcodeModRR(0xF3, 0x0F, opcode3, 0, src, dst.getNr());
    }

    /**
     * Convert an SSE operation into the 3'rd opcode byte for that operation.
     *
     * @param operation
     * @return the 3'rd opcode byte.
     */
    private final int sseOperationToOpcode3(int operation) {
        final int opcode3;
        switch (operation) {
            case SSE_ADD:
                opcode3 = 0x58;
                break;
            case SSE_SUB:
                opcode3 = 0x5C;
                break;
            case SSE_MUL:
                opcode3 = 0x59;
                break;
            case SSE_DIV:
                opcode3 = 0x5E;
                break;
            default:
                throw new IllegalArgumentException("Invalid SSE operation "
                    + operation);
        }
        return opcode3;
    }

    /**
     * Create a bound lReg, [rReg+rDisp]
     *
     * @param lReg
     * @param rReg
     * @param rDisp
     */
    public final void writeBOUND(GPR lReg, GPR rReg, int rDisp) {
        if (code64) {
            throw new InvalidOpcodeException();
        }
        write1bOpcodeModRM(0x62, 0, rReg, rDisp, lReg.getNr());
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeBreakPoint()
     */
    public void writeBreakPoint() {
        write8(0xCC);
    }

    /**
     * Create a relative call to a given label
     *
     * @param label
     */
    public final void writeCALL(Label label) {
        write8(0xe8); // call rel32
        writeRelativeObjectRef(label);
    }

    /**
     * Create a call to address stored at the given offset in the given table
     * pointer.
     *
     * @param tablePtr
     * @param offset
     * @param rawAddress If true, tablePtr is a raw address
     */
    public final void writeCALL(Object tablePtr, int offset, boolean rawAddress) {
        if (code64) {
            throw new InvalidOpcodeException();
        }
        write8(0xFF); // Opcode
        write8(0x15); // effective address == disp32
        writeObjectRef(tablePtr, offset, rawAddress);
    }

    public void writeCALL(GPR reg) {
        testSize(reg, mode.getSize());
        // Since CALL in 64-bit mode always use 64-bit targets, we
        // specify a 0 operand size, so we won't get a REX prefix
        write1bOpcodeModRR(0xFF, 0, reg, 2);
    }

    /**
     * Create a call to address stored at the given [reg+offset].
     *
     * @param reg
     * @param offset
     */
    public final void writeCALL(GPR reg, int offset) {
        // Since CALL in 64-bit mode always use 64-bit targets, we
        // specify a 0 operand size, so we won't get a REX prefix
        write1bOpcodeModRM(0xFF, 0, reg, offset, 2);
    }

    public void writeCALL(GPR regBase, GPR regIndex, int scale, int disp) {
        // Since CALL in 64-bit mode always use 64-bit targets, we
        // specify a 0 operand size, so we won't get a REX prefix
        write1bOpcodeModRMSib(0xFF, 0, regBase, disp, 2, scale, regIndex);
    }

    public void writeCALL(GPR regIndex, int scale, int disp) {
        testOperandSize(BITS32, mode.getSize());
        switch (scale) {
            case 1:
                scale = 0;
                break;
            case 2:
                scale = 1;
                break;
            case 4:
                scale = 2;
                break;
            case 8:
                scale = 3;
                break;
            default:
                throw new IllegalArgumentException("scale");
        }

        //TODO review
        write8(0xFF);
        write8(0x14);
        write8(0x05 | regIndex.getNr() << 3 | scale << 6);
        write32(disp);
    }

    /**
     * Create a cdq
     * Sign extend EAX to EDX:EAX in 32-bit operand size.
     * Sign extend RAX to RDX:RAX in 64-bit operand size.
     */
    public final void writeCDQ(int operandSize) {
        testOperandSize(operandSize, BITS32 | BITS64);
        if (operandSize == BITS64) {
            if (!code64) {
                throw new InvalidOpcodeException();
            }
            write8(REX_W_PREFIX);
        }
        write8(0x99);
    }

    /**
     * Create a cdqe. Sign extend EAX to RAX. Only valid in 64-bit mode.
     */
    public void writeCDQE() throws InvalidOpcodeException {
        if (!code64) {
            throw new InvalidOpcodeException();
        }
        write8(REX_W_PREFIX);
        write8(0x98);
    }

    public void writeCLD() {
        write8(0xFC);
    }

    public void writeCLI() {
        write8(0xFA);
    }

    public void writeCLTS() {
        write8(CRX_PREFIX);
        write8(0x06);
    }

    /**
     * Create a CMOVcc dst,src
     *
     * @param ccOpcode
     * @param dst
     * @param src
     */
    public void writeCMOVcc(int ccOpcode, GPR dst, GPR src) {
        if (!haveCMOV) {
            throw new InvalidOpcodeException("CMOVcc not supported");
        }
        write2bOpcodeModRR(0x0F, ccOpcode - 0x40, src.getSize(), src, dst
            .getNr());
    }

    /**
     * Create a CMOVcc dst,[src+srcDisp]
     *
     * @param dst
     * @param src
     * @param srcDisp
     */
    public void writeCMOVcc(int ccOpcode, GPR dst, GPR src, int srcDisp) {
        if (!haveCMOV) {
            throw new InvalidOpcodeException("CMOVcc not supported");
        }
        write2bOpcodeModRM(0x0F, ccOpcode - 0x40, dst.getSize(), src, srcDisp,
            dst.getNr());
    }

    /**
     * Create a CMP [reg1+disp], reg2
     *
     * @param reg1
     * @param disp
     * @param reg2
     */
    public void writeCMP(GPR reg1, int disp, GPR reg2) {
        testSize(reg1, BITS32 | BITS64);
        testSize(reg2, BITS32 | BITS64);
        write1bOpcodeModRM(0x39, reg2.getSize(), reg1, disp, reg2.getNr());
    }

    /**
     * Create a CMP reg1, reg2
     *
     * @param reg1
     * @param reg2
     */
    public final void writeCMP(GPR reg1, GPR reg2) {
        testSize(reg1, BITS32 | BITS64);
        testSize(reg2, BITS32 | BITS64);
        write1bOpcodeModRR(0x39, reg1.getSize(), reg1, reg2.getNr());
    }

    /**
     * Create a CMP reg1, [reg2+disp]
     *
     * @param reg1
     * @param reg2
     * @param disp
     */
    public void writeCMP(GPR reg1, SR reg2, int disp) {
        testSize(reg1, BITS32);
        writeSegPrefix(reg2);
        write8(0x3b);
        write8(0x05 | reg1.getNr() << 3);
        write32(disp);
    }

    /**
     * Create a CMP reg1, [reg2+disp]
     *
     * @param reg1
     * @param reg2
     * @param disp
     */
    public void writeCMP(GPR reg1, GPR reg2, int disp) {
        testSize(reg1, BITS32 | BITS64);
        testSize(reg2, BITS32 | BITS64);
        write1bOpcodeModRM(0x3b, reg1.getSize(), reg2, disp, reg1.getNr());
    }

    /**
     * Create a CMP reg, imm32
     *
     * @param reg
     * @param imm32
     */
    public final void writeCMP_Const(GPR reg, int imm32) {
        testSize(reg, BITS8 | BITS16 | BITS32 | BITS64);
        int size = reg.getSize();
        if ((size & (BITS32 | BITS64)) == size) {
            if (isByte(imm32)) {
                write1bOpcodeModRR(0x83, reg.getSize(), reg, 7);
                write8(imm32);
            } else {
                write1bOpcodeModRR(0x81, reg.getSize(), reg, 7);
                write32(imm32);
            }
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            if (isByte(imm32)) {
                write1bOpcodeModRR(0x83, reg.getSize(), reg, 7);
                write8(imm32);
            } else {
                write1bOpcodeModRR(0x81, reg.getSize(), reg, 7);
                write16(imm32);
            }
        } else if (size == BITS8) {
            write1bOpcodeModRR(0x80, reg.getSize(), reg, 7);
            write8(imm32);
        }
    }

    /**
     * Create a CMP [reg+disp], imm32
     *
     * @param reg
     * @param disp
     * @param imm32
     */
    public void writeCMP_Const(int operandSize, GPR reg, int disp, int imm32) {
        testSize(reg, mode.getSize());
        if (isByte(imm32)) {
            write1bOpcodeModRM(0x83, operandSize, reg, disp, 7);
            write8(imm32);
        } else {
            write1bOpcodeModRM(0x81, operandSize, reg, disp, 7);
            write32(imm32);
        }
    }

    /**
     * Create a CMP [reg:disp], imm32
     *
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeCMP_Const(int operandSize, SR dstReg, int dstDisp, int imm32) {
        testOperandSize(operandSize, BITS32);
        writeSegPrefix(dstReg);
        write8(0x81);
        write8(0x3d);
        write32(dstDisp);
        write32(imm32);
    }

    /**
     * Create a CMP eax,imm32 or CMP rax,imm32
     *
     * @param imm32
     */
    public final void writeCMP_EAX(int operandSize, int imm32) {
        testOperandSize(operandSize, BITS32 | BITS64);
        write1bOpcodeREXPrefix(operandSize, 0);
        write8(0x3d);
        write32(imm32);
    }

    /**
     * Create a CMP [reg+regDisp], imm32
     *
     * @param memPtr
     * @param imm32
     */
    public final void writeCMP_MEM(int operandSize, int memPtr, int imm32) {
        testOperandSize(operandSize, BITS32 | BITS64);
        write1bOpcodeREXPrefix(operandSize, 0);
        write8(0x81); // Opcode
        write8(0x3D); // effective address == disp32
        write32(memPtr);
        write32(imm32);
    }

    /**
     * Create a CMP reg,[memPtr]
     *
     * @param reg
     * @param memPtr
     */
    public void writeCMP_MEM(GPR reg, int memPtr) {
        testSize(reg, mode.getSize());
        if (code64) {
            throw new InvalidOpcodeException("Not implemented");
        }
        write8(0x3b); // opcode
        write8((reg.getNr() << 3) | 5); // disp32
        write32(memPtr);
    }

    /**
     * Create a CMPXCHG dword [dstReg], srcReg
     *
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     * @param lock
     */
    public final void writeCMPXCHG_EAX(GPR dstReg, int dstDisp, GPR srcReg,
                                       boolean lock) {
        if (lock) {
            write8(0xF0);
        }
        write2bOpcodeModRM(0x0F, 0xB1, srcReg.getSize(), dstReg, dstDisp,
            srcReg.getNr());
    }

    /**
     *
     */
    public void writeCPUID() {
        write8(CRX_PREFIX);
        write8(0xA2);
    }

    /**
     * Create a dec reg32
     *
     * @param dstReg
     */
    public final void writeDEC(GPR dstReg) {
        testSize(dstReg, BITS32 | BITS64);
        if (code32) {
            write8(0x48 + dstReg.getNr());
        } else {
            write1bOpcodeModRR(0xFF, dstReg.getSize(), dstReg, 1);
        }
    }

    /**
     * Create a dec dword [dstReg+dstDisp]
     *
     * @param dstReg
     * @param dstDisp
     */
    public final void writeDEC(int operandSize, GPR dstReg, int dstDisp) {
        testSize(dstReg, mode.getSize());
        testOperandSize(operandSize, BITS32 | BITS64);
        write1bOpcodeModRM(0xff, operandSize, dstReg, dstDisp, 1);
    }

    /**
     * Create an div edx:eax, srcReg.
     * <p/>
     * If srcReg is 64-bit, the div rdx:rax, srcReg is created.
     *
     * @param srcReg
     */
    public final void writeDIV_EAX(GPR srcReg) {
        write1bOpcodeModRR(0xF7, srcReg.getSize(), srcReg, 6);
    }

    public void writeEMMS() {
        write8(0x0F);
        write8(0x77);
    }


    /**
     * Create a fadd dword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFADD32(GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0xd8, 0, srcReg, srcDisp, 0);
    }

    /**
     * Create a fadd qword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFADD64(GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0xdc, 0, srcReg, srcDisp, 0);
    }

    public void writeFADDP(X86Register fpuReg) {
        write8(0xde);
        write8(0xc0 + fpuReg.getNr());
    }

    /**
     * Create a fchs
     */
    public final void writeFCHS() {
        write8(0xd9);
        write8(0xe0);
    }

    /**
     * Create a fdiv dword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFDIV32(GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0xd8, 0, srcReg, srcDisp, 6);
    }

    /**
     * Create a fdiv qword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFDIV64(GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0xdc, 0, srcReg, srcDisp, 6);
    }

    public void writeFDIVP(X86Register fpuReg) {
        write8(0xde);
        write8(0xf8 + fpuReg.getNr());
    }

    /**
     * Create a ffree
     *
     * @param fReg
     */
    public final void writeFFREE(X86Register fReg) {
        write8(0xdd);
        write8(0xc0 | fReg.getNr());
    }

    /**
     * Create a fild dword [dstReg+dstDisp]
     *
     * @param dstReg
     * @param dstDisp
     */
    public final void writeFILD32(GPR dstReg, int dstDisp) {
        write1bOpcodeModRM(0xdb, 0, dstReg, dstDisp, 0);
    }

    /**
     * Create a fild qword [dstReg+dstDisp]
     *
     * @param dstReg
     * @param dstDisp
     */
    public final void writeFILD64(GPR dstReg, int dstDisp) {
        write1bOpcodeModRM(0xdf, 0, dstReg, dstDisp, 5);
    }

    /**
     * Create a fistp dword [dstReg+dstDisp]
     *
     * @param dstReg
     * @param dstDisp
     */
    public final void writeFISTP32(GPR dstReg, int dstDisp) {
        write1bOpcodeModRM(0xdb, 0, dstReg, dstDisp, 3);
    }

    /**
     * Create a fistp qword [dstReg+dstDisp]
     *
     * @param dstReg
     * @param dstDisp
     */
    public final void writeFISTP64(GPR dstReg, int dstDisp) {
        write1bOpcodeModRM(0xdf, 0, dstReg, dstDisp, 7);
    }

    /**
     * Create a fld dword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFLD32(GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0xd9, 0, srcReg, srcDisp, 0);
    }

    /**
     * Create a fld dword [srcBaseReg+scrIndexReg*srcScale+srcDisp]
     *
     * @param srcBaseReg
     * @param srcIndexReg
     * @param srcScale
     * @param srcDisp
     */
    public void writeFLD32(GPR srcBaseReg, GPR srcIndexReg, int srcScale,
                           int srcDisp) {
        write1bOpcodeModRMSib(0xd9, 0, srcBaseReg, srcDisp, 0, srcScale,
            srcIndexReg);
    }

    /**
     * Create a fld qword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFLD64(GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0xdd, 0, srcReg, srcDisp, 0);
    }

    /**
     * Create a fld qword [srcBaseReg+scrIndexReg*srcScale+srcDisp]
     *
     * @param srcBaseReg
     * @param srcIndexReg
     * @param srcScale
     * @param srcDisp
     */
    public void writeFLD64(GPR srcBaseReg, GPR srcIndexReg, int srcScale,
                           int srcDisp) {
        write1bOpcodeModRMSib(0xdd, 0, srcBaseReg, srcDisp, 0, srcScale,
            srcIndexReg);
    }

    /**
     * Create a fldcw word [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFLDCW(GPR srcReg, int srcDisp) {
        write8(0xd9);
        writeModRM(srcReg.getNr() & 7, srcDisp, 5);
    }

    /**
     * Create a fmul dword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFMUL32(GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0xd8, 0, srcReg, srcDisp, 1);
    }

    /**
     * Create a fmul qword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFMUL64(GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0xdc, 0, srcReg, srcDisp, 1);
    }

    public void writeFMULP(X86Register fpuReg) {
        write8(0xde);
        write8(0xc8 + fpuReg.getNr());
    }

    /**
     * Create a fninit
     */
    public final void writeFNINIT() {
        write8(0xdb);
        write8(0xe3);
    }

    /**
     * Create a fnsave [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFNSAVE(GPR srcReg, int srcDisp) {
        write8(0xdd);
        writeModRM(srcReg.getNr() & 7, srcDisp, 6);
    }

    /**
     * Create a fnstsw, Store fp status word in AX
     */
    public final void writeFNSTSW_AX() {
        write8(0xdf);
        write8(0xe0);
    }

    /**
     * Create a fprem
     */
    public final void writeFPREM() {
        write8(0xd9);
        write8(0xf8);
    }

    /**
     * Create a frstor [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFRSTOR(GPR srcReg, int srcDisp) {
        write8(0xdd);
        writeModRM(srcReg.getNr() & 7, srcDisp, 4);
    }

    /**
     * Create a fstcw word [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFSTCW(GPR srcReg, int srcDisp) {
        write8(0x9b);
        write8(0xd9);
        writeModRM(srcReg.getNr() & 7, srcDisp, 7);
    }

    public void writeFSTP(X86Register fpuReg) {
        write8(0xDD);
        write8(0xD8 + fpuReg.getNr());
    }

    /**
     * Create a fstp dword [dstReg+dstDisp]
     *
     * @param dstReg
     * @param dstDisp
     */
    public final void writeFSTP32(GPR dstReg, int dstDisp) {
        write1bOpcodeModRM(0xd9, 0, dstReg, dstDisp, 3);
    }

    /**
     * Create a fstp qword [dstReg+dstDisp]
     *
     * @param dstReg
     * @param dstDisp
     */
    public final void writeFSTP64(GPR dstReg, int dstDisp) {
        write1bOpcodeModRM(0xdd, 0, dstReg, dstDisp, 3);
    }

    /**
     * Create a fsub dword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFSUB32(GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0xd8, 0, srcReg, srcDisp, 4);
    }

    /**
     * Create a fsub qword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFSUB64(GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0xdc, 0, srcReg, srcDisp, 4);
    }

    public void writeFSUBP(X86Register fpuReg) {
        write8(0xde);
        write8(0xe8 + fpuReg.getNr());
    }

    /**
     * Create a fucompp, Compare - Pop twice
     */
    public final void writeFUCOMPP() {
        write8(0xda);
        write8(0xe9);
    }

    public void writeFXCH(X86Register fpuReg) {
        write8(0xd9);
        write8(0xc8 + fpuReg.getNr());
    }

    /**
     * Create a fxrstor [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFXRSTOR(GPR srcReg, int srcDisp) {
        write8(0x0f);
        write8(0xae);
        writeModRM(srcReg.getNr() & 7, srcDisp, 1);
    }

    /**
     * Create a fxsave [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     */
    public final void writeFXSAVE(GPR srcReg, int srcDisp) {
        write8(0x0f);
        write8(0xae);
        writeModRM(srcReg.getNr() & 7, srcDisp, 0);
    }

    public void writeHLT() {
        write8(0xF4);
    }

    /**
     * Create an idiv edx:eax, srcReg.
     * If srcReg is 64-bit, the idiv rdx:rax, srcReg is created.
     *
     * @param srcReg
     */
    public final void writeIDIV_EAX(GPR srcReg) {
        write1bOpcodeModRR(0xF7, srcReg.getSize(), srcReg, 7);
    }

    /**
     * @param srcReg
     * @param srcDisp
     */
    public void writeIDIV_EAX(int operandSize, GPR srcReg, int srcDisp) {
        testOperandSize(operandSize, BITS32 | BITS64);
        write1bOpcodeModRM(0xF7, operandSize, srcReg, srcDisp, 7);
    }

    /**
     * @param dstReg
     * @param srcReg
     */
    public void writeIMUL(GPR dstReg, GPR srcReg) {
        write2bOpcodeModRR(0x0F, 0xAF, srcReg.getSize(), srcReg, dstReg.getNr());
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeIMUL(GPR dstReg, GPR srcReg, int srcDisp) {
        write2bOpcodeModRM(0x0F, 0xAF, dstReg.getSize(), srcReg, srcDisp,
            dstReg.getNr());
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param imm32
     */
    public void writeIMUL_3(GPR dstReg, GPR srcReg, int imm32) {
        write1bOpcodeModRR(0x69, dstReg.getSize(), srcReg, dstReg.getNr());
        write32(imm32);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     * @param imm32
     */
    public void writeIMUL_3(GPR dstReg, GPR srcReg, int srcDisp, int imm32) {
        write1bOpcodeModRM(0x69, dstReg.getSize(), srcReg, srcDisp, dstReg
            .getNr());
        write32(imm32);
    }

    /**
     * Create a imul eax, srcReg.
     * <p/>
     * If srcReg is 64-bit, an imul rax, srcReg is created.
     *
     * @param srcReg
     */
    public final void writeIMUL_EAX(GPR srcReg) {
        write1bOpcodeModRR(0xF7, srcReg.getSize(), srcReg, 5);
    }

    public void writeIN(int operandSize) {
        if (operandSize == X86Constants.BITS8) {
            write8(0xEC);
        } else if (operandSize == X86Constants.BITS16) {
            write8(0xED);
        } else if (operandSize == X86Constants.BITS32) {
            write8(X86Constants.OSIZE_PREFIX);
            write8(0xED);
        } else {
            throw new IllegalArgumentException("Invalid operand size for IN: " + operandSize);
        }
    }

    public void writeIN(int operandSize, int imm8) {
        if (operandSize == X86Constants.BITS8) {
            write8(0xE4);
            write8(imm8);
        } else if (operandSize == X86Constants.BITS16) {
            write8(0xE5);
            write8(imm8);
        } else if (operandSize == X86Constants.BITS32) {
            write8(X86Constants.OSIZE_PREFIX);
            write8(0xE5);
            write8(imm8);
        } else {
            throw new IllegalArgumentException("Invalid operand size for IN: " + operandSize);
        }
    }

    /**
     * Create a inc reg32
     *
     * @param dstReg
     */
    public final void writeINC(GPR dstReg) {
        testSize(dstReg, BITS32 | BITS64);
        if (code32) {
            write8(0x40 + dstReg.getNr());
        } else {
            write1bOpcodeModRR(0xFF, dstReg.getSize(), dstReg, 0);
        }
    }

    /**
     * Create a inc size [dstReg:disp]
     *
     * @param dstReg
     */
    public final void writeINC(int operandSize, SR dstReg, int disp) {
        testOperandSize(operandSize, BITS32);
        writeSegPrefix(dstReg);
        write8(0xff);
        write8(0x05);
        write32(disp);
    }


    /**
     * Create a inc size [dstReg+disp]
     *
     * @param dstReg
     */
    public final void writeINC(int operandSize, GPR dstReg, int disp) {
        testSize(dstReg, mode.getSize());
        testOperandSize(operandSize, BITS8 | BITS16 | BITS32 | BITS64);
        if (operandSize == BITS32 || operandSize == BITS64) {
            write1bOpcodeModRM(0xFF, operandSize, dstReg, disp, 0);
        } else if (operandSize == BITS16) {
            write8(OSIZE_PREFIX);
            write1bOpcodeModRM(0xFF, operandSize, dstReg, disp, 0);
        } else if (operandSize == BITS8) {
            write1bOpcodeModRM(0xFE, operandSize, dstReg, disp, 0);
        }
    }

    /**
     * Create a inc size [dstReg+disp]
     *
     * @param dstReg
     */
    public final void writeINC(int operandSize, GPR dstReg, GPR dstIdxReg, int scale, int disp) {
        testSize(dstReg, mode.getSize());
        testSize(dstIdxReg, mode.getSize());
        testOperandSize(operandSize, BITS32 | BITS64);
        write1bOpcodeModRMSib(0xFF, operandSize, dstReg, disp, 0, scale, dstIdxReg);
    }

    /**
     * Create a inc size [dstReg+disp]
     *
     * @param operandSize
     * @param dstDisp
     */
    public final void writeINC(int operandSize, int dstDisp) {
        testOperandSize(operandSize, BITS8 | BITS16 | BITS32);
        //TODO review
        if (operandSize == BITS32) {
            write8(0xFF);
            write8(5);
            write32(dstDisp);
        } else if (operandSize == BITS16) {
            write8(OSIZE_PREFIX);
            write8(0xFF);
            write8(5);
            write32(dstDisp);
        } else if (operandSize == BITS8) {
            write8(0xFE);
            write8(5);
            write32(dstDisp);
        }
    }


    /**
     * Create a int vector
     *
     * @param vector
     */
    public final void writeINT(int vector) {
        write8(0xCD);
        write8(vector);
    }

    /**
     *
     */
    public void writeIRET() {
        write8(0xCF);
    }

    /**
     * Create a conditional jump to a label.
     *
     * @param label
     * @param jumpOpcode
     */
    public final void writeJCC(Label label, int jumpOpcode) {
        final ObjectRef ref = getObjectRef(label);
        final int shortOffset = m_used + 2;
        if (ref.isResolved() && isByteDistance(ref, shortOffset)) {
            try {
                // We can do a short jump
                write8(jumpOpcode - 0x10); // jcc imm8
                write8(ref.getOffset() - shortOffset);
            } catch (UnresolvedObjectRefException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            write8(0x0f); // jxx rel32
            write8(jumpOpcode);
            writeRelativeObjectRef(label);
        }
    }

    public final void writeJECXZ(Label label) {
        final ObjectRef ref = getObjectRef(label);
        final int shortOffset = m_used + 2;
        if (ref.isResolved()) {
            if (isByteDistance(ref, shortOffset)) {
                try {
                    write8(0xE3);
                    write8(ref.getOffset() - shortOffset);
                } catch (UnresolvedObjectRefException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                try {
                    throw new IllegalArgumentException("Invalid jump distance: " + (ref.getOffset() - shortOffset));
                } catch (UnresolvedObjectRefException rex) {
                    throw new RuntimeException(rex);
                }
            }
        } else {
            write8(0xE3);

            final int ofs = m_used + 1;
            final X86ObjectRef xref = (X86ObjectRef) getObjectRef(label);
            xref.setRelJump();
            if (xref.isResolved()) {
                try {
                    write8(xref.getOffset() - ofs);
                } catch (UnresolvedObjectRefException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                xref.addUnresolvedLink(m_used, 1);
                write8(ofs);
            }
        }
    }


    //TODO this method does not handle the forward jumps correctly, needs further work.
    //Also the general purpose version of the method writeJCC(Lable lebal, int jumpcode)
    //for handling byte sized target for the jump would renders this method unnecessary.

    /**
     * Create a LOOP label instruction. The given label must have be resolved
     * before!
     *
     * @param label
     */
    public final void writeJECXZ0(Label label) {
        final ObjectRef ref = getObjectRef(label);
        write8(0x67);
        write8(0xE3);
        final int offset = m_used + 1;
        if (ref.isResolved()) {
            try {
                int distance = ref.getOffset() - offset;
                if (X86Utils.isByte(distance)) {
                    write8(distance);
                } else {
                    throw new UnresolvedObjectRefException("Label " + label
                        + " is out of range (distance " + distance + ')');
                }
            } catch (UnresolvedObjectRefException x) {
                throw new RuntimeException(x);
            }
        } else {
            ref.addUnresolvedLink(m_used, 1);
            write8(offset);
        }
    }


    /**
     * Create a relative jump to a given label
     *
     * @param label
     */
    public final void writeJMP(Label label) {
        final ObjectRef ref = getObjectRef(label);
        final int shortOffset = m_used + 2;
        if (ref.isResolved() && isByteDistance(ref, shortOffset)) {
            try {
                // We can do a short jump
                write8(0xEB); // jmp imm8
                write8(ref.getOffset() - shortOffset);
            } catch (UnresolvedObjectRefException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            write8(0xe9); // jmp rel32
            writeRelativeObjectRef(label);
        }
    }

    private final boolean isByteDistance(ObjectRef ref, int offset) {
        try {
            final int distance = ref.getOffset() - offset;
            return X86Utils.isByte(distance);
        } catch (UnresolvedObjectRefException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Create a absolute jump to address stored at the given offset in the given
     * table pointer.
     *
     * @param tablePtr
     * @param offset
     * @param rawAddress If true, tablePtr is a raw address
     */
    public void writeJMP(Object tablePtr, int offset, boolean rawAddress) {
        if (code64) {
            throw new InvalidOpcodeException();
        }
        write8(0xFF); // Opcode
        write8(0x25); // effective address == disp32
        writeObjectRef(tablePtr, offset, rawAddress);
    }

    /**
     * Create a absolute jump to address stored at the given offset (in
     * register) in the given table pointer.
     *
     * @param tablePtr
     * @param offsetReg
     */
    public void writeJMP(Object tablePtr, GPR offsetReg) {
        write2bOpcodeReg(0xFF, 0xA0, offsetReg);
        // effective address == disp32[reg]
        writeObjectRef(tablePtr, 0, false);
    }

    /**
     * Create a absolute jump to address in register.
     *
     * @param reg
     */
    public final void writeJMP(GPR reg) {
        testSize(reg, mode.getSize());
        // Since JMP defaults to 64-bit in 64-bit mode, we give 0 as
        // operand size to avoid a REX prefix.
        write1bOpcodeModRR(0xff, 0, reg, 4);
    }

    /**
     * Create a absolute jump to [reg+disp]
     *
     * @param reg
     * @param disp
     */
    public final void writeJMP(GPR reg, int disp) {
        testSize(reg, mode.getSize());
        write2bOpcodeReg(0xFF, 0xA0, reg);
        write32(disp);
    }

    /**
     * Create a absolute jump to disp1:disp2
     *
     * @param seg
     * @param disp
     */
    public final void writeJMP(int operandSize, int seg, int disp) {
        testOperandSize(operandSize, BITS16 | BITS32);
        testOperandSize(mode.getSize(), BITS32);
        if (operandSize == BITS16) {
            write8(OSIZE_PREFIX);
        }
        write8(0xea);
        if (operandSize == BITS16) {
            write16(disp);
        } else {
            write32(disp);
        }
        write16(seg);
    }

    /**
     * Create a ldmxcsr dword [srcReg+disp]
     *
     * @param srcReg
     * @param disp
     */
    public final void writeLDMXCSR(GPR srcReg, int disp) {
        write8(0x0f);
        write8(0xae);
        writeModRM(srcReg.getNr() & 7, disp, 2);
    }

    /**
     * Create a lea dstReg,[srcReg+disp]
     *
     * @param dstReg
     * @param srcReg
     * @param disp
     */
    public final void writeLEA(GPR dstReg, GPR srcReg, int disp) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        write1bOpcodeModRM(0x8d, dstReg.getSize(), srcReg, disp, dstReg.getNr());
    }

    /**
     * Create a lea dstReg,[srcReg+srcIdxReg*scale+disp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcIdxReg
     * @param scale
     * @param disp
     */
    public final void writeLEA(GPR dstReg, GPR srcReg, GPR srcIdxReg,
                               int scale, int disp) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        testSize(srcIdxReg, mode.getSize());
        write1bOpcodeModRMSib(0x8d, dstReg.getSize(), srcReg, disp, dstReg
            .getNr(), scale, srcIdxReg);
    }

    /**
     * Create a lea dstReg,[srcReg+srcIdxReg*scale+disp]
     *
     * @param dstReg
     * @param srcIdxReg
     * @param scale
     * @param disp
     */
    public final void writeLEA(GPR dstReg, GPR srcIdxReg, int scale, int disp) {
        testSize(dstReg, mode.getSize());
        testSize(srcIdxReg, mode.getSize());
        switch (scale) {
            case 1:
                scale = 0;
                break;
            case 2:
                scale = 1;
                break;
            case 4:
                scale = 2;
                break;
            case 8:
                scale = 3;
                break;
            default:
                throw new IllegalArgumentException("scale");
        }

        //TODO review
        write8(0x8d);
        write8(0x04 | dstReg.getNr() << 3);
        write8(0x05 | srcIdxReg.getNr() << 3 | scale << 6);
        write32(disp);
    }

    public final void writeLGDT(int disp) {
        testOperandSize(mode.getSize(), BITS32);
        //TODO review
        write8(0x0f);
        write8(0x01);
        write8(0x15);
        write32(disp);
    }

    public final void writeLIDT(int disp) {
        testOperandSize(mode.getSize(), BITS32);
        //TODO review
        write8(0x0f);
        write8(0x01);
        write8(0x1d);
        write32(disp);
    }

    /**
     * Create a lmsw srcReg
     *
     * @param srcReg
     */
    public final void writeLMSW(GPR srcReg) {
        testSize(srcReg, X86Constants.BITS16);
        write8(0x0f);
        write8(0x01);
        writeModRR(srcReg.getNr() & 7, 6);
    }

    /**
     * Create a LODSD
     */
    public final void writeLODSD() {
        write8(OSIZE_PREFIX);
        write8(0xAD);
    }

    /**
     * Create a LODSW
     */
    public final void writeLODSW() {
        write8(0xAD);
    }

    /**
     * Create a LOOP label instruction. The given label must have be resolved
     * before!
     *
     * @param label
     * @throws UnresolvedObjectRefException
     */
    public final void writeLOOP(Label label)
        throws UnresolvedObjectRefException {
        final ObjectRef ref = getObjectRef(label);
        if (ref.isResolved()) {
            write8(0xE2);
            final int offset = m_used + 1;
            int distance = ref.getOffset() - offset;
            if (X86Utils.isByte(distance)) {
                write8(distance);
            } else {
                throw new UnresolvedObjectRefException("Label " + label
                    + " is out of range (distance " + distance + ')');
            }
        } else {
            throw new UnresolvedObjectRefException("Label " + label
                + " is not resolved");
        }
    }

    /**
     * Create a ltr word reg
     *
     * @param srcReg
     */
    public final void writeLTR(GPR srcReg) {
        testSize(srcReg, X86Constants.BITS16);
        write8(0x0f);
        write8(0x00);
        writeModRR(srcReg.getNr() & 7, 3);
    }

    /**
     * Write a REX prefix byte if needed for ModRM and ModRR encoded opcodes.
     *
     * @param operandSize
     * @param reg
     */
    private final void write1bOpcodeREXPrefix(int operandSize, int reg) {
        int rex = 0;
        if (operandSize == BITS64) {
            rex |= REX_W_PREFIX;
        }
        if (reg > 7) {
            rex |= REX_B_PREFIX;
        }
        if (rex != 0) {
            write8(rex);
        }
    }

    /**
     * Write a 1 byte opcode that has the register encoded in the single byte.
     *
     * @param opcode1
     * @param reg
     */
    private final void write1bOpcodeReg(int opcode1, X86Register reg) {
        write1bOpcodeREXPrefix(reg.getSize(), reg.getNr());
        write8(opcode1 + (reg.getNr() & 7));
    }

    /**
     * Write a 2 byte opcode that has the register encoded in the single byte.
     *
     * @param opcode1
     * @param opcode2
     * @param reg
     */
    private final void write2bOpcodeReg(int opcode1, int opcode2, X86Register reg) {
        write1bOpcodeREXPrefix(reg.getSize(), reg.getNr());
        write8(opcode1);
        write8(opcode2 + (reg.getNr() & 7));
    }

    /**
     * Write a REX prefix byte if needed for ModRM and ModRR encoded opcodes.
     *
     * @param rm
     * @param reg
     */
    private final void writeModRMRREXPrefix(int operandSize, X86Register rm,
                                            int reg) {
        int rex = 0;
        if (operandSize == BITS64) {
            rex |= REX_W_PREFIX;
        }
        if ((rm != null) && (rm.getNr() > 7)) {
            rex |= REX_B_PREFIX;
        }
        if (reg > 7) {
            rex |= REX_R_PREFIX;
        }
        if (rex != 0) {
            write8(rex);
        }
    }

    /**
     * Write a REX prefix byte if needed.
     *
     * @param operandSize
     * @param base
     * @param reg
     * @param index
     */
    private final void writeModRMSibREXPrefix(int operandSize, GPR base,
                                              int reg, GPR index) {
        int rex = 0;
        if (operandSize == BITS64) {
            rex |= REX_W_PREFIX;
        }
        if (base.getNr() > 7) {
            rex |= REX_B_PREFIX;
        }
        if (reg > 7) {
            rex |= REX_R_PREFIX;
        }
        if (index.getNr() > 7) {
            rex |= REX_X_PREFIX;
        }
        if (rex != 0) {
            write8(rex);
        }
    }

    /**
     * Write a mod-r/m byte+offset for the following addressing scheme's [rm]
     * disp8[rm] disp32[rm]
     *
     * @param rm
     * @param disp
     * @param reg
     */
    private final void writeModRM(int rm, int disp, int reg) {
        if ((rm < 0) || (rm > 7)) {
            throw new IllegalArgumentException("rm");
        }
        if ((reg < 0) || (reg > 7)) {
            throw new IllegalArgumentException("reg");
        }
        if (rm == X86Register.ESP.getNr()) {
            if (isByte(disp)) {
                write8(0x40 | (reg << 3) | rm);
                write8(0x24);
                write8(disp);
            } else {
                write8(0x80 | (reg << 3) | rm);
                write8(0x24);
                write32(disp);
            }
        } else {
            if ((disp == 0) && (rm != X86Register.EBP.getNr())) {
                write8(0x00 | (reg << 3) | rm);
            } else if (isByte(disp)) {
                write8(0x40 | (reg << 3) | rm);
                write8(disp);
            } else {
                write8(0x80 | (reg << 3) | rm);
                write32(disp);
            }
        }
    }

    /**
     * Write a 1-byte instruction followed by a mod-r/m byte+offset for the
     * following addressing scheme's [rm] disp8[rm] disp32[rm]
     *
     * @param opcode
     * @param rm
     * @param disp
     * @param reg
     */
    private final void write1bOpcodeModRM(int opcode, int operandSize, GPR rm,
                                          int disp, int reg) {
        writeModRMRREXPrefix(operandSize, rm, reg);
        write8(opcode);
        writeModRM(rm.getNr() & 7, disp, reg & 7);
    }

    /**
     * Write a 2-byte instruction followed by a mod-r/m byte+offset for the
     * following addressing scheme's [rm] disp8[rm] disp32[rm]
     *
     * @param opcode1
     * @param opcode2
     * @param operandSize
     * @param rm
     * @param disp
     * @param reg
     */
    private final void write2bOpcodeModRM(int opcode1, int opcode2,
                                          int operandSize, GPR rm, int disp, int reg) {
        writeModRMRREXPrefix(operandSize, rm, reg);
        write8(opcode1);
        write8(opcode2);
        writeModRM(rm.getNr() & 7, disp, reg & 7);
    }

    /**
     * Write a 2-byte instruction followed by a mod-r/m byte+offset for the
     * following addressing scheme's [rm] disp8[rm] disp32[rm]
     *
     * @param opcode1
     * @param opcode2
     * @param opcode3
     * @param operandSize
     * @param rm
     * @param disp
     * @param reg
     */
    private final void write3bOpcodeModRM(int opcode1, int opcode2,
                                          int opcode3, int operandSize, GPR rm, int disp, int reg) {
        writeModRMRREXPrefix(operandSize, rm, reg);
        write8(opcode1);
        write8(opcode2);
        write8(opcode3);
        writeModRM(rm.getNr() & 7, disp, reg & 7);
    }

    /**
     * Write a 1-byte instruction followed by a mod-r/m byte+offset for the
     * following addressing scheme's disp32
     *
     * @param opcode
     * @param disp
     * @param reg
     */
    private final void write1bOpcodeModMem(int opcode, int operandSize,
                                           int disp, int reg) {
        writeModRMRREXPrefix(operandSize, null, reg);
        write8(opcode);
        writeModRM(5, disp, reg & 7);
    }

    /**
     * Write a mod-r/m byte+offset+scale+index+base for the following addressing
     * scheme's [rm] disp8[rm] disp32[rm] To create
     * <code>[index*scale+disp]</code> code, set base to -1.
     *
     * @param base
     * @param disp
     * @param reg
     * @param scale
     * @param index
     */
    private final void writeModRMSib(int base, int disp, int reg, int scale,
                                     int index) {
        if ((base < -1) || (base > 7))
            throw new IllegalArgumentException("base");
        if ((reg < 0) || (reg > 7))
            throw new IllegalArgumentException("reg");
        if ((index < 0) || (index > 7))
            throw new IllegalArgumentException("index");

        switch (scale) {
            case 1:
                scale = 0;
                break;
            case 2:
                scale = 1;
                break;
            case 4:
                scale = 2;
                break;
            case 8:
                scale = 3;
                break;
            default:
                throw new IllegalArgumentException("scale");
        }

        if (base == -1) {
            write8(0x00 | (reg << 3) | 4);
            write8((scale << 6) | (index << 3) | 5);
            write32(disp);
        } else if ((disp == 0) && (base != X86Register.EBP.getNr())) {
            write8(0x00 | (reg << 3) | 4);
            write8((scale << 6) | (index << 3) | base);
        } else if (isByte(disp)) {
            write8(0x40 | (reg << 3) | 4);
            write8((scale << 6) | (index << 3) | base);
            write8(disp);
        } else {
            write8(0x80 | (reg << 3) | 4);
            write8((scale << 6) | (index << 3) | base);
            write32(disp);
        }
    }

    /**
     * Write a 1-byte instruction followed by a mod-r/m byte+offset for the
     * following addressing scheme's [rm] disp8[rm] disp32[rm]
     *
     * @param opcode
     * @param base
     * @param disp
     * @param reg
     * @param scale
     * @param index
     */
    private final void write1bOpcodeModRMSib(int opcode, int operandSize,
                                             GPR base, int disp, int reg, int scale, GPR index) {
        testSize(base, mode.getSize());
        testSize(index, mode.getSize());
        writeModRMSibREXPrefix(operandSize, base, reg, index);
        write8(opcode);
        writeModRMSib(base.getNr() & 7, disp, reg & 7, scale, index.getNr() & 7);
    }

    /**
     * Write a mod-r/m byte for the following addressing scheme rm
     *
     * @param rm
     * @param reg
     */
    private final void writeModRR(int rm, int reg) {
        if ((rm < 0) || (rm > 7))
            throw new IllegalArgumentException("rm");
        if ((reg < 0) || (reg > 7))
            throw new IllegalArgumentException("reg");
        write8(0xc0 | (reg << 3) | rm);
    }

    /**
     * Write a 1-byte instruction followed by a mod-r/m byte for the following
     * addressing scheme rm
     *
     * @param opcode
     * @param operandSize Size of the operands ({@link X86Constants}.BITSxxx)
     * @param rm
     * @param reg
     */
    private final void write1bOpcodeModRR(int opcode, int operandSize, GPR rm,
                                          int reg) {
        writeModRMRREXPrefix(operandSize, rm, reg);
        write8(opcode);
        writeModRR(rm.getNr() & 7, reg & 7);
    }

    /**
     * Write a 2-byte instruction followed by a mod-r/m byte for the following
     * addressing scheme rm
     *
     * @param opcode1
     * @param opcode2
     * @param operandSize
     * @param rm
     * @param reg
     */
    private final void write2bOpcodeModRR(int opcode1, int opcode2,
                                          int operandSize, GPR rm, int reg) {
        writeModRMRREXPrefix(operandSize, rm, reg);
        write8(opcode1);
        write8(opcode2);
        writeModRR(rm.getNr() & 7, reg & 7);
    }

    /**
     * Write a 3-byte instruction followed by a mod-r/m byte for the following
     * addressing scheme rm
     *
     * @param opcode1
     * @param opcode2
     * @param opcode3
     * @param operandSize
     * @param rm
     * @param reg
     */
    private final void write3bOpcodeModRR(int opcode1, int opcode2,
                                          int opcode3, int operandSize, X86Register rm, int reg) {
        writeModRMRREXPrefix(operandSize, rm, reg);
        write8(opcode1);
        write8(opcode2);
        write8(opcode3);
        writeModRR(rm.getNr() & 7, reg & 7);
    }

    private void writeModRR_MMX(int opcode, X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        write8(0x0F);
        write8(opcode);
        writeModRR(srcMmx.getNr() & 7, dstMmx.getNr() & 7);
    }

    /**
     * Create a mov [dstReg:dstDisp], <srcReg>
     *
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public final void writeMOV(SR dstReg, int dstDisp, GPR srcReg) {
        testSize(srcReg, mode.getSize());
        writeSegPrefix(dstReg);
        write8(0x89);
        write8(0x05 | srcReg.getNr() << 3);
        write32(dstDisp);
    }

    /**
     * Create a mov [dstReg+dstDisp], <srcReg>
     *
     * @param operandSize
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public final void writeMOV(int operandSize, GPR dstReg, int dstDisp,
                               GPR srcReg) {
        testSize(dstReg, BITS8 | BITS16 | BITS32 | BITS64);
        testSize(srcReg, BITS8 | BITS16 | BITS32 | BITS64);
        //TODO review
        testDst(dstReg, dstDisp);
        final int opcode;
        switch (operandSize) {
            case X86Constants.BITS8:
                testSuitableForBits8(srcReg);
                opcode = 0x88;
                break;
            case X86Constants.BITS16:
                opcode = 0x89;
                write8(OSIZE_PREFIX);
                break;
            case X86Constants.BITS32:
            case X86Constants.BITS64:
                opcode = 0x89;
                break;
            default:
                throw new IllegalArgumentException("Invalid operandSize "
                    + operandSize);
        }
        write1bOpcodeModRM(opcode, operandSize, dstReg, dstDisp, srcReg.getNr());
    }

    /**
     * Create a mov <dstReg>, <srcReg>
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeMOV(CRX dstReg, GPR srcReg) {
        testSize(srcReg, mode.getSize());
        write8(CRX_PREFIX);
        write8(0x22);
        writeModRR(srcReg.getNr() & 7, dstReg.getNr() & 7);
    }

    /**
     * Create a mov <dstReg>, <srcReg>
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeMOV(GPR dstReg, CRX srcReg) {
        testSize(dstReg, mode.getSize());
        write8(CRX_PREFIX);
        write8(0x20);
        writeModRR(dstReg.getNr() & 7, srcReg.getNr() & 7);
    }

    /**
     * Create a mov <dstReg>, <srcReg>
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeMOV(SR dstReg, GPR srcReg) {
        if (X86Register.CS.equals(dstReg))
            throw new IllegalArgumentException("Cannot MOV to CS");

        testSize(srcReg, BITS16 | BITS32);
        write8(0x8E);
        writeModRR(srcReg.getNr() & 7, dstReg.getNr() & 7);
    }

    /**
     * Create a mov <dstReg>, <srcReg>
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeMOV(GPR dstReg, SR srcReg) {
        testSize(dstReg, BITS16 | BITS32);
        write8(0x8C);
        writeModRR(dstReg.getNr() & 7, srcReg.getNr() & 7);
    }


    /**
     * Create a mov <dstReg>, <srcReg>
     *
     * @param operandSize
     * @param dstReg
     * @param srcReg
     */
    public final void writeMOV(int operandSize, GPR dstReg, GPR srcReg) {
        testSize(dstReg, BITS8 | BITS16 | BITS32 | BITS64);
        testSize(srcReg, BITS8 | BITS16 | BITS32 | BITS64);
        //TODO review
        final int opcode;
        switch (operandSize) {
            case X86Constants.BITS8:
                testSuitableForBits8(dstReg);
                testSuitableForBits8(srcReg);
                opcode = 0x88;
                break;
            case X86Constants.BITS16:
                opcode = 0x89;
                write8(OSIZE_PREFIX);
                break;
            case X86Constants.BITS32:
            case X86Constants.BITS64:
                opcode = 0x89;
                break;
            default:
                throw new IllegalArgumentException("Invalid operandSize "
                    + operandSize);
        }
        write1bOpcodeModRR(opcode, operandSize, dstReg, srcReg.getNr());
    }

    /**
     * Create a mov dstReg, [srcReg:srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public final void writeMOV(GPR dstReg, SR srcReg, int srcDisp) {
        testSize(dstReg, mode.getSize());
        writeSegPrefix(srcReg);
        write8(0x8b);
        write8(0x05 | dstReg.getNr() << 3);
        write32(srcDisp);
    }

    /**
     * Create a mov dstReg, [srcReg+srcDisp]
     *
     * @param operandSize
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public final void writeMOV(int operandSize, GPR dstReg, GPR srcReg,
                               int srcDisp) {
        testSize(dstReg, BITS8 | BITS16 | BITS32 | BITS64);
        testSize(srcReg, mode.getSize());
        //TODO review
        final int opcode;
        switch (operandSize) {
            case X86Constants.BITS8:
                testSuitableForBits8(dstReg);
                opcode = 0x8a;
                break;
            case X86Constants.BITS16:
                opcode = 0x8b;
                write8(OSIZE_PREFIX);
                break;
            case X86Constants.BITS32:
            case X86Constants.BITS64:
                opcode = 0x8b;
                break;
            default:
                throw new IllegalArgumentException("Invalid operandSize "
                    + operandSize);
        }
        write1bOpcodeModRM(opcode, operandSize, srcReg, srcDisp, dstReg.getNr());
    }

    /**
     * Create a mov [dstReg+dstIdxReg*scale+dstDisp], <srcReg>
     *
     * @param operandSize
     * @param dstReg
     * @param dstIdxReg
     * @param scale
     * @param dstDisp
     * @param srcReg
     */
    public final void writeMOV(int operandSize, GPR dstReg, GPR dstIdxReg,
                               int scale, int dstDisp, GPR srcReg) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        testSize(dstIdxReg, mode.getSize());
        final int opcode;
        switch (operandSize) {
            case X86Constants.BITS8:
                testSuitableForBits8(srcReg);
                opcode = 0x88;
                break;
            case X86Constants.BITS16:
                opcode = 0x89;
                write8(OSIZE_PREFIX);
                break;
            case X86Constants.BITS32:
            case X86Constants.BITS64:
                opcode = 0x89;
                break;
            default:
                throw new IllegalArgumentException("Invalid operandSize "
                    + operandSize);
        }
        write1bOpcodeModRMSib(opcode, operandSize, dstReg, dstDisp, srcReg
            .getNr(), scale, dstIdxReg);
    }

    /**
     * Create a mov dstReg, [srcReg+srcIdxReg*scale+srcDisp]
     *
     * @param operandSize
     * @param dstReg
     * @param srcReg
     * @param srcIdxReg
     * @param scale
     * @param srcDisp
     */
    public final void writeMOV(int operandSize, GPR dstReg, GPR srcReg,
                               GPR srcIdxReg, int scale, int srcDisp) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        testSize(srcIdxReg, mode.getSize());
        final int opcode;
        switch (operandSize) {
            case X86Constants.BITS8:
                testSuitableForBits8(dstReg);
                opcode = 0x8a;
                break;
            case X86Constants.BITS16:
                opcode = 0x8b;
                write8(OSIZE_PREFIX);
                break;
            case X86Constants.BITS32:
            case X86Constants.BITS64:
                opcode = 0x8b;
                break;
            default:
                throw new IllegalArgumentException("Invalid operandSize "
                    + operandSize);
        }
        write1bOpcodeModRMSib(opcode, operandSize, srcReg, srcDisp, dstReg
            .getNr(), scale, srcIdxReg);
    }

    /**
     * @param dstReg
     * @param srcDisp
     */
    public final void writeMOV(GPR dstReg, int srcDisp) {
        testSize(dstReg, BITS8 | BITS16 | BITS32);
        int size = dstReg.getSize();
        //TODO review
        if (size == BITS32) {
            write8(0x8b);
            write8(dstReg.getNr() << 3 | 5);
            write32(srcDisp);
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            write8(0x8b);
            write8(dstReg.getNr() << 3 | 5);
            write32(srcDisp);
        } else if (size == BITS8) {
            write8(0x8a);
            write8(dstReg.getNr() << 3 | 5);
            write32(srcDisp);
        }
    }

    /**
     * @param dstDisp
     * @param srcReg
     */
    public final void writeMOV(int dstDisp, GPR srcReg) {
        testSize(srcReg, BITS8 | BITS16 | BITS32);
        int size = srcReg.getSize();
        //TODO review
        if (size == BITS32) {
            write8(0x89);
            write8(srcReg.getNr() << 3 | 5);
            write32(dstDisp);
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            write8(0x89);
            write8(srcReg.getNr() << 3 | 5);
            write32(dstDisp);
        } else if (size == BITS8) {
            write8(0x88);
            write8(srcReg.getNr() << 3 | 5);
            write32(dstDisp);
        }
    }

    /**
     * @param operandSize
     * @param dstDisp
     * @param imm32
     */
    public final void writeMOV_Const(int operandSize, int dstDisp, int imm32) {
        testOperandSize(operandSize, BITS8 | BITS16 | BITS32);
        //TODO review
        if (operandSize == BITS32) {
            write8(0xc7);
            write8(5);
            write32(dstDisp);
            write32(imm32);
        } else if (operandSize == BITS16) {
            write8(OSIZE_PREFIX);
            write8(0xc7);
            write8(5);
            write32(dstDisp);
            write16(imm32);
        } else if (operandSize == BITS8) {
            write8(0xc6);
            write8(5);
            write32(dstDisp);
            write8(imm32);
        }
    }

    /**
     * Create a MOV reg,imm32 or MOV reg,imm64
     *
     * @param dstReg
     * @param imm32
     */
    public final void writeMOV_Const(GPR dstReg, int imm32) {
        testSize(dstReg, BITS8 | BITS16 | BITS32 | BITS64);
        if (dstReg.getSize() == BITS32) {
            write1bOpcodeReg(0xB8, dstReg);
            write32(imm32);
        } else if (dstReg.getSize() == BITS16) {
            write8(OSIZE_PREFIX);
            write1bOpcodeReg(0xB8, dstReg);
            write16(imm32);
        } else if (dstReg.getSize() == BITS8) {
            write1bOpcodeReg(0xB0, dstReg);
            write8(imm32);
        } else {
            writeMOV_Const(dstReg, (long) imm32);
        }
    }

    /**
     * Create a MOV reg,imm64 depending on the reg size. Only valid in 64-bit
     * mode.
     *
     * @param dstReg
     * @param imm64
     * @throws InvalidOpcodeException In 32-bit modes.
     */
    public void writeMOV_Const(GPR dstReg, long imm64)
        throws InvalidOpcodeException {
        if (!code64) {
            throw new InvalidOpcodeException();
        }
        testSize(dstReg, BITS64);
        write1bOpcodeReg(0xB8, dstReg);
        write64(imm64);
    }

    /**
     * Create a mov [destReg:destDisp], imm32
     *
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public final void writeMOV_Const(int operandSize, SR dstReg,
                                     int dstDisp, int imm32) {
        testOperandSize(operandSize, BITS32);
        writeSegPrefix(dstReg);
        write8(0xC7);
        write8(0x05);
        write32(dstDisp);
        write32(imm32);
    }

    /**
     * Create a mov [destReg+destDisp], imm32
     *
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public final void writeMOV_Const(int operandSize, GPR dstReg,
                                     int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
        testOperandSize(operandSize, BITS8 | BITS16 | BITS32 | BITS64);
        if (operandSize == BITS8) {
            write1bOpcodeModRM(0xC6, operandSize, dstReg, dstDisp, 0);
            write8(imm32);
        } else {
            if (operandSize == BITS16) {
                write8(OSIZE_PREFIX);
            }
            write1bOpcodeModRM(0xC7, operandSize, dstReg, dstDisp, 0);
            if (operandSize == BITS16) {
                write16(imm32);
            } else {
                write32(imm32);
            }
        }
    }

    /**
     * Create a mov reg, label
     *
     * @param dstReg
     * @param label
     */
    public final void writeMOV_Const(GPR dstReg, Object label) {
        testSize(dstReg, mode.getSize());
        testOperandSize(dstReg.getSize(), mode.getSize());
        write1bOpcodeReg(0xB8, dstReg);
        writeObjectRef(label, 0, false);
    }

    /**
     * Create a mov size [destReg+dstIdxReg*scale+destDisp], imm32
     *
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeMOV_Const(int operandSize, GPR dstReg, GPR dstIdxReg,
                               int scale, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
        testSize(dstIdxReg, mode.getSize());
        write1bOpcodeModRMSib(0xC7, operandSize, dstReg, dstDisp, 0, scale,
            dstIdxReg);
        write32(imm32);
    }

    //todo 64 bits support

    /**
     * @param operandSize
     * @param dstMmx
     * @param srcReg
     * @param srcDisp
     */
    public void writeMOVD(int operandSize, MMX dstMmx, GPR srcReg, int srcDisp) {
        testSize(srcReg, mode.getSize());
        final int opcode;
        switch (operandSize) {
            case X86Constants.BITS32:
//      case X86Constants.BITS64:
                opcode = 0x6E;
                break;
            default:
                throw new IllegalArgumentException("Invalid operandSize "
                    + operandSize);
        }
        write2bOpcodeModRM(0x0F, opcode, operandSize, srcReg, srcDisp, dstMmx.getNr());
    }

    //todo 64 bits support

    /**
     * @param operandSize
     * @param dstReg
     * @param dstDisp
     * @param srcMmx
     */
    public void writeMOVD(int operandSize, X86Register.GPR dstReg, int dstDisp, X86Register.MMX srcMmx) {
        testSize(dstReg, mode.getSize());
        final int opcode;
        switch (operandSize) {
            case X86Constants.BITS32:
//          case X86Constants.BITS64:
                opcode = 0x7E;
                break;
            default:
                throw new IllegalArgumentException("Invalid operandSize "
                    + operandSize);
        }
        write2bOpcodeModRM(0x0F, opcode, operandSize, dstReg, dstDisp, srcMmx.getNr());
    }

    //todo 64 bits support

    /**
     * @param dstMmx
     * @param srcMmx
     */
    public void writeMOVQ(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        writeModRR_MMX(0x6F, dstMmx, srcMmx);
    }

    //todo 64 bits support

    /**
     * @param operandSize
     * @param dstMmx
     * @param srcReg
     * @param srcDisp
     */
    public void writeMOVQ(int operandSize, X86Register.MMX dstMmx, X86Register.GPR srcReg, int srcDisp) {
        testSize(srcReg, mode.getSize());
        final int opcode;
        switch (operandSize) {
//      case X86Constants.BITS32:
            case X86Constants.BITS64:
                opcode = 0x6F;
                break;
            default:
                throw new IllegalArgumentException("Invalid operandSize "
                    + operandSize);
        }
        write8(0x0F);
        write8(opcode);
        writeModRM(srcReg.getNr() & 7, srcDisp, dstMmx.getNr() & 7);
    }

    //todo 64 bits support

    /**
     * @param operandSize
     * @param dstMmx
     * @param srcDisp
     */
    public void writeMOVQ(int operandSize, X86Register.MMX dstMmx, int srcDisp) {
        testOperandSize(mode.getSize(), X86Constants.BITS32);
        final int opcode;
        switch (operandSize) {
//      case X86Constants.BITS32:
            case X86Constants.BITS64:
                opcode = 0x6F;
                break;
            default:
                throw new IllegalArgumentException("Invalid operandSize "
                    + operandSize);
        }
        write8(0x0F);
        write8(opcode);
        write8(dstMmx.getNr() << 3 | 5);
        write32(srcDisp);
    }

    /**
     * Create a movsb
     */
    public void writeMOVSB() {
        write8(0xA4);
    }

    /**
     * Create a movsd
     */
    public void writeMOVSD() {
        write8(OSIZE_PREFIX);
        write8(0xA5);
    }

    /**
     * Create a movsd [dst+dstDisp],src
     *
     * @param dst
     * @param src
     */
    public void writeMOVSD(X86Register.GPR dst, int dstDisp, X86Register.XMM src) {
        write3bOpcodeModRM(0xF2, 0x0F, 0x11, 0, dst, dstDisp, src.getNr());
    }

    /**
     * Create a movsd dst,[src+srcDisp]
     *
     * @param dst
     * @param src
     */
    public void writeMOVSD(X86Register.XMM dst, X86Register.GPR src, int srcDisp) {
        write3bOpcodeModRM(0xF2, 0x0F, 0x10, 0, src, srcDisp, dst.getNr());
    }

    /**
     * Create a movsd dst,src
     *
     * @param dst
     * @param src
     */
    public void writeMOVSD(X86Register.XMM dst, X86Register.XMM src) {
        write3bOpcodeModRR(0xF2, 0x0F, 0x10, 0, dst, src.getNr());
    }

    /**
     * Create a movss [dst+dstDisp],src
     *
     * @param dst
     * @param src
     */
    public void writeMOVSS(X86Register.GPR dst, int dstDisp, X86Register.XMM src) {
        write3bOpcodeModRM(0xF3, 0x0F, 0x11, 0, dst, dstDisp, src.getNr());
    }

    /**
     * Create a movss dst,[src+srcDisp]
     *
     * @param dst
     * @param src
     */
    public void writeMOVSS(X86Register.XMM dst, X86Register.GPR src, int srcDisp) {
        write3bOpcodeModRM(0xF3, 0x0F, 0x10, 0, src, srcDisp, dst.getNr());
    }

    /**
     * Create a movss dst,src
     *
     * @param dst
     * @param src
     */
    public void writeMOVSS(X86Register.XMM dst, X86Register.XMM src) {
        write3bOpcodeModRR(0xF3, 0x0F, 0x10, 0, dst, src.getNr());
    }

    /**
     * Create a movsx <dstReg>, <srcReg>
     *
     * @param dstReg
     * @param srcReg
     * @param srcSize
     */
    public final void writeMOVSX(GPR dstReg, GPR srcReg, int srcSize) {
        if (srcSize == X86Constants.BITS8) {
            testSuitableForBits8(dstReg);
            write2bOpcodeModRR(0x0F, 0xBE, dstReg.getSize(), srcReg, dstReg
                .getNr());
        } else if (srcSize == X86Constants.BITS16) {
            write2bOpcodeModRR(0x0F, 0xBF, dstReg.getSize(), srcReg, dstReg
                .getNr());
        } else {
            throw new IllegalArgumentException("Unknown srcSize " + srcSize);
        }
    }

    public void writeMOVSX(GPR dstReg, GPR srcReg, int srcDisp, int srcSize) {
        if (srcSize == X86Constants.BITS8) {
            testSuitableForBits8(dstReg);
            write2bOpcodeModRM(0x0F, 0xBE, dstReg.getSize(), srcReg, srcDisp,
                dstReg.getNr());
        } else if (srcSize == X86Constants.BITS16) {
            write2bOpcodeModRM(0x0F, 0xBF, dstReg.getSize(), srcReg, srcDisp,
                dstReg.getNr());
        } else {
            throw new IllegalArgumentException("Unknown srcSize " + srcSize);
        }
    }

    /**
     * Create a movsxd dstReg, srcReg. Sign extends the srcReg to dstReg. Only
     * valid in 64-bit mode.
     *
     * @param dstReg
     * @param srcReg
     */
    public void writeMOVSXD(GPR64 dstReg, GPR32 srcReg)
        throws InvalidOpcodeException {
        if (!code64) {
            throw new InvalidOpcodeException();
        }
        write1bOpcodeModRR(0x63, dstReg.getSize(), srcReg, dstReg.getNr());
    }

    /**
     * Create a movsw
     */
    public void writeMOVSW() {
        write8(0xA5);
    }

    /**
     * Create a movzx <dstReg>, <srcReg>
     *
     * @param dstReg
     * @param srcReg
     * @param srcSize
     */
    public final void writeMOVZX(GPR dstReg, GPR srcReg, int srcSize) {
        if (srcSize == X86Constants.BITS8) {
            testSuitableForBits8(dstReg);
            write2bOpcodeModRR(0x0F, 0xB6, dstReg.getSize(), srcReg, dstReg
                .getNr());
        } else if (srcSize == X86Constants.BITS16) {
            write2bOpcodeModRR(0x0F, 0xB7, dstReg.getSize(), srcReg, dstReg
                .getNr());
        } else {
            throw new IllegalArgumentException("Unknown srcSize " + srcSize);
        }
    }

    public void writeMOVZX(GPR dstReg, GPR srcReg, int srcDisp, int srcSize) {
        if (srcSize == X86Constants.BITS8) {
            testSuitableForBits8(dstReg);
            write2bOpcodeModRM(0x0F, 0xB6, dstReg.getSize(), srcReg, srcDisp,
                dstReg.getNr());
        } else if (srcSize == X86Constants.BITS16) {
            write2bOpcodeModRM(0x0F, 0xB7, dstReg.getSize(), srcReg, srcDisp,
                dstReg.getNr());
        } else {
            throw new IllegalArgumentException("Unknown srcSize " + srcSize);
        }
    }

    /**
     * Create a mul eax, srcReg
     *
     * @param srcReg
     */
    public final void writeMUL_EAX(GPR srcReg) {
        write1bOpcodeModRR(0xF7, srcReg.getSize(), srcReg, 4);
    }

    /**
     * Create a neg dstReg
     *
     * @param dstReg
     */
    public final void writeNEG(GPR dstReg) {
        testSize(dstReg, BITS32 | BITS64);
        write1bOpcodeModRR(0xf7, dstReg.getSize(), dstReg, 3);
    }

    /**
     * Create a neg dword [dstReg+dstDisp]
     *
     * @param dstReg
     * @param dstDisp
     */
    public final void writeNEG(int operandSize, GPR dstReg, int dstDisp) {
        testSize(dstReg, mode.getSize());
        testOperandSize(operandSize, BITS32 | BITS64);
        write1bOpcodeModRM(0xf7, operandSize, dstReg, dstDisp, 3);
    }

    /**
     * Create a nop
     */
    public final void writeNOP() {
        write8(0x90);
    }

    /**
     * Create a not dstReg
     *
     * @param dstReg
     */
    public final void writeNOT(GPR dstReg) {
        write1bOpcodeModRR(0xf7, dstReg.getSize(), dstReg, 2);
    }

    /**
     * Create a not dword [dstReg+dstDisp]
     *
     * @param dstReg
     * @param dstDisp
     */
    public final void writeNOT(int operandSize, GPR dstReg, int dstDisp) {
        testOperandSize(operandSize, BITS32 | BITS64);
        write1bOpcodeModRM(0xf7, operandSize, dstReg, dstDisp, 2);
    }

    /**
     * Create 32-bit reference to an absolute address like: dd label
     *
     * @param object
     */
    public final void writeObjectRef(Object object) {
        writeObjectRef(object, 0, false);
    }

    /**
     * Create 32-bit reference to an absolute address like: dd label
     *
     * @param object
     */
    public final void setObjectRef(int offset, Object object) {
        setObjectRef(offset, object, 0, false);
    }

    /**
     * Create 32-bit reference to an absolute address like: dd label
     *
     * @param object
     * @param offset
     * @param rawAddress If true, object is a raw address, not a normal object.
     */
    private final void writeObjectRef(Object object, int offset,
                                      boolean rawAddress) {
        if (object == null) {
            writeWord(offset);
        } else if (rawAddress) {
            if (mode.is32()) {
                write32(resolver.addressOf32(object) + offset);
            } else {
                write64(resolver.addressOf64(object) + offset);
            }
        } else if ((resolver != null) && (!(object instanceof Label))) {
            if (mode.is32()) {
                write32(resolver.addressOf32(object) + offset);
            } else {
                write64(resolver.addressOf64(object) + offset);
            }
        } else {
            final X86ObjectRef ref = (X86ObjectRef) getObjectRef(object);
            if (ref.isResolved()) {
                try {
                    //System.out.println("Resolved offset " + ref.getOffset());
                    writeWord(ref.getOffset() + baseAddr + offset);
                } catch (UnresolvedObjectRefException e) {
                    throw new RuntimeException(e);
                }
            } else {
                //System.out.println("Unresolved");
                ref.addUnresolvedLink(m_used, getWordSize());
                writeWord(-(baseAddr + offset));
            }
        }
    }

    /**
     * Create 32-bit reference to an absolute address like: dd label
     *
     * @param object
     * @param offset
     * @param rawAddress If true, object is a raw address, not a normal object.
     */
    private final void setObjectRef(int dataOffset, Object object, int offset,
                                    boolean rawAddress) {
        if (object == null) {
            setWord(dataOffset, offset);
        } else if (rawAddress) {
            if (mode.is32()) {
                set32(dataOffset, resolver.addressOf32(object) + offset);
            } else {
                set64(dataOffset, resolver.addressOf64(object) + offset);
            }
        } else if ((resolver != null) && (!(object instanceof Label))) {
            if (mode.is32()) {
                set32(dataOffset, resolver.addressOf32(object) + offset);
            } else {
                set64(dataOffset, resolver.addressOf64(object) + offset);
            }
        } else {
            final X86ObjectRef ref = (X86ObjectRef) getObjectRef(object);
            if (ref.isResolved()) {
                try {
                    //System.out.println("Resolved offset " + ref.getOffset());
                    setWord(dataOffset, ref.getOffset() + baseAddr + offset);
                } catch (UnresolvedObjectRefException e) {
                    throw new RuntimeException(e);
                }
            } else {
                //System.out.println("Unresolved");
                ref.addUnresolvedLink(dataOffset, getWordSize());
                setWord(dataOffset, -(baseAddr + offset));
            }
        }
    }

    /**
     * @param dstReg
     * @param imm32
     */
    public void writeOR(GPR dstReg, int imm32) {
        testSize(dstReg, BITS8 | BITS16 | BITS32 | BITS64);
        int size = dstReg.getSize();
        if ((size & (BITS32 | BITS64)) == size) {
            if (isByte(imm32)) {
                write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 1);
                write8(imm32);
            } else {
                write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 1);
                write32(imm32);
            }
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            if (isByte(imm32)) {
                write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 1);
                write8(imm32);
            } else {
                write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 1);
                write16(imm32);
            }
        } else if (size == BITS8) {
            write1bOpcodeModRR(0x80, dstReg.getSize(), dstReg, 1);
            write8(imm32);
        }
    }

    /**
     * @param operandSize
     * @param dstDisp
     * @param imm32
     */
    public void writeOR(int operandSize, int dstDisp, int imm32) {
        testOperandSize(operandSize, BITS8 | BITS16 | BITS32);
        //TODO review
        if (operandSize == BITS32) {
            write8(0x81);
            write8((1 << 3) | 5);
            write32(dstDisp);
            write32(imm32);
        } else if (operandSize == BITS16) {
            write8(OSIZE_PREFIX);
            write8(0x81);
            write8((1 << 3) | 5);
            write32(dstDisp);
            write16(imm32);
        } else if (operandSize == BITS8) {
            write8(0x80);
            write8((1 << 3) | 5);
            write32(dstDisp);
            write8(imm32);
        }
    }

    /**
     * @param operandSize
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeOR(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
        if (isByte(imm32)) {
            write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 1);
            write8(imm32);
        } else {
            write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 1);
            write32(imm32);
        }
    }

    /**
     * @param operandSize
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeOR(int operandSize, SR dstReg, int dstDisp, int imm32) {
        testOperandSize(operandSize, BITS32);
        writeSegPrefix(dstReg);
        write8(0x81);
        write8(0x0D);
        write32(dstDisp);
        write32(imm32);
    }

    /**
     * Create a OR [dstReg+dstDisp], srcReg
     *
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public final void writeOR(GPR dstReg, int dstDisp, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, BITS8 | BITS16 | BITS32 | BITS64);
        int size = srcReg.getSize();
        if ((size & (BITS32 | BITS64)) == size) {
            write1bOpcodeModRM(0x09, size, dstReg, dstDisp, srcReg.getNr());
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            write1bOpcodeModRM(0x09, size, dstReg, dstDisp, srcReg.getNr());
        } else if (size == BITS8) {
            write1bOpcodeModRM(0x08, size, dstReg, dstDisp, srcReg.getNr());
        }
    }

    /**
     * Create a OR dstReg, srcReg
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeOR(GPR dstReg, GPR srcReg) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        write1bOpcodeModRR(0x09, dstReg.getSize(), dstReg, srcReg.getNr());
    }

    /**
     * Create a OR dstReg, [srcReg+srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeOR(GPR dstReg, GPR srcReg, int srcDisp) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        write1bOpcodeModRM(0x0B, dstReg.getSize(), srcReg, srcDisp, dstReg
            .getNr());
    }

    public void writeOUT(int operandSize) {
        if (operandSize == X86Constants.BITS8) {
            write8(0xEE);
        } else if (operandSize == X86Constants.BITS16) {
            write8(0xEF);
        } else if (operandSize == X86Constants.BITS32) {
            write8(X86Constants.OSIZE_PREFIX);
            write8(0xEF);
        } else {
            throw new IllegalArgumentException("Invalid operand size for OUT: " + operandSize);
        }
    }

    public void writeOUT(int operandSize, int imm8) {
        if (operandSize == X86Constants.BITS8) {
            write8(0xE6);
            write8(imm8);
        } else if (operandSize == X86Constants.BITS16) {
            write8(0xE7);
            write8(imm8);
        } else if (operandSize == X86Constants.BITS32) {
            write8(X86Constants.OSIZE_PREFIX);
            write8(0xE7);
            write8(imm8);
        } else {
            throw new IllegalArgumentException("Invalid operand size for OUT: " + operandSize);
        }
    }

    public void writePACKUSWB(MMX dstMmx, MMX srcMmx) {
        writeModRR_MMX(0x67, dstMmx, srcMmx);
    }

    public void writePADDW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        writeModRR_MMX(0xFD, dstMmx, srcMmx);
    }

    public void writePAND(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        writeModRR_MMX(0xDB, dstMmx, srcMmx);
    }

    public void writePCMPGTW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        writeModRR_MMX(0x65, dstMmx, srcMmx);
    }

    public void writePMULLW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        writeModRR_MMX(0xD5, dstMmx, srcMmx);
    }

    /**
     * Create a pop reg32
     *
     * @param dstReg
     */
    public final void writePOP(GPR dstReg) {
        testSize(dstReg, BITS32 | BITS64);
        write1bOpcodeReg(0x58, dstReg);
    }

    /**
     * Create a pop sreg
     *
     * @param dstReg
     */
    public final void writePOP(SR dstReg) {
        if (X86Register.ES.equals(dstReg)) {
            write8(0x07);
        } else if (X86Register.DS.equals(dstReg)) {
            write8(0x1F);
        } else if (X86Register.FS.equals(dstReg)) {
            write8(0x0F);
            write8(0xA1);
        } else if (X86Register.GS.equals(dstReg)) {
            write8(0x0F);
            write8(0xA9);
        } else if (X86Register.SS.equals(dstReg)) {
            write8(0x17);
        } else if (X86Register.CS.equals(dstReg)) {
            throw new IllegalArgumentException("Cannot POP to CS");
        } else {
            throw new IllegalArgumentException("Unknown segment register: " + dstReg);
        }
    }

    /**
     * Create a pop dword [reg32+disp]
     *
     * @param dstReg
     * @param dstDisp
     */
    public final void writePOP(GPR dstReg, int dstDisp) {
        testSize(dstReg, mode.getSize());
        // POP has no encoding for 32-bit in 64-bit mode, so give
        // operand size 0 to avoid a REX prefix.
        write1bOpcodeModRM(0x8f, 0, dstReg, dstDisp, 0);
    }

    public void writePOPA() {
        if (code64) {
            throw new InvalidOpcodeException();
        }
        write8(0x61);
    }

    public void writePOPF() {
        write8(0x9D);
    }

    public void writePrefix(int prefix) {
        write8(prefix);
    }

    public void writePSHUFW(MMX dstMmx, MMX srcMmx, int imm8) {
        writeModRR_MMX(0x70, dstMmx, srcMmx);
        write8(imm8);
    }

    public void writePSRLW(X86Register.MMX mmx, int imm8) {
        write8(0x0F);
        write8(0x71);
        writeModRR(mmx.getNr() & 7, 2);
        write8(imm8);
    }

    public void writePSUBW(MMX dstMmx, MMX srcMmx) {
        writeModRR_MMX(0xF9, dstMmx, srcMmx);
    }

    public void writePUNPCKLBW(MMX dstMmx, MMX srcMmx) {
        writeModRR_MMX(0x60, dstMmx, srcMmx);
    }

    /**
     * Create a push dword imm32
     *
     * @param imm32
     * @return The ofset of the start of the instruction.
     */
    public final int writePUSH(int imm32) {
        final int rc = m_used;
        if (isByte(imm32)) {
            write8(0x6A); // PUSH imm8
            write8(imm32);
        } else {
            write8(0x68); // PUSH imm32
            write32(imm32);
        }
        return rc;
    }

    /**
     * Create a push srcReg
     *
     * @param srcReg
     * @return The ofset of the start of the instruction.
     */
    public final int writePUSH(GPR srcReg) {
        testSize(srcReg, BITS32 | BITS64);
        final int rc = m_used;
        write1bOpcodeReg(0x50, srcReg);
        return rc;
    }

    /**
     * Create a push srcReg
     *
     * @param srcReg
     * @return The ofset of the start of the instruction.
     */
    public final int writePUSH(SR srcReg) {
        final int rc = m_used;
        if (X86Register.ES.equals(srcReg)) {
            write8(0x06);
        } else if (X86Register.DS.equals(srcReg)) {
            write8(0x1E);
        } else if (X86Register.FS.equals(srcReg)) {
            write8(0x0F);
            write8(0xA0);
        } else if (X86Register.GS.equals(srcReg)) {
            write8(0x0F);
            write8(0xA8);
        } else if (X86Register.SS.equals(srcReg)) {
            write8(0x16);
        } else if (X86Register.CS.equals(srcReg)) {
            write8(0x0E);
        } else {
            throw new IllegalArgumentException("Unknown segment register: " + srcReg);
        }
        return rc;
    }

    /**
     * Create a push d/qword [srcReg+srcDisp]
     *
     * @param srcReg
     * @param srcDisp
     * @return The ofset of the start of the instruction.
     */
    public final int writePUSH(GPR srcReg, int srcDisp) {
        testSize(srcReg, mode.getSize());
        // PUSH has not encoding for 32-bit in 64-bit mode, so give
        // operand size 0 to avoid a REX prefix.
        final int rc = m_used;
        write1bOpcodeModRM(0xFF, 0, srcReg, srcDisp, 6);
        return rc;
    }

    /**
     * Create a push d/qword [srcReg+srcDisp]
     *
     * @param sr
     * @param srcDisp
     * @return The ofset of the start of the instruction.
     */
    public final int writePUSH(SR sr, int srcDisp) {
        testOperandSize(4, mode.getSize());
        // PUSH has not encoding for 32-bit in 64-bit mode, so give
        // operand size 0 to avoid a REX prefix.
        final int rc = m_used;
        writeSegPrefix(sr);
        write8(0xFF);
        write8(0x35);
        write32(srcDisp);
        return rc;
    }

    /**
     * Create a push d/qword [baseReg+indexReg*scale+disp]
     *
     * @param srcBaseReg
     * @param srcIndexReg
     * @param srcScale
     * @param srcDisp
     * @return The ofset of the start of the instruction.
     */
    public final int writePUSH(GPR srcBaseReg, GPR srcIndexReg, int srcScale,
                               int srcDisp) {
        testSize(srcBaseReg, mode.getSize());
        testSize(srcIndexReg, mode.getSize());
        // PUSH has not encoding for 32-bit in 64-bit mode, so give
        // operand size 0 to avoid a REX prefix.
        final int rc = m_used;
        write1bOpcodeModRMSib(0xFF, 0, srcBaseReg, srcDisp, 6, srcScale,
            srcIndexReg);
        return rc;
    }

    /**
     * Create a push dword <object>
     *
     * @param objRef
     * @return The offset of the start of the instruction.
     */
    public final int writePUSH_Const(Object objRef) {
        if (code64) {
            throw new InvalidOpcodeException("Not support in 64-bit mode");
        }
        final int rc = m_used;
        write8(0x68); // PUSH imm32
        writeObjectRef(objRef, 0, false);
        return rc;
    }

    public void writePUSHA() {
        if (code64) {
            throw new InvalidOpcodeException();
        }
        write8(0x60);
    }

    public void writePUSHF() {
        write8(0x9C);
    }

    public void writePXOR(MMX dstMmx, MMX srcMmx) {
        writeModRR_MMX(0xEF, dstMmx, srcMmx);
    }

    public void writeRDTSC() {
        write8(0x0F);
        write8(0x31);
    }

    /**
     * Create 32-bit offset relative to the current (after this offset) offset.
     *
     * @param object
     */
    public final void writeRelativeObjectRef(Label object) {
        if (object == null) {
            throw new NullPointerException();
        }

        final int ofs = m_used + 4;
        final X86ObjectRef ref = (X86ObjectRef) getObjectRef(object);
        ref.setRelJump();
        if (ref.isResolved()) {
            try {
                write32(ref.getOffset() - ofs);
            } catch (UnresolvedObjectRefException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            ref.addUnresolvedLink(m_used, 4);
            write32(ofs);
        }
    }

    /**
     * Create a ret near to caller
     */
    public final void writeRET() {
        write8(0xc3);
    }

    /**
     * Create a ret imm16 near to caller
     *
     * @param imm16
     */
    public final void writeRET(int imm16) {
        write8(0xc2);
        write16(imm16);
    }

    /**
     * Create a sahf
     */
    public final void writeSAHF() {
        if (code64) {
            throw new InvalidOpcodeException();
        }
        write8(0x9e);
    }

    /**
     * Create a SAL dstReg,imm8
     *
     * @param dstReg
     * @param imm8
     */
    public final void writeSAL(GPR dstReg, int imm8) {
        write1bOpcodeModRR(0xc1, dstReg.getSize(), dstReg, 4);
        write8(imm8);
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm8
     */
    public void writeSAL(int operandSize, GPR dstReg, int dstDisp, int imm8) {
        write1bOpcodeModRM(0xc1, operandSize, dstReg, dstDisp, 4);
        write8(imm8);
    }

    /**
     * Create a SAL dstReg,cl
     *
     * @param dstReg
     */
    public final void writeSAL_CL(GPR dstReg) {
        write1bOpcodeModRR(0xd3, dstReg.getSize(), dstReg, 4);
    }

    /**
     * @param dstReg
     * @param dstDisp
     */
    public void writeSAL_CL(int operandSize, GPR dstReg, int dstDisp) {
        write1bOpcodeModRM(0xd3, operandSize, dstReg, dstDisp, 4);
    }

    /**
     * Create a SAR dstReg,imm8
     *
     * @param dstReg
     * @param imm8
     */
    public final void writeSAR(GPR dstReg, int imm8) {
        write1bOpcodeModRR(0xc1, dstReg.getSize(), dstReg, 7);
        write8(imm8);
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm8
     */
    public void writeSAR(int operandSize, GPR dstReg, int dstDisp, int imm8) {
        write1bOpcodeModRM(0xc1, operandSize, dstReg, dstDisp, 7);
        write8(imm8);
    }

    /**
     * Create a SAR dstReg,cl
     *
     * @param dstReg
     */
    public final void writeSAR_CL(GPR dstReg) {
        write1bOpcodeModRR(0xd3, dstReg.getSize(), dstReg, 7);
    }

    /**
     * @param dstReg
     * @param dstDisp
     */
    public void writeSAR_CL(int operandSize, GPR dstReg, int dstDisp) {
        write1bOpcodeModRM(0xd3, operandSize, dstReg, dstDisp, 7);
    }

    /**
     * Create a SBB dstReg, imm32
     *
     * @param dstReg
     * @param imm32
     */
    public void writeSBB(GPR dstReg, int imm32) {
        if (isByte(imm32)) {
            write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 3);
            write8(imm32);
        } else {
            write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 3);
            write32(imm32);
        }
    }

    /**
     * Create a SBB dword [dstReg+dstDisp], <imm32>
     *
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public final void writeSBB(int operandSize, GPR dstReg, int dstDisp,
                               int imm32) {
        if (isByte(imm32)) {
            write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 3);
            write8(imm32);
        } else {
            write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 3);
            write32(imm32);
        }
    }

    /**
     * Create a SBB [dstReg+dstDisp], srcReg
     *
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public final void writeSBB(GPR dstReg, int dstDisp, GPR srcReg) {
        write1bOpcodeModRM(0x19, srcReg.getSize(), dstReg, dstDisp, srcReg
            .getNr());
    }

    /**
     * Create a SBB dstReg, srcReg
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeSBB(GPR dstReg, GPR srcReg) {
        write1bOpcodeModRR(0x19, dstReg.getSize(), dstReg, srcReg.getNr());
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeSBB(GPR dstReg, GPR srcReg, int srcDisp) {
        write1bOpcodeModRM(0x1B, dstReg.getSize(), srcReg, srcDisp, dstReg
            .getNr());
    }

    private final void writeSegPrefix(SR reg) {
        if (reg.equals(DS)) {
            write8(0x3e);
        } else if (reg.equals(ES)) {
            write8(0x26);
        } else if (reg.equals(FS)) {
            write8(0x64);
        } else if (reg.equals(GS)) {
            write8(0x65);
        } else if (reg.equals(SS)) {
            write8(0x36);
        } else if (reg.equals(CS)) {
            write8(0x2e);
        } else {
            throw new IllegalArgumentException("Unsopported segment register: " + reg);
        }
    }

    /**
     * Create a SETcc dstReg
     *
     * @param dstReg
     * @param cc
     */
    public void writeSETCC(GPR dstReg, int cc) {
        testSuitableForBits8(dstReg);
        // No change in 64-bit encoding, so give operand size 0 to
        // avoid a REX prefix.
        write2bOpcodeModRR(0x0F, 0x90 + (cc & 0x0f), 0, dstReg, 0);
    }

    /**
     * Create a SHL dstReg,imm8
     *
     * @param dstReg
     * @param imm8
     */
    public final void writeSHL(GPR dstReg, int imm8) {
        testSize(dstReg, mode.getSize());
        write1bOpcodeModRR(0xc1, dstReg.getSize(), dstReg, 4);
        write8(imm8);
    }

    public void writeSHL(int operandSize, GPR dstReg, int dstDisp, int imm8) {
        testSize(dstReg, mode.getSize());
        write1bOpcodeModRM(0xc1, operandSize, dstReg, dstDisp, 4);
        write8(imm8);
    }

    /**
     * Create a SHL dstReg,cl
     *
     * @param dstReg
     */
    public final void writeSHL_CL(GPR dstReg) {
        testSize(dstReg, mode.getSize());
        write1bOpcodeModRR(0xd3, dstReg.getSize(), dstReg, 4);
    }

    public void writeSHL_CL(int operandSize, GPR dstReg, int dstDisp) {
        testSize(dstReg, mode.getSize());
        write1bOpcodeModRM(0xd3, operandSize, dstReg, dstDisp, 4);
    }

    /**
     * Create a SHLD dstReg,srcReg,cl
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeSHLD_CL(GPR dstReg, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        write2bOpcodeModRR(0x0F, 0xa5, dstReg.getSize(), dstReg, srcReg.getNr());
    }

    /**
     * Create a SHL dstReg,imm8
     *
     * @param dstReg
     * @param imm8
     */
    public final void writeSHR(GPR dstReg, int imm8) {
        testSize(dstReg, BITS32 | BITS64);
        write1bOpcodeModRR(0xc1, dstReg.getSize(), dstReg, 5);
        write8(imm8);
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm8
     */
    public void writeSHR(int operandSize, GPR dstReg, int dstDisp, int imm8) {
        testSize(dstReg, mode.getSize());
        write1bOpcodeModRM(0xc1, operandSize, dstReg, dstDisp, 5);
        write8(imm8);
    }

    /**
     * Create a SHR dstReg,cl
     *
     * @param dstReg
     */
    public final void writeSHR_CL(GPR dstReg) {
        testSize(dstReg, BITS32 | BITS64);
        write1bOpcodeModRR(0xd3, dstReg.getSize(), dstReg, 5);
    }

    /**
     * @param dstReg
     * @param dstDisp
     */
    public void writeSHR_CL(int operandSize, GPR dstReg, int dstDisp) {
        testSize(dstReg, mode.getSize());
        write1bOpcodeModRM(0xd3, operandSize, dstReg, dstDisp, 5);
    }

    /**
     * Create a SHRD dstReg,srcReg,cl
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeSHRD_CL(GPR dstReg, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        write2bOpcodeModRR(0x0F, 0xad, dstReg.getSize(), dstReg, srcReg.getNr());
    }

    /**
     *
     */
    public void writeSTD() {
        write8(0xFD);
    }

    /**
     *
     */
    public void writeSTI() {
        write8(0xFB);
    }

    /**
     * Create a stmxcsr dword [srcReg+disp]
     *
     * @param srcReg
     * @param disp
     */
    public final void writeSTMXCSR(GPR srcReg, int disp) {
        write8(0x0f);
        write8(0xae);
        writeModRM(srcReg.getNr() & 7, disp, 3);
    }

    /**
     *
     */
    public void writeSTOSB() {
        write8(0xAA);
    }

    /**
     *
     */
    public void writeSTOSD() {
        write8(0xAB);
    }

    /**
     *
     */
    public void writeSTOSW() {
        write8(OSIZE_PREFIX);
        write8(0xAB);
    }

    /**
     * Create a SUB reg, imm32
     *
     * @param reg
     * @param imm32
     */
    public final void writeSUB(GPR reg, int imm32) {
        testSize(reg, BITS32 | BITS64);
        if (isByte(imm32)) {
            write1bOpcodeModRR(0x83, reg.getSize(), reg, 5);
            write8(imm32);
        } else {
            write1bOpcodeModRR(0x81, reg.getSize(), reg, 5);
            write32(imm32);
        }
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeSUB(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
        if (isByte(imm32)) {
            write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 5);
            write8(imm32);
        } else {
            write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 5);
            write32(imm32);
        }
    }

    /**
     * Create a SUB [dstDisp], <srcReg>
     *
     * @param dstDisp
     * @param srcReg
     */
    public final void writeSUB(int dstDisp, GPR srcReg) {
        testSize(srcReg, BITS8 | BITS16 | BITS32);
        int size = srcReg.getSize();
        //TODO review//TODO review
        if (size == BITS32) {
            write8(0x29);
            write8(srcReg.getNr() << 3 | 5);
            write32(dstDisp);
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            write8(0x29);
            write8(srcReg.getNr() << 3 | 5);
            write32(dstDisp);
        } else if (size == BITS8) {
            write8(0x28);
            write8(srcReg.getNr() << 3 | 5);
            write32(dstDisp);
        }
    }

    /**
     * Create a SUB [dstReg+dstDisp], <srcReg>
     *
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public final void writeSUB(GPR dstReg, int dstDisp, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        write1bOpcodeModRM(0x29, srcReg.getNr(), dstReg, dstDisp, srcReg
            .getNr());
    }

    /**
     * Create a SUB dstReg, srcReg
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeSUB(GPR dstReg, GPR srcReg) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        write1bOpcodeModRR(0x29, dstReg.getSize(), dstReg, srcReg.getNr());
    }

    /**
     * Create a SUB dstReg, [srcReg+srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeSUB(GPR dstReg, GPR srcReg, int srcDisp) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        write1bOpcodeModRM(0x2B, dstReg.getSize(), srcReg, srcDisp, dstReg
            .getNr());
    }

    /**
     * Create a TEST reg, imm32
     *
     * @param reg
     * @param imm32
     */
    public final void writeTEST(GPR reg, int imm32) {
        testSize(reg, BITS8 | BITS16 | BITS32 | BITS64);
        int size = reg.getSize();
        if ((size & (BITS32 | BITS64)) == size) {
            write1bOpcodeModRR(0xF7, reg.getSize(), reg, 0);
            write32(imm32);
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            write1bOpcodeModRR(0xF7, reg.getSize(), reg, 0);
            write16(imm32);
        } else if (size == BITS8) {
            write1bOpcodeModRR(0xF6, reg.getSize(), reg, 0);
            write8(imm32);
        }
    }

    /**
     * Create a TEST [reg+disp], imm32
     *
     * @param reg
     * @param disp
     * @param imm32
     */
    public void writeTEST(int operandSize, GPR reg, int disp, int imm32) {
        testSize(reg, BITS32 | BITS64);
        testOperandSize(operandSize, BITS32 | BITS64);
        write1bOpcodeModRM(0xF7, operandSize, reg, disp, 0);
        write32(imm32);
    }

    /**
     * Create a TEST [reg+disp], imm32
     *
     * @param reg
     * @param disp
     * @param imm32
     */
    public void writeTEST(int operandSize, SR reg, int disp, int imm32) {
        testOperandSize(operandSize, BITS32);
        writeSegPrefix(reg);
        write8(0xF7);
        write8(0x05);
        write32(disp);
        write32(imm32);
    }

    /**
     * Create a TEST reg1, reg2
     *
     * @param reg1
     * @param reg2
     */
    public void writeTEST(GPR reg1, GPR reg2) {
        final int size = reg1.getSize();
        if (size != reg2.getSize()) {
            throw new IllegalArgumentException("Operand size mismatch");
        }

        testOperandSize(size, BITS8 | BITS16 | BITS32 | BITS64);
        if (size == BITS32 || size == BITS64) {
            write1bOpcodeModRR(0x85, reg1.getSize(), reg1, reg2.getNr());
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            write1bOpcodeModRR(0x85, reg1.getSize(), reg1, reg2.getNr());
        } else if (size == BITS8) {
            write1bOpcodeModRR(0x84, reg1.getSize(), reg1, reg2.getNr());
        }
    }

    /**
     * Create a TEST al, imm8
     *
     * @param value
     */
    public final void writeTEST_AL(int value) {
        write8(0xa8);
        write8(value);
    }

    /**
     * Create a TEST eax,imm32 or TEST rax,imm32
     *
     * @param value
     */
    public final void writeTEST_EAX(int operandSize, int value) {
        testOperandSize(operandSize, BITS32 | BITS64);
        write1bOpcodeREXPrefix(operandSize, 0);
        write8(0xa9);
        write32(value);
    }

    public final void writeTEST(int operandSize, int destDisp, int imm32) {
        testOperandSize(operandSize, BITS8 | BITS16 | BITS32);
        //TODO review
        if (operandSize == BITS32) {
            write8(0xf7);
            write8(5);
            write32(destDisp);
            write32(imm32);
        } else if (operandSize == BITS16) {
            write8(OSIZE_PREFIX);
            write8(0xf7);
            write8(5);
            write32(destDisp);
            write16(imm32);
        } else if (operandSize == BITS8) {
            write8(0xf6);
            write8(5);
            write32(destDisp);
            write8(imm32);
        }
    }

    /**
     * Write my contents to the given stream.
     *
     * @param os
     * @throws IOException
     */
    public final void writeTo(OutputStream os) throws IOException {
        os.write(m_data, 0, m_used);
    }

    /**
     * @param dstDisp
     * @param srcReg
     */
    public void writeXCHG(int dstDisp, GPR srcReg) {
        testSize(srcReg, BITS8 | BITS16 | BITS32);
        int size = srcReg.getSize();
        //TODO review
        if (size == BITS32) {
            write8(0x87);
            write8(srcReg.getNr() << 3 | 5);
            write32(dstDisp);
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            write8(0x87);
            write8(srcReg.getNr() << 3 | 5);
            write32(dstDisp);
        } else if (size == BITS8) {
            write8(0x86);
            write8(srcReg.getNr() << 3 | 5);
            write32(dstDisp);
        }
    }

    public void writeXCHG(SR dstReg, int dstDisp, GPR srcReg) {
        testSize(srcReg, mode.getSize());
        writeSegPrefix(dstReg);
        write8(0x87);
        write8(0x05 | srcReg.getNr() << 3);
        write32(dstDisp);
    }

    public void writeXCHG(GPR dstReg, int dstDisp, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        write1bOpcodeModRM(0x87, srcReg.getSize(), dstReg, dstDisp, srcReg
            .getNr());
    }

    public void writeXCHG(GPR dstReg, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        if (dstReg == X86Register.EAX) {
            write8(0x90 + srcReg.getNr());
        } else if (srcReg == X86Register.EAX) {
            write8(0x90 + dstReg.getNr());
        } else {
            write1bOpcodeModRR(0x87, dstReg.getSize(), dstReg, srcReg.getNr());
        }
    }

    /**
     * @param dstReg
     * @param imm32
     */
    public void writeXOR(GPR dstReg, int imm32) {
        testSize(dstReg, BITS32 | BITS64);
        if (isByte(imm32)) {
            write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 6);
            write8(imm32);
        } else {
            write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 6);
            write32(imm32);
        }
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeXOR(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
        if (isByte(imm32)) {
            write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 6);
            write8(imm32);
        } else {
            write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 6);
            write32(imm32);
        }
    }

    /**
     * Create a XOR [dstReg+dstDisp], srcReg
     *
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public final void writeXOR(GPR dstReg, int dstDisp, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, BITS8 | BITS16 | BITS32 | BITS64);
        int size = srcReg.getSize();
        if ((size & (BITS32 | BITS64)) == size) {
            write1bOpcodeModRM(0x31, size, dstReg, dstDisp, srcReg.getNr());
        } else if (size == BITS16) {
            write8(OSIZE_PREFIX);
            write1bOpcodeModRM(0x31, size, dstReg, dstDisp, srcReg.getNr());
        } else if (size == BITS8) {
            write1bOpcodeModRM(0x30, size, dstReg, dstDisp, srcReg.getNr());
        }
    }

    /**
     * Create a XOR dstReg, srcReg
     *
     * @param dstReg
     * @param srcReg
     */
    public final void writeXOR(GPR dstReg, GPR srcReg) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        write1bOpcodeModRR(0x31, dstReg.getSize(), dstReg, srcReg.getNr());
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeXOR(GPR dstReg, GPR srcReg, int srcDisp) {
        testSize(dstReg, BITS32 | BITS64);
        testSize(srcReg, BITS32 | BITS64);
        write1bOpcodeModRM(0x33, dstReg.getSize(), srcReg, srcDisp, dstReg
            .getNr());
    }

    public void writeRDMSR() {
        write8(0x0F);
        write8(0x32);
    }

    public void writeWRMSR() {
        write8(0x0F);
        write8(0x30);
    }

    /**
     * Does the given value fit in an 8-bit signed byte?
     *
     * @param value
     * @return boolean
     */
    public boolean isByte(int value) {
        return byteValueEnabled && X86Utils.isByte(value);
    }

    public boolean isByteValueEnabled() {
        return byteValueEnabled;
    }

    public void setByteValueEnabled(boolean byteValueEnabled) {
        this.byteValueEnabled = byteValueEnabled;
    }
}
