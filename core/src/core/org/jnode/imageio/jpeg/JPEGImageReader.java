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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class JPEGImageReader extends ImageReader {
    private JPEGDecoderAdapter decoder;

    protected JPEGImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    public int getHeight(int imageIndex) throws IOException {
        checkIndex(imageIndex);
        decodeStream();
        return decoder.getHeight();
    }

    public IIOMetadata getImageMetadata(int imageIndex)
        throws IOException {
        checkIndex(imageIndex);
        //todo handle metadata
        return null;
    }

    public Iterator getImageTypes(int imageIndex) throws IOException {
        return null;
    }

    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    public IIOMetadata getStreamMetadata()
        throws IOException {
        //todo handle metadata
        return null;
    }

    public int getWidth(int imageIndex)
        throws IOException {
        checkIndex(imageIndex);
        decodeStream();
        return decoder.getWidth();
    }

    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IOException {
        checkIndex(imageIndex);
        decodeStream();
        return decoder.getImage();
    }

    private void checkIndex(int imageIndex) throws IndexOutOfBoundsException {
        if (imageIndex != 0)
            throw new IndexOutOfBoundsException();
    }

    private void checkStream() throws IOException {
        if (!(input instanceof ImageInputStream))
            throw new IllegalStateException("Input not an ImageInputStream.");
        if (input == null)
            throw new IllegalStateException("No input stream.");
    }

    private void decodeStream() throws IOException, IIOException {
        if (decoder != null)
            return;

        checkStream();

        decoder = new JPEGDecoderAdapter((ImageInputStream) input);
        decoder.decode();
    }
}
