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
 
package org.jnode.vm;

import java.nio.ByteBuffer;
import java.nio.VMDirectByteBuffer;

import org.jnode.system.MemoryResource;
import org.jnode.system.MultiMediaMemoryResource;
import org.jnode.system.Resource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.scheduler.VmProcessor;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * Default implementation of MemoryResource.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
class MemoryResourceImpl extends Region implements MemoryResource {

    /** My parent */
    private final MemoryResourceImpl parent;
	/** Start address */
	protected final Address start;
	/** Exclusive end address */
	private final Address end;
	/** Size in bytes */
	private final Extent size;
	/** Has this resource been released? */
	private boolean released;
	/** First active memory-resource */
	private static Region resources;
	/** Data for mapping over byte arrays */
	private final Object data;
	/** Resource owner for byte arrays */
	private static final ResourceOwner BYTE_ARRAY_OWNER = new SimpleResourceOwner("byte-array");
	/** Size of an object reference */
	private final int slotSize;
	/** My children */
	private MemoryResourceImpl children;
	/** Offset relative to my parent */
	private final Offset offset;

	/**
	 * Create a new instance
	 * 
	 * @param owner
	 * @param start
	 * @param size
	 */
	protected MemoryResourceImpl(MemoryResourceImpl parent, ResourceOwner owner, Address start, Extent size) {
		super(owner);
		this.parent = parent;
		this.start = start;
		if (parent != null) {
		    this.offset = start.toWord().sub(parent.start.toWord()).toOffset();
		} else {
		    this.offset = start.toWord().toOffset();
		}
		this.end = start.add(size);
		this.size = size;
		this.released = false;
		this.data = null;
		this.slotSize = VmProcessor.current().getArchitecture().getReferenceSize();
	}

	/**
	 * Create a new instance
	 * 
	 * @param arrayData
	 * @param length
	 * @param elementSize
	 */
	public MemoryResourceImpl(Object arrayData, int length, int elementSize) {
		super(BYTE_ARRAY_OWNER);
		this.parent = null;
		this.data = arrayData;
		this.size = Extent.fromIntZeroExtend(length * elementSize);
		this.start = VmMagic.getArrayData(arrayData);
		this.offset = start.toWord().toOffset();
		this.end = start.add(length * elementSize);
		this.released = false;
		this.slotSize = VmProcessor.current().getArchitecture().getReferenceSize();
	}

    /**
     * Create a bytebuffer that has the same content as this resource.
     * @return a bytebuffer that has the same content as this resource
     */
    public ByteBuffer asByteBuffer() {
        return VMDirectByteBuffer.wrap(this);
    }
    
	/**
	 * Claim a memory region
	 * 
	 * @param owner
	 * @param start
	 * @param size
	 * @param mode
	 * @return The claimed resource
	 * @throws ResourceNotFreeException
	 */
	protected static synchronized MemoryResource claimMemoryResource(ResourceOwner owner, Address start, Extent size, int mode) throws ResourceNotFreeException {
		if (start != null) {
			final MemoryResourceImpl res = new MemoryResourceImpl(null, owner, start, size);
			if (isFree(resources, res)) {
				resources = add(resources, res);
				return res;
			} else {
				throw new ResourceNotFreeException();
			}
		} else {
			// Find a range
			Address ptr;
			if (mode == ResourceManager.MEMMODE_ALLOC_DMA) {
				ptr = Unsafe.getMinAddress();
			} else {
				ptr = Unsafe.getMemoryEnd();
			}
			MemoryResourceImpl res = new MemoryResourceImpl(null, owner, ptr, size);
			while (!isFree(resources, res)) {
				ptr = ptr.add(64 * 1024);
				res = new MemoryResourceImpl(null, owner, ptr, size);
			}
			resources = add(resources, res);
			return res;
		}
	}

	/**
	 * Gets a 8-bit signed byte at the given memory address
	 * 
	 * @param memPtr
	 * @return byte
	 */
	public byte getByte(int memPtr) {
		testMemPtr(memPtr, 1);
		return start.loadByte(Offset.fromIntZeroExtend(memPtr));
	}

	/**
	 * Gets multiple 8-bit signed bytes from the given memory address
	 * 
	 * @param memPtr
	 * @param dst
	 * @param dstOfs
	 * @param length
	 */
	public void getBytes(int memPtr, byte[] dst, int dstOfs, int length) {
		if (dstOfs < 0) {
			throw new IndexOutOfBoundsException("dstOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (dstOfs + length > dst.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(memPtr, length);
		final Address dstPtr = VmMagic.getArrayData(dst).add(dstOfs);
        final Extent size = Extent.fromIntZeroExtend(length);
		Unsafe.copy(start.add(Offset.fromIntZeroExtend(memPtr)), dstPtr, size);
	}

	/**
	 * Gets a 16-bit signed short at the given memory address
	 * 
	 * @param memPtr
	 * @return short
	 */
	public short getShort(int memPtr) {
		testMemPtr(memPtr, 2);
		return start.loadShort(Offset.fromIntZeroExtend(memPtr));
	}

	/**
	 * Gets multiple 16-bit signed bytes from the given memory address
	 * 
	 * @param memPtr
	 * @param dst
	 * @param dstOfs
	 * @param length
	 */
	public void getShorts(int memPtr, short[] dst, int dstOfs, int length) {
		if (dstOfs < 0) {
			throw new IndexOutOfBoundsException("dstOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (dstOfs + length > dst.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(memPtr, length * 2);
		final Address dstPtr = VmMagic.getArrayData(dst).add(dstOfs * 2);
        final Extent size = Extent.fromIntZeroExtend(length * 2);
		Unsafe.copy(start.add(Offset.fromIntZeroExtend(memPtr)), dstPtr, size);
	}

	/**
	 * Gets a 16-bit unsigned char at the given memory address
	 * 
	 * @param memPtr
	 * @return char
	 */
	public char getChar(int memPtr) {
		testMemPtr(memPtr, 2);
		return start.loadChar(Offset.fromIntZeroExtend(memPtr));
	}

	/**
	 * Gets multiple 16-bit unsigned chars from the given memory address
	 * 
	 * @param memPtr
	 * @param dst
	 * @param dstOfs
	 * @param length
	 */
	public void getChars(int memPtr, char[] dst, int dstOfs, int length) {
		if (dstOfs < 0) {
			throw new IndexOutOfBoundsException("dstOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (dstOfs + length > dst.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(memPtr, length * 2);
		final Address dstPtr = VmMagic.getArrayData(dst).add(dstOfs * 2);
        final Extent size = Extent.fromIntZeroExtend(length * 2);
		Unsafe.copy(start.add(Offset.fromIntZeroExtend(memPtr)), dstPtr, size);
	}

	/**
	 * Gets a 32-bit signed int at the given memory address
	 * 
	 * @param memPtr
	 * @return int
	 */
	public int getInt(int memPtr) {
		testMemPtr(memPtr, 4);
		return start.loadInt(Offset.fromIntZeroExtend(memPtr));
	}

	/**
	 * Gets multiple 32-bit signed ints from the given memory address
	 * 
	 * @param memPtr
	 * @param dst
	 * @param dstOfs
	 * @param length
	 */
	public void getInts(int memPtr, int[] dst, int dstOfs, int length) {
		if (dstOfs < 0) {
			throw new IndexOutOfBoundsException("dstOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (dstOfs + length > dst.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(memPtr, length * 4);
		final Address dstPtr = VmMagic.getArrayData(dst).add(dstOfs * 4);
        final Extent size = Extent.fromIntZeroExtend(length * 4);
		Unsafe.copy(start.add(Offset.fromIntZeroExtend(memPtr)), dstPtr, size);
	}

	/**
	 * Gets a 64-bit signed long at the given memory address
	 * 
	 * @param memPtr
	 * @return long
	 */
	public long getLong(int memPtr) {
		testMemPtr(memPtr, 8);
		return start.loadLong(Offset.fromIntZeroExtend(memPtr));
	}

	/**
	 * Gets multiple 64-bit signed longs from the given memory address
	 * 
	 * @param memPtr
	 * @param dst
	 * @param dstOfs
	 * @param length
	 */
	public void getLongs(int memPtr, long[] dst, int dstOfs, int length) {
		if (dstOfs < 0) {
			throw new IndexOutOfBoundsException("dstOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (dstOfs + length > dst.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(memPtr, length * 8);
		final Address dstPtr = VmMagic.getArrayData(dst).add(dstOfs * 8);
        final Extent size = Extent.fromIntZeroExtend(length * 8);
		Unsafe.copy(start.add(Offset.fromIntZeroExtend(memPtr)), dstPtr, size);
	}

	/**
	 * Gets a float at the given memory address
	 * 
	 * @param memPtr
	 * @return float
	 */
	public float getFloat(int memPtr) {
		testMemPtr(memPtr, 4);
		return start.loadFloat(Offset.fromIntZeroExtend(memPtr));
	}

	/**
	 * Gets multiple 32-bit floats from the given memory address
	 * 
	 * @param memPtr
	 * @param dst
	 * @param dstOfs
	 * @param length
	 */
	public void getFloats(int memPtr, float[] dst, int dstOfs, int length) {
		if (dstOfs < 0) {
			throw new IndexOutOfBoundsException("dstOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (dstOfs + length > dst.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(memPtr, length * 4);
		final Address dstPtr = VmMagic.getArrayData(dst).add(dstOfs * 4);
        final Extent size = Extent.fromIntZeroExtend(length * 4);
		Unsafe.copy(start.add(Offset.fromIntZeroExtend(memPtr)), dstPtr, size);
	}

	/**
	 * Gets a double at the given memory address
	 * 
	 * @param memPtr
	 * @return double
	 */
	public double getDouble(int memPtr) {
		testMemPtr(memPtr, 8);
		return start.loadDouble(Offset.fromIntZeroExtend(memPtr));
	}

	/**
	 * Gets multiple 64-bit doubles from the given memory address
	 * 
	 * @param memPtr
	 * @param dst
	 * @param dstOfs
	 * @param length
	 */
	public void getDoubles(int memPtr, double[] dst, int dstOfs, int length) {
		if (dstOfs < 0) {
			throw new IndexOutOfBoundsException("dstOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (dstOfs + length > dst.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(memPtr, length * 8);
		final Address dstPtr = VmMagic.getArrayData(dst).add(dstOfs * 8);
        final Extent size = Extent.fromIntZeroExtend(length * 8);
		Unsafe.copy(start.add(Offset.fromIntZeroExtend(memPtr)), dstPtr, size);
	}

	/**
	 * Gets a object reference at the given memory address
	 * 
	 * @param memPtr
	 * @return Object
	 */
	public Object getObject(int memPtr) {
		if (this.data != null) {
			throw new SecurityException("Cannot get an Object from a byte-array");
		}
		testMemPtr(memPtr, 4);
		return start.loadObjectReference(Offset.fromIntSignExtend(memPtr));
	}

	/**
	 * Sets a byte at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setByte(int memPtr, byte value) {
		testMemPtr(memPtr, 1);
		start.store(value, Offset.fromIntSignExtend(memPtr));
	}

	/**
	 * Sets multiple 8-bit signed bytes at the given memory address
	 * 
	 * @param src
	 * @param srcOfs
	 * @param dstPtr
	 * @param length
	 */
	public void setBytes(byte[] src, int srcOfs, int dstPtr, int length) {
		if (srcOfs < 0) {
			throw new IndexOutOfBoundsException("srcOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (srcOfs + length > src.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(dstPtr, length);
		final Address srcPtr = VmMagic.getArrayData(src).add(srcOfs);
        final Extent size = Extent.fromIntZeroExtend(length);
		Unsafe.copy(srcPtr, start.add(dstPtr), size);
	}

	/**
	 * Sets a char at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setChar(int memPtr, char value) {
		testMemPtr(memPtr, 2);
		start.store(value, Offset.fromIntSignExtend(memPtr));
	}

	/**
	 * Sets multiple 16-bit unsigned chars at the given memory address
	 * 
	 * @param src
	 * @param srcOfs
	 * @param dstPtr
	 * @param length
	 */
	public void setChars(char[] src, int srcOfs, int dstPtr, int length) {
		if (srcOfs < 0) {
			throw new IndexOutOfBoundsException("srcOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (srcOfs + length > src.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(dstPtr, length * 2);
		final Address srcPtr = VmMagic.getArrayData(src).add(srcOfs * 2);
        final Extent size = Extent.fromIntZeroExtend(length * 2);
		Unsafe.copy(srcPtr, start.add(dstPtr), size);
	}

	/**
	 * Sets a short at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setShort(int memPtr, short value) {
		testMemPtr(memPtr, 2);
		start.store(value, Offset.fromIntSignExtend(memPtr));
	}

	/**
	 * Sets multiple 16-bit signed shorts at the given memory address
	 * 
	 * @param src
	 * @param srcOfs
	 * @param dstPtr
	 * @param length
	 */
	public void setShorts(short[] src, int srcOfs, int dstPtr, int length) {
		if (srcOfs < 0) {
			throw new IndexOutOfBoundsException("srcOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (srcOfs + length > src.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(dstPtr, length * 2);
		final Address srcPtr = VmMagic.getArrayData(src).add(srcOfs * 2);
        final Extent size = Extent.fromIntZeroExtend(length * 2);
		Unsafe.copy(srcPtr, start.add(dstPtr), size);
	}

	/**
	 * Sets an int at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setInt(int memPtr, int value) {
		testMemPtr(memPtr, 4);
		start.store(value, Offset.fromIntSignExtend(memPtr));
	}

	/**
	 * Sets multiple 32-bit signed ints at the given memory address
	 * 
	 * @param src
	 * @param srcOfs
	 * @param dstPtr
	 * @param length
	 */
	public void setInts(int[] src, int srcOfs, int dstPtr, int length) {
		if (srcOfs < 0) {
			throw new IndexOutOfBoundsException("srcOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (srcOfs + length > src.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(dstPtr, length * 4);
		final Address srcPtr = VmMagic.getArrayData(src).add(srcOfs * 4);
        final Extent size = Extent.fromIntZeroExtend(length * 4);
		Unsafe.copy(srcPtr, start.add(dstPtr), size);
	}

	/**
	 * Sets a float at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setFloat(int memPtr, float value) {
		testMemPtr(memPtr, 4);
		start.store(value, Offset.fromIntSignExtend(memPtr));
	}

	/**
	 * Sets multiple 32-bit floats at the given memory address
	 * 
	 * @param src
	 * @param srcOfs
	 * @param dstPtr
	 * @param length
	 */
	public void setFloats(float[] src, int srcOfs, int dstPtr, int length) {
		if (srcOfs < 0) {
			throw new IndexOutOfBoundsException("srcOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (srcOfs + length > src.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(dstPtr, length * 4);
		final Address srcPtr = VmMagic.getArrayData(src).add(srcOfs * 4);
        final Extent size = Extent.fromIntZeroExtend(length * 4);
		Unsafe.copy(srcPtr, start.add(dstPtr), size);
	}

	/**
	 * Sets a long at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setLong(int memPtr, long value) {
		testMemPtr(memPtr, 8);
		start.store(value, Offset.fromIntSignExtend(memPtr));
	}

	/**
	 * Sets multiple 64-bit signed longs at the given memory address
	 * 
	 * @param src
	 * @param srcOfs
	 * @param dstPtr
	 * @param length
	 */
	public void setLongs(long[] src, int srcOfs, int dstPtr, int length) {
		if (srcOfs < 0) {
			throw new IndexOutOfBoundsException("srcOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (srcOfs + length > src.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(dstPtr, length * 8);
		final Address srcPtr = VmMagic.getArrayData(src).add(srcOfs * 8);
        final Extent size = Extent.fromIntZeroExtend(length * 8);
		Unsafe.copy(srcPtr, start.add(dstPtr), size);
	}

	/**
	 * Sets a double at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setDouble(int memPtr, double value) {
		testMemPtr(memPtr, 8);
		start.store(value, Offset.fromIntSignExtend(memPtr));
	}

	/**
	 * Sets multiple 64-bit doubles at the given memory address
	 * 
	 * @param src
	 * @param srcOfs
	 * @param dstPtr
	 * @param length
	 */
	public void setDoubles(double[] src, int srcOfs, int dstPtr, int length) {
		if (srcOfs < 0) {
			throw new IndexOutOfBoundsException("srcOfs < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}
		if (srcOfs + length > src.length) {
			throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
		}
		testMemPtr(dstPtr, length * 8);
		final Address srcPtr = VmMagic.getArrayData(src).add(srcOfs * 8);
        final Extent size = Extent.fromIntZeroExtend(length * 8);
		Unsafe.copy(srcPtr, start.add(dstPtr), size);
	}

	/**
	 * Sets a Object at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setObject(int memPtr, Object value) {
		if (this.data != null) {
			throw new SecurityException("Cannot set an Object in a byte-array");
		}
		testMemPtr(memPtr, 4);
		start.store(ObjectReference.fromObject(value), Offset.fromIntSignExtend(memPtr));
	}

	/**
	 * Fill the memory at the given memory address with size times 0 bytes.
	 * 
	 * memPtr must be VmObject.SLOT_SIZE aligned
	 * 
	 * size % VmObject.SLOT_SIZE must be 0
	 * 
	 * @param memPtr
	 * @param size
	 */
	public void clear(int memPtr, int size) {
		testMemPtr(memPtr, size);
		Unsafe.clear(start.add(Offset.fromIntZeroExtend(memPtr)), Extent.fromIntSignExtend(size));
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.system.MemoryResource#copy(int, int, int)
	 */
	public void copy(int srcMemPtr, int destMemPtr, int length) {
		testMemPtr(srcMemPtr, length);
		testMemPtr(destMemPtr, length);
        final Extent size = Extent.fromIntZeroExtend(length);
		Unsafe.copy(start.add(srcMemPtr), start.add(destMemPtr), size);
	}

	/**
	 * Remove a child from my list of children.
	 * @param child
	 */
	private synchronized final void removeChild(MemoryResourceImpl child) {
	    this.children = (MemoryResourceImpl)remove(this.children, child);
	}
	
	/**
	 * Give up this resource. After this method has been called, the resource cannot be used
	 * anymore.
	 */
	public final void release() {
	    if (!this.released) {
	        // Mark released as true
	        this.released = true;
	        
	        // Release all children
	        synchronized (this) {
	            while (this.children != null) {
	                this.children.release();
	            }
	        }
	        
	        if (parent != null) {
		        // Remove me from parent.
	            parent.removeChild(this);
	        } else if (data == null) {
	            // Remove me from global memory resource list
	            synchronized (getClass()) {
	                resources = remove(resources, this);
	            }
	        }
	    }
	}

	protected final void testMemPtr(int memPtr, int size) {
		if (released) {
			throw new IndexOutOfBoundsException("MemoryResource is released");
		}
		final Word end = Word.fromIntZeroExtend(memPtr + size);
		if ((memPtr < 0) || end.GT(this.size.toWord())) {
			throw new IndexOutOfBoundsException("At " + memPtr + ", this.size=" + this.size.toLong());
		}
	}

	/**
	 * Returns the size of this buffer in bytes.
	 * 
	 * @return int
	 */
	public Extent getSize() {
		return size;
	}

	/**
	 * Gets the address of the first byte of this buffer
	 * @return Address of first byte in buffer
	 */
	public Address getAddress() {
		return start;
	}

	/**
	 * Compare to regions.
	 * 
	 * @param otherRegion
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal
	 *         to, or greater than the specified region. If the regions overlap, 0 is returned.
	 */
	public int compareTo(Region otherRegion) {
		final MemoryResourceImpl other = (MemoryResourceImpl) otherRegion;
		if (this.end.LE(other.start)) {
			// this < other
			return -1;
		}
		if (this.start.GE(other.end)) {
			// this > other
			return 1;
		}
		// this overlaps other
		return 0;
	}

	/**
	 * Compare this region with a given address.
	 * 
	 * @param address
	 * @return a negative integer, zero, or a positive integer as this region is less than,
	 *         overlapping, or greater than the address.
	 */
	public int compareTo(VmAddress address) {
		final Address addr = Address.fromAddress(address);
		if (this.end.LE(addr)) {
			// this < address
			return -1;
		}
		if (this.start.GE(addr)) {
			// this > other
			return 1;
		}
		// this overlaps address
		return 0;
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setByte(int, byte, int)
	 */
	public void setByte(int memPtr, byte value, int count) {
		testMemPtr(memPtr, count);
		Unsafe.setBytes(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setChar(int, char, int)
	 */
	public void setChar(int memPtr, char value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.setChars(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setDouble(int, double, int)
	 */
	public void setDouble(int memPtr, double value, int count) {
		testMemPtr(memPtr, count * 8);
		Unsafe.setDoubles(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setFloat(int, float, int)
	 */
	public void setFloat(int memPtr, float value, int count) {
		testMemPtr(memPtr, count * 4);
		Unsafe.setFloats(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setInt24(int, int, int)
	 */
	public void setInt24(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 3);
		Unsafe.setInts24(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setInt(int, int, int)
	 */
	public void setInt(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 4);
		Unsafe.setInts(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setLong(int, long, int)
	 */
	public void setLong(int memPtr, long value, int count) {
		testMemPtr(memPtr, count * 8);
		Unsafe.setLongs(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setObject(int, java.lang.Object, int)
	 */
	public void setObject(int memPtr, Object value, int count) {
		testMemPtr(memPtr, count * slotSize);
		Unsafe.setObjects(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setShort(int, short, int)
	 */
	public void setShort(int memPtr, short value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.setShorts(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andByte(int, byte, int)
	 */
	public void andByte(int memPtr, byte value, int count) {
		testMemPtr(memPtr, count);
		Unsafe.andByte(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andChar(int, char, int)
	 */
	public void andChar(int memPtr, char value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.andChar(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	public void andInt24(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 3);
		Unsafe.andInt24(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andInt(int, int, int)
	 */
	public void andInt(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 4);
		Unsafe.andInt(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andLong(int, long, int)
	 */
	public void andLong(int memPtr, long value, int count) {
		testMemPtr(memPtr, count * 8);
		Unsafe.andLong(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andShort(int, short, int)
	 */
	public void andShort(int memPtr, short value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.andShort(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orByte(int, byte, int)
	 */
	public void orByte(int memPtr, byte value, int count) {
		testMemPtr(memPtr, count);
		Unsafe.orByte(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orChar(int, char, int)
	 */
	public void orChar(int memPtr, char value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.orChar(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	public void orInt24(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 3);
		Unsafe.orInt24(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orInt(int, int, int)
	 */
	public void orInt(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 4);
		Unsafe.orInt(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orLong(int, long, int)
	 */
	public void orLong(int memPtr, long value, int count) {
		testMemPtr(memPtr, count * 8);
		Unsafe.orLong(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orShort(int, short, int)
	 */
	public void orShort(int memPtr, short value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.orShort(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorByte(int, byte, int)
	 */
	public void xorByte(int memPtr, byte value, int count) {
		testMemPtr(memPtr, count);
		Unsafe.xorByte(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorChar(int, char, int)
	 */
	public void xorChar(int memPtr, char value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.xorChar(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorInt(int, int, int)
	 */
	public void xorInt24(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 3);
		Unsafe.xorInt24(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorInt(int, int, int)
	 */
	public void xorInt(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 4);
		Unsafe.xorInt(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorLong(int, long, int)
	 */
	public void xorLong(int memPtr, long value, int count) {
		testMemPtr(memPtr, count * 8);
		Unsafe.xorLong(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorShort(int, short, int)
	 */
	public void xorShort(int memPtr, short value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.xorShort(start.add(Offset.fromIntZeroExtend(memPtr)), value, count);
	}

	/**
	 * Get a memory resource for a portion of this memory resources.
	 * The first area of this memory resource that fits the given size
	 * and it not claimed by any child resource is returned.
	 * If not large enought area if found, a ResourceNotFreeException is thrown.
	 * A child resource is always releases when the parent is released.
	 * A child resource can be released without releasing the parent.
	 * 
	 * @param size Length of the returned resource in bytes.
	 * @param align 
	 * @return a memory resource for a portion of this memory resources
	 * @throws IndexOutOfBoundsException 
	 * @throws ResourceNotFreeException 
	 */
	public MemoryResource claimChildResource(Extent size, int align)
	throws IndexOutOfBoundsException, ResourceNotFreeException {
		if (released) {
			throw new IndexOutOfBoundsException("MemoryResource is released");
		}
	    if (align <= 0) {
	        throw new IllegalArgumentException("Align must be >= 1");
	    }
	    
	    Offset offset = Offset.zero();
	    final Word alignMask = Word.fromIntZeroExtend(align - 1);
	    while (true) {
		    final Address addr = this.start.add(offset);
		    final MemoryResourceImpl child = new MemoryResourceImpl(this, getOwner(), addr, size);
		    final MemoryResourceImpl existingChild = (MemoryResourceImpl)get(this.children, child);
		    if (existingChild == null) {
		        // We found a free region
		        this.children = (MemoryResourceImpl)add(this.children, child);
		        return child;
		    }
		    // We found an existing child, skip over that.
		    offset = existingChild.getOffset().add(existingChild.getSize());
		    
		    // Align the new offset
		    if (!offset.toWord().and(alignMask).isZero()) {
		        offset = offset.toWord().add(alignMask).and(alignMask.not()).toOffset();
		    }
		    
		    // Do we have space left?
	        if (offset.toWord().add(size).GT(this.size.toWord())) {
	            throw new ResourceNotFreeException();
	        }
	    }	    
	}
	
	/**
	 * Get a memory resource for a portion of this memory resources.
	 * The first area of this memory resource that fits the given size
	 * and it not claimed by any child resource is returned.
	 * If not large enought area if found, a ResourceNotFreeException is thrown.
	 * A child resource is always releases when the parent is released.
	 * A child resource can be released without releasing the parent.
	 * 
	 * @param size Length of the returned resource in bytes.
	 * @param align Align of this boundary. Align must be a multiple of 2.
	 * @return a memory resource for a portion of this memory resources
	 * @throws IndexOutOfBoundsException 
	 * @throws ResourceNotFreeException 
	 */
	public MemoryResource claimChildResource(int size, int align)
			throws IndexOutOfBoundsException, ResourceNotFreeException {
		return claimChildResource(Extent.fromIntZeroExtend(size), align);
	}
	
	/**
	 * Get a memory resource for a portion of this memory resources.
	 * A child resource is always releases when the parent is released.
	 * A child resource can be released without releasing the parent.
	 * 
	 * @param offset Offset relative to the start of this resource.
	 * @param size Length of the returned resource in bytes.
	 * @param allowOverlaps If true, overlapping child resources will be allowed, otherwise overlapping child resources will resulut in a ResourceNotFreeException.
	 * @return  a memory resource for a portion of this memory resources
	 * @throws IndexOutOfBoundsException 
	 * @throws ResourceNotFreeException 
	 */
	public MemoryResource claimChildResource(Offset offset, Extent size, boolean allowOverlaps)
	throws IndexOutOfBoundsException, ResourceNotFreeException {
		if (released) {
			throw new IndexOutOfBoundsException("MemoryResource is released");
		}
	    if (offset.toWord().add(size).GT(this.size.toWord())) {
	        throw new IndexOutOfBoundsException("Offset + size > this.size");	        
	    }
	    final Address addr = this.start.add(offset);
	    final MemoryResourceImpl child = new MemoryResourceImpl(this, getOwner(), addr, size);
	    synchronized (this) {
	        // Re-test released flag
			if (released) {
				throw new IndexOutOfBoundsException("MemoryResource is released");
			}
			if (!allowOverlaps) {
			    if (!isFree(this.children, child)) {
			        throw new ResourceNotFreeException();
			    }
			}
	        this.children = (MemoryResourceImpl)add(this.children, child);
	    }
	    return child;
	}
	
	/**
	 * Get a memory resource for a portion of this memory resources.
	 * A child resource is always releases when the parent is released.
	 * A child resource can be released without releasing the parent.
	 * 
	 * @param offset Offset relative to the start of this resource.
	 * @param size Length of the returned resource in bytes.
	 * @param allowOverlaps If true, overlapping child resources will be allowed, otherwise overlapping child resources will resulut in a ResourceNotFreeException.
	 * @return a memory resource for a portion of this memory resource
	 * @throws IndexOutOfBoundsException 
	 * @throws ResourceNotFreeException 
	 */
	public MemoryResource claimChildResource(int offset, int size, boolean allowOverlaps)
	throws IndexOutOfBoundsException, ResourceNotFreeException {
		return claimChildResource(Offset.fromIntZeroExtend(offset), Extent.fromIntZeroExtend(size), allowOverlaps);
	}
	
    /**
     * Creates a multi media memory resource wrapping this given memory resource.
     * @return The created instance. This will never be null.
     */
    public final MultiMediaMemoryResource asMultiMediaMemoryResource() {
        final MultiMediaMemoryResourceImpl child;
        child = Vm.getArch().createMultiMediaMemoryResource(this);
        this.children = (MemoryResourceImpl)add(this.children, child);
        return child;
    }
    
	/**
	 * Gets the parent resource if any.
	 * @return The parent resource, or null if this resource has no parent.
	 */
	public final Resource getParent() {
	    return parent;
	}	
	
	/**
	 * Gets the offset relative to my parent.
	 * If this resource has no parent, the address of this buffer is returned.
	 * @return the offset relative to my parent or address of this buffer
	 */
	public final Offset getOffset() {
	    return this.offset;
	}
}
