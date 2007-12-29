package sun.awt.image;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.Kernel;

/**
 * @see sun.awt.image.ImagingLib
 * @author Levente S\u00e1ntha
 */
class NativeImagingLib {
    /**
     * @see sun.awt.image.ImagingLib#init()
     */
    private static void init() {
        //todo implement it
    }
    /**
     * @see sun.awt.image.ImagingLib#transformBI(java.awt.image.BufferedImage, java.awt.image.BufferedImage, double[], int)
     */
    private static int transformBI(BufferedImage arg1, BufferedImage arg2, double[] arg3, int arg4) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.awt.image.ImagingLib#transformRaster(java.awt.image.Raster, java.awt.image.Raster, double[], int)
     */
    private static int transformRaster(Raster arg1, Raster arg2, double[] arg3, int arg4) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.awt.image.ImagingLib#convolveBI(java.awt.image.BufferedImage, java.awt.image.BufferedImage, java.awt.image.Kernel, int)
     */
    private static int convolveBI(BufferedImage arg1, BufferedImage arg2, Kernel arg3, int arg4) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.awt.image.ImagingLib#convolveRaster(java.awt.image.Raster, java.awt.image.Raster, java.awt.image.Kernel, int)
     */
    private static int convolveRaster(Raster arg1, Raster arg2, Kernel arg3, int arg4) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.awt.image.ImagingLib#lookupByteBI(java.awt.image.BufferedImage, java.awt.image.BufferedImage, byte[][])
     */
    private static int lookupByteBI(BufferedImage arg1, BufferedImage arg2, byte[][] arg3) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.awt.image.ImagingLib#lookupByteRaster(java.awt.image.Raster, java.awt.image.Raster, byte[][])
     */
    private static int lookupByteRaster(Raster arg1, Raster arg2, byte[][] arg3) {
        //todo implement it
        return 0;
    }
}
