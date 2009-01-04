package java.lang.reflect;

import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.Vm;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @see java.lang.reflect.Array
 */
class NativeArray { //TODO OPTIMIZE IT!

    /**
     * @see java.lang.reflect.Array#getLength(java.lang.Object)
     */
    private static int getLength(Object arg1) {
        if (arg1 instanceof Object[])
            return ((Object[]) arg1).length;
        if (arg1 instanceof boolean[])
            return ((boolean[]) arg1).length;
        if (arg1 instanceof byte[])
            return ((byte[]) arg1).length;
        if (arg1 instanceof char[])
            return ((char[]) arg1).length;
        if (arg1 instanceof short[])
            return ((short[]) arg1).length;
        if (arg1 instanceof int[])
            return ((int[]) arg1).length;
        if (arg1 instanceof long[])
            return ((long[]) arg1).length;
        if (arg1 instanceof float[])
            return ((float[]) arg1).length;
        if (arg1 instanceof double[])
            return ((double[]) arg1).length;
        if (arg1 == null)
            throw new NullPointerException();
        throw new IllegalArgumentException();
    }

    /**
     * @see java.lang.reflect.Array#get(java.lang.Object, int)
     */
    private static Object get(Object arg1, int arg2) {
        if (arg1 instanceof Object[])
            return ((Object[]) arg1)[arg2];
        if (arg1 instanceof boolean[])
            return ((boolean[]) arg1)[arg2] ? Boolean.TRUE : Boolean.FALSE;
        if (arg1 instanceof byte[])
            return ((byte[]) arg1)[arg2];
        if (arg1 instanceof char[])
            return ((char[]) arg1)[arg2];
        if (arg1 instanceof short[])
            return ((short[]) arg1)[arg2];
        if (arg1 instanceof int[])
            return ((int[]) arg1)[arg2];
        if (arg1 instanceof long[])
            return ((long[]) arg1)[arg2];
        if (arg1 instanceof float[])
            return ((float[]) arg1)[arg2];
        if (arg1 instanceof double[])
            return ((double[]) arg1)[arg2];
        if (arg1 == null)
            throw new NullPointerException();
        throw new IllegalArgumentException();
    }

    /**
     * @see java.lang.reflect.Array#getBoolean(java.lang.Object, int)
     */
    private static boolean getBoolean(Object arg1, int arg2) {
        if (arg1 instanceof boolean[])
            return ((boolean[]) arg1)[arg2];
        if (arg1 == null)
            throw new NullPointerException();
        throw new IllegalArgumentException();
    }

    /**
     * @see java.lang.reflect.Array#getByte(java.lang.Object, int)
     */
    private static byte getByte(Object arg1, int arg2) {
        if (arg1 instanceof byte[])
            return ((byte[]) arg1)[arg2];
        if (arg1 == null)
            throw new NullPointerException();
        throw new IllegalArgumentException();
    }

    /**
     * @see java.lang.reflect.Array#getChar(java.lang.Object, int)
     */
    private static char getChar(Object arg1, int arg2) {
        if (arg1 instanceof char[])
            return ((char[]) arg1)[arg2];
        if (arg1 == null)
            throw new NullPointerException();
        throw new IllegalArgumentException();
    }

    /**
     * @see java.lang.reflect.Array#getShort(java.lang.Object, int)
     */
    private static short getShort(Object arg1, int arg2) {
        if (arg1 instanceof short[])
            return ((short[]) arg1)[arg2];
        return getByte(arg1, arg2);
    }

    /**
     * @see java.lang.reflect.Array#getInt(java.lang.Object, int)
     */
    private static int getInt(Object arg1, int arg2) {
        if (arg1 instanceof int[])
            return ((int[]) arg1)[arg2];
        if (arg1 instanceof char[])
            return ((char[]) arg1)[arg2];
        return getShort(arg1, arg2);
    }

    /**
     * @see java.lang.reflect.Array#getLong(java.lang.Object, int)
     */
    private static long getLong(Object arg1, int arg2) {
        if (arg1 instanceof long[])
            return ((long[]) arg1)[arg2];
        return getInt(arg1, arg2);
    }

    /**
     * @see java.lang.reflect.Array#getFloat(java.lang.Object, int)
     */
    private static float getFloat(Object arg1, int arg2) {
        if (arg1 instanceof float[])
            return ((float[]) arg1)[arg2];
        return getLong(arg1, arg2);
    }

    /**
     * @see java.lang.reflect.Array#getDouble(java.lang.Object, int)
     */
    private static double getDouble(Object arg1, int arg2) {
        if (arg1 instanceof double[])
            return ((double[]) arg1)[arg2];
        return getFloat(arg1, arg2);
    }

    /**
     * @see java.lang.reflect.Array#set(java.lang.Object, int, java.lang.Object)
     */
    private static void set(Object arg1, int arg2, Object arg3) {
        if (arg1 instanceof Object[]) {
            // Too bad the API won't let us throw the easier ArrayStoreException!
            if (arg3 != null
                && !arg1.getClass().getComponentType().isInstance(arg3))
                throw new IllegalArgumentException();
            ((Object[]) arg1)[arg2] = arg3;
        } else if (arg3 instanceof Byte)
            setByte(arg1, arg2, (Byte) arg3);
        else if (arg3 instanceof Short)
            setShort(arg1, arg2, (Short) arg3);
        else if (arg3 instanceof Integer)
            setInt(arg1, arg2, (Integer) arg3);
        else if (arg3 instanceof Long)
            setLong(arg1, arg2, (Long) arg3);
        else if (arg3 instanceof Float)
            setFloat(arg1, arg2, (Float) arg3);
        else if (arg3 instanceof Double)
            setDouble(arg1, arg2, (Double) arg3);
        else if (arg3 instanceof Character)
            setChar(arg1, arg2, (Character) arg3);
        else if (arg3 instanceof Boolean)
            setBoolean(arg1, arg2, (Boolean) arg3);
        else if (arg1 == null)
            throw new NullPointerException();
        else
            throw new IllegalArgumentException();
    }

    /**
     * @see java.lang.reflect.Array#setBoolean(java.lang.Object, int, boolean)
     */
    private static void setBoolean(Object arg1, int arg2, boolean arg3) {
        if (arg1 instanceof boolean[])
            ((boolean[]) arg1)[arg2] = arg3;
        else if (arg1 == null)
            throw new NullPointerException();
        else
            throw new IllegalArgumentException();
    }

    /**
     * @see java.lang.reflect.Array#setByte(java.lang.Object, int, byte)
     */
    private static void setByte(Object arg1, int arg2, byte arg3) {
        if (arg1 instanceof byte[])
            ((byte[]) arg1)[arg2] = arg3;
        else
            setShort(arg1, arg2, arg3);
    }

    /**
     * @see java.lang.reflect.Array#setChar(java.lang.Object, int, char)
     */
    private static void setChar(Object arg1, int arg2, char arg3) {
        if (arg1 instanceof char[])
            ((char[]) arg1)[arg2] = arg3;
        else
            setInt(arg1, arg2, arg3);
    }

    /**
     * @see java.lang.reflect.Array#setShort(java.lang.Object, int, short)
     */
    private static void setShort(Object arg1, int arg2, short arg3) {
        if (arg1 instanceof short[])
            ((short[]) arg1)[arg2] = arg3;
        else
            setInt(arg1, arg2, arg3);
    }

    /**
     * @see java.lang.reflect.Array#setInt(java.lang.Object, int, int)
     */
    private static void setInt(Object arg1, int arg2, int arg3) {
        if (arg1 instanceof int[])
            ((int[]) arg1)[arg2] = arg3;
        else
            setLong(arg1, arg2, arg3);
    }

    /**
     * @see java.lang.reflect.Array#setLong(java.lang.Object, int, long)
     */
    private static void setLong(Object arg1, int arg2, long arg3) {
        if (arg1 instanceof long[])
            ((long[]) arg1)[arg2] = arg3;
        else
            setFloat(arg1, arg2, arg3);
    }

    /**
     * @see java.lang.reflect.Array#setFloat(java.lang.Object, int, float)
     */
    private static void setFloat(Object arg1, int arg2, float arg3) {
        if (arg1 instanceof float[])
            ((float[]) arg1)[arg2] = arg3;
        else
            setDouble(arg1, arg2, arg3);
    }

    /**
     * @see java.lang.reflect.Array#setDouble(java.lang.Object, int, double)
     */
    private static void setDouble(Object arg1, int arg2, double arg3) {
        if (arg1 instanceof double[])
            ((double[]) arg1)[arg2] = arg3;
        else if (arg1 == null)
            throw new NullPointerException();
        else
            throw new IllegalArgumentException();
    }

    /**
     * @see java.lang.reflect.Array#newArray(java.lang.Class, int)
     */
    private static Object newArray(Class arg1, int arg2) {
        if (!((Class<?>) arg1).isPrimitive())
            return createObjectArray((Class<?>) arg1, arg2);
        if ((Class<?>) arg1 == boolean.class)
            return new boolean[arg2];
        if ((Class<?>) arg1 == byte.class)
            return new byte[arg2];
        if ((Class<?>) arg1 == char.class)
            return new char[arg2];
        if ((Class<?>) arg1 == short.class)
            return new short[arg2];
        if ((Class<?>) arg1 == int.class)
            return new int[arg2];
        if ((Class<?>) arg1 == long.class)
            return new long[arg2];
        if ((Class<?>) arg1 == float.class)
            return new float[arg2];
        if ((Class<?>) arg1 == double.class)
            return new double[arg2];
        // assert componentType == void.class
        throw new IllegalArgumentException();
    }

    /**
     * @see java.lang.reflect.Array#multiNewArray(java.lang.Class, int[])
     */
    private static Object multiNewArray(Class arg1, int[] arg2) {
        if (arg2.length <= 0)
            throw new IllegalArgumentException("Empty dimensions array.");

        if (arg2.length - 1 == 0)
            return newInstance((Class<?>) arg1, arg2[0]);

        Object toAdd = createMultiArray((Class) (Class<?>) arg1, arg2, arg2.length - 1 - 1);
        Class thisType = toAdd.getClass();
        Object[] retval
            = (Object[]) createObjectArray(thisType, arg2[(arg2.length - 1)]);
        if (arg2[(arg2.length - 1)] > 0)
            retval[0] = toAdd;
        int i = arg2[(arg2.length - 1)];
        while (--i > 0)
            retval[i] = createMultiArray((Class) (Class<?>) arg1, arg2, arg2.length - 1 - 1);
        return retval;
    }

    static Object createMultiArray(Class type, int[] dimensions,
                                   int index) {
        if (index == 0)
            return newInstance(type, dimensions[0]);

        Object toAdd = createMultiArray(type, dimensions, index - 1);
        Class thisType = toAdd.getClass();
        Object[] retval
            = (Object[]) createObjectArray(thisType, dimensions[index]);
        if (dimensions[index] > 0)
            retval[0] = toAdd;
        int i = dimensions[index];
        while (--i > 0)
            retval[i] = createMultiArray(type, dimensions, index - 1);
        return retval;
    }

    /**
     * Creates a new single-dimensioned array.
     *
     * @param componentType the type of the array to create
     * @param length        the length of the array to create
     * @return the created array, cast to an Object
     * @throws NullPointerException       if <code>componentType</code> is null
     * @throws IllegalArgumentException   if <code>componentType</code> is
     *                                    <code>Void.TYPE</code>
     * @throws NegativeArraySizeException when length is less than 0
     * @throws OutOfMemoryError           if memory allocation fails
     */
    public static Object newInstance(Class<?> componentType, int length) {
        if (!componentType.isPrimitive())
            return createObjectArray(componentType, length);
        if (componentType == boolean.class)
            return new boolean[length];
        if (componentType == byte.class)
            return new byte[length];
        if (componentType == char.class)
            return new char[length];
        if (componentType == short.class)
            return new short[length];
        if (componentType == int.class)
            return new int[length];
        if (componentType == long.class)
            return new long[length];
        if (componentType == float.class)
            return new float[length];
        if (componentType == double.class)
            return new double[length];
        // assert componentType == void.class
        throw new IllegalArgumentException();
    }

    /**
     * Dynamically create an array of objects.
     *
     * @param type guaranteed to be a valid object type
     * @param dim  the length of the array
     * @return the new array
     * @throws NegativeArraySizeException if dim is negative
     * @throws OutOfMemoryError           if memory allocation fails
     */
    static Object createObjectArray(final Class type, int dim) {
        final VmType vmClass = AccessController.doPrivileged(
            new PrivilegedAction<VmType>() {
                public VmType run() {
                    return VmType.fromClass(type);
                }
            });

        final String arrClsName = vmClass.getArrayClassName();
        final VmType arrCls;
        try {
            final VmClassLoader curLoader = vmClass.getLoader();
            arrCls = curLoader.loadClass(arrClsName, true);
            if (arrCls == null) {
                throw new NoClassDefFoundError(arrClsName);
            }
        } catch (ClassNotFoundException ex) {
            throw new NoClassDefFoundError(arrClsName);
        }

        return Vm.getHeapManager().newArray((VmArrayClass) arrCls, dim);
    }

}
