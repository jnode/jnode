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
 
package org.jnode.vm.classmgr;

import org.jnode.vm.InternString;
import org.jnode.vm.objects.VmSystemObject;

/**
 * A VmCP is the runtime representation of a constant pool
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
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

    private int used;

    /**
     * Construct a new VmCP with a given number of entries
     *
     * @param count
     */
    protected VmCP(int count) {
        this.cp = new Object[count];
        this.used = count;
    }

    /**
     * Gets the number of enntries in this CP
     *
     * @return int
     */
    public int getLength() {
        return cp.length;
    }

    /**
     * Read an int out of this CP
     *
     * @param index The index where to read
     * @return int
     */
    public int getInt(int index) {
        if (index == 0)
            return 0;
        else
            return ((VmConstInt) get(index)).intValue();
    }

    /**
     * Write an int into this CP
     *
     * @param index The index where to read
     * @param data  The int to write
     */
    protected void setInt(int index, int data) {
        set(index, new VmConstInt(data));
    }

    /**
     * Read a long out of this CP
     *
     * @param index The index where to read
     * @return long
     */
    public long getLong(int index) {
        return ((VmConstLong) get(index)).longValue();
    }

    /**
     * Write a long into this CP
     *
     * @param index The index where to read
     * @param data  The long to write
     */
    protected void setLong(int index, long data) {
        set(index, new VmConstLong(data));
    }

    /**
     * Read a float out of this CP
     *
     * @param index The index where to read
     * @return float
     */
    public float getFloat(int index) {
        return ((VmConstFloat) get(index)).floatValue();
    }

    /**
     * Write a float into this CP
     *
     * @param index The index where to read
     * @param data  The float to write
     */
    protected void setFloat(int index, float data) {
        set(index, new VmConstFloat(data));
    }

    /**
     * Read a double out of this CP
     *
     * @param index The index where to read
     * @return double
     */
    public double getDouble(int index) {
        return ((VmConstDouble) get(index)).doubleValue();
    }

    /**
     * Write a double into this CP
     *
     * @param index The index where to read
     * @param data  The double to write
     */
    protected void setDouble(int index, double data) {
        set(index, new VmConstDouble(data));
    }

    protected String getUTF8(int index) {
        return (String) get(index);
    }

    protected void setUTF8(int index, String value) {
        set(index, InternString.internString(value));
    }

    public VmConstString getString(int index) {
        return (VmConstString) get(index);
    }

    protected void setString(int index, VmConstString value) {
        set(index, value);
    }

    public VmConstClass getConstClass(int index) {
        return (VmConstClass) get(index);
    }

    protected void setConstClass(int index, VmConstClass value) {
        set(index, value);
    }

    public VmConstFieldRef getConstFieldRef(int index) {
        return (VmConstFieldRef) get(index);
    }

    protected void setConstFieldRef(int index, VmConstFieldRef value) {
        set(index, value);
    }

    public VmConstMethodRef getConstMethodRef(int index) {
        return (VmConstMethodRef) get(index);
    }

    protected void setConstMethodRef(int index, VmConstMethodRef value) {
        set(index, value);
    }

    public VmConstIMethodRef getConstIMethodRef(int index) {
        return (VmConstIMethodRef) get(index);
    }

    protected void setConstIMethodRef(int index, VmConstIMethodRef value) {
        set(index, value);
    }

    public final Object getAny(int index) {
        return get(index);
    }

    /**
     * Gets the index of a constant in this CP, or -1 if not found.
     *
     * @param object
     * @return int
     */
    public final int indexOf(Object object) {
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
     *
     * @param index The index where to read
     * @return Object
     */
    private final Object get(int index) {
        return cp[index];
    }

    /**
     * Write an Object into this CP
     *
     * @param index The index where to read
     * @param data  The Object to write
     */
    private void set(int index, Object data) {
        if (data == null) {
            throw new NullPointerException(
                "Cannot set a null data");
        }
        cp[index] = data;
    }

    final void reset(int index) {
        cp[index] = null;
    }
}
