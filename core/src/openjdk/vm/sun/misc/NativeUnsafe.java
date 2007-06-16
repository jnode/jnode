/*
 * $Id$
 */
package sun.misc;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;

/**
 * @author Levente Sántha
 */
class NativeUnsafe {
    static void registerNatives() {
    }

    public static int getInt(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putInt(Unsafe instance, Object o, long offset, int x) {
        throw new UnsupportedOperationException();
    }

    public static Object getObject(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putObject(Unsafe instance, Object o, long offset, Object x) {
        throw new UnsupportedOperationException();
    }

    public static boolean getBoolean(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putBoolean(Unsafe instance, Object o, long offset, boolean x) {
        throw new UnsupportedOperationException();
    }

    public static byte getByte(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putByte(Unsafe instance, Object o, long offset, byte x) {
        throw new UnsupportedOperationException();
    }

    public static short getShort(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putShort(Unsafe instance, Object o, long offset, short x) {
        throw new UnsupportedOperationException();
    }

    public static char getChar(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putChar(Unsafe instance, Object o, long offset, char x) {
        throw new UnsupportedOperationException();
    }

    public static long getLong(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putLong(Unsafe instance, Object o, long offset, long x) {
        throw new UnsupportedOperationException();
    }

    public static float getFloat(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putFloat(Unsafe instance, Object o, long offset, float x) {
        throw new UnsupportedOperationException();
    }

    public static double getDouble(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putDouble(Unsafe instance, Object o, long offset, double x) {
        throw new UnsupportedOperationException();
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

    public static long staticFieldOffset(Unsafe instance, Field f) {
        throw new UnsupportedOperationException();
    }

    public static long objectFieldOffset(Unsafe instance, Field f) {
        throw new UnsupportedOperationException();
    }

    public static Object staticFieldBase(Unsafe instance, Field f) {
        throw new UnsupportedOperationException();
    }

    public static void ensureClassInitialized(Unsafe instance, Class c) {
        c.getVmClass().initialize();
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
                             ProtectionDomain protectionDomain) {
        throw new UnsupportedOperationException();
    }

    public static Class defineClass(Unsafe instance, String name, byte[] b, int off, int len) {
        throw new UnsupportedOperationException();
    }

    public static Object allocateInstance(Unsafe instance, Class cls)
            throws InstantiationException {
        throw new UnsupportedOperationException();
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


    public static final boolean compareAndSwapObject(Unsafe instance, Object o, long offset,
                                              Object expected,
                                              Object x) {
        throw new UnsupportedOperationException();
    }

    public static final boolean compareAndSwapInt(Unsafe instance, Object o, long offset,
                                           int expected,
                                           int x) {
        throw new UnsupportedOperationException();
    }

    public static final boolean compareAndSwapLong(Unsafe instance, Object o, long offset,
                                            long expected,
                                            long x) {
        throw new UnsupportedOperationException();
    }

    public static Object getObjectVolatile(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putObjectVolatile(Unsafe instance, Object o, long offset, Object x) {
        throw new UnsupportedOperationException();
    }

    public static int getIntVolatile(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putIntVolatile(Unsafe instance, Object o, long offset, int x) {
        throw new UnsupportedOperationException();
    }

    public static boolean getBooleanVolatile(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putBooleanVolatile(Unsafe instance, Object o, long offset, boolean x) {
        throw new UnsupportedOperationException();
    }

    public static byte getByteVolatile(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putByteVolatile(Unsafe instance, Object o, long offset, byte x) {
        throw new UnsupportedOperationException();
    }

    public static short getShortVolatile(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putShortVolatile(Unsafe instance, Object o, long offset, short x) {
        throw new UnsupportedOperationException();
    }

    public static char getCharVolatile(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putCharVolatile(Unsafe instance, Object o, long offset, char x) {
        throw new UnsupportedOperationException();
    }

    public static long getLongVolatile(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putLongVolatile(Unsafe instance, Object o, long offset, long x) {
        throw new UnsupportedOperationException();
    }

    public static float getFloatVolatile(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putFloatVolatile(Unsafe instance, Object o, long offset, float x) {
        throw new UnsupportedOperationException();
    }

    public static double getDoubleVolatile(Unsafe instance, Object o, long offset) {
        throw new UnsupportedOperationException();
    }

    public static void putDoubleVolatile(Unsafe instance, Object o, long offset, double x) {
        throw new UnsupportedOperationException();
    }

    public static void putOrderedObject(Unsafe instance, Object o, long offset, Object x) {
        throw new UnsupportedOperationException();
    }

    public static void putOrderedInt(Unsafe instance, Object o, long offset, int x) {
        throw new UnsupportedOperationException();
    }

    public static void putOrderedLong(Unsafe instance, Object o, long offset, long x) {
        throw new UnsupportedOperationException();
    }

    public static void unpark(Unsafe instance, Object thread) {
        throw new UnsupportedOperationException();
    }

    public static void park(Unsafe instance, boolean isAbsolute, long time) {
        throw new UnsupportedOperationException();
    }

    public static int getLoadAverage(Unsafe instance, double[] loadavg, int nelems) {
        throw new UnsupportedOperationException();
    }
}
