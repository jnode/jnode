/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.io.PrintStream;
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
	private static final byte TYPE_STRING = 0x06;
	private static final byte TYPE_CLASS = 0x07;
	private static final byte MAX_TYPE = TYPE_CLASS;

	private int[] statics;
	private byte[] types;
	private final int[] typeCounter = new int[MAX_TYPE + 1];
	private transient Object[] objects;
	private int next;
	private final int slotLength;
	private final boolean lsbFirst;
	private transient ObjectResolver resolver;
	private transient boolean locked;

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
	 * Allocate an String type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocConstantStringField(String value) {
		final int idx = alloc(TYPE_STRING, slotLength);
		setRawObject(idx, value);
		return idx;
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
	 * Allocate an {@link org.jnode.vm.classmgr.VmMethod} type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocMethod(VmMethod method) {
		final int idx = alloc(TYPE_METHOD, slotLength);
		setRawObject(idx, method);
		return idx;
	}

	/**
	 * Allocate an {@link org.jnode.vm.classmgr.VmType} type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocClass(VmType type) {
		final int idx = alloc(TYPE_CLASS, slotLength);
		setRawObject(idx, type);
		return idx;
	}

	final void setInt(int idx, int value) {
		if (locked) {
			throw new RuntimeException("Locked");
		}
		if (types[idx] != TYPE_INT) {
			throw new IllegalArgumentException("Type error " + types[idx]);
		}
		statics[idx] = value;
	}

	final void setObject(int idx, Object value) {
		if (locked) {
			throw new RuntimeException("Locked");
		}
		if (types[idx] != TYPE_OBJECT) {
			throw new IllegalArgumentException("Type error " + types[idx]);
		}
		setRawObject(idx, value);
	}

	/*final void setMethod(int idx, VmMethod value) {
		if (locked) {
			throw new RuntimeException("Locked");
		}
		if (types[idx] != TYPE_METHOD) {
			throw new IllegalArgumentException("Type error " + types[idx]);
		}
		setRawObject(idx, value);
	}*/

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
		if (locked) {
			throw new RuntimeException("Locked");
		}
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
		if (locked) {
			throw new RuntimeException("Locked");
		}
		final int idx = next;
		types[idx] = type;
		typeCounter[type]++;
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
	 * Get the statics type at a given index
	 * 
	 * @return int
	 */
	public final int getType(int index) {
		return types[index];
	}

	/**
	 * Let all objects in this statics-table make a visit to the given visitor.
	 * 
	 * @param visitor
	 */
	public void walk(ObjectVisitor visitor, ObjectResolver resolver) {
		walk(TYPE_OBJECT, visitor, resolver);
	}

	/**
	 * Let all methods in this statics-table make a visit to the given visitor.
	 * 
	 * @param visitor
	 */
	public void walkMethods(ObjectVisitor visitor) {
		walk(TYPE_METHOD, visitor, getResolver());
	}

	/**
	 * Let all objects in this statics-table make a visit to the given visitor.
	 * 
	 * @param visitor
	 */
	private void walk(int visitType, ObjectVisitor visitor, ObjectResolver resolver) {
		final int[] table;
		final byte[] types;
		final int length;
		table = this.statics;
		types = this.types;
		length = this.next;
		if (slotLength == 1) {
			for (int i = 0; i < length; i++) {
				final byte type = types[i];
				if (type == visitType) {
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

	public final void dumpStatistics(PrintStream out) {
		out.println("#static int fields  " + typeCounter[TYPE_INT]);
		out.println("#static long fields " + typeCounter[TYPE_LONG]);
		out.println("#methods            " + typeCounter[TYPE_METHOD]);
		out.println("#types              " + typeCounter[TYPE_CLASS]);
		out.println("table.length        " + next);
	}

	/**
	 * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
	 */
	public void verifyBeforeEmit() {
		//System.out.println("VmStatics#verifyBeforeEmit " + slotLength + ", " + resolver);
		final int max = statics.length;
		int count = 0;
		for (int i = 0; i < max; i++) {
			final Object value = objects[i];
			if (value != null) {
				count++;
				if (slotLength == 1) {
					statics[i] = resolver.addressOf32(value);
					if (statics[i] == 0) {
						throw new RuntimeException("addressof32(" + value + ") is null");
					}
				} else {
					final long lvalue = resolver.addressOf64(value);
					if (lvalue == 0L) {
						throw new RuntimeException("addressof64(" + value + ") is null");
					}
					if (lsbFirst) {
						statics[i + 0] = (int) (lvalue & 0xFFFFFFFFL);
						statics[i + 1] = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
					} else {
						statics[i + 1] = (int) (lvalue & 0xFFFFFFFFL);
						statics[i + 0] = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
					}
				}
			} else if (types[i] == TYPE_METHOD) {
				throw new RuntimeException("Method is null");
			}
		}
		objects = null;
		locked = true;
		//System.out.println("VmStatics#verifyBeforeEmit count=" + count);
	}
}
