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

import java.lang.reflect.Field;
import java.lang.reflect.Member;

import sun.reflect.ConstantPool;

/**
 * @author Levente S\u00e1ntha
 */
public class VmConstantPool extends ConstantPool {
    private VmType vmt;
    private VmCP cp;
    private VmClassLoader loader;

    public VmConstantPool(VmType vmt) {
        this.vmt = vmt;
        this.cp = vmt.getCP();
        this.loader = vmt.getLoader();
    }

    @Override
    public Class getClassAt(int index) {
        VmConstClass cc = cp.getConstClass(index);
        cc.doResolve(loader);
        VmType vmt = cc.getResolvedVmClass();
        return (vmt == null) ? null : vmt.asClass();
    }

    @Override
    public Class getClassAtIfLoaded(int index) {
        VmConstClass cc = cp.getConstClass(index);
        VmType vmt = cc.getResolvedVmClass();
        return (vmt == null) ? null : vmt.asClass();
    }

    @Override
    public double getDoubleAt(int index) {
        return cp.getDouble(index);
    }

    @Override
    public Field getFieldAt(int index) {
        VmConstFieldRef f = cp.getConstFieldRef(index);
        f.doResolve(loader);
        VmField vmf = f.getResolvedVmField();
        return vmf.asField();
    }

    @Override
    public Field getFieldAtIfLoaded(int index) {
        VmConstFieldRef f = cp.getConstFieldRef(index);
        try {
            VmField vmf = f.getResolvedVmField();
            return vmf.asField();
        } catch (NotResolvedYetException x) {
            return null;
        }
    }

    @Override
    public float getFloatAt(int index) {
        return cp.getFloat(index);
    }

    @Override
    public int getIntAt(int index) {
        return cp.getInt(index);
    }

    @Override
    public long getLongAt(int index) {
        return cp.getLong(index);
    }

    // Fetches the class name, member (field, method or interface
    // method) name, and type descriptor as an array of three Strings
    @Override
    public String[] getMemberRefInfoAt(int index) {
        //todo implement it
        throw new UnsupportedOperationException();
    }

    // Returns either a Method or Constructor.
    // Static initializers are returned as Method objects.
    @Override
    public Member getMethodAt(int index) {
        //todo implement it
        throw new UnsupportedOperationException();
    }

    @Override
    public Member getMethodAtIfLoaded(int index) {
        //todo implement it
        throw new UnsupportedOperationException();
    }

    // Number of entries in this constant pool (= maximum valid constant pool index)
    @Override
    public int getSize() {
        return cp.getLength();
    }

    @Override
    public String getStringAt(int index) {
        return (vmt.isSharedStatics() ? loader.getSharedStatics() : loader.getIsolatedStatics()).
            getStringEntry(cp.getString(index).getSharedStaticsIndex());
    }

    @Override
    public String getUTF8At(int index) {
        return cp.getUTF8(index);
    }
}
