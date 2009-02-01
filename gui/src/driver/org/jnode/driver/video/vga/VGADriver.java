/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.driver.video.vga;

import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.driver.video.AbstractFrameBufferDriver;
import org.jnode.driver.video.AlreadyOpenException;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.NotOpenException;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.UnknownConfigurationException;
import org.jnode.driver.video.vgahw.VgaConstants;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author epr
 */
public class VGADriver extends AbstractFrameBufferDriver implements VgaConstants {

    static final IndexColorModel COLOR_MODEL = new IndexColorModel(4, 16, REDS, GREENS, BLUES) {
        // Typically overridden
        public SampleModel createCompatibleSampleModel(int w, int h) {
            // return new VGASampleModel(w, h);
            return new SinglePixelPackedSampleModel(DataBuffer.TYPE_BYTE, w, h, new int[] {0xFF});
        }

        /**
         * Converts an sRGB pixel int value to an array containing a single
         * pixel of the color space of the color model. <p/>
         * <p>
         * This method performs the inverse function of
         * <code>getRGB(Object inData)</code>. <p/> Outline of conversion
         * process: <p/>
         * <ol>
         * <p/>
         * <li>Convert rgb to normalized [0.0, 1.0] sRGB values.</li>
         * <p/>
         * <li>Convert to color space components using fromRGB in ColorSpace.</li>
         * <p/>
         * <li>If color model has alpha and should be premultiplied, multiply
         * color space components with alpha value</li>
         * <p/>
         * <li>Scale the components to the correct number of bits.</li>
         * <p/>
         * <li>Arrange the components in the output array</li>
         * <p/>
         * </ol>
         * 
         * @param rgb The color to be converted to dataElements. A pixel in sRGB
         *            color space, encoded in default 0xAARRGGBB format, assumed
         *            not alpha premultiplied.
         * @param pixel to avoid needless creation of arrays, an array to use to
         *            return the pixel can be given. If null, a suitable array
         *            will be created.
         * @return An array of transferType values representing the color, in
         *         the color model format. The color model defines whether the
         * @see #getRGB(Object)
         */
        public Object getDataElements(int rgb, Object pixel) {
            // TODO determin the reight color here
            byte[] p = new byte[1];
            int min_i = 0;
            int min_rgb = Integer.MAX_VALUE;
            for (int i = 0; i < 16; i++) {
                int c = getRGB(i);
                if (c == rgb) {
                    min_i = i;
                    break;
                } else {
                    int r1 = (0x00FF0000 & c) >> 16;
                    int g1 = (0x0000FF00 & c) >> 8;
                    int b1 = (0x000000FF & c);
                    int r2 = (0x00FF0000 & rgb) >> 16;
                    int g2 = (0x0000FF00 & rgb) >> 8;
                    int b2 = (0x000000FF & rgb);
                    int dr = r1 - r2;
                    dr = dr < 0 ? -dr : dr;
                    int dg = g1 - g2;
                    dg = dg < 0 ? -dg : dg;
                    int db = b1 - b2;
                    db = db < 0 ? -db : db;
                    int v = dr + dg + db;
                    if (min_rgb < v) {
                        min_rgb = v;
                        min_i = i;
                    }
                }
            }
            p[0] = (byte) min_i;
            return p;
        }
    };

    private static final FrameBufferConfiguration[] CONFIGS = {new VGAConfiguration(640, 480, COLOR_MODEL)};

    private FrameBufferConfiguration currentConfig;
    private VGASurface vga;

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected synchronized void stopDevice() throws DriverException {
        if (vga != null) {
            vga.close();
        }
        super.stopDevice();
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getConfigurations()
     */
    public FrameBufferConfiguration[] getConfigurations() {
        return CONFIGS;
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getCurrentConfiguration()
     */
    public FrameBufferConfiguration getCurrentConfiguration() {
        return currentConfig;
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#open(org.jnode.driver.video.FrameBufferConfiguration)
     */
    public synchronized Surface open(FrameBufferConfiguration config)
        throws UnknownConfigurationException, AlreadyOpenException, DeviceException {
        if (currentConfig != null) {
            throw new AlreadyOpenException();
        } else if (config.equals(CONFIGS[0])) {
            try {
                vga = new VGASurface(this);
                currentConfig = config;
                vga.open(config);
                return vga;
            } catch (ResourceNotFreeException ex) {
                throw new DeviceException(ex);
            } catch (DriverException ex) {
                throw new DeviceException(ex);
            }
        } else {
            throw new UnknownConfigurationException();
        }
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getCurrentSurface()
     */
    public synchronized Surface getCurrentSurface() throws NotOpenException {
        if (currentConfig != null) {
            return vga;
        } else {
            throw new NotOpenException();
        }
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#isOpen()
     */
    public final synchronized boolean isOpen() {
        return (currentConfig != null);
    }

    /**
     * The given surface is closed.
     */
    synchronized void close(VGASurface vga) {
        currentConfig = null;
    }
}
