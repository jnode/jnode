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
 
package org.jnode.imageio.jpeg;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class JPEGImageReaderSpi extends ImageReaderSpi {
    static final String vendorName = "JNode";
    static final String version = "0.1";
    static final String readerClassName = "org.jnode.imageio.jpeg.JPEGImageReader";
    static final String[] names = {"JPEG"};
    static final String[] suffixes = {".jpeg", ".jpg", ".jpe"};
    static final String[] MIMETypes = {"image/jpeg"};
    static final String[] writerSpiNames = {"org.jnode.imageio.jpeg.JPEGImageWriterSpi"};

    static final boolean supportsStandardStreamMetadataFormat = false;
    static final String nativeStreamMetadataFormatName = null;
    static final String nativeStreamMetadataFormatClassName = null;
    static final String[] extraStreamMetadataFormatNames = null;
    static final String[] extraStreamMetadataFormatClassNames = null;
    static final boolean supportsStandardImageMetadataFormat = false;
    static final String nativeImageMetadataFormatName = null;
    static final String nativeImageMetadataFormatClassName = null;
    static final String[] extraImageMetadataFormatNames = null;
    static final String[] extraImageMetadataFormatClassNames = null;

    private static org.jnode.imageio.jpeg.JPEGImageReaderSpi readerSpi;

    public JPEGImageReaderSpi() {
        super(vendorName, version,
            names, suffixes, MIMETypes,
            readerClassName,
            STANDARD_INPUT_TYPE,
            writerSpiNames,
            supportsStandardStreamMetadataFormat,
            nativeStreamMetadataFormatName,
            nativeStreamMetadataFormatClassName,
            extraStreamMetadataFormatNames,
            extraStreamMetadataFormatClassNames,
            supportsStandardImageMetadataFormat,
            nativeImageMetadataFormatName,
            nativeImageMetadataFormatClassName,
            extraImageMetadataFormatNames,
            extraImageMetadataFormatClassNames);
    }

    public String getDescription(Locale locale) {
        return "JPEG ISO 10918-1, JFIF V1.02";
    }

    public boolean canDecodeInput(Object input) throws IOException {
        if (!(input instanceof ImageInputStream))
            return false;

        ImageInputStream in = (ImageInputStream) input;
        boolean retval;

        in.mark();
        try {
            //todo implement a less expensive canDecode()
            new JPEGDecoderAdapter(in).decode();
            retval = true;
        } catch (JPEGException e) {
            retval = false;
        }
        in.reset();

        return retval;
    }

    public ImageReader createReaderInstance(Object extension) {
        return new JPEGImageReader(this);
    }

    public static void registerSpis(IIORegistry reg) {
        reg.registerServiceProvider(getReaderSpi(), ImageReaderSpi.class);
    }

    public static synchronized JPEGImageReaderSpi getReaderSpi() {
        if (readerSpi == null)
            readerSpi = new JPEGImageReaderSpi();
        return readerSpi;
    }
}
