/**
* VmCP.java
**/

package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;

/**
* A VmCP is the runtime representation of a constant pool
**/
public final class VmCP extends VmSystemObject {

	public static final int TYPE_UTF8 = 1;
	public static final int TYPE_INT = 3;
	public static final int TYPE_FLOAT = 4;
	public static final int TYPE_LONG = 5;
	public static final int TYPE_DOUBLE = 6;
	public static final int TYPE_CLASSREF = 7;
	public static final int TYPE_STRING = 8;
	public static final int TYPE_FIELDREF = 9;
	public static final int TYPE_METHODREF = 10;
	public static final int TYPE_IMETHODREF = 11;
	public static final int TYPE_NAMEANDTYPE = 12;

	private Object[] cp;
	private byte[] tags;
	private int used;

	/**
	 * Construct a new VmCP with a given number of entries
	 * @param count
	 * @param tags
	 */
	protected VmCP(int count, byte[] tags) {
		this.cp = new Object[count];
		this.tags = tags;
		this.used = count;
	}

	/**
	 * Gets the number of enntries in this CP
	 * @return int
	 */
	public int getLength() {
		return cp.length;
	}

	/**
	 * Read an int out of this CP
	 * @param index		The index where to read
	 * @return int
	 */
	public int getInt(int index) {
		if (index == 0)
			return 0;
		else
			return ((Integer) get(index)).intValue();
	}

	/**
	 * Write an int into this CP
	 * @param index		The index where to read
	 * @param data		The int to write
	 */
	protected void setInt(int index, int data) {
		set(index, new Integer(data));
	}

	/**
	 * Add an int to this CP
	 * @param data
	 * @return The index of the new entry
	 */	
	public int addInt(int data) {
		return add(new Integer(data), TYPE_INT);
	}

	/**
	 * Read a long out of this CP
	 * @param index		The index where to read
	 * @return long
	 */
	public long getLong(int index) {
		return ((Long) get(index)).longValue();
	}

	/**
	 * Write a long into this CP
	 * @param index		The index where to read
	 * @param data		The long to write
	 */
	protected void setLong(int index, long data) {
		set(index, new Long(data));
	}

	/**
	 * Add a long to this CP
	 * @param data
	 * @return The index of the new entry
	 */	
	public int addLong(long data) {
		return add(new Long(data), TYPE_LONG);
	}

	protected String getUTF8(int index) {
		return (String) get(index);
	}

	protected void setUTF8(int index, String value) {
		set(index, value.intern());
	}

	/**
	 * Add an UTF8 to this CP
	 * @param data
	 * @return The index of the new entry
	 */	
	public int addUTF8(String data) {
		return add(data.intern(), TYPE_UTF8);
	}

	public VmConstString getString(int index) {
		return (VmConstString) get(index);
	}

	protected void setString(int index, VmConstString value) {
		set(index, value);
	}

	/**
	 * Add a String to this CP
	 * @param value
	 * @return The index of the new entry
	 */	
	public int addString(VmConstString value) {
		return add(value, TYPE_STRING);
	}

	public VmConstClass getConstClass(int index) {
		return (VmConstClass) get(index);
	}

	protected void setConstClass(int index, VmConstClass value) {
		set(index, value);
	}

	/**
	 * Add an class reference to this CP
	 * @param data
	 * @return The index of the new entry
	 */	
	public int addInt(VmConstClass data) {
		return add(data, TYPE_CLASSREF);
	}

	public VmConstFieldRef getConstFieldRef(int index) {
		return (VmConstFieldRef) get(index);
	}

	protected void setConstFieldRef(int index, VmConstFieldRef value) {
		set(index, value);
	}

	/**
	 * Add a field reference to this CP
	 * @param data
	 * @return The index of the new entry
	 */	
	public int addInt(VmConstFieldRef data) {
		return add(data, TYPE_FIELDREF);
	}

	public VmConstMethodRef getConstMethodRef(int index) {
		return (VmConstMethodRef) get(index);
	}

	protected void setConstMethodRef(int index, VmConstMethodRef value) {
		set(index, value);
	}

	/**
	 * Add an method reference to this CP
	 * @param data
	 * @return The index of the new entry
	 */	
	public int addConstMethodRef(VmConstMethodRef data) {
		return add(data, TYPE_METHODREF);
	}

	public VmConstIMethodRef getConstIMethodRef(int index) {
		return (VmConstIMethodRef) get(index);
	}

	protected void setConstIMethodRef(int index, VmConstIMethodRef value) {
		set(index, value);
	}

	/**
	 * Add an imethod reference to this CP
	 * @param data
	 * @return The index of the new entry
	 */	
	public int addConstIMethodRef(VmConstIMethodRef data) {
		return add(data, TYPE_IMETHODREF);
	}

	public VmConstNameAndType getConstNameAndType(int index) {
		return (VmConstNameAndType) get(index);
	}

	protected void setConstNameAndType(int index, VmConstNameAndType value) {
		set(index, value);
	}

	/**
	 * Add a  name&amp;type to this CP
	 * @param data
	 * @return The index of the new entry
	 */	
	public int addConstNameAndType(VmConstNameAndType data) {
		return add(data, TYPE_NAMEANDTYPE);
	}

	public Object getAny(int index) {
		return get(index);
	}

	public byte getType(int index) {
		return tags[index];
	}
	
	/**
	 * Gets the index of a constant in this CP, or -1 if not found.
	 * @param object
	 * @return int
	 */
	public int indexOf(Object object) {
		for (int i = 0; i < used; i++) {
			final Object o = cp[i];
			if ((o != null) && (o.equals(object))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Read an Object out of this CP
	 * @param index		The index where to read
	 * @return Object
	 */
	private Object get(int index) {
		Object result = cp[index];
		/*if (result == null) {
			System.err.println("Warning: cp[" + index + "] (tag " + tags[index] + ") returns null");
		}*/
		return result;
	}

	/**
	 * Write an Object into this CP
	 * @param index		The index where to read
	 * @param data		The Object to write
	 */
	private void set(int index, Object data) {
		if (data == null) {
			throw new NullPointerException("Cannot set a null data");
		}
		cp[index] = data;
	}

	private void ensureSize(int size) {
		final int curLength = cp.length;
		if (size > curLength) {
			synchronized (this) {
				final int newLength = size + 4;
				final Object[] newCP = new Object[newLength];
				final byte[] newTags = new byte[newLength];
				System.arraycopy(cp, 0, newCP, 0, cp.length);
				System.arraycopy(tags, 0, newTags, 0, cp.length);
				this.cp = newCP;
				this.tags = newTags;
			}
		}
	}

	private int add(Object object, int tag) {
		final int index = used;
		ensureSize(used+1);
		cp[index] = object;
		tags[index] = (byte)tag;
		return index;
	}
}
