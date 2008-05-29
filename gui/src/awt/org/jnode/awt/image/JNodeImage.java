/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.awt.image;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.util.Hashtable;
import java.util.LinkedList;
import org.jnode.awt.util.AwtUtils;

/**
 * @author epr
 */
public class JNodeImage extends Image {

    int width;
    int height;
    int availableInfo;
    Hashtable<String, Object> properties = new Hashtable<String, Object>();
    Object pixels;
    ColorModel colorModel;
    private boolean productionStarted;
    private ImageProducer initProducer;

    /**
     * Create an empty, transparent image
     *
     * @param width
     * @param height
     */
    public JNodeImage(int width, int height) {
        this(width, height, ColorModel.getRGBdefault());
    }

    /**
     * Create an empty, transparent image
     *
     * @param width
     * @param height
     * @param colorModel
     */
    public JNodeImage(int width, int height, ColorModel colorModel) {
        this.width = width;
        this.height = height;
        this.availableInfo = ImageObserver.ALLBITS | ImageObserver.HEIGHT | ImageObserver.WIDTH | ImageObserver.ALLBITS;
        this.colorModel = colorModel;
    }

    /**
     * Create a new instance
     *
     * @param producer
     */
    public JNodeImage(ImageProducer producer) {
        this.initProducer = producer;
        this.colorModel = ColorModel.getRGBdefault();
    }

    /**
     * @see java.awt.Image#flush()
     */
    public void flush() {
        // TODO Auto-generated method stub

    }

    /**
     * @return The graphics
     * @see java.awt.Image#getGraphics()
     */
    public Graphics getGraphics() {
        return null;
    }

    /**
     * @param observer
     * @return the height
     * @see java.awt.Image#getHeight(java.awt.image.ImageObserver)
     */
    public int getHeight(ImageObserver observer) {
        if ((availableInfo & ImageObserver.HEIGHT) == 0) {
            prepare(observer);
        }
        return height;
    }

    /**
     * @param name
     * @param observer
     * @return the property
     * @see java.awt.Image#getProperty(java.lang.String, java.awt.image.ImageObserver)
     */
    public Object getProperty(String name, ImageObserver observer) {
        if ((availableInfo & ImageObserver.PROPERTIES) == 0) {
            prepare(observer);
        }
        return properties.get(name);
    }

    /**
     * @return the source
     * @see java.awt.Image#getSource()
     */
    public ImageProducer getSource() {
        return new ForwardingProducer(null);
    }

    /**
     * @param observer
     * @return the width
     * @see java.awt.Image#getWidth(java.awt.image.ImageObserver)
     */
    public int getWidth(ImageObserver observer) {
        if ((availableInfo & ImageObserver.WIDTH) == 0) {
            prepare(observer);
        }
        return width;
    }

    public int checkImage() {
        return availableInfo;
    }

    /**
     * Synchronizes the image loading.
     *
     * @since PJA2.0
     */
    public void sync() {
        loadInitImage(true, null);
    }

    public boolean prepare(ImageObserver observer) {
        if (!productionStarted) {
            loadInitImage(false, observer);
        } else if ((availableInfo & ImageObserver.ERROR) != 0) {
            if (observer != null) {
                observer.imageUpdate(this, availableInfo, -1, -1, -1, -1);
            }
        } else {
            new ForwardingProducer(observer).startProduction(null);
        }

        return (availableInfo & ImageObserver.ALLBITS) != 0;
    }

    protected int getWidth() {
        return width;
    }

    protected int getHeight() {
        return height;
    }

    /**
     * Gets the array used to store the pixels of this image.
     *
     * @return an array of <code>int</code> or <code>null</code> if the array
     *         contains <code>byte</code> or if the image was flushed.
     */
    protected int[] getPixels() {
        Object pixelsArray = getPixelsArray();
        return pixelsArray instanceof int[] ? (int[]) pixelsArray : null;
    }

    /**
     * Gets the array used to store the pixels of this image.
     *
     * @return an array of <code>int</code> or <code>byte</code>.
     * @since PJA2.3
     */
    protected Object getPixelsArray() {
        if (pixels == null && width >= 0 && height >= 0 && (availableInfo & ImageObserver.ERROR) == 0) {
            // v2.3 : Added support for 8 bit color images
            if (colorModel == null || !(colorModel instanceof IndexColorModel))
                // Should take into account the size required to store each pixel with model.getPixelSize ()
                // but for the moment only default RGB model is used
                pixels = new int[width * height];
            else
                pixels = new byte[width * height];
        }

        return pixels;
    }

    /**
     * Gets the color at the point <code>(x,y)</code>.
     *
     * @param x the point coordinates.
     * @param y
     * @return the color of the point in default RGB model.
     * @since PJA2.0
     */
    protected int getPixelColor(int x, int y) {
        if ((availableInfo & ImageObserver.ERROR) != 0 || pixels == null || x < 0 || x >= width || y < 0 || y >= height)
            return 0;

        // v2.3 : Added support for 8 bit color images
        Object pixelsArray = getPixelsArray();
        if (pixelsArray instanceof int[])
            return ((int[]) pixelsArray)[x + y * width];
        else if (pixelsArray instanceof byte[])
            return colorModel.getRGB(((byte[]) pixelsArray)[x + y * width] & 0xFF);
        else
            return 0;
    }

    /**
     * Sets the color at the point <code>(x,y)</code>.
     *
     * @param x    the point coordinates.
     * @param y
     * @param ARGB the color of the point in default RGB model.
     * @since PJA2.0
     */
    protected void setPixelColor(int x, int y, int ARGB) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            // v2.3 : Added support for 8 bit color images
            // Get the buffer with getPixelsArray () to ensure that pixels array is available
            Object pixelsArray = getPixelsArray();

            if (pixelsArray instanceof int[]) {
                ((int[]) pixelsArray)[x + y * width] = ARGB;
            } else if (pixelsArray instanceof byte[]) {
                ((byte[]) pixelsArray)[x + y * width] =
                    (byte) AwtUtils.getClosestColorIndex((IndexColorModel) colorModel, ARGB);
            }
        }
    }

    private synchronized void loadInitImage(boolean wait, final ImageObserver observer) {
        if (!productionStarted) {
            final ImageProducer producer = initProducer;
            // Loads asynchronously initial image if not yet done
            new Thread() {
                public void run() {
                    producer.startProduction(new JNodeConsumer(producer, observer));
                }
            }
                .start();

            productionStarted = true;
            // v1.2 : Moved wait () out of if (!productionStarted) block
            //        because loadInitImage () can called first with parameter wait == false (by prepareImage ())
            //        and then can be called again with parameter wait == true. In that case,
            //        current thread must be stopped to complete image download.
        }

        try {
            // If image isn't downloaded yet
            if ((availableInfo & ImageObserver.ERROR) == 0 && (availableInfo & ImageObserver.ALLBITS) == 0) {
                // Wait the producer notifies us when image is ready
                if (wait) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            availableInfo = ImageObserver.ABORT | ImageObserver.ERROR;
        }
    }

    /**
     * ImageProducer implementation.
     * This producer is used to forward data to ImageConsumer instances and
     * to inform ImageObserver instances.
     */
    private class ForwardingProducer implements ImageProducer {

        /**
         * All consumers
         */
        private final LinkedList<ImageConsumer> consumers = new LinkedList<ImageConsumer>();
        /**
         * The observer, can be null
         */
        private final ImageObserver observer;

        public ForwardingProducer(ImageObserver observer) {
            this.observer = observer;
        }

        public synchronized void addConsumer(final ImageConsumer ic) {
            if (ic != null && isConsumer(ic)) {
                return;
            }

            if ((availableInfo & ImageObserver.ERROR) == 0) {
                // v1.1 : forgot to check ic
                if (ic != null) {
                    consumers.add(ic);
                }

                // Complete image production before drawing in it
                sync();
            }

            synchronized (JNodeImage.this) {
                if ((availableInfo & ImageObserver.ERROR) != 0) {
                    if (ic != null) {
                        ic.imageComplete(ImageConsumer.IMAGEERROR);
                    }
                    if (observer != null) {
                        observer.imageUpdate(JNodeImage.this, availableInfo, -1, -1, -1, -1);
                    }
                } else {
                    if (ic != null) {
                        ic.setDimensions(width, height);
                        ic.setHints(
                            ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME | ImageConsumer.TOPDOWNLEFTRIGHT);
                        ic.setProperties(properties);
                        if (colorModel != null) {
                            ic.setColorModel(colorModel);
                        }
                        if (pixels instanceof int[]) {
                            ic.setPixels(0, 0, width, height, colorModel, (int[]) pixels, 0, width);
                        } else {
                            ic.setPixels(0, 0, width, height, colorModel, (byte[]) pixels, 0, width);
                        }
                        ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
                    }

                    if (observer != null) {
                        observer.imageUpdate(JNodeImage.this, availableInfo, 0, 0, width, height);
                    }
                }
            }
        }

        public synchronized void removeConsumer(ImageConsumer ic) {
            consumers.remove(ic);
        }

        public synchronized boolean isConsumer(ImageConsumer ic) {
            return consumers.contains(ic);
        }

        public void startProduction(ImageConsumer ic) {
            addConsumer(ic);
        }

        public void requestTopDownLeftRightResend(ImageConsumer ic) {
            // Useless, already sent in that order
        }
    }

    /**
     * ImageConsumer implementation
     */
    private class JNodeConsumer implements ImageConsumer {
        private final ImageProducer producer;
        private final ImageObserver observer;

        public JNodeConsumer(ImageProducer producer, ImageObserver observer) {
            this.producer = producer;
            this.observer = observer;
        }

        public void setDimensions(int width, int height) {
            JNodeImage.this.width = width;
            JNodeImage.this.height = height;
            JNodeImage.this.availableInfo |= ImageObserver.WIDTH | ImageObserver.HEIGHT;
            if (observer != null) {
                observer.imageUpdate(JNodeImage.this, availableInfo, 0, 0, width, height);
            }
        }

        public void setHints(int hints) {
        }

        @SuppressWarnings("unchecked")
        public void setProperties(Hashtable props) {
            JNodeImage.this.properties = props;
            JNodeImage.this.availableInfo |= ImageObserver.PROPERTIES;
            if (observer != null) {
                observer.imageUpdate(JNodeImage.this, availableInfo, 0, 0, width, height);
            }
        }

        public void setColorModel(ColorModel model) {
            //System.out.println("setColorModel " + model);
            JNodeImage.this.colorModel = model;
        }

        public void setPixels(int x, int y, int width, int height, ColorModel model, byte pixels[], int offset,
                              int scansize) {
            synchronized (JNodeImage.this) {
                // v1.1 : check if image not flushed
                if ((availableInfo & ImageObserver.ERROR) == 0) {
                    final Object pixelsArray = getPixelsArray();
                    final int[] intPixels;
                    final byte[] bytePixels;
                    int lastARGB = 0;
                    byte lastIndex = (byte) -1;

                    if (pixelsArray instanceof int[]) {
                        intPixels = (int[]) pixelsArray;
                        bytePixels = null;
                    } else {
                        bytePixels = (byte[]) pixelsArray;
                        intPixels = null;
                    }

                    for (int row = 0, destRow = y * JNodeImage.this.width;
                         row < height; row++, destRow += JNodeImage.this.width) {
                        final int rowOff = offset + row * scansize;
                        for (int col = 0; col < width; col++)
                            // v2.3 : Added support for 8 bit color images
                            if (intPixels != null) {
                                // v1.2 : Added & 0xFF to disable sign bit
                                intPixels[destRow + x + col] = model.getRGB(pixels[rowOff + col] & 0xFF);
                            } else if (colorModel == model) {
                                bytePixels[destRow + x + col] = pixels[rowOff + col];
                            } else {
                                final int ARGB = model.getRGB(pixels[rowOff + col] & 0xFF);
                                if (lastIndex == -1 || lastARGB != ARGB) {
                                    // Keep track of the last color
                                    lastARGB = ARGB;
                                    lastIndex =
                                        (byte) AwtUtils.getClosestColorIndex((IndexColorModel) colorModel, lastARGB);
                                }
                                bytePixels[destRow + x + col] = lastIndex;
                            }
                    }
                    JNodeImage.this.availableInfo |= ImageObserver.SOMEBITS;
                }
            }
        }

        public void setPixels(int x, int y, int width, int height, ColorModel model, int pixels[], int offset,
                              int scansize) {
            synchronized (JNodeImage.this) {
                // v1.1 : check if image not flushed
                if ((availableInfo & ImageObserver.ERROR) == 0) {
                    final Object pixelsArray = getPixelsArray();
                    final int[] intPixels;
                    final byte[] bytePixels;
                    int lastARGB = 0;
                    byte lastIndex = (byte) -1;

                    if (pixelsArray instanceof int[]) {
                        intPixels = (int[]) pixelsArray;
                        bytePixels = null;
                    } else {
                        bytePixels = (byte[]) pixelsArray;
                        intPixels = null;
                    }

                    for (int row = 0, destRow = y * JNodeImage.this.width;
                         row < height; row++, destRow += JNodeImage.this.width) {
                        final int rowOff = offset + row * scansize;
                        for (int col = 0; col < width; col++) {
                            int ARGB = model == null ? pixels[rowOff + col] : model.getRGB(pixels[rowOff + col]);
                            // v2.3 : Added support for 8 bit color images
                            if (intPixels != null)
                                // If model == null, consider it's the default RGB model
                                intPixels[destRow + x + col] = ARGB;
                            else {
                                if (lastIndex == -1 || lastARGB != ARGB) {
                                    // Keep track of the last color
                                    lastARGB = ARGB;
                                    lastIndex =
                                        (byte) AwtUtils.getClosestColorIndex((IndexColorModel) colorModel, lastARGB);
                                }
                                bytePixels[destRow + x + col] = lastIndex;

                            }
                        }
                    }
                    availableInfo |= ImageObserver.SOMEBITS;
                }
            }
        }

        public void imageComplete(int status) {
            synchronized (JNodeImage.this) {
                if (status == IMAGEERROR) {
                    availableInfo = ImageObserver.ERROR;
                } else if (status == IMAGEABORTED) {
                    availableInfo = ImageObserver.ABORT | ImageObserver.ERROR;
                } else {
                    availableInfo &= ~ImageObserver.SOMEBITS;
                    if (status == STATICIMAGEDONE) {
                        availableInfo |= ImageObserver.ALLBITS;
                    } else if (status == SINGLEFRAMEDONE) {
                        // This implementation manages only one frame
                        availableInfo |= ImageObserver.ALLBITS /*ImageObserver.FRAMEBITS*/;
                    }
                }

                if (status == IMAGEERROR || status == IMAGEABORTED) {
                    pixels = null;
                }

                producer.removeConsumer(this);
                if (observer != null) {
                    if ((availableInfo & ImageObserver.ERROR) != 0) {
                        observer.imageUpdate(JNodeImage.this, availableInfo, -1, -1, -1, -1);
                    } else {
                        observer.imageUpdate(JNodeImage.this, availableInfo, 0, 0, width, height);
                    }
                }

                // v1.2 : Moved synchronized to method start
                JNodeImage.this.notifyAll();
            }
        }
    }
}
