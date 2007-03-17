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
 
package org.jnode.vm.classmgr;

import java.io.PrintStream;
import java.nio.ByteOrder;

import org.jnode.assembler.Label;
import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.Vm;
import org.jnode.vm.VmAddress;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.UnboxedObject;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public abstract class VmStatics extends VmStaticsBase {

	private int[] statics;
	private transient Object[] objects;
	private final int slotLength;
	private final boolean lsbFirst;
	private transient ObjectResolver resolver;
	private transient boolean locked;
    private final VmStaticsAllocator allocator;

	/**
	 * Initialize this instance
	 */
	public VmStatics(VmArchitecture arch, ObjectResolver resolver, int size) {
        this(new VmStaticsAllocator(size), arch, resolver);
	}

    /**
     * Initialize this instance
     */
    protected VmStatics(VmStaticsAllocator allocator, VmArchitecture arch, ObjectResolver resolver) {
        this.statics = new int[allocator.getCapacity()];
        this.allocator = allocator;
        this.objects = new Object[allocator.getCapacity()];
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
		return allocator.alloc(TYPE_INT, 1);
	}

	/**
	 * Allocate an long/double type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocLongField() {
		return allocator.alloc(TYPE_LONG, 2);
	}

	/**
	 * Allocate an Object type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocObjectField() {
		return allocator.alloc(TYPE_OBJECT, slotLength);
	}

	/**
	 * Allocate an String type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocConstantStringField(String value) {
		final int idx = allocator.alloc(TYPE_STRING, slotLength);
		setRawObject(idx, value);
		return idx;
	}

	/**
	 * Allocate an org.jnode.vm.Address type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	public final int allocAddressField() {
		return allocator.alloc(TYPE_ADDRESS, slotLength);
	}

	/**
	 * Allocate an method code type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocMethodCode() {
		final int idx = allocator.alloc(TYPE_METHOD_CODE, slotLength);
		return idx;
	}
    
    /**
     * Set the pointer of the native code of a method in the table at the given index.
     * @param idx
     * @param nativeCode
     */
    final void setMethodCode(int idx, VmAddress nativeCode) {
        allocator.testType(idx, TYPE_METHOD_CODE);
        setRawObject(idx, nativeCode);
    }

	/**
	 * Allocate an {@link org.jnode.vm.classmgr.VmType} type entry.
	 * 
	 * @return the index of the allocated entry.
	 */
	final int allocClass(VmType type) {
		final int idx = allocator.alloc(TYPE_CLASS, slotLength);
		setRawObject(idx, type);
		return idx;
	}
    
    /**
     * Gets the type at the given index.
     * @param idx
     * @return
     */
    final VmType getTypeEntry(int idx) {
        allocator.testType(idx, TYPE_CLASS);
        return (VmType)getRawObject(idx);
    }

    /**
     * Get a String entry at the given index.
     *
     * @param idx
     * @return the string
     */
    public final String getStringEntry(int idx) {
        allocator.testType(idx, TYPE_STRING);
        return (String)getRawObject(idx);
    }

    public final void setInt(int idx, int value) {
        allocator.testType(idx, TYPE_INT);
		if (statics[idx] != value) {
			if (locked) {
				throw new RuntimeException("Locked");
			}
		    statics[idx] = value;
		}
	}

    public final int getInt(int idx) {
        allocator.testType(idx, TYPE_INT);
        return statics[idx];
    }

	public final void setObject(int idx, Object value) {
        allocator.testType(idx, TYPE_OBJECT);
		if (setRawObject(idx, value)) {
			if (locked) {
				throw new RuntimeException("Locked");
			}		    
		}
	}

    public final void setAddress(int idx, Label value) {
        if (!Vm.isWritingImage()) {
            throw new IllegalStateException("Only allowed during bootimage creation.");
        }
        allocator.testType(idx, TYPE_ADDRESS);
        if (setRawObject(idx, value)) {
            if (locked) {
                throw new RuntimeException("Locked");
            }           
        }
    }

    public final void setAddress(int idx, UnboxedObject value) {
        if (!Vm.isWritingImage()) {
            throw new IllegalStateException("Only allowed during bootimage creation.");
        }
        allocator.testType(idx, TYPE_ADDRESS);
        if (setRawObject(idx, value)) {
            if (locked) {
                throw new RuntimeException("Locked");
            }           
        }
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

	private final boolean setRawObject(int idx, Object value) {
		if (objects != null) {
		    if (objects[idx] != value) {
		        objects[idx] = value;
		        return true;
		    } else {
		        return false;
		    }
		} else {
			final Address valuePtr = ObjectReference.fromObject(value).toAddress();
			if (slotLength == 1) {
				statics[idx] = valuePtr.toInt();
			} else {
				final long lvalue = valuePtr.toLong();
				if (lsbFirst) {
					statics[idx + 0] = (int) (lvalue & 0xFFFFFFFFL);
					statics[idx + 1] = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
				} else {
					statics[idx + 1] = (int) (lvalue & 0xFFFFFFFFL);
					statics[idx + 0] = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
				}
			}
			return true;
		}
	}

    private final Object getRawObject(int idx) {
        if (objects != null) {
            return objects[idx];
        } else {
            final Address ptr;
            if (slotLength == 1) {
                ptr = Address.fromIntZeroExtend(statics[idx]);
            } else {
                final long lsb;
                final long msb;
                if (lsbFirst) {
                    lsb = statics[idx+0] & 0xFFFFFFFFL;
                    msb = statics[idx+1] & 0xFFFFFFFFL;
                } else {
                    lsb = statics[idx+1] & 0xFFFFFFFFL;
                    msb = statics[idx+0] & 0xFFFFFFFFL;                    
                }
                ptr = Address.fromLong(lsb | (msb << 32));
            }
            return ptr.toObjectReference().toObject();
        }
    }

	public final void setLong(int idx, long value) {
		if (locked) {
			throw new RuntimeException("Locked");
		}
        allocator.testType(idx, TYPE_LONG);
		if (lsbFirst) {
			statics[idx + 0] = (int) (value & 0xFFFFFFFFL);
			statics[idx + 1] = (int) ((value >>> 32) & 0xFFFFFFFFL);
		} else {
			statics[idx + 1] = (int) (value & 0xFFFFFFFFL);
			statics[idx + 0] = (int) ((value >>> 32) & 0xFFFFFFFFL);
		}
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
		return allocator.getType(index);
	}

	/**
	 * Let all objects in this statics-table make a visit to the given visitor.
	 * 
	 * @param visitor
	 */
	public boolean walk(ObjectVisitor visitor) {
		return walk(TYPE_OBJECT, visitor);
	}

	/**
	 * Let all objects in this statics-table make a visit to the given visitor.
	 * 
	 * @param visitor
     * @return false if the last visit returned false, true otherwise.
	 */
	private boolean walk(int visitType, ObjectVisitor visitor) {
		final int[] table;
		final byte[] types;
		final int length;
		table = this.statics;
		types = allocator.getTypes();
		length = allocator.getLength();
		if (slotLength == 1) {
			for (int i = 0; i < length; i++) {
				final byte type = types[i];
				if (type == visitType) {
					final Object object;
					object = Address.fromIntZeroExtend(table[i]).toObjectReference().toObject();
					if (object != null) {
						final boolean rc = visitor.visit(object);
						if (!rc) {
                            return false;
						}
					}
				}
			}
		} else {
            for (int i = 0; i < length; i++) {
                final byte type = types[i];
                if (type == visitType) {
                    final Object object;
                    final long lsb = table[i+0] & 0xFFFFFFFFL;
                    final long msb = table[i+1] & 0xFFFFFFFFL;
                    i++;
                    object = Address.fromLong(lsb | (msb << 32)).toObjectReference().toObject();
                    if (object != null) {
                        final boolean rc = visitor.visit(object);
                        if (!rc) {
                            return false;
                        }
                    }
                }
            }
		}
        return true;
	}

	public final void dumpStatistics(PrintStream out) {
        allocator.dumpStatistics(out);
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
					if ((statics[i] == 0) && (allocator.getType(i) != TYPE_ADDRESS)) {
						throw new RuntimeException("addressof32(" + value + ") is null");
					}
				} else {
					final long lvalue = resolver.addressOf64(value);
					if ((lvalue == 0L) && (allocator.getType(i) != TYPE_ADDRESS)) {
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
			} else if (allocator.getType(i) == TYPE_METHOD_CODE) {
				throw new RuntimeException("Method is null");
			}
		}
		objects = null;
		locked = true;
		//System.out.println("VmStatics#verifyBeforeEmit count=" + count);
	}
    
    /**
     * Gets my statics allocator.
     * @return
     */
    protected final VmStaticsAllocator getAllocator() {
        return allocator;
    }
}
