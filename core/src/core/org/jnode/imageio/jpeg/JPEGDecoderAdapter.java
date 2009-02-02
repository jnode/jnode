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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

/**
 * @author Levente S\u00e1ntha
 */
public class JPEGDecoderAdapter implements JPEGDecoder.PixelArray {
    private JPEGDecoder dec;
    private InputStream is;
    private BufferedImage im;

    public JPEGDecoderAdapter(final ImageInputStream in) throws JPEGException {
        dec = new JPEGDecoder();
        is = new BufferedInputStream(new InputStream() {
            public int read() throws IOException {
                return in.read();
            }
        });
    }

    public int getHeight() throws JPEGException {
        decode();
        return im.getHeight();
    }

    public int getWidth() throws JPEGException {
        decode();
        return im.getWidth();
    }

    public BufferedImage getImage() {
        return im;
    }

    public void decode() throws JPEGException {
        if (im == null) {
            try {
                dec.decode(is, this);
            } catch (Exception x) {
                throw new JPEGException(x.getMessage());
            }
        }
    }

    public void setPixel(int x, int y, int argb) {
        im.setRGB(x, y, argb);
    }

    public void setSize(int width, int height) throws Exception {
        im = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
}
