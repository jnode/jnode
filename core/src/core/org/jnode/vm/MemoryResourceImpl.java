/*
 * $Id$
 */
package org.jnode.vm;

import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.vm.classmgr.VmArray;

/**
 * Default implementation of MemoryResource.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class MemoryResourceImpl extends Region implements MemoryResource {

	/** Start address */
	private final Address start;
	/** Exclusive end address */
	private final Address end;
	/** Size in bytes */
	private final long size;
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

	/**
	 * Create a new instance
	 * 
	 * @param owner
	 * @param start
	 * @param size
	 */
	private MemoryResourceImpl(ResourceOwner owner, Address start, long size) {
		super(owner);
		this.start = start;
		this.end = Unsafe.add(start, Unsafe.longToAddress(size));
		this.size = size;
		this.released = false;
		this.data = null;
		this.slotSize = Unsafe.getCurrentProcessor().getArchitecture().getReferenceSize();
	}

	public MemoryResourceImpl(Object arrayData, int length, int elementSize) {
		super(BYTE_ARRAY_OWNER);
		this.data = arrayData;
		this.size = length * elementSize;
		this.start = Address.addressOfArrayData(arrayData);
		this.end = Unsafe.add(start, length * elementSize);
		this.released = false;
		this.slotSize = Unsafe.getCurrentProcessor().getArchitecture().getReferenceSize();
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
	protected static synchronized MemoryResource claimMemoryResource(ResourceOwner owner, Address start, long size, int mode) throws ResourceNotFreeException {
		if (start != null) {
			final MemoryResourceImpl res = new MemoryResourceImpl(owner, start, size);
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
			MemoryResourceImpl res = new MemoryResourceImpl(owner, ptr, size);
			while (!isFree(resources, res)) {
				ptr = Unsafe.add(ptr, 64 * 1024);
				res = new MemoryResourceImpl(owner, ptr, size);
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
		return Unsafe.getByte(start, memPtr);
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
		Address dstPtr = Unsafe.add(Unsafe.addressOf(dst), (VmArray.DATA_OFFSET * slotSize) + dstOfs);
		Unsafe.copy(Unsafe.add(start, memPtr), dstPtr, length);
	}

	/**
	 * Gets a 16-bit signed short at the given memory address
	 * 
	 * @param memPtr
	 * @return short
	 */
	public short getShort(int memPtr) {
		testMemPtr(memPtr, 2);
		return Unsafe.getShort(start, memPtr);
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
		Address dstPtr = Unsafe.add(Unsafe.addressOf(dst), (VmArray.DATA_OFFSET * slotSize) + (dstOfs * 2));
		Unsafe.copy(Unsafe.add(start, memPtr), dstPtr, length * 2);
	}

	/**
	 * Gets a 16-bit unsigned char at the given memory address
	 * 
	 * @param memPtr
	 * @return char
	 */
	public char getChar(int memPtr) {
		testMemPtr(memPtr, 2);
		return Unsafe.getChar(start, memPtr);
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
		Address dstPtr = Unsafe.add(Unsafe.addressOf(dst), (VmArray.DATA_OFFSET * slotSize) + (dstOfs * 2));
		Unsafe.copy(Unsafe.add(start, memPtr), dstPtr, length * 2);
	}

	/**
	 * Gets a 32-bit signed int at the given memory address
	 * 
	 * @param memPtr
	 * @return int
	 */
	public int getInt(int memPtr) {
		testMemPtr(memPtr, 4);
		return Unsafe.getInt(start, memPtr);
	}

	/**
	 * Gets multiple 32-bit signed ints from the given memory address
	 * 
	 * @param memPtr
	 * @param dst
	 * @param dstOfs
	 * @param length
	 */
	public void getInts(int memPtr, char[] dst, int dstOfs, int length) {
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
		Address dstPtr = Unsafe.add(Unsafe.addressOf(dst), (VmArray.DATA_OFFSET * slotSize) + (dstOfs * 4));
		Unsafe.copy(Unsafe.add(start, memPtr), dstPtr, length * 4);
	}

	/**
	 * Gets a 64-bit signed long at the given memory address
	 * 
	 * @param memPtr
	 * @return long
	 */
	public long getLong(int memPtr) {
		testMemPtr(memPtr, 8);
		return Unsafe.getLong(start, memPtr);
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
		Address dstPtr = Unsafe.add(Unsafe.addressOf(dst), (VmArray.DATA_OFFSET * slotSize) + (dstOfs * 8));
		Unsafe.copy(Unsafe.add(start, memPtr), dstPtr, length * 8);
	}

	/**
	 * Gets a float at the given memory address
	 * 
	 * @param memPtr
	 * @return float
	 */
	public float getFloat(int memPtr) {
		testMemPtr(memPtr, 4);
		return Unsafe.getFloat(start, memPtr);
	}

	/**
	 * Gets multiple 32-bit floats from the given memory address
	 * 
	 * @param memPtr
	 * @param dst
	 * @param dstOfs
	 * @param length
	 */
	public void getChars(int memPtr, float[] dst, int dstOfs, int length) {
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
		Address dstPtr = Unsafe.add(Unsafe.addressOf(dst), (VmArray.DATA_OFFSET * slotSize) + (dstOfs * 4));
		Unsafe.copy(Unsafe.add(start, memPtr), dstPtr, length * 4);
	}

	/**
	 * Gets a double at the given memory address
	 * 
	 * @param memPtr
	 * @return double
	 */
	public double getDouble(int memPtr) {
		testMemPtr(memPtr, 8);
		return Unsafe.getDouble(start, memPtr);
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
		Address dstPtr = Unsafe.add(Unsafe.addressOf(dst), (VmArray.DATA_OFFSET * slotSize) + (dstOfs * 8));
		Unsafe.copy(Unsafe.add(start, memPtr), dstPtr, length * 8);
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
		return Unsafe.getObject(start, memPtr);
	}

	/**
	 * Sets a byte at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setByte(int memPtr, byte value) {
		testMemPtr(memPtr, 1);
		Unsafe.setByte(start, memPtr, value);
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
		Address srcPtr = Unsafe.add(Unsafe.addressOf(src), (VmArray.DATA_OFFSET * slotSize) + srcOfs);
		Unsafe.copy(srcPtr, Unsafe.add(start, dstPtr), length);
	}

	/**
	 * Sets a char at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setChar(int memPtr, char value) {
		testMemPtr(memPtr, 2);
		Unsafe.setChar(start, memPtr, value);
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
		Address srcPtr = Unsafe.add(Unsafe.addressOf(src), (VmArray.DATA_OFFSET * slotSize) + (srcOfs * 2));
		Unsafe.copy(srcPtr, Unsafe.add(start, dstPtr), length * 2);
	}

	/**
	 * Sets a short at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setShort(int memPtr, short value) {
		testMemPtr(memPtr, 2);
		Unsafe.setShort(start, memPtr, value);
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
		Address srcPtr = Unsafe.add(Unsafe.addressOf(src), (VmArray.DATA_OFFSET * slotSize) + (srcOfs * 2));
		Unsafe.copy(srcPtr, Unsafe.add(start, dstPtr), length * 2);
	}

	/**
	 * Sets an int at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setInt(int memPtr, int value) {
		testMemPtr(memPtr, 4);
		Unsafe.setInt(start, memPtr, value);
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
		Address srcPtr = Unsafe.add(Unsafe.addressOf(src), (VmArray.DATA_OFFSET * slotSize) + (srcOfs * 4));
		Unsafe.copy(srcPtr, Unsafe.add(start, dstPtr), length * 4);
	}

	/**
	 * Sets a float at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setFloat(int memPtr, float value) {
		testMemPtr(memPtr, 4);
		Unsafe.setFloat(start, memPtr, value);
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
		Address srcPtr = Unsafe.add(Unsafe.addressOf(src), (VmArray.DATA_OFFSET * slotSize) + (srcOfs * 4));
		Unsafe.copy(srcPtr, Unsafe.add(start, dstPtr), length * 4);
	}

	/**
	 * Sets a long at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setLong(int memPtr, long value) {
		testMemPtr(memPtr, 8);
		Unsafe.setLong(start, memPtr, value);
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
		Address srcPtr = Unsafe.add(Unsafe.addressOf(src), (VmArray.DATA_OFFSET * slotSize) + (srcOfs * 8));
		Unsafe.copy(srcPtr, Unsafe.add(start, dstPtr), length * 8);
	}

	/**
	 * Sets a double at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	public void setDouble(int memPtr, double value) {
		testMemPtr(memPtr, 8);
		Unsafe.setDouble(start, memPtr, value);
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
		Address srcPtr = Unsafe.add(Unsafe.addressOf(src), (VmArray.DATA_OFFSET * slotSize) + (srcOfs * 8));
		Unsafe.copy(srcPtr, Unsafe.add(start, dstPtr), length * 8);
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
		Unsafe.setObject(start, memPtr, value);
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
		Unsafe.clear(Unsafe.add(start, memPtr), size);
	}

	public void copy(int srcMemPtr, int destMemPtr, int size) {
		testMemPtr(srcMemPtr, size);
		testMemPtr(destMemPtr, size);
		Unsafe.copy(Unsafe.add(start, srcMemPtr), Unsafe.add(start, destMemPtr), size);
	}

	/**
	 * Give up this resource. After this method has been called, the resource cannot be used
	 * anymore.
	 */
	public void release() {
		if (data == null) {
			if (!this.released) {
				this.released = true;
				synchronized (getClass()) {
					resources = remove(resources, this);
				}
			}
		}
	}

	private void testMemPtr(int memPtr, int size) {
		if (released) {
			throw new IndexOutOfBoundsException("MemoryResource is released");
		}
		if ((memPtr < 0) || ((memPtr + size) > this.size)) {
			throw new IndexOutOfBoundsException("At " + memPtr + ", this.size=" + this.size);
		}
	}

	/**
	 * Returns the size of this buffer in bytes.
	 * 
	 * @return int
	 */
	public long getSize() {
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
		int rc = Unsafe.compare(this.end, other.start);
		if (rc <= 0) {
			// this < other
			return -1;
		}
		rc = Unsafe.compare(this.start, other.end);
		if (rc >= 0) {
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
	public int compareTo(Address address) {
		int rc = Unsafe.compare(this.end, address);
		if (rc <= 0) {
			// this < address
			return -1;
		}
		rc = Unsafe.compare(this.start, address);
		if (rc > 0) {
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
		Unsafe.setBytes(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setChar(int, char, int)
	 */
	public void setChar(int memPtr, char value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.setChars(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setDouble(int, double, int)
	 */
	public void setDouble(int memPtr, double value, int count) {
		testMemPtr(memPtr, count * 8);
		Unsafe.setDoubles(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setFloat(int, float, int)
	 */
	public void setFloat(int memPtr, float value, int count) {
		testMemPtr(memPtr, count * 4);
		Unsafe.setFloats(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setInt24(int, int, int)
	 */
	public void setInt24(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 3);
		Unsafe.setInts24(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setInt(int, int, int)
	 */
	public void setInt(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 4);
		Unsafe.setInts(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setLong(int, long, int)
	 */
	public void setLong(int memPtr, long value, int count) {
		testMemPtr(memPtr, count * 8);
		Unsafe.setLongs(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setObject(int, java.lang.Object, int)
	 */
	public void setObject(int memPtr, Object value, int count) {
		testMemPtr(memPtr, count * slotSize);
		Unsafe.setObjects(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#setShort(int, short, int)
	 */
	public void setShort(int memPtr, short value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.setShorts(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andByte(int, byte, int)
	 */
	public void andByte(int memPtr, byte value, int count) {
		testMemPtr(memPtr, count);
		Unsafe.andByte(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andChar(int, char, int)
	 */
	public void andChar(int memPtr, char value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.andChar(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andInt24(int, int, int)
	 */
	public void andInt24(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 3);
		Unsafe.andInt24(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andInt(int, int, int)
	 */
	public void andInt(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 4);
		Unsafe.andInt(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andLong(int, long, int)
	 */
	public void andLong(int memPtr, long value, int count) {
		testMemPtr(memPtr, count * 8);
		Unsafe.andLong(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#andShort(int, short, int)
	 */
	public void andShort(int memPtr, short value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.andShort(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orByte(int, byte, int)
	 */
	public void orByte(int memPtr, byte value, int count) {
		testMemPtr(memPtr, count);
		Unsafe.orByte(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orChar(int, char, int)
	 */
	public void orChar(int memPtr, char value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.orChar(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orInt24(int, int, int)
	 */
	public void orInt24(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 3);
		Unsafe.orInt24(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orInt(int, int, int)
	 */
	public void orInt(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 4);
		Unsafe.orInt(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orLong(int, long, int)
	 */
	public void orLong(int memPtr, long value, int count) {
		testMemPtr(memPtr, count * 8);
		Unsafe.orLong(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#orShort(int, short, int)
	 */
	public void orShort(int memPtr, short value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.orShort(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorByte(int, byte, int)
	 */
	public void xorByte(int memPtr, byte value, int count) {
		testMemPtr(memPtr, count);
		Unsafe.xorByte(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorChar(int, char, int)
	 */
	public void xorChar(int memPtr, char value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.xorChar(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorInt(int, int, int)
	 */
	public void xorInt24(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 3);
		Unsafe.xorInt24(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorInt(int, int, int)
	 */
	public void xorInt(int memPtr, int value, int count) {
		testMemPtr(memPtr, count * 4);
		Unsafe.xorInt(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorLong(int, long, int)
	 */
	public void xorLong(int memPtr, long value, int count) {
		testMemPtr(memPtr, count * 8);
		Unsafe.xorLong(Unsafe.add(start, memPtr), value, count);
	}

	/**
	 * @param memPtr
	 * @param value
	 * @param count
	 * @see org.jnode.system.MemoryResource#xorShort(int, short, int)
	 */
	public void xorShort(int memPtr, short value, int count) {
		testMemPtr(memPtr, count * 2);
		Unsafe.xorShort(Unsafe.add(start, memPtr), value, count);
	}
}
