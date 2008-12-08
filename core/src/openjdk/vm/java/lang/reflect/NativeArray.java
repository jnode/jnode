package java.lang.reflect;

/**
 * @see java.lang.reflect.Array
 */
class NativeArray { //TODO OPTIMIZE IT!
    /**
     * @see java.lang.reflect.Array#getLength(java.lang.Object)
     */
    private static int getLength(Object arg1) {
        return CPArray.getLength(arg1);
    }
    /**
     * @see java.lang.reflect.Array#get(java.lang.Object, int)
     */
    private static Object get(Object arg1, int arg2) {
        return CPArray.get(arg1, arg2);
    }
    /**
     * @see java.lang.reflect.Array#getBoolean(java.lang.Object, int)
     */
    private static boolean getBoolean(Object arg1, int arg2) {
        return CPArray.getBoolean(arg1, arg2);
    }
    /**
     * @see java.lang.reflect.Array#getByte(java.lang.Object, int)
     */
    private static byte getByte(Object arg1, int arg2) {
        return CPArray.getByte(arg1, arg2);
    }
    /**
     * @see java.lang.reflect.Array#getChar(java.lang.Object, int)
     */
    private static char getChar(Object arg1, int arg2) {
        return CPArray.getChar(arg1, arg2);
    }
    /**
     * @see java.lang.reflect.Array#getShort(java.lang.Object, int)
     */
    private static short getShort(Object arg1, int arg2) {
        return CPArray.getShort(arg1, arg2);
    }
    /**
     * @see java.lang.reflect.Array#getInt(java.lang.Object, int)
     */
    private static int getInt(Object arg1, int arg2) {
        return CPArray.getInt(arg1, arg2);
    }
    /**
     * @see java.lang.reflect.Array#getLong(java.lang.Object, int)
     */
    private static long getLong(Object arg1, int arg2) {
        return CPArray.getLong(arg1, arg2);
    }
    /**
     * @see java.lang.reflect.Array#getFloat(java.lang.Object, int)
     */
    private static float getFloat(Object arg1, int arg2) {
        return CPArray.getFloat(arg1, arg2);
    }
    /**
     * @see java.lang.reflect.Array#getDouble(java.lang.Object, int)
     */
    private static double getDouble(Object arg1, int arg2) {
        return CPArray.getDouble(arg1, arg2);
    }
    /**
     * @see java.lang.reflect.Array#set(java.lang.Object, int, java.lang.Object)
     */
    private static void set(Object arg1, int arg2, Object arg3) {
        CPArray.set(arg1, arg2, arg3);
    }
    /**
     * @see java.lang.reflect.Array#setBoolean(java.lang.Object, int, boolean)
     */
    private static void setBoolean(Object arg1, int arg2, boolean arg3) {
        CPArray.setBoolean(arg1, arg2, arg3);
    }
    /**
     * @see java.lang.reflect.Array#setByte(java.lang.Object, int, byte)
     */
    private static void setByte(Object arg1, int arg2, byte arg3) {
        CPArray.setByte(arg1, arg2, arg3);
    }
    /**
     * @see java.lang.reflect.Array#setChar(java.lang.Object, int, char)
     */
    private static void setChar(Object arg1, int arg2, char arg3) {
        CPArray.setChar(arg1, arg2, arg3);
    }
    /**
     * @see java.lang.reflect.Array#setShort(java.lang.Object, int, short)
     */
    private static void setShort(Object arg1, int arg2, short arg3) {
        CPArray.setShort(arg1, arg2, arg3);
    }
    /**
     * @see java.lang.reflect.Array#setInt(java.lang.Object, int, int)
     */
    private static void setInt(Object arg1, int arg2, int arg3) {
        CPArray.setInt(arg1, arg2, arg3);
    }
    /**
     * @see java.lang.reflect.Array#setLong(java.lang.Object, int, long)
     */
    private static void setLong(Object arg1, int arg2, long arg3) {
        CPArray.setLong(arg1, arg2, arg3);
    }
    /**
     * @see java.lang.reflect.Array#setFloat(java.lang.Object, int, float)
     */
    private static void setFloat(Object arg1, int arg2, float arg3) {
        CPArray.setFloat(arg1, arg2, arg3);
    }
    /**
     * @see java.lang.reflect.Array#setDouble(java.lang.Object, int, double)
     */
    private static void setDouble(Object arg1, int arg2, double arg3) {
        CPArray.setDouble(arg1, arg2, arg3);
    }
    /**
     * @see java.lang.reflect.Array#newArray(java.lang.Class, int)
     */
    private static Object newArray(Class arg1, int arg2) {
        return CPArray.newInstance(arg1, arg2);
    }
    /**
     * @see java.lang.reflect.Array#multiNewArray(java.lang.Class, int[])
     */
    private static Object multiNewArray(Class arg1, int[] arg2) {
        return CPArray.newInstance(arg1, arg2);
    }
}
