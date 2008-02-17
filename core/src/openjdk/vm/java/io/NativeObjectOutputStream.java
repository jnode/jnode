package java.io;

/**
 * @see java.io.ObjectOutputStream
 * @author Levente S\u00e1ntha
 */
class NativeObjectOutputStream {
    /**
     * @see java.io.ObjectOutputStream#floatsToBytes(float[], int, byte[], int, int)
     */
    private static void floatsToBytes(float[] src, int srcpos, byte[] dst, int dstpos, int nfloats) {
        if(src == null) throw new NullPointerException();
        if(dst == null) throw new NullPointerException();
        int srcend = srcpos + nfloats;
        while( srcpos < srcend ){
            int ival = Float.floatToIntBits(src[srcpos ++]);
            dst[dstpos ++ ] = (byte) ((ival >> 24) & 0xff);
            dst[dstpos ++ ] = (byte) ((ival >> 16) & 0xff);
            dst[dstpos ++ ] = (byte) ((ival >>  8) & 0xff);
            dst[dstpos ++ ] = (byte) ((ival      ) & 0xff);
        }
    }
    /**
     * @see java.io.ObjectOutputStream#doublesToBytes(double[], int, byte[], int, int)
     */
    private static void doublesToBytes(double[] src, int srcpos, byte[] dst, int dstpos, int ndoubles) {
        if(src == null) throw new NullPointerException();
        if(dst == null) throw new NullPointerException();
        int srcend = srcpos + ndoubles;
        while( srcpos < srcend ){
            long lval = Double.doubleToLongBits(src[srcpos ++]);
            dst[dstpos ++ ] = (byte) ((lval >> 56) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >> 48) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >> 40) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >> 32) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >> 24) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >> 16) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval >>  8) & 0xff);
            dst[dstpos ++ ] = (byte) ((lval      ) & 0xff);
        }
    }
}
