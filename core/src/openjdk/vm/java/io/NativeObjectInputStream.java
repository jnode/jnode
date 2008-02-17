package java.io;

import org.jnode.vm.VmSystem;

/**
 * @see java.io.ObjectInputStream
 * @author Levente S\u00e1ntha
 */
class NativeObjectInputStream {
    /**
     * @see java.io.ObjectInputStream#bytesToFloats(byte[], int, float[], int, int)
     */
    private static void bytesToFloats(byte[] src, int srcpos, float[] dst, int dstpos, int nfloats) {
        if(src == null) throw new NullPointerException();
        if(dst == null) throw new NullPointerException();
        int dstend = dstpos + nfloats;
        while(dstpos < dstend){
            int ival =
                    ((src[srcpos ++ ] & 0xff) << 24) +
                    ((src[srcpos ++ ] & 0xff) << 16) +
                    ((src[srcpos ++ ] & 0xff) <<  8) +
                    ((src[srcpos ++ ] & 0xff)      );
            dst[dstpos ++ ] = Float.intBitsToFloat(ival);
        }
    }
    /**
     * @see java.io.ObjectInputStream#bytesToDoubles(byte[], int, double[], int, int)
     */
    private static void bytesToDoubles(byte[] src, int srcpos, double[] dst, int dstpos, int ndoubles) {
        if(src == null) throw new NullPointerException();
        if(dst == null) throw new NullPointerException();
        int dstend = dstpos + ndoubles;
        while(dstpos < dstend){
            long lval =
                    ((src[srcpos ++ ] & 0xffL) << 56) +
                    ((src[srcpos ++ ] & 0xffL) << 48) +
                    ((src[srcpos ++ ] & 0xffL) << 40) +
                    ((src[srcpos ++ ] & 0xffL) << 32) +
                    ((src[srcpos ++ ] & 0xffL) << 24) +
                    ((src[srcpos ++ ] & 0xffL) << 16) +
                    ((src[srcpos ++ ] & 0xffL) <<  8) +
                    ((src[srcpos ++ ] & 0xffL)      );
            dst[dstpos ++ ] = Double.longBitsToDouble(lval);
        }
    }
    /**
     * @see java.io.ObjectInputStream#latestUserDefinedLoader()
     */
    private static ClassLoader latestUserDefinedLoader() {
        Class[] ctx = VmSystem.getClassContext();
        for(int i = 0; i < ctx.length; i++){
            Class c = ctx[i];
            if(!sun.reflect.MethodAccessor.class.isAssignableFrom(c) &&
            !sun.reflect.ConstructorAccessor.class.isAssignableFrom(c)){
                return c.getClassLoader();
            }
        }
        return null;
    }
}
