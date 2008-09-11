/*
 * $Id$
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
