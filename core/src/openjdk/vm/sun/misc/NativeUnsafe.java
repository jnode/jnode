/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package sun.misc;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.CodeSource;
import java.security.Policy;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.HashMap;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Address;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.VmMagic;
import org.jnode.vm.Vm;
import org.jnode.vm.VmReflection;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.SharedStatics;

/**
 * @author Levente S\u00e1ntha
 */
@MagicPermission
@SharedStatics
class NativeUnsafe {
    static void registerNatives() {
    }

    public static int getInt(Unsafe instance, Object o, long offset) {
        if(o instanceof StaticAccess){
            return ((StaticAccess) o).getInt((int) offset);
        } else {
            return ObjectReference.fromObject(o).toAddress().add((int) offset).loadInt();
        }
    }

    public static void putInt(Unsafe instance, Object o, long offset, int x) {
        if(o instanceof StaticAccess){
            ((StaticAccess) o).setInt(x, (int)offset);
        } else {
            ObjectReference.fromObject(o).toAddress().add((int)offset).store(x);
        }
    }

    /**
     * @see org.jnode.vm.VmReflection#getObject(org.jnode.vm.classmgr.VmField, Object)
     */
    public static Object getObject(Unsafe instance, Object o, long offset) {
        if(o instanceof StaticAccess){
            return ((StaticAccess) o).getObject((int)offset);
        } else {
            return ObjectReference.fromObject(o).toAddress().add((int) offset).loadObjectReference().toObject();
        }
    }

    /**
     * @see org.jnode.vm.VmReflection#setObject(org.jnode.vm.classmgr.VmField, Object, Object)
     */
    public static void putObject(Unsafe instance, Object o, long offset, Object x) {
        if(o instanceof StaticAccess){
            ((StaticAccess) o).setObject(x, (int)offset);
        } else {
            ObjectReference.fromObject(o).toAddress().add((int)offset).store(ObjectReference.fromObject(x));
        }
    }

    public static boolean getBoolean(Unsafe instance, Object o, long offset) {
        if(o instanceof StaticAccess){
            return ((StaticAccess) o).getBoolean((int) offset);
        } else {
            return ObjectReference.fromObject(o).toAddress().add((int) offset).loadByte() != 0;
        }
    }

    public static void putBoolean(Unsafe instance, Object o, long offset, boolean x) {
        if(o instanceof StaticAccess){
            ((StaticAccess) o).setBoolean(x, (int)offset);
        } else {
            ObjectReference.fromObject(o).toAddress().add((int)offset).store((byte)(x ? 1 : 0));
        }
    }

    public static byte getByte(Unsafe instance, Object o, long offset) {
        if(o instanceof StaticAccess){
            return ((StaticAccess) o).getByte((int) offset);
        } else {
            return ObjectReference.fromObject(o).toAddress().add((int) offset).loadByte();
        }
    }

    public static void putByte(Unsafe instance, Object o, long offset, byte x) {
        if(o instanceof StaticAccess){
            ((StaticAccess) o).setByte(x, (int) offset);
        } else {
            ObjectReference.fromObject(o).toAddress().add((int)offset).store(x);
        }
    }

    public static short getShort(Unsafe instance, Object o, long offset) {
        if(o instanceof StaticAccess){
            return ((StaticAccess) o).getShort((int) offset);
        } else {
            return ObjectReference.fromObject(o).toAddress().add((int) offset).loadShort();
        }
    }

    public static void putShort(Unsafe instance, Object o, long offset, short x) {
        if(o instanceof StaticAccess){
            ((StaticAccess) o).setShort(x, (int) offset);
        } else {
            ObjectReference.fromObject(o).toAddress().add((int)offset).store(x);
        }
    }

    public static char getChar(Unsafe instance, Object o, long offset) {
        if(o instanceof StaticAccess){
            return ((StaticAccess) o).getChar((int) offset);
        } else {
            return ObjectReference.fromObject(o).toAddress().add((int) offset).loadChar();
        }
    }

    public static void putChar(Unsafe instance, Object o, long offset, char x) {
        if(o instanceof StaticAccess){
            ((StaticAccess) o).setChar(x, (int) offset);
        } else {
            ObjectReference.fromObject(o).toAddress().add((int)offset).store(x);
        }
    }

    public static long getLong(Unsafe instance, Object o, long offset) {
        if(o instanceof StaticAccess){
            return ((StaticAccess) o).getLong((int) offset);
        } else {
            return ObjectReference.fromObject(o).toAddress().add((int) offset).loadLong();
        }
    }

    public static void putLong(Unsafe instance, Object o, long offset, long x) {
        if(o instanceof StaticAccess){
            ((StaticAccess) o).setLong(x, (int) offset);
        } else {
            ObjectReference.fromObject(o).toAddress().add((int)offset).store(x);
        }
    }

    public static float getFloat(Unsafe instance, Object o, long offset) {
        if(o instanceof StaticAccess){
            return ((StaticAccess) o).getFloat((int) offset);
        } else {
            return ObjectReference.fromObject(o).toAddress().add((int) offset).loadFloat();
        }
    }

    public static void putFloat(Unsafe instance, Object o, long offset, float x) {
        if(o instanceof StaticAccess){
            ((StaticAccess) o).setFloat(x, (int) offset);
        } else {
            ObjectReference.fromObject(o).toAddress().add((int)offset).store(x);
        }
    }

    public static double getDouble(Unsafe instance, Object o, long offset) {
        if(o instanceof StaticAccess){
            return ((StaticAccess) o).getDouble((int) offset);
        } else {
            return ObjectReference.fromObject(o).toAddress().add((int) offset).loadDouble();
        }
    }

    public static void putDouble(Unsafe instance, Object o, long offset, double x) {
        if(o instanceof StaticAccess){
            ((StaticAccess) o).setDouble(x, (int) offset);
        } else {
            ObjectReference.fromObject(o).toAddress().add((int)offset).store(x);
        }
    }

    public static byte getByte(Unsafe instance, long address) {
        throw new UnsupportedOperationException();
    }

    public static void putByte(Unsafe instance, long address, byte x) {
        throw new UnsupportedOperationException();
    }

    public static short getShort(Unsafe instance, long address) {
        throw new UnsupportedOperationException();
    }

    public static void putShort(Unsafe instance, long address, short x) {
        throw new UnsupportedOperationException();
    }

    public static char getChar(Unsafe instance, long address) {
        throw new UnsupportedOperationException();
    }

    public static void putChar(Unsafe instance, long address, char x) {
        throw new UnsupportedOperationException();
    }

    public static int getInt(Unsafe instance, long address) {
        throw new UnsupportedOperationException();
    }

    public static void putInt(Unsafe instance, long address, int x) {
        throw new UnsupportedOperationException();
    }

    public static long getLong(Unsafe instance, long address) {
        throw new UnsupportedOperationException();
    }

    public static void putLong(Unsafe instance, long address, long x) {
        throw new UnsupportedOperationException();
    }

    public static float getFloat(Unsafe instance, long address) {
        throw new UnsupportedOperationException();
    }

    public static void putFloat(Unsafe instance, long address, float x) {
        throw new UnsupportedOperationException();
    }

    public static double getDouble(Unsafe instance, long address) {
        throw new UnsupportedOperationException();
    }

    public static void putDouble(Unsafe instance, long address, double x) {
        throw new UnsupportedOperationException();
    }

    public static long getAddress(Unsafe instance, long address) {
        throw new UnsupportedOperationException();
    }

    public static void putAddress(Unsafe instance, long address, long x) {
        throw new UnsupportedOperationException();
    }

    public static long allocateMemory(Unsafe instance, long bytes) {
        throw new UnsupportedOperationException();
    }

    public static long reallocateMemory(Unsafe instance, long address, long bytes) {
        throw new UnsupportedOperationException();
    }

    public static void setMemory(Unsafe instance, long address, long bytes, byte value) {
        throw new UnsupportedOperationException();
    }

    public static void copyMemory(Unsafe instance, long srcAddress, long destAddress,
                           long bytes) {
        throw new UnsupportedOperationException();
    }

    public static void freeMemory(Unsafe instance, long address) {
        throw new UnsupportedOperationException();
    }
    
    @MagicPermission
    static class StaticAccess {
        protected long  address;

        StaticAccess(long address){
            this.address = address;
        }

        final boolean getBoolean(int offset){
            return Address.fromLong(address).add(offset).loadInt() != 0;
        }

        final void setBoolean(boolean value, int offset){
            Address.fromLong(address).add(offset).store(value ? 1 : 0);
        }

        final byte getByte(int offset) {
            return (byte) Address.fromLong(address).add(offset).loadInt();
        }

        final void setByte(byte value, int offset) {
            Address.fromLong(address).add(offset).store((int) value);
        }

        final short getShort(int offset){
            return (short) Address.fromLong(address).add(offset).loadInt();
        }

        final void setShort(short value, int offset){
            Address.fromLong(address).add(offset).store((int) value);
        }

        final char getChar(int offset){
            return (char) Address.fromLong(address).add(offset).loadInt();
        }

        final void setChar(char value, int offset){
            Address.fromLong(address).add(offset).store((int) value);
        }

        final int getInt(int offset){
            return Address.fromLong(address).add(offset).loadInt();
        }

        final void setInt(int value, int offset){
            Address.fromLong(address).add(offset).store(value);
        }

        final long getLong(int offset){
            return Address.fromLong(address).add(offset).loadLong();
        }

        final void setLong(long value, int offset){
            Address.fromLong(address).add(offset).store(value);
        }

        final float getFloat(int offset){
            return Address.fromLong(address).add(offset).loadFloat();
        }

        final void setFloat(float value, int offset){
            Address.fromLong(address).add(offset).store(value);
        }

        final double getDouble(int offset){
            return Address.fromLong(address).add(offset).loadDouble();
        }

        final void setDouble(double value, int offset){
            Address.fromLong(address).add(offset).store(value);
        }

        Object getObject(int offset){
            return Address.fromLong(address).add(offset).loadObjectReference().toObject();
        }

        void setObject(Object obj, int offset){
            Address.fromLong(address).add(offset).store(ObjectReference.fromObject(obj));
        }
    }

    /**
     * @see org.jnode.vm.VmReflection#getObject(org.jnode.vm.classmgr.VmField, Object)
     */
    static class IrregularStaticAccess  extends StaticAccess {

        IrregularStaticAccess(long address) {
            super(address);
        }

        @Override
        Object getObject(int offset){
            Object obj = super.getObject(offset);
            //handles the reflective access to static final String fields, which didn't work.
            if(obj instanceof VmConstString){
                VmConstString cs = (VmConstString) obj;
                obj = Vm.getVm().getSharedStatics().getStringEntry(cs.getSharedStaticsIndex());
            }
            return obj;
        }

        @Override
        void setObject(Object obj, int offset){
            //do nothing - since this is access if for final fields
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @see org.jnode.vm.VmReflection#getStaticFieldAddress(org.jnode.vm.classmgr.VmStaticField)
     */
    public static Object staticFieldBase(Unsafe instance, Field f) {
        final VmProcessor proc = VmProcessor.current();
		final Address tablePtr;
        VmStaticField sf = (VmStaticField) getVmField(f);
        if (sf.isShared()) {
            tablePtr = VmMagic.getArrayData(proc.getSharedStaticsTable());
        } else {
            tablePtr = VmMagic.getArrayData(proc.getIsolatedStaticsTable());
        }
		Object ret = tablePtr.loadObjectReference().toObject();
        if(sf.isStatic() && sf.isFinal() && f.getType().equals(String.class))
            ret = new IrregularStaticAccess(tablePtr.toLong());
        else
            ret = new StaticAccess(tablePtr.toLong());
        return ret;
    }

    /**
     * @see org.jnode.vm.VmReflection#getInstanceFieldAddress(Object, org.jnode.vm.classmgr.VmInstanceField)   
     */
    public static long staticFieldOffset(Unsafe instance, Field f) {
		final int offset;
        VmStaticField sf = (VmStaticField) getVmField(f);
        if (sf.isShared()) {
            offset = sf.getSharedStaticsIndex() << 2;
        } else {
            offset = sf.getIsolatedStaticsIndex() << 2;
        }
		return offset;
    }

    public static long objectFieldOffset(Unsafe instance, Field f) {
        return ((VmInstanceField) getVmField(f)).getOffset();
    }

    private static VmField getVmField(Field f) {
        VmType<?> vmClass = VmType.fromClass((Class<?>) f.getDeclaringClass());
        //vmClass.link();
        return vmClass.getDeclaredField(f.getSlot());
    }

    public static void ensureClassInitialized(Unsafe instance, Class c) {
        VmType.fromClass(c).initialize();
    }

    public static int arrayBaseOffset(Unsafe instance, Class arrayClass) {
        throw new UnsupportedOperationException();
    }

    public static int arrayIndexScale(Unsafe instance, Class arrayClass) {
        throw new UnsupportedOperationException();
    }

    public static int addressSize(Unsafe instance) {
        throw new UnsupportedOperationException();
    }

    public static int pageSize(Unsafe instance) {
        throw new UnsupportedOperationException();
    }

    public static Class defineClass(Unsafe instance, String name, byte[] b, int off, int len,
                                    ClassLoader loader,
                                    ProtectionDomain protDomain) {
        if (protDomain == null) {
            protDomain = AccessController
                    .doPrivileged(new PrivilegedAction<ProtectionDomain>() {

                        public ProtectionDomain run() {
                            final CodeSource cs = new CodeSource(null, (Certificate[]) null);
                            return new ProtectionDomain(cs, Policy.getPolicy().getPermissions(cs));
                        }
                    });
        }
        return loader.getVmClassLoader().defineClass(name, b, off, len, protDomain).asClass();

    }

    public static Class defineClass(Unsafe instance, String name, byte[] b, int off, int len) {
        throw new UnsupportedOperationException();
    }

    public static Object allocateInstance(Unsafe instance, Class cls)
            throws InstantiationException {
        //todo improve exception handling
        try {
            return VmReflection.newInstance(VmType.fromClass(cls).getMethod("<init>", new VmType[]{}));
        } catch (Exception x) {
            throw new InstantiationException("Reason: " + x.getMessage());
        }
    }

    public static void monitorEnter(Unsafe instance, Object o) {
        throw new UnsupportedOperationException();
    }

    public static void monitorExit(Unsafe instance, Object o) {
        throw new UnsupportedOperationException();
    }

    public static boolean tryMonitorEnter(Unsafe instance, Object o) {
        throw new UnsupportedOperationException();
    }


    public static void throwException(Unsafe instance, Throwable ee) {
        throw new UnsupportedOperationException();
    }

    public static boolean compareAndSwapObject(Unsafe instance, Object o, long offset,
                                              Object expected, Object x) {
        //todo make sure it's atomic
        final Address address = ObjectReference.fromObject(o).toAddress().add((int) offset);
        if(address.loadObjectReference().toObject() == expected){
            address.store(ObjectReference.fromObject(x));
            return true;
        } else {
            return false;
        }
    }

    public static boolean compareAndSwapInt(Unsafe instance, Object o, long offset,
                                           int expected, int x) {
        //todo make sure it's atomic
        final Address address = ObjectReference.fromObject(o).toAddress().add((int) offset);
        if(address.loadInt() == expected){
            address.store(x);
            return true;
        } else {
            return false;
        }
    }

    public static final boolean compareAndSwapLong(Unsafe instance, Object o, long offset,
                                            long expected,
                                            long x) {
        //todo make sure it's atomic
        final Address address = ObjectReference.fromObject(o).toAddress().add((int) offset);
        if(address.loadLong() == expected){
            address.store(x);
            return true;
        } else {
            return false;
        }
    }

    public static Object getObjectVolatile(Unsafe instance, Object o, long offset) {
        //todo implement volatile semantics
        return getObject(instance, o,  offset);
    }

    public static void putObjectVolatile(Unsafe instance, Object o, long offset, Object x) {
        //todo implement volatile semantics
        putObject(instance, o, offset, x);
    }

    public static int getIntVolatile(Unsafe instance, Object o, long offset) {
        //todo implement volatile semantics
        return getInt(instance, o, offset);
    }

    public static void putIntVolatile(Unsafe instance, Object o, long offset, int x) {
        //todo implement volatile semantics
        putInt(instance, o, offset, x);
    }

    public static boolean getBooleanVolatile(Unsafe instance, Object o, long offset) {
        //todo ensure volatile semantics
        return getBoolean(instance, o, offset);
    }

    public static void putBooleanVolatile(Unsafe instance, Object o, long offset, boolean x) {
        //todo ensure volatile semantics
        putBoolean(instance, o, offset, x);
    }

    public static byte getByteVolatile(Unsafe instance, Object o, long offset) {
        //todo ensure volatile semantics
        return getByte(instance, o, offset);
    }

    public static void putByteVolatile(Unsafe instance, Object o, long offset, byte x) {
        //todo ensure volatile semantics
        putByte(instance, o, offset, x);
    }

    public static short getShortVolatile(Unsafe instance, Object o, long offset) {
        //todo ensure volatile semantics
        return getShort(instance, o, offset);
    }

    public static void putShortVolatile(Unsafe instance, Object o, long offset, short x) {
        //todo ensure volatile semantics
        putShort(instance, o, offset, x);
    }

    public static char getCharVolatile(Unsafe instance, Object o, long offset) {
        //todo ensure volatile semantics
        return getChar(instance, o, offset);
    }

    public static void putCharVolatile(Unsafe instance, Object o, long offset, char x) {
        //todo ensure volatile semantics
        putChar(instance, o, offset, x);
    }

    public static long getLongVolatile(Unsafe instance, Object o, long offset) {
        //todo ensure volatile semantics
        return getLong(instance, o, offset);
    }

    public static void putLongVolatile(Unsafe instance, Object o, long offset, long x) {
        //todo ensure volatile semantics
        putLong(instance, o, offset, x);
    }

    public static float getFloatVolatile(Unsafe instance, Object o, long offset) {
        //todo ensure volatile semantics
        return getFloat(instance, o, offset);
    }

    public static void putFloatVolatile(Unsafe instance, Object o, long offset, float x) {
        //todo ensure volatile semantics
        putFloat(instance, o, offset, x);
    }

    public static double getDoubleVolatile(Unsafe instance, Object o, long offset) {
        //todo ensure volatile semantics
        return getDouble(instance, o, offset);
    }

    public static void putDoubleVolatile(Unsafe instance, Object o, long offset, double x) {
        //todo ensure volatile semantics
        putDouble(instance, o, offset, x);
    }

    public static void putOrderedObject(Unsafe instance, Object o, long offset, Object x) {
        //todo ensure ordered semantics
        putObject(instance, o, offset, x);
    }

    public static void putOrderedInt(Unsafe instance, Object o, long offset, int x) {
        //todo ensure ordered semantics
        putInt(instance, o, offset, x);
    }

    public static void putOrderedLong(Unsafe instance, Object o, long offset, long x) {
        //todo ensure ordered semantics
        putLong(instance, o, offset, x);
    }

    static final Map<Object, ThreadParker> parking = new HashMap<Object, ThreadParker>();
    @SharedStatics
    private static class ThreadParker {
        private synchronized void park(long time){
            try {
                wait(time);
            }catch (InterruptedException x){
                //ignore
            }
        }

        private synchronized void unpark() {
            notifyAll();
        }
    }
    /**
     * @see Unsafe#unpark(Object)
     */
    public static void unpark(Unsafe instance, Object thread) {
        synchronized (parking){
            ThreadParker p =parking.get(thread);
            if(p != null){
                p.unpark();
            } else {
                parking.put(thread, new ThreadParker());
            }
        }
    }

    /**
     * @see Unsafe#park(boolean, long)
     */
    public static void park(Unsafe instance, boolean isAbsolute, long time) {
        //todo add proper support for nanotime parking
        ThreadParker p;
        synchronized (parking){
            Thread thread = Thread.currentThread();
            p = parking.get(thread);
            if(p == null){
                if(isAbsolute){
                    time = time - System.currentTimeMillis();
                    if(time < 0) time = 1;
                } else if(time > 0) {
                    time = time / 10000000;
                    if(time == 0)
                        time = 1;
                } else if (time < 0){
                    time = 1;
                }
                p = new ThreadParker();
                parking.put(thread, p);
            } else {                
                p = null;
            }
        }

        if(p != null){
            p.park(time);
            synchronized (parking){
                parking.remove(Thread.currentThread());
            }
        }
    }

    public static int getLoadAverage(Unsafe instance, double[] loadavg, int nelems) {
        throw new UnsupportedOperationException();
    }
}
