/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.nio.ByteOrder;

import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmSystemObject;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmStatics extends VmSystemObject {

	private static final int SIZE = 1 << 16;
	private static final byte TYPE_INT = 0x01;
	private static final byte TYPE_LONG = 0x02;
	private static final byte TYPE_OBJECT = 0x03;
	private static final byte TYPE_ADDRESS = 0x04;
	private static final byte TYPE_METHOD = 0x05;

	private int[] statics;
	private byte[] types;
	private transient Object[] objects;
	private int next;
	private final int slotLength;
	private final boolean lsbFirst;
	private transient ObjectResolver resolver;

	static int staticFieldCount;
	static int staticMethodCount;
	static int methodCount;
	static int typeCount;

	/**
	 * Initialize this instance
	 */
	public VmStatics(VmArchitecture arch, ObjectResolver resolver) {
		this.statics = new int[SIZE];
		this.types = new byte[SIZE];
		this.objects = new Object[SIZE];
		this.lsbFirst = (arch.getByteOrder() == ByteOrder.LITTLE_ENDIAN);
		this.slotLength = arch.getReferenceSize() >> 2;
		this.resolver = resolver;
	}

	/**
	 * Allocate an int/float type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocIntField() {
		return alloc(TYPE_INT, 1);
	}

	/**
	 * Allocate an long/double type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocLongField() {
		return alloc(TYPE_LONG, 2);
	}

	/**
	 * Allocate an Object type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocObjectField() {
		return alloc(TYPE_OBJECT, slotLength);
	}

	/**
	 * Allocate an org.jnode.vm.Address type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocAddressField() {
		return alloc(TYPE_ADDRESS, slotLength);
	}

	/**
	 * Allocate an org.jnode.vm.classmgr.VmMethod type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocMethod() {
		return alloc(TYPE_METHOD, slotLength);
	}

	final void setInt(int idx, int value) {
		if (types[idx] != TYPE_INT) {
			throw new IllegalArgumentException("Type error " + types[idx]);
		}
		statics[idx] = value;
	}

	final void setObject(int idx, Object value) {
		if (types[idx] != TYPE_OBJECT) {
			throw new IllegalArgumentException("Type error " + types[idx]);
		}
		setRawObject(idx, value);
	}

	final void setMethod(int idx, VmMethod value) {
		if (types[idx] != TYPE_METHOD) {
			throw new IllegalArgumentException("Type error " + types[idx]);
		}
		setRawObject(idx, value);
	}

	private final void setRawObject(int idx, Object value) {
		if (objects != null) {
			objects[idx] = value;
		} else {
			final ObjectResolver resolver = getResolver();
			if (slotLength == 1) {
				statics[idx] = resolver.addressOf32(value);
			} else {
				final long lvalue = resolver.addressOf64(value);
				if (lsbFirst) {
					statics[idx + 0] = (int) (lvalue & 0xFFFFFFFFL);
					statics[idx + 1] = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
				} else {
					statics[idx + 1] = (int) (lvalue & 0xFFFFFFFFL);
					statics[idx + 0] = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
				}
			}
		}
	}

	final void setLong(int idx, long value) {
		if (types[idx] != TYPE_LONG) {
			throw new IllegalArgumentException("Type error " + types[idx]);
		}
		if (lsbFirst) {
			statics[idx + 0] = (int) (value & 0xFFFFFFFFL);
			statics[idx + 1] = (int) ((value >>> 32) & 0xFFFFFFFFL);
		} else {
			statics[idx + 1] = (int) (value & 0xFFFFFFFFL);
			statics[idx + 0] = (int) ((value >>> 32) & 0xFFFFFFFFL);
		}
	}

	/**
	 * Allocate an entry.
	 * 
	 * @param type
	 * @param length
	 * @return the index of the allocated entry.
	 */
	private final synchronized int alloc(byte type, int length) {
		final int idx = next;
		types[idx] = type;
		next += length;
		return idx;
	}

	/**
	 * Get the full statics table.
	 * 
	 * @return The statics table.
	 */
	public final Object getTable() {
		return statics;
	}

	/**
	 * Let all objects in this statics-table make a visit to the given visitor.
	 * 
	 * @param visitor
	 */
	public void walk(ObjectVisitor visitor, ObjectResolver resolver) {
		final int[] table;
		final byte[] types;
		final int length;
		synchronized (this) {
			table = this.statics;
			types = this.types;
			length = this.next;
		}
		if (slotLength == 1) {
			for (int i = 0; i < length; i++) {
				final byte type = types[i];
				if (type == TYPE_OBJECT) {
					final Object object;
					object = resolver.objectAt32(table[i]);
					if (object != null) {
						final boolean rc = visitor.visit(object);
						if (!rc) {
							i = length;
						}
					}
				}
			}
		} else {
			throw new RuntimeException("SlotSize != 1 not implemented yet");
		}
	}

	private final ObjectResolver getResolver() {
		if (resolver == null) {
			resolver = new Unsafe.UnsafeObjectResolver();
		}
		return resolver;
	}

	public final void dumpStatistics() {
		System.out.println("#static fields  " + staticFieldCount);
		System.out.println("#static methods " + staticMethodCount);
		System.out.println("#methods        " + methodCount);
		System.out.println("#types          " + typeCount);
		System.out.println("table.length    " + next);
	}

	/**
	 * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
	 */
	public void verifyBeforeEmit() {
		final int max = statics.length;
		for (int i = 0; i < max; i++) {
			Object value = objects[i];
			if (value != null) {
				if (slotLength == 1) {
					statics[i] = resolver.addressOf32(value);
				} else {
					final long lvalue = resolver.addressOf64(value);
					if (lsbFirst) {
						statics[i + 0] = (int) (lvalue & 0xFFFFFFFFL);
						statics[i + 1] = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
					} else {
						statics[i + 1] = (int) (lvalue & 0xFFFFFFFFL);
						statics[i + 0] = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
					}
				}
			}
		}
	}
}
