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
