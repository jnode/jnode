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
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.X86CpuID;

/**
 * Implementation of AbstractX86Stream.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente Sántha
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
			int size = getLength() - m_objptr;
			set32(m_objptr - 12, size);
			m_objptr = -1;
			inObject = false;
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

		public void addUnresolvedLink(int offset) {
			if (unresolvedLinks == null) {
				unresolvedLinks = new LinkedList();
			}
			unresolvedLinks.add(new Integer(offset));
		}

		public int getOffset() {
			return dataOffset;
		}

		public int[] getUnresolvedOffsets() {
			int cnt = unresolvedLinks.size();
			int[] offsets = new int[cnt];
			int ofs = 0;
			for (Iterator i = unresolvedLinks.iterator(); i.hasNext(); ofs++) {
				offsets[ofs] = ((Integer) i.next()).intValue();
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
			this.dataOffset = offset;
			if (unresolvedLinks != null) {
				// Link all unresolved links
				for (Iterator i = unresolvedLinks.iterator(); i.hasNext();) {
					final int addr = ((Integer) i.next()).intValue();
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
				unresolvedLinks = null;
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

	public X86BinaryAssembler(X86CpuID cpuId, int baseAddr) {
		this(cpuId, baseAddr, 1024, 128, 1024);
	}

	public X86BinaryAssembler(X86CpuID cpuId, int baseAddr,
			int initialObjectRefsCapacity, int initialSize, int growSize) {
		super(cpuId);
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
	public void clear() {
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

		// The default header is 8-bytes long. The size fields add another
		// 4 bytes, which adds up to 12 which masy not be objectaligned.
		// Write some slack until it is aligned again
		int alignSlack = 0;
		while (ObjectLayout.objectAlign(alignSlack + 12) != (alignSlack + 12)) {
			write32(0);
			alignSlack += 4;
		}
		// System.out.println("alignSlack=" + alignSlack);

		write32(0); // Size
		write32(ObjectFlags.GC_DEFAULT_COLOR); // Flags
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
			throw new RuntimeException("Write to [EBP+0]");
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
			throw new RuntimeException(
					"Register is not suitable for BITS8 datasize");
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
		if (!inObject) {
			throw new IllegalArgumentException("Cannot write out of an object");
		}
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
		if (!inObject) {
			throw new IllegalArgumentException("Cannot write out of an object");
		}
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
	public void writeADC(X86Register dstReg, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRR(0x83, dstReg.getNr(), 2);
			write8(imm32);
		} else {
			writeModRR(0x81, dstReg.getNr(), 2);
			write32(imm32);
		}
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeADC(org.jnode.assembler.x86.Register,
	 *      int, int)
	 */
	public void writeADC(X86Register dstReg, int dstDisp, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRM(0x83, dstReg.getNr(), dstDisp, 2);
			write8(imm32);
		} else {
			writeModRM(0x81, dstReg.getNr(), dstDisp, 2);
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
	public final void writeADC(X86Register dstReg, int dstDisp,
			X86Register srcReg) {
		writeModRM(0x11, dstReg.getNr(), dstDisp, srcReg.getNr());
	}

	/**
	 * Create a ADC dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public void writeADC(X86Register dstReg, X86Register srcReg) {
		writeModRR(0x11, dstReg.getNr(), srcReg.getNr());
	}

	/**
	 * Create a ADC dstReg, [srcReg+srcDisp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeADC(X86Register dstReg, X86Register srcReg, int srcDisp) {
		writeModRM(0x13, srcReg.getNr(), srcDisp, dstReg.getNr());
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeADD(X86Register dstReg, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRR(0x83, dstReg.getNr(), 0);
			write8(imm32);
		} else {
			writeModRR(0x81, dstReg.getNr(), 0);
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
	public void writeADD(X86Register dstReg, int dstDisp, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRM(0x83, dstReg.getNr(), dstDisp, 0);
			write8(imm32);
		} else {
			writeModRM(0x81, dstReg.getNr(), dstDisp, 0);
			write32(imm32);
		}
	}

	/**
	 * Create a ADD [dstReg+dstDisp], <srcReg>
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public final void writeADD(X86Register dstReg, int dstDisp,
			X86Register srcReg) {
		writeModRM(0x01, dstReg.getNr(), dstDisp, srcReg.getNr());
	}

	/**
	 * Create a ADD dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeADD(X86Register dstReg, X86Register srcReg) {
		writeModRR(0x01, dstReg.getNr(), srcReg.getNr());
	}

	/**
	 * Create a ADD dstReg, [srcReg+srcDisp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeADD(X86Register dstReg, X86Register srcReg,
			int srcDisp) {
		writeModRM(0x03, srcReg.getNr(), srcDisp, dstReg.getNr());
	}

	/**
	 * Create a AND reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public final void writeAND(X86Register reg, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRR(0x83, reg.getNr(), 4);
			write8(imm32);
		} else {
			writeModRR(0x81, reg.getNr(), 4);
			write32(imm32);
		}
	}

	// LS, PR
	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeAND(X86Register dstReg, int dstDisp, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRM(0x83, dstReg.getNr(), dstDisp, 4);
			write8(imm32);
		} else {
			writeModRM(0x81, dstReg.getNr(), dstDisp, 4);
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
	public final void writeAND(X86Register dstReg, int dstDisp,
			X86Register srcReg) {
		writeModRM(0x21, dstReg.getNr(), dstDisp, srcReg.getNr());
	}

	/**
	 * Create a AND dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeAND(X86Register dstReg, X86Register srcReg) {
		writeModRR(0x21, dstReg.getNr(), srcReg.getNr());
	}

	// LS, PR
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeAND(X86Register dstReg, X86Register srcReg, int srcDisp) {
		writeModRM(0x23, srcReg.getNr(), srcDisp, dstReg.getNr());
	}

	public void writeArithSSEDOp(int operation, X86Register dst, X86Register src) {
		write8(0xF2);
		write8(0x0F);
		switch (operation) {
		case SSE_ADD:
			write8(0x58);
			break;
		case SSE_SUB:
			write8(0x5C);
			break;
		case SSE_MUL:
			write8(0x59);
			break;
		case SSE_DIV:
			write8(0x5E);
			break;
		}
		writeModRR(src.getNr(), dst.getNr());
	}

	public void writeArithSSEDOp(int operation, X86Register dst,
			X86Register src, int srcDisp) {
		write8(0xF2);
		write8(0x0F);
		switch (operation) {
		case SSE_ADD:
			write8(0x58);
			break;
		case SSE_SUB:
			write8(0x5C);
			break;
		case SSE_MUL:
			write8(0x59);
			break;
		case SSE_DIV:
			write8(0x5E);
			break;
		}
		writeModRM(src.getNr(), srcDisp, dst.getNr());
	}

	public void writeArithSSESOp(int operation, X86Register dst, X86Register src) {
		write8(0xF3);
		write8(0x0F);
		switch (operation) {
		case SSE_ADD:
			write8(0x58);
			break;
		case SSE_SUB:
			write8(0x5C);
			break;
		case SSE_MUL:
			write8(0x59);
			break;
		case SSE_DIV:
			write8(0x5E);
			break;
		}
		writeModRR(src.getNr(), dst.getNr());
	}

	public void writeArithSSESOp(int operation, X86Register dst,
			X86Register src, int srcDisp) {
		write8(0xF3);
		write8(0x0F);
		switch (operation) {
		case SSE_ADD:
			write8(0x58);
			break;
		case SSE_SUB:
			write8(0x5C);
			break;
		case SSE_MUL:
			write8(0x59);
			break;
		case SSE_DIV:
			write8(0x5E);
			break;
		}
		writeModRM(src.getNr(), srcDisp, dst.getNr());
	}

	/**
	 * Create a bound lReg, [rReg+rDisp]
	 * 
	 * @param lReg
	 * @param rReg
	 * @param rDisp
	 */
	public final void writeBOUND(X86Register lReg, X86Register rReg, int rDisp) {
		writeModRM(0x62, rReg.getNr(), rDisp, lReg.getNr());
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
		write8(0xFF); // Opcode
		write8(0x15); // effective address == disp32
		writeObjectRef(tablePtr, offset, rawAddress);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCALL(org.jnode.assembler.x86.Register)
	 */
	public void writeCALL(X86Register reg) {
		writeModRR(0xFF, reg.getNr(), 2);
	}

	/**
	 * Create a call to address stored at the given [reg+offset].
	 * 
	 * @param reg
	 * @param offset
	 */
	public final void writeCALL(X86Register reg, int offset) {
		writeModRM(0xFF, reg.getNr(), offset, 2);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCALL(org.jnode.assembler.x86.Register,
	 *      org.jnode.assembler.x86.Register, int, int)
	 */
	public void writeCALL(X86Register regBase, X86Register regIndex, int scale,
			int disp) {
		writeModRMSib(0xFF, regBase.getNr(), disp, 2, scale, regIndex.getNr());
	}

	/**
	 * Create a cdq
	 */
	public final void writeCDQ() {
		write8(0x99);
	}

	/**
	 * Create a CMOVcc dst,src
	 * 
	 * @param ccOpcode
	 * @param dst
	 * @param src
	 */
	public void writeCMOVcc(int ccOpcode, X86Register dst, X86Register src) {
		if (!haveCMOV) {
			throw new IllegalArgumentException(
					"CMOVcc not support on current cpu");
		}
		write8(0x0F);
		writeModRR(ccOpcode - 0x40, src.getNr(), dst.getNr());
	}

	/**
	 * Create a CMOVcc dst,[src+srcDisp]
	 * 
	 * @param dst
	 * @param src
	 * @param srcDisp
	 */
	public void writeCMOVcc(int ccOpcode, X86Register dst, X86Register src,
			int srcDisp) {
		if (!haveCMOV) {
			throw new IllegalArgumentException(
					"CMOVcc not support on current cpu");
		}
		write8(0x0F);
		writeModRM(ccOpcode - 0x40, src.getNr(), srcDisp, dst.getNr());
	}

	/**
	 * Create a CMP [reg1+disp], reg2
	 * 
	 * @param reg1
	 * @param disp
	 * @param reg2
	 */
	public void writeCMP(X86Register reg1, int disp, X86Register reg2) {
		writeModRM(0x39, reg1.getNr(), disp, reg2.getNr());
	}

	/**
	 * Create a CMP reg1, reg2
	 * 
	 * @param reg1
	 * @param reg2
	 */
	public final void writeCMP(X86Register reg1, X86Register reg2) {
		writeModRR(0x39, reg1.getNr(), reg2.getNr());
	}

	/**
	 * Create a CMP reg1, [reg2+disp]
	 * 
	 * @param reg1
	 * @param reg2
	 * @param disp
	 */
	public void writeCMP(X86Register reg1, X86Register reg2, int disp) {
		writeModRM(0x3b, reg2.getNr(), disp, reg1.getNr());
	}

	/**
	 * Create a CMP reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public final void writeCMP_Const(X86Register reg, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRR(0x83, reg.getNr(), 7);
			write8(imm32);
		} else {
			writeModRR(0x81, reg.getNr(), 7);
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
	public void writeCMP_Const(X86Register reg, int disp, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRM(0x83, reg.getNr(), disp, 7);
			write8(imm32);
		} else {
			writeModRM(0x81, reg.getNr(), disp, 7);
			write32(imm32);
		}
	}

	/**
	 * Create a CMP EAX, imm32
	 * 
	 * @param imm32
	 */
	public final void writeCMP_EAX(int imm32) {
		write8(0x3d);
		write32(imm32);
	}

	/**
	 * Create a CMP [reg+regDisp], imm32
	 * 
	 * @param memPtr
	 * @param imm32
	 */
	public final void writeCMP_MEM(int memPtr, int imm32) {
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
	public void writeCMP_MEM(X86Register reg, int memPtr) {
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
	public final void writeCMPXCHG_EAX(X86Register dstReg, int dstDisp,
			X86Register srcReg, boolean lock) {
		if (lock) {
			write8(0xF0);
		}
		write8(0x0F);
		writeModRM(0xB1, dstReg.getNr(), dstDisp, srcReg.getNr());
	}

	/**
	 * Create a dec reg32
	 * 
	 * @param dstReg
	 */
	public final void writeDEC(X86Register dstReg) {
		write8(0x48 + dstReg.getNr());
	}

	/**
	 * Create a dec dword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public final void writeDEC(X86Register dstReg, int dstDisp) {
		writeModRM(0xff, dstReg.getNr(), dstDisp, 1);
	}

	/**
	 * Create a fadd dword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeFADD32(X86Register srcReg, int srcDisp) {
		writeModRM(0xd8, srcReg.getNr(), srcDisp, 0);
	}

	/**
	 * Create a fadd qword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeFADD64(X86Register srcReg, int srcDisp) {
		writeModRM(0xdc, srcReg.getNr(), srcDisp, 0);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFADDP(org.jnode.assembler.x86.Register)
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
	public final void writeFDIV32(X86Register srcReg, int srcDisp) {
		writeModRM(0xd8, srcReg.getNr(), srcDisp, 6);
	}

	/**
	 * Create a fdiv qword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeFDIV64(X86Register srcReg, int srcDisp) {
		writeModRM(0xdc, srcReg.getNr(), srcDisp, 6);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFDIVP(org.jnode.assembler.x86.Register)
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
	public final void writeFILD32(X86Register dstReg, int dstDisp) {
		writeModRM(0xdb, dstReg.getNr(), dstDisp, 0);
	}

	/**
	 * Create a fild qword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public final void writeFILD64(X86Register dstReg, int dstDisp) {
		writeModRM(0xdf, dstReg.getNr(), dstDisp, 5);
	}

	/**
	 * Create a fistp dword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public final void writeFISTP32(X86Register dstReg, int dstDisp) {
		writeModRM(0xdb, dstReg.getNr(), dstDisp, 3);
	}

	/**
	 * Create a fistp qword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public final void writeFISTP64(X86Register dstReg, int dstDisp) {
		writeModRM(0xdf, dstReg.getNr(), dstDisp, 7);
	}

	/**
	 * Create a fld dword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeFLD32(X86Register srcReg, int srcDisp) {
		writeModRM(0xd9, srcReg.getNr(), srcDisp, 0);
	}

	/**
	 * Create a fld dword [srcBaseReg+scrIndexReg*srcScale+srcDisp]
	 * 
	 * @param srcBaseReg
	 * @param srcIndexReg
	 * @param srcScale
	 * @param srcDisp
	 */
	public void writeFLD32(X86Register srcBaseReg, X86Register srcIndexReg,
			int srcScale, int srcDisp) {
		writeModRMSib(0xd9, srcBaseReg.getNr(), srcDisp, 0, srcScale,
				srcIndexReg.getNr());
	}

	/**
	 * Create a fld qword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeFLD64(X86Register srcReg, int srcDisp) {
		writeModRM(0xdd, srcReg.getNr(), srcDisp, 0);
	}

	/**
	 * Create a fld qword [srcBaseReg+scrIndexReg*srcScale+srcDisp]
	 * 
	 * @param srcBaseReg
	 * @param srcIndexReg
	 * @param srcScale
	 * @param srcDisp
	 */
	public void writeFLD64(X86Register srcBaseReg, X86Register srcIndexReg,
			int srcScale, int srcDisp) {
		writeModRMSib(0xdd, srcBaseReg.getNr(), srcDisp, 0, srcScale,
				srcIndexReg.getNr());
	}

	/**
	 * Create a fmul dword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeFMUL32(X86Register srcReg, int srcDisp) {
		writeModRM(0xd8, srcReg.getNr(), srcDisp, 1);
	}

	/**
	 * Create a fmul qword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeFMUL64(X86Register srcReg, int srcDisp) {
		writeModRM(0xdc, srcReg.getNr(), srcDisp, 1);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFMULP(org.jnode.assembler.x86.Register)
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
	 * @see org.jnode.assembler.x86.X86Assembler#writeFSTP(org.jnode.assembler.x86.Register)
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
	public final void writeFSTP32(X86Register dstReg, int dstDisp) {
		writeModRM(0xd9, dstReg.getNr(), dstDisp, 3);
	}

	/**
	 * Create a fstp qword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public final void writeFSTP64(X86Register dstReg, int dstDisp) {
		writeModRM(0xdd, dstReg.getNr(), dstDisp, 3);
	}

	/**
	 * Create a fsub dword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeFSUB32(X86Register srcReg, int srcDisp) {
		writeModRM(0xd8, srcReg.getNr(), srcDisp, 4);
	}

	/**
	 * Create a fsub qword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeFSUB64(X86Register srcReg, int srcDisp) {
		writeModRM(0xdc, srcReg.getNr(), srcDisp, 4);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFSUBP(org.jnode.assembler.x86.Register)
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
	 * @see org.jnode.assembler.x86.X86Assembler#writeFXCH(org.jnode.assembler.x86.Register)
	 */
	public void writeFXCH(X86Register fpuReg) {
		write8(0xd9);
		write8(0xc8 + fpuReg.getNr());
	}

	/**
	 * Create a idiv eax, srcReg
	 * 
	 * @param srcReg
	 */
	public final void writeIDIV_EAX(X86Register srcReg) {
		writeModRR(0xF7, srcReg.getNr(), 7);
	}

	// LS
	/**
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeIDIV_EAX(X86Register srcReg, int srcDisp) {
		writeModRM(0xF7, srcReg.getNr(), srcDisp, 7);
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public void writeIMUL(X86Register dstReg, X86Register srcReg) {
		write8(0x0F);
		writeModRR(0xAF, srcReg.getNr(), dstReg.getNr());
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeIMUL(X86Register dstReg, X86Register srcReg, int srcDisp) {
		write8(0x0F);
		writeModRM(0xAF, srcReg.getNr(), srcDisp, dstReg.getNr());
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param imm32
	 */
	public void writeIMUL_3(X86Register dstReg, X86Register srcReg, int imm32) {
		writeModRR(0x69, srcReg.getNr(), dstReg.getNr());
		write32(imm32);
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 * @param imm32
	 */
	public void writeIMUL_3(X86Register dstReg, X86Register srcReg,
			int srcDisp, int imm32) {
		writeModRM(0x69, srcReg.getNr(), srcDisp, dstReg.getNr());
		write32(imm32);
	}

	/**
	 * Create a imul eax, srcReg
	 * 
	 * @param srcReg
	 */
	public final void writeIMUL_EAX(X86Register srcReg) {
		writeModRR(0xF7, srcReg.getNr(), 5);
	}

	/**
	 * Create a inc reg32
	 * 
	 * @param dstReg
	 */
	public final void writeINC(X86Register dstReg) {
		write8(0x40 + dstReg.getNr());
	}

	/**
	 * Create a inc [reg32+disp]
	 * 
	 * @param dstReg
	 */
	public final void writeINC(X86Register dstReg, int disp) {
		writeModRM(0xFF, dstReg.getNr(), disp, 0);
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
	public void writeJMP(Object tablePtr, X86Register offsetReg) {
		write8(0xFF); // Opcode
		write8(0xA0 | offsetReg.getNr()); // effective address == disp32[reg]
		writeObjectRef(tablePtr, 0, false);
	}

	/**
	 * Create a absolute jump to address in register
	 * 
	 * @param reg32
	 */
	public final void writeJMP(X86Register reg32) {
		writeModRR(0xff, reg32.getNr(), 4);
	}

	/**
	 * Create a absolute jump to [reg32+disp]
	 * 
	 * @param reg32
	 */
	public final void writeJMP(X86Register reg32, int disp) {
		write8(0xFF); // Opcode
		write8(0xA0 | reg32.getNr()); // effective address == disp32[reg]
		write32(disp);
	}

	/**
	 * Create a lea dstReg,[srcReg+disp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param disp
	 */
	public final void writeLEA(X86Register dstReg, X86Register srcReg, int disp) {
		writeModRM(0x8d, srcReg.getNr(), disp, dstReg.getNr());
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
	public final void writeLEA(X86Register dstReg, X86Register srcReg,
			X86Register srcIdxReg, int scale, int disp) {
		writeModRMSib(0x8d, srcReg.getNr(), disp, dstReg.getNr(), scale,
				srcIdxReg.getNr());
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
	 * Write a mod-r/m byte+offset for the following addressing scheme's [rm]
	 * disp8[rm] disp32[rm]
	 * 
	 * @param rm
	 * @param disp
	 * @param reg
	 */
	public final void writeModRM(int rm, int disp, int reg) {
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
	public final void writeModRM(int opcode, int rm, int disp, int reg) {
		write8(opcode);
		writeModRM(rm, disp, reg);
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
	public final void writeModRMSib(int base, int disp, int reg, int scale,
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
	public final void writeModRMSib(int opcode, int base, int disp, int reg,
			int scale, int index) {
		write8(opcode);
		writeModRMSib(base, disp, reg, scale, index);
	}

	/**
	 * Write a mod-r/m byte for the following addressing scheme rm
	 * 
	 * @param rm
	 * @param reg
	 */
	public final void writeModRR(int rm, int reg) {
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
	 * @param rm
	 * @param reg
	 */
	public final void writeModRR(int opcode, int rm, int reg) {
		write8(opcode);
		writeModRR(rm, reg);
	}

	/**
	 * Create a mov [dstReg+dstDisp], <srcReg>
	 * 
	 * @param operandSize
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public final void writeMOV(int operandSize, X86Register dstReg,
			int dstDisp, X86Register srcReg) {
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
			opcode = 0x89;
			break;
		default:
			throw new IllegalArgumentException("Invalid operandSize "
					+ operandSize);
		}
		writeModRM(opcode, dstReg.getNr(), dstDisp, srcReg.getNr());
	}

	/**
	 * Create a mov <dstReg>, <srcReg>
	 * 
	 * @param operandSize
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeMOV(int operandSize, X86Register dstReg,
			X86Register srcReg) {
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
			opcode = 0x89;
			break;
		default:
			throw new IllegalArgumentException("Invalid operandSize "
					+ operandSize);
		}
		writeModRR(opcode, dstReg.getNr(), srcReg.getNr());
	}

	/**
	 * Create a mov dstReg, [srcReg+srcDisp]
	 * 
	 * @param operandSize
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeMOV(int operandSize, X86Register dstReg,
			X86Register srcReg, int srcDisp) {
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
			opcode = 0x8b;
			break;
		default:
			throw new IllegalArgumentException("Invalid operandSize "
					+ operandSize);
		}
		writeModRM(opcode, srcReg.getNr(), srcDisp, dstReg.getNr());
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
	public final void writeMOV(int operandSize, X86Register dstReg,
			X86Register dstIdxReg, int scale, int dstDisp, X86Register srcReg) {
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
			opcode = 0x89;
			break;
		default:
			throw new IllegalArgumentException("Invalid operandSize "
					+ operandSize);
		}
		writeModRMSib(opcode, dstReg.getNr(), dstDisp, srcReg.getNr(), scale,
				dstIdxReg.getNr());
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
	public final void writeMOV(int operandSize, X86Register dstReg,
			X86Register srcReg, X86Register srcIdxReg, int scale, int srcDisp) {
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
			opcode = 0x8b;
			break;
		default:
			throw new IllegalArgumentException("Invalid operandSize "
					+ operandSize);
		}
		writeModRMSib(opcode, srcReg.getNr(), srcDisp, dstReg.getNr(), scale,
				srcIdxReg.getNr());
	}

	/**
	 * Create a mov <reg>, <imm32>
	 * 
	 * @param destReg
	 * @param imm32
	 */
	public final void writeMOV_Const(X86Register destReg, int imm32) {
		write8(0xb8 + destReg.getNr()); // MOV reg,imm32
		write32(imm32);
	}

	/**
	 * Create a mov [destReg+destDisp], <imm32>
	 * 
	 * @param destReg
	 * @param destDisp
	 * @param imm32
	 */
	public final void writeMOV_Const(X86Register destReg, int destDisp,
			int imm32) {
		writeModRM(0xC7, destReg.getNr(), destDisp, 0);
		write32(imm32);
	}

	/**
	 * Create a mov <reg>, <label>
	 * 
	 * @param dstReg
	 * @param label
	 */
	public final void writeMOV_Const(X86Register dstReg, Object label) {
		write8(0xb8 + dstReg.getNr());
		writeObjectRef(label, 0, false);
	}

	/**
	 * Create a mov [destReg+dstIdxReg*scale+destDisp], <imm32>
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeMOV_Const(X86Register dstReg, X86Register dstIdxReg,
			int scale, int dstDisp, int imm32) {
		writeModRMSib(0xC7, dstReg.getNr(), dstDisp, 0, scale, dstIdxReg
				.getNr());
		write32(imm32);
	}

	/**
	 * Create a movsx <dstReg>, <srcReg>
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcSize
	 */
	public final void writeMOVSX(X86Register dstReg, X86Register srcReg,
			int srcSize) {
		write8(0x0f);
		if (srcSize == X86Constants.BITS8) {
			testSuitableForBits8(dstReg);
			writeModRR(0xBE, srcReg.getNr(), dstReg.getNr());
		} else if (srcSize == X86Constants.BITS16) {
			writeModRR(0xBF, srcReg.getNr(), dstReg.getNr());
		} else {
			throw new IllegalArgumentException("Unknown srcSize " + srcSize);
		}
	}

	public void writeMOVSX(X86Register dstReg, X86Register srcReg, int srcDisp,
			int srcSize) {
		write8(0x0f);
		if (srcSize == X86Constants.BITS8) {
			testSuitableForBits8(dstReg);
			writeModRM(0xBE, srcReg.getNr(), srcDisp, dstReg.getNr());
		} else if (srcSize == X86Constants.BITS16) {
			writeModRM(0xBF, srcReg.getNr(), srcDisp, dstReg.getNr());
		} else {
			throw new IllegalArgumentException("Unknown srcSize " + srcSize);
		}
	}

	/**
	 * Create a movzx <dstReg>, <srcReg>
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcSize
	 */
	public final void writeMOVZX(X86Register dstReg, X86Register srcReg,
			int srcSize) {
		write8(0x0f);
		if (srcSize == X86Constants.BITS8) {
			testSuitableForBits8(dstReg);
			writeModRR(0xB6, srcReg.getNr(), dstReg.getNr());
		} else if (srcSize == X86Constants.BITS16) {
			writeModRR(0xB7, srcReg.getNr(), dstReg.getNr());
		} else {
			throw new IllegalArgumentException("Unknown srcSize " + srcSize);
		}
	}

	public void writeMOVZX(X86Register dstReg, X86Register srcReg, int srcDisp,
			int srcSize) {
		write8(0x0f);
		if (srcSize == X86Constants.BITS8) {
			testSuitableForBits8(dstReg);
			writeModRM(0xB6, srcReg.getNr(), srcDisp, dstReg.getNr());
		} else if (srcSize == X86Constants.BITS16) {
			writeModRM(0xB7, srcReg.getNr(), srcDisp, dstReg.getNr());
		} else {
			throw new IllegalArgumentException("Unknown srcSize " + srcSize);
		}
	}

	/**
	 * Create a mul eax, srcReg
	 * 
	 * @param srcReg
	 */
	public final void writeMUL_EAX(X86Register srcReg) {
		writeModRR(0xF7, srcReg.getNr(), 4);
	}

	/**
	 * Create a neg dstReg
	 * 
	 * @param dstReg
	 */
	public final void writeNEG(X86Register dstReg) {
		writeModRR(0xf7, dstReg.getNr(), 3);
	}

	/**
	 * Create a neg dword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public final void writeNEG(X86Register dstReg, int dstDisp) {
		writeModRM(0xf7, dstReg.getNr(), dstDisp, 3);
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
	public final void writeNOT(X86Register dstReg) {
		writeModRR(0xf7, dstReg.getNr(), 2);
	}

	/**
	 * Create a not dword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public final void writeNOT(X86Register dstReg, int dstDisp) {
		writeModRM(0xf7, dstReg.getNr(), dstDisp, 2);
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
			write32(offset);
		} else if (rawAddress) {
			write32(resolver.addressOf32(object) + offset);
		} else if ((resolver != null) && (!(object instanceof Label))) {
			// System.out.println("Using resolver for " + object);
			write32(resolver.addressOf32(object) + offset);
		} else {
			final X86ObjectRef ref = (X86ObjectRef) getObjectRef(object);
			if (ref.isResolved())
				write32(ref.getOffset() + baseAddr + offset);
			else {
				ref.addUnresolvedLink(m_used);
				write32(-(baseAddr + offset));
			}
		}
	}

	// LS, PR
	/**
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeOR(X86Register dstReg, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRR(0x83, dstReg.getNr(), 1);
			write8(imm32);
		} else {
			writeModRR(0x81, dstReg.getNr(), 1);
			write32(imm32);
		}
	}

	// LS, PR
	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeOR(X86Register dstReg, int dstDisp, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRM(0x83, dstReg.getNr(), dstDisp, 1);
			write8(imm32);
		} else {
			writeModRM(0x81, dstReg.getNr(), dstDisp, 1);
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
	public final void writeOR(X86Register dstReg, int dstDisp,
			X86Register srcReg) {
		writeModRM(0x09, dstReg.getNr(), dstDisp, srcReg.getNr());
	}

	/**
	 * Create a OR dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeOR(X86Register dstReg, X86Register srcReg) {
		writeModRR(0x09, dstReg.getNr(), srcReg.getNr());
	}

	// LS, PR
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeOR(X86Register dstReg, X86Register srcReg, int srcDisp) {
		writeModRM(0x0B, srcReg.getNr(), srcDisp, dstReg.getNr());
	}

	/**
	 * Create a pop reg32
	 * 
	 * @param dstReg
	 */
	public final void writePOP(X86Register dstReg) {
		write8(0x58 + dstReg.getNr()); // POP reg32
	}

	/**
	 * Create a pop dword [reg32+disp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public final void writePOP(X86Register dstReg, int dstDisp) {
		writeModRM(0x8f, dstReg.getNr(), dstDisp, 0); // POP [reg32+disp]
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePOPA()
	 */
	public void writePOPA() {
		write8(0x61);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePrefix(int)
	 */
	public void writePrefix(int prefix) {
		write8(prefix);
	}

	/**
	 * Create a push dword <imm32>
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
	 * Create a push reg32
	 * 
	 * @param srcReg
	 * @return The ofset of the start of the instruction.
	 */
	public final int writePUSH(X86Register srcReg) {
		final int rc = m_used;
		write8(0x50 + srcReg.getNr()); // PUSH reg32
		return rc;
	}

	/**
	 * Create a push dword [reg32+disp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 * @return The ofset of the start of the instruction.
	 */
	public final int writePUSH(X86Register srcReg, int srcDisp) {
		final int rc = m_used;
		writeModRM(0xFF, srcReg.getNr(), srcDisp, 6); // PUSH [reg32+disp]
		return rc;
	}

	/**
	 * Create a push dword [baseReg+indexReg*scale+disp]
	 * 
	 * @param srcBaseReg
	 * @param srcIndexReg
	 * @param srcScale
	 * @param srcDisp
	 * @return The ofset of the start of the instruction.
	 */
	public final int writePUSH(X86Register srcBaseReg, X86Register srcIndexReg,
			int srcScale, int srcDisp) {
		final int rc = m_used;
		writeModRMSib(0xFF, srcBaseReg.getNr(), srcDisp, 6, srcScale,
				srcIndexReg.getNr());
		return rc;
	}

	// PR
	/**
	 * Create a push dword <object>
	 * 
	 * @param objRef
	 * @return The offset of the start of the instruction.
	 */
	public final int writePUSH_Const(Object objRef) {
		final int rc = m_used;
		write8(0x68); // PUSH imm32
		writeObjectRef(objRef, 0, false);
		return rc;
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePUSHA()
	 */
	public void writePUSHA() {
		write8(0x60);
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

		int ofs = m_used + 4;
		X86ObjectRef ref = (X86ObjectRef) getObjectRef(object);
		ref.setRelJump();
		if (ref.isResolved()) {
			write32(ref.getOffset() - ofs);
		} else {
			ref.addUnresolvedLink(m_used);
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
		write8(0x9e);
	}

	/**
	 * Create a SAL dstReg,imm8
	 * 
	 * @param dstReg
	 * @param imm8
	 */
	public final void writeSAL(X86Register dstReg, int imm8) {
		writeModRR(0xc1, dstReg.getNr(), 4);
		write8(imm8);
	}

	// LS
	/**
	 * @param dstReg
	 * @param dstDisp
	 * @param imm8
	 */
	public void writeSAL(X86Register dstReg, int dstDisp, int imm8) {
		writeModRM(0xc1, dstReg.getNr(), dstDisp, 4);
		write8(imm8);
	}

	/**
	 * Create a SAL dstReg,cl
	 * 
	 * @param dstReg
	 */
	public final void writeSAL_CL(X86Register dstReg) {
		writeModRR(0xd3, dstReg.getNr(), 4);
	}

	// LS
	/**
	 * @param dstReg
	 * @param dstDisp
	 */
	public void writeSAL_CL(X86Register dstReg, int dstDisp) {
		writeModRM(0xd3, dstReg.getNr(), dstDisp, 4);
	}

	/**
	 * Create a SAR dstReg,imm8
	 * 
	 * @param dstReg
	 * @param imm8
	 */
	public final void writeSAR(X86Register dstReg, int imm8) {
		writeModRR(0xc1, dstReg.getNr(), 7);
		write8(imm8);
	}

	// LS
	/**
	 * @param dstReg
	 * @param dstDisp
	 * @param imm8
	 */
	public void writeSAR(X86Register dstReg, int dstDisp, int imm8) {
		writeModRM(0xc1, dstReg.getNr(), dstDisp, 7);
		write8(imm8);
	}

	/**
	 * Create a SAR dstReg,cl
	 * 
	 * @param dstReg
	 */
	public final void writeSAR_CL(X86Register dstReg) {
		writeModRR(0xd3, dstReg.getNr(), 7);
	}

	// LS
	/**
	 * @param dstReg
	 * @param dstDisp
	 */
	public void writeSAR_CL(X86Register dstReg, int dstDisp) {
		writeModRM(0xd3, dstReg.getNr(), dstDisp, 7);
	}

	/**
	 * Create a SBB dstReg, imm32
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeSBB(X86Register dstReg, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRR(0x83, dstReg.getNr(), 3);
			write8(imm32);
		} else {
			writeModRR(0x81, dstReg.getNr(), 3);
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
	public final void writeSBB(X86Register dstReg, int dstDisp, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRM(0x83, dstReg.getNr(), dstDisp, 3);
			write8(imm32);
		} else {
			writeModRM(0x81, dstReg.getNr(), dstDisp, 3);
			write32(imm32);
		}
	}

	/**
	 * Create a SBB [dstReg+dstDisp], <srcReg>
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public final void writeSBB(X86Register dstReg, int dstDisp,
			X86Register srcReg) {
		writeModRM(0x19, dstReg.getNr(), dstDisp, srcReg.getNr());
	}

	/**
	 * Create a SBB dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeSBB(X86Register dstReg, X86Register srcReg) {
		writeModRR(0x19, dstReg.getNr(), srcReg.getNr());
	}

	/**
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeSBB(X86Register dstReg, X86Register srcReg, int srcDisp) {
		writeModRM(0x1B, srcReg.getNr(), srcDisp, dstReg.getNr());
	}

	/**
	 * Create a SETcc dstReg
	 * 
	 * @param dstReg
	 * @param cc
	 */
	public void writeSETCC(X86Register dstReg, int cc) {
		write8(0x0F);
		writeModRR(0x90 + (cc & 0x0f), dstReg.getNr(), 0);
	}

	/**
	 * Create a SHL dstReg,imm8
	 * 
	 * @param dstReg
	 * @param imm8
	 */
	public final void writeSHL(X86Register dstReg, int imm8) {
		writeModRR(0xc1, dstReg.getNr(), 4);
		write8(imm8);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHL(org.jnode.assembler.x86.Register,
	 *      int, int)
	 */
	public void writeSHL(X86Register dstReg, int dstDisp, int imm8) {
		writeModRM(0xc1, dstReg.getNr(), dstDisp, 4);
		write8(imm8);
	}

	/**
	 * Create a SHL dstReg,cl
	 * 
	 * @param dstReg
	 */
	public final void writeSHL_CL(X86Register dstReg) {
		writeModRR(0xd3, dstReg.getNr(), 4);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHL_CL(org.jnode.assembler.x86.Register,
	 *      int)
	 */
	public void writeSHL_CL(X86Register dstReg, int dstDisp) {
		writeModRM(0xd3, dstReg.getNr(), dstDisp, 4);
	}

	/**
	 * Create a SHLD dstReg,srcReg,cl
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeSHLD_CL(X86Register dstReg, X86Register srcReg) {
		write8(0x0f);
		writeModRR(0xa5, dstReg.getNr(), srcReg.getNr());
	}

	/**
	 * Create a SHL dstReg,imm8
	 * 
	 * @param dstReg
	 * @param imm8
	 */
	public final void writeSHR(X86Register dstReg, int imm8) {
		writeModRR(0xc1, dstReg.getNr(), 5);
		write8(imm8);
	}

	/**
	 * @param dstReg
	 * @param dstDisp
	 * @param imm8
	 */
	public void writeSHR(X86Register dstReg, int dstDisp, int imm8) {
		writeModRM(0xc1, dstReg.getNr(), dstDisp, 5);
		write8(imm8);
	}

	/**
	 * Create a SHR dstReg,cl
	 * 
	 * @param dstReg
	 */
	public final void writeSHR_CL(X86Register dstReg) {
		writeModRR(0xd3, dstReg.getNr(), 5);
	}

	/**
	 * @param dstReg
	 * @param dstDisp
	 */
	public void writeSHR_CL(X86Register dstReg, int dstDisp) {
		writeModRM(0xd3, dstReg.getNr(), dstDisp, 5);
	}

	/**
	 * Create a SHRD dstReg,srcReg,cl
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeSHRD_CL(X86Register dstReg, X86Register srcReg) {
		write8(0x0f);
		writeModRR(0xad, dstReg.getNr(), srcReg.getNr());
	}

	/**
	 * Create a SUB reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public final void writeSUB(X86Register reg, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRR(0x83, reg.getNr(), 5);
			write8(imm32);
		} else {
			writeModRR(0x81, reg.getNr(), 5);
			write32(imm32);
		}
	}

	// LS, PR
	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeSUB(X86Register dstReg, int dstDisp, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRM(0x83, dstReg.getNr(), dstDisp, 5);
			write8(imm32);
		} else {
			writeModRM(0x81, dstReg.getNr(), dstDisp, 5);
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
	public final void writeSUB(X86Register dstReg, int dstDisp,
			X86Register srcReg) {
		writeModRM(0x29, dstReg.getNr(), dstDisp, srcReg.getNr());
	}

	/**
	 * Create a SUB dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeSUB(X86Register dstReg, X86Register srcReg) {
		writeModRR(0x29, dstReg.getNr(), srcReg.getNr());
	}

	// LS, PR
	/**
	 * Create a SUB dstReg, [srcReg+srcDisp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeSUB(X86Register dstReg, X86Register srcReg, int srcDisp) {
		writeModRM(0x2B, srcReg.getNr(), srcDisp, dstReg.getNr());
	}

	/**
	 * Create a TEST reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public final void writeTEST(X86Register reg, int imm32) {
		writeModRR(0xF7, reg.getNr(), 0);
		write32(imm32);
	}

	/**
	 * Create a TEST [reg+disp], imm32
	 * 
	 * @param reg
	 * @param disp
	 * @param imm32
	 */
	public void writeTEST(X86Register reg, int disp, int imm32) {
		writeModRM(0xF7, reg.getNr(), disp, 0);
		write32(imm32);
	}

	/**
	 * Create a TEST reg1, reg2
	 * 
	 * @param reg1
	 * @param reg2
	 */
	public void writeTEST(X86Register reg1, X86Register reg2) {
		writeModRR(0x85, reg1.getNr(), reg2.getNr());
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
	 * Create a TEST eax, imm32
	 * 
	 * @param value
	 */
	public final void writeTEST_EAX(int value) {
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
	 * @see org.jnode.assembler.x86.X86Assembler#writeXCHG(org.jnode.assembler.x86.Register,
	 *      int, org.jnode.assembler.x86.Register)
	 */
	public void writeXCHG(X86Register dstReg, int dstDisp, X86Register srcReg) {
		writeModRM(0x87, dstReg.getNr(), dstDisp, srcReg.getNr());
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeXCHG(org.jnode.assembler.x86.Register,
	 *      org.jnode.assembler.x86.Register)
	 */
	public void writeXCHG(X86Register dstReg, X86Register srcReg) {
		if (dstReg == X86Register.EAX) {
			write8(0x90 + srcReg.getNr());
		} else if (srcReg == X86Register.EAX) {
			write8(0x90 + dstReg.getNr());
		} else {
			writeModRR(0x87, dstReg.getNr(), srcReg.getNr());
		}
	}

	// LS, PR
	/**
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeXOR(X86Register dstReg, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRR(0x83, dstReg.getNr(), 6);
			write8(imm32);
		} else {
			writeModRR(0x81, dstReg.getNr(), 6);
			write32(imm32);
		}
	}

	// LS, PR
	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeXOR(X86Register dstReg, int dstDisp, int imm32) {
		if (X86Utils.isByte(imm32)) {
			writeModRM(0x83, dstReg.getNr(), dstDisp, 6);
			write8(imm32);
		} else {
			writeModRM(0x81, dstReg.getNr(), dstDisp, 6);
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
	public final void writeXOR(X86Register dstReg, int dstDisp,
			X86Register srcReg) {
		writeModRM(0x31, dstReg.getNr(), dstDisp, srcReg.getNr());
	}

	/**
	 * Create a XOR dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeXOR(X86Register dstReg, X86Register srcReg) {
		writeModRR(0x31, dstReg.getNr(), srcReg.getNr());
	}

	// LS, PR
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeXOR(X86Register dstReg, X86Register srcReg, int srcDisp) {
		writeModRM(0x33, srcReg.getNr(), srcDisp, dstReg.getNr());
	}

}
