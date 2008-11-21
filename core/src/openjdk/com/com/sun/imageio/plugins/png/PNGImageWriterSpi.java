/*
 * Copyright 2000-2005 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.imageio.plugins.png;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.util.Locale;
import javax.imageio.ImageWriter;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.spi.ImageWriterSpi;

/**
 */
public class PNGImageWriterSpi extends ImageWriterSpi {

    private static final String vendorName = "Sun Microsystems, Inc.";
    
    private static final String version = "1.0";
    
    private static final String[] names = { "png", "PNG" };
    
    private static final String[] suffixes = { "png" };
    
    private static final String[] MIMETypes = { "image/png", "image/x-png" };
    
    private static final String writerClassName =
        "com.sun.imageio.plugins.png.PNGImageWriter";
    
    private static final String[] readerSpiNames = {
        "com.sun.imageio.plugins.png.PNGImageReaderSpi"
    };

    public PNGImageWriterSpi() {
          super(vendorName,
                version,
                names,
                suffixes,
                MIMETypes,
                writerClassName,
                STANDARD_OUTPUT_TYPE,
                readerSpiNames,
                false,
                null, null,
                null, null,
                true,
                PNGMetadata.nativeMetadataFormatName,
                "com.sun.imageio.plugins.png.PNGMetadataFormat",
                null, null
                );
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
        SampleModel sampleModel = type.getSampleModel();
        ColorModel colorModel = type.getColorModel();

        // Find the maximum bit depth across all channels
        int[] sampleSize = sampleModel.getSampleSize(); 
        int bitDepth = sampleSize[0];
        for (int i = 1; i < sampleSize.length; i++) {
            if (sampleSize[i] > bitDepth) {
                bitDepth = sampleSize[i];
            }
        }

        // Ensure bitDepth is between 1 and 16
        if (bitDepth < 1 || bitDepth > 16) {
            return false;
        }

        // Check number of bands, alpha
        int numBands = sampleModel.getNumBands();
        if (numBands < 1 || numBands > 4) {
            return false;
        }

        boolean hasAlpha = colorModel.hasAlpha();
        // Fix 4464413: PNGTransparency reg-test was failing
        // because for IndexColorModels that have alpha,
        // numBands == 1 && hasAlpha == true, thus causing
        // the check below to fail and return false.
        if (colorModel instanceof IndexColorModel) {
            return true;
        }
        if ((numBands == 1 || numBands == 3) && hasAlpha) {
            return false;
        }
        if ((numBands == 2 || numBands == 4) && !hasAlpha) {
            return false;
        }

        return true;
    }

    public String getDescription(Locale locale) {
        return "Standard PNG image writer";
    }

    public ImageWriter createWriterInstance(Object extension) {
        return new PNGImageWriter(this);
    }
}
