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

package org.jnode.assembler.x86;
 
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.jnode.assembler.BootImageNativeStream;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR32;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.assembler.x86.X86Register.CRX;
import org.jnode.assembler.x86.X86Register.XMM;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.X86CpuID;

/**
 * Implementation of AbstractX86Stream.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 * @author Patrik Reali (patrik_reali@users.sourceforge.net)
 */
public class X86BinaryAssembler extends X86Assembler implements X86Constants,
		BootImageNativeStream, X86Operation {
	public static final class Key {

		private final Object key;

		public Key(Object key) {
			this.key = key;
		}

		/**
		 * @param obj
		 * @see java.lang.Object#equals(java.lang.Object)
		 * @return True if obj is equal to this, false otherwise
		 */
		public final boolean equals(Object obj) {
			/*
			 * if (!(obj instanceof Key)) { return false;
			 */
			obj = ((Key) obj).key;
			if (this.key instanceof Label) {
				return key.equals(obj);
			} else {
				return (obj == this.key);
			}
		}

		/**
		 * @see java.lang.Object#hashCode()
		 * @return The hashcode
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

	public final class UnresolvedOffset {
		private final int offset;

		private final int patchSize;

		public UnresolvedOffset(int offset, int patchSize) {
			if ((patchSize != 4) && (patchSize != 8)) {
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

	public class X86ObjectRef extends NativeStream.ObjectRef {

		private int dataOffset;

		private boolean isPublic;

		private boolean isRelJump;

		private LinkedList unresolvedLinks; // Array of data_offsets where

		public X86ObjectRef(Object object) {
			super(object);
			this.dataOffset = -1;
			this.unresolvedLinks = null;
			this.isPublic = false;
			this.isRelJump = false;
		}

		public void addUnresolvedLink(int offset, int patchSize) {
			if (unresolvedLinks == null) {
				unresolvedLinks = new LinkedList();
			}
			unresolvedLinks.add(new UnresolvedOffset(offset, patchSize));
		}

		public int getOffset() throws UnresolvedObjectRefException {
			if (!isResolved()) {
				throw new UnresolvedObjectRefException("Unresolved object: "
						+ this);
			}
			return dataOffset;
		}

		public int[] getUnresolvedOffsets() {
			int cnt = unresolvedLinks.size();
			int[] offsets = new int[cnt];
			int ofs = 0;
			for (Iterator i = unresolvedLinks.iterator(); i.hasNext(); ofs++) {
				offsets[ofs] = ((UnresolvedOffset) i.next()).getOffset();
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
		 * @throws UnresolvedObjectRefException
		 *             The given objectref is not resolved.
		 */
		public void link(ObjectRef objectRef)
				throws UnresolvedObjectRefException {
			if (!objectRef.isResolved()) {
				throw new UnresolvedObjectRefException(objectRef.getObject()
						.toString());
			}
			setOffset(objectRef.getOffset());
		}

		public void setOffset(int offset) {
			if (this.dataOffset != -1) {
				if ("".equals(getObject().toString())) {
					return;
				}
				throw new RuntimeException(
						"Offset is already set. Duplicate labels? ("
								+ getObject() + ")");
			}
			if (offset < 0) {
				throw new IllegalArgumentException("Offset: " + offset);
			}
			this.dataOffset = offset;
			if (unresolvedLinks != null) {
				// Link all unresolved links
				for (Iterator i = unresolvedLinks.iterator(); i.hasNext();) {
					final UnresolvedOffset unrOfs = (UnresolvedOffset) i.next();
					final int addr = unrOfs.getOffset();
					if (unrOfs.getPatchSize() == 4) {
						resolve32(addr, offset);
					} else {
						resolve64(addr, offset);
					}
				}
				unresolvedLinks = null;
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
				if (get8(addr - 1) == 0xe9) // JMP
				{
					set8(addr - 1, 0x90); // NOP
					set32(addr, 0x90909090); // 4 NOP's
				} else if (get8(addr - 2) == 0x0f) // Jcc
				{
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

	private Map objectRefs; // Integer(labelnr),Integer(offset)

	private ObjectResolver resolver;

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
		this.m_data = new byte[0];
		this.m_used = 0;
		this.objectRefs.clear();
	}

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
			objectRefs = new HashMap(initialObjectRefsCapacity);
		}
		Key key = new Key(keyObj);
		ObjectRef ref = (ObjectRef) objectRefs.get(key);
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
	public final Collection getObjectRefs() {
		if (objectRefs == null) {
			objectRefs = new HashMap(initialObjectRefsCapacity);
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
	public final Collection getUnresolvedObjectRefs() {
		final Collection coll = getObjectRefs();
		final LinkedList result = new LinkedList();
		for (Iterator i = coll.iterator(); i.hasNext();) {
			final ObjectRef ref = (ObjectRef) i.next();
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
		final Collection coll = getObjectRefs();
		for (Iterator i = coll.iterator(); i.hasNext();) {
			final ObjectRef ref = (ObjectRef) i.next();
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

	public final void setWord(int offset, long word) {
		if (mode.is32()) {
			set32(offset, (int) word);
		} else {
			set64(offset, word);
		}
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
	 * @param resolver
	 *            The resolver to set
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
	 * @see ObjectInfo
	 * @return The info for the started object
	 */
	public final ObjectInfo startObject(VmType cls) {
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
			final Object[] tib = ((VmClassType) cls).getTIB();
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
		//	throw new IllegalArgumentException("Cannot write out of an object");
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
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 2);
			write8(imm32);
		} else {
			write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 2);
			write32(imm32);
		}
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeADC(int, GPR, int, int)
	 */
	public void writeADC(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
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
        testSize(dstReg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
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
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 0);
			write8(imm32);
		} else {
			write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 0);
			write32(imm32);
		}
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
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
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
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
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
        testSize(dstReg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 4);
			write8(imm32);
		} else {
			write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 4);
			write32(imm32);
		}
	}

	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeAND(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 4);
			write8(imm32);
		} else {
			write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 4);
			write32(imm32);
		}
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
        testSize(srcReg, mode.getSize());
		write1bOpcodeModRM(0x21, srcReg.getSize(), dstReg, dstDisp, srcReg
				.getNr());
	}

	/**
	 * Create a AND dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeAND(GPR dstReg, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
		write1bOpcodeModRR(0x21, dstReg.getSize(), dstReg, srcReg.getNr());
	}

	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeAND(GPR dstReg, GPR srcReg, int srcDisp) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
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
	 * @param rawAddress
	 *            If true, tablePtr is a raw address
	 */
	public final void writeCALL(Object tablePtr, int offset, boolean rawAddress) {
        if (code64) {
            throw new InvalidOpcodeException();
        }
		write8(0xFF); // Opcode
		write8(0x15); // effective address == disp32
		writeObjectRef(tablePtr, offset, rawAddress);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCALL(GPR)
	 */
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

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCALL(GPR, GPR, int, int)
	 */
	public void writeCALL(GPR regBase, GPR regIndex, int scale, int disp) {
		// Since CALL in 64-bit mode always use 64-bit targets, we
		// specify a 0 operand size, so we won't get a REX prefix
		write1bOpcodeModRMSib(0xFF, 0, regBase, disp, 2, scale, regIndex);
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
        testSize(reg1, mode.getSize());
        testSize(reg2, mode.getSize());
		write1bOpcodeModRM(0x39, reg2.getSize(), reg1, disp, reg2.getNr());
	}

	/**
	 * Create a CMP reg1, reg2
	 * 
	 * @param reg1
	 * @param reg2
	 */
	public final void writeCMP(GPR reg1, GPR reg2) {
        testSize(reg1, mode.getSize());
        testSize(reg2, mode.getSize());
		write1bOpcodeModRR(0x39, reg1.getSize(), reg1, reg2.getNr());
	}

	/**
	 * Create a CMP reg1, [reg2+disp]
	 * 
	 * @param reg1
	 * @param reg2
	 * @param disp
	 */
	public void writeCMP(GPR reg1, GPR reg2, int disp) {
        testSize(reg1, mode.getSize());
        testSize(reg2, mode.getSize());
		write1bOpcodeModRM(0x3b, reg1.getSize(), reg2, disp, reg1.getNr());
	}

	/**
	 * Create a CMP reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public final void writeCMP_Const(GPR reg, int imm32) {
        testSize(reg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRR(0x83, reg.getSize(), reg, 7);
			write8(imm32);
		} else {
			write1bOpcodeModRR(0x81, reg.getSize(), reg, 7);
			write32(imm32);
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
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRM(0x83, operandSize, reg, disp, 7);
			write8(imm32);
		} else {
			write1bOpcodeModRM(0x81, operandSize, reg, disp, 7);
			write32(imm32);
		}
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
        testSize(dstReg, mode.getSize());
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

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFADDP(X86Register)
	 */
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

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFDIVP(X86Register)
	 */
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

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFMULP(X86Register)
	 */
	public void writeFMULP(X86Register fpuReg) {
		write8(0xde);
		write8(0xc8 + fpuReg.getNr());
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
	 * @see org.jnode.assembler.x86.X86Assembler#writeFSTP(X86Register)
	 */
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

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFSUBP(X86Register)
	 */
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

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFXCH(X86Register)
	 */
	public void writeFXCH(X86Register fpuReg) {
		write8(0xd9);
		write8(0xc8 + fpuReg.getNr());
	}

    /**
     *
     */
    public void writeHLT() {
        write8(0xF4);
    }

	/**
	 * Create an idiv edx:eax, srcReg.
	 * 
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
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public void writeIMUL(GPR dstReg, GPR srcReg) {
		write2bOpcodeModRR(0x0F, 0xAF, srcReg.getSize(), srcReg, dstReg.getNr());
	}

	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeIMUL(GPR dstReg, GPR srcReg, int srcDisp) {
		write2bOpcodeModRM(0x0F, 0xAF, dstReg.getSize(), srcReg, srcDisp,
				dstReg.getNr());
	}

	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param imm32
	 */
	public void writeIMUL_3(GPR dstReg, GPR srcReg, int imm32) {
		write1bOpcodeModRR(0x69, dstReg.getSize(), srcReg, dstReg.getNr());
		write32(imm32);
	}

	/**
	 * 
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
	 * 
	 * If srcReg is 64-bit, an imul rax, srcReg is created.
	 * 
	 * @param srcReg
	 */
	public final void writeIMUL_EAX(GPR srcReg) {
		write1bOpcodeModRR(0xF7, srcReg.getSize(), srcReg, 5);
	}

    public void writeIN(int operandSize) {
        if(operandSize == X86Constants.BITS8){
            write8(0xEC);
        } else if(operandSize == X86Constants.BITS16){
            write8(0xED);
        } else if(operandSize == X86Constants.BITS32){
            write8(X86Constants.OSIZE_PREFIX);
            write8(0xED);
        } else {
            throw new IllegalArgumentException("Invalid operand size for IN: " + operandSize);
        }
    }

    public void writeIN(int operandSize, int imm8) {
        if(operandSize == X86Constants.BITS8){
            write8(0xE4);
            write8(imm8);
        } else if(operandSize == X86Constants.BITS16){
            write8(0xE5);
            write8(imm8);
        } else if(operandSize == X86Constants.BITS32){
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
        testSize(dstReg, mode.getSize());
		if (code32) {
			write8(0x40 + dstReg.getNr());
		} else {
			write1bOpcodeModRR(0xFF, dstReg.getSize(), dstReg, 0);
		}
	}

	/**
	 * Create a inc size [dstReg+disp]
	 * 
	 * @param dstReg
	 */
	public final void writeINC(int operandSize, GPR dstReg, int disp) {
        testSize(dstReg, mode.getSize());
		testOperandSize(operandSize, BITS32 | BITS64);
		write1bOpcodeModRM(0xFF, operandSize, dstReg, disp, 0);
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
	 * Create a conditional jump to a label The opcode sequence is: 0x0f
	 * <jumpOpcode><rel32>
	 * 
	 * @param label
	 * @param jumpOpcode
	 */
	public final void writeJCC(Label label, int jumpOpcode) {
		write8(0x0f); // jxx rel32
		write8(jumpOpcode);
		writeRelativeObjectRef(label);
	}

	/**
	 * Create a relative jump to a given label
	 * 
	 * @param label
	 */
	public final void writeJMP(Label label) {
		write8(0xe9); // jmp rel32
		writeRelativeObjectRef(label);
	}

	/**
	 * Create a absolute jump to address stored at the given offset in the given
	 * table pointer.
	 * 
	 * @param tablePtr
	 * @param offset
	 * @param rawAddress
	 *            If true, tablePtr is a raw address
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
	 * @param reg32
	 */
	public final void writeJMP(GPR reg, int disp) {
        testSize(reg, mode.getSize());
        write2bOpcodeReg(0xFF, 0xA0, reg);
		write32(disp);
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
	 * Create a LODSD
	 */
	public final void writeLODSD() {
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
						+ " is out of range (distance " + distance + ")");
			}
		} else {
			throw new UnresolvedObjectRefException("Label " + label
					+ " is not resolved");
		}
	}

	/**
	 * Write a REX prefix byte if needed for ModRM and ModRR encoded opcodes.
	 * 
	 * @param rm
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
     * @param opcode1
     * @param reg
     */
    private final void write1bOpcodeReg(int opcode1, X86Register reg) {
        write1bOpcodeREXPrefix(reg.getSize(), reg.getNr());
        write8(opcode1 + (reg.getNr() & 7));
    }

    /**
     * Write a 2 byte opcode that has the register encoded in the single byte.
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
		if (rm.getNr() > 7) {
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
	 * @param rm
	 * @param reg
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
			if (X86Utils.isByte(disp)) {
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
			} else if (X86Utils.isByte(disp)) {
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
	 * @param opcode
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
	 * @param opcode
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
		} else if (X86Utils.isByte(disp)) {
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
	 * @param operandSize
	 *            Size of the operands ({@link X86Constants}.BITSxxx)
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
	 * @param opcode
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
	 * @param opcode
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
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
		testDst(dstReg, dstDisp);
		final int opcode;
		switch (operandSize) {
		case X86Constants.BITS8:
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
        testSize(srcReg, mode.getSize());
        write8(CRX_PREFIX);
        write8(0x20);
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
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
		final int opcode;
		switch (operandSize) {
		case X86Constants.BITS8:
			testSuitableForBits8(dstReg);
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
	 * Create a mov dstReg, [srcReg+srcDisp]
	 * 
	 * @param operandSize
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeMOV(int operandSize, GPR dstReg, GPR srcReg,
			int srcDisp) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
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
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
        testSize(dstIdxReg, mode.getSize());
		final int opcode;
		switch (operandSize) {
		case X86Constants.BITS8:
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
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
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
	 * Create a MOV reg,imm32 or MOV reg,imm64
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public final void writeMOV_Const(GPR dstReg, int imm32) {
        testSize(dstReg, mode.getSize());
		testSize(dstReg, BITS32 | BITS64);
		if (dstReg.getSize() == BITS32) {
            write1bOpcodeReg(0xB8, dstReg);
			write32(imm32);
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
	 * @throws InvalidOpcodeException
	 *             In 32-bit modes.
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
	 * Create a mov [destReg+destDisp], imm32
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public final void writeMOV_Const(int operandSize, GPR dstReg,
			int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
		testOperandSize(operandSize, BITS32 | BITS64);
		write1bOpcodeModRM(0xC7, operandSize, dstReg, dstDisp, 0);
		write32(imm32);
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
        testSize(dstReg, mode.getSize());
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
	 * @param offset
	 * @param rawAddress
	 *            If true, object is a raw address, not a normal object.
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
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeOR(GPR dstReg, int imm32) {
        testSize(dstReg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 1);
			write8(imm32);
		} else {
			write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 1);
			write32(imm32);
		}
	}

	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeOR(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 1);
			write8(imm32);
		} else {
			write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 1);
			write32(imm32);
		}
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
        testSize(srcReg, mode.getSize());
		write1bOpcodeModRM(0x09, srcReg.getSize(), dstReg, dstDisp, srcReg
				.getNr());
	}

	/**
	 * Create a OR dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeOR(GPR dstReg, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
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
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
		write1bOpcodeModRM(0x0B, dstReg.getSize(), srcReg, srcDisp, dstReg
				.getNr());
	}

    public void writeOUT(int operandSize) {
        if(operandSize == X86Constants.BITS8){
            write8(0xEE);
        } else if(operandSize == X86Constants.BITS16){
            write8(0xEF);
        } else if(operandSize == X86Constants.BITS32){
            write8(X86Constants.OSIZE_PREFIX);
            write8(0xEF);
        } else {
            throw new IllegalArgumentException("Invalid operand size for OUT: " + operandSize);
        }
    }

    public void writeOUT(int operandSize, int imm8) {
        if(operandSize == X86Constants.BITS8){
            write8(0xE6);
            write8(imm8);
        } else if(operandSize == X86Constants.BITS16){
            write8(0xE7);
            write8(imm8);
        } else if(operandSize == X86Constants.BITS32){
            write8(X86Constants.OSIZE_PREFIX);
            write8(0xE7);
            write8(imm8);
        } else {
            throw new IllegalArgumentException("Invalid operand size for OUT: " + operandSize);
        }
    }

	/**
	 * Create a pop reg32
	 * 
	 * @param dstReg
	 */
	public final void writePOP(GPR dstReg) {
        testSize(dstReg, mode.getSize());
        write1bOpcodeReg(0x58, dstReg);
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

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePOPA()
	 */
	public void writePOPA() {
		if (code64) {
			throw new InvalidOpcodeException();
		}
		write8(0x61);
	}

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePOPF()
     */
    public void writePOPF() {
        write8(0x9D);
    }

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePrefix(int)
	 */
	public void writePrefix(int prefix) {
		write8(prefix);
	}

	/**
	 * Create a push dword imm32
	 * 
	 * @param imm32
	 * @return The ofset of the start of the instruction.
	 */
	public final int writePUSH(int imm32) {
		final int rc = m_used;
		write8(0x68); // PUSH imm32
		write32(imm32);
		return rc;
	}

	/**
	 * Create a push srcReg
	 * 
	 * @param srcReg
	 * @return The ofset of the start of the instruction.
	 */
	public final int writePUSH(GPR srcReg) {
        testSize(srcReg, mode.getSize());
        final int rc = m_used;
        write1bOpcodeReg(0x50, srcReg);
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

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePUSHA()
	 */
	public void writePUSHA() {
		if (code64) {
			throw new InvalidOpcodeException();
		}
		write8(0x60);
	}

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePUSHF()
     */
    public void writePUSHF() {
        write8(0x9C);
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
		if (X86Utils.isByte(imm32)) {
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
		if (X86Utils.isByte(imm32)) {
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

	/**
	 * Create a SETcc dstReg
	 * 
	 * @param dstReg
	 * @param cc
	 */
	public void writeSETCC(GPR dstReg, int cc) {
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

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHL(int, GPR, int, int)
	 */
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

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHL_CL(int, GPR, int)
	 */
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
        testSize(dstReg, mode.getSize());
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
        testSize(dstReg, mode.getSize());
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
	 * Create a SUB reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public final void writeSUB(GPR reg, int imm32) {
        testSize(reg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRR(0x83, reg.getSize(), reg, 5);
			write8(imm32);
		} else {
			write1bOpcodeModRR(0x81, reg.getSize(), reg, 5);
			write32(imm32);
		}
	}

	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeSUB(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRM(0x83, operandSize, dstReg, dstDisp, 5);
			write8(imm32);
		} else {
			write1bOpcodeModRM(0x81, operandSize, dstReg, dstDisp, 5);
			write32(imm32);
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
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
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
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
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
        testSize(reg, mode.getSize());
		write1bOpcodeModRR(0xF7, reg.getSize(), reg, 0);
		write32(imm32);
	}

	/**
	 * Create a TEST [reg+disp], imm32
	 * 
	 * @param reg
	 * @param disp
	 * @param imm32
	 */
	public void writeTEST(int operandSize, GPR reg, int disp, int imm32) {
        testSize(reg, mode.getSize());
		testOperandSize(operandSize, BITS32 | BITS64);
		write1bOpcodeModRM(0xF7, operandSize, reg, disp, 0);
		write32(imm32);
	}

	/**
	 * Create a TEST reg1, reg2
	 * 
	 * @param reg1
	 * @param reg2
	 */
	public void writeTEST(GPR reg1, GPR reg2) {
        testSize(reg1, mode.getSize());
        testSize(reg2, mode.getSize());
		write1bOpcodeModRR(0x85, reg1.getSize(), reg1, reg2.getNr());
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
	 * @see org.jnode.assembler.x86.X86Assembler#writeXCHG(GPR, int, GPR)
	 */
	public void writeXCHG(GPR dstReg, int dstDisp, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
		write1bOpcodeModRM(0x87, srcReg.getSize(), dstReg, dstDisp, srcReg
				.getNr());
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeXCHG(GPR, GPR)
	 */
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
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeXOR(GPR dstReg, int imm32) {
        testSize(dstReg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
			write1bOpcodeModRR(0x83, dstReg.getSize(), dstReg, 6);
			write8(imm32);
		} else {
			write1bOpcodeModRR(0x81, dstReg.getSize(), dstReg, 6);
			write32(imm32);
		}
	}

	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeXOR(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        testSize(dstReg, mode.getSize());
		if (X86Utils.isByte(imm32)) {
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
        testSize(srcReg, mode.getSize());
		write1bOpcodeModRM(0x31, srcReg.getSize(), dstReg, dstDisp, srcReg
				.getNr());
	}

	/**
	 * Create a XOR dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeXOR(GPR dstReg, GPR srcReg) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
		write1bOpcodeModRR(0x31, dstReg.getSize(), dstReg, srcReg.getNr());
	}

	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeXOR(GPR dstReg, GPR srcReg, int srcDisp) {
        testSize(dstReg, mode.getSize());
        testSize(srcReg, mode.getSize());
		write1bOpcodeModRM(0x33, dstReg.getSize(), srcReg, srcDisp, dstReg
				.getNr());
	}
}
