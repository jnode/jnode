/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.nio.ByteOrder;

import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.ObjectVisitor;
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

	private int[] statics;
	private byte[] types;
	private int next;
	private final int slotLength;
	static int staticFieldCount;
	static int staticMethodCount;
	static int typeCount;
	private final boolean lsbFirst;

	/**
	 * Initialize this instance
	 */
	public VmStatics(VmArchitecture arch) {
		this.statics = new int[SIZE];
		this.types = new byte[SIZE];
		this.lsbFirst = (arch.getByteOrder() == ByteOrder.LITTLE_ENDIAN);
		this.slotLength = arch.getReferenceSize() >> 2;
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

	final void setInt(int idx, int value) {
		if (types[idx] != TYPE_INT) {
			throw new IllegalArgumentException("Type error " + types[idx]);
		}
		statics[idx] = value;
	}

	final void setObject(int idx, Object value, ObjectResolver resolver) {
		if (types[idx] != TYPE_OBJECT) {
			throw new IllegalArgumentException("Type error " + types[idx]);
		}
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

	public static final void dumpStatistics() {
		System.out.println("#static fields  " + staticFieldCount);
		System.out.println("#static methods " + staticMethodCount);
		System.out.println("#types          " + typeCount);
	}

	public static void main(String[] args) {
		dumpStatistics();
	}
}
