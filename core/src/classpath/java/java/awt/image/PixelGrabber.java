/* PixelGrabber.java -- Java class for providing image data 
   Copyright (C) 1999 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.awt.image;

import java.awt.Image;
import java.util.Hashtable;

/**
   PixelGrabber is an ImageConsumer designed to extract a rectangular region of pixels
   from an Image
 */
public class PixelGrabber implements ImageConsumer {

	private final int dstX;
	private final int dstY;
	private int dstWidth;
	private int dstHeight;
	private int status;
	private int dstScansize;
	private final int dstOffset;
	private ColorModel dstModel = ColorModel.getRGBdefault();
	//int hints;
	//Hashtable props;
	private int intPixels[];
	private byte pixel_bufferb[];
	private boolean grabbing;
	private ImageProducer ip;

	private static final int GRABBEDBITS = (ImageObserver.FRAMEBITS | ImageObserver.ALLBITS);
	private static final int DONEBITS = (GRABBEDBITS | ImageObserver.ERROR);

	/**
	 * Create a PixelGrabber used to grab pixels from the specified Image 
	 * in the specified rectangle
	 *
	 * @param img the Image to grab pixels from
	 * @param x the x coordinate of the rectangle
	 * @param y the y coordinate of the rectangle
	 * @param w the width of the rectangle
	 * @param h the height of the rectangle
	 * @param pixels the array of pixel values
	 * @param offset the index of the first pixels in the <code>pixels</code> array
	 * @param scansize the width to use in extracting pixels from the <code>pixels</code> array
	 */
	public PixelGrabber(Image img, int x, int y, int w, int h, int pix[], int off, int scansize) {
		this(img.getSource(), x, y, w, h, pix, off, scansize);
	}

	/**
	 * Create a PixelGrabber used to grab pixels from the specified ImageProducer
	 * in the specified rectangle
	 *
	 * @param ip the ImageProducer to grab pixels from
	 * @param x the x coordinate of the rectangle
	 * @param y the y coordinate of the rectangle
	 * @param w the width of the rectangle
	 * @param h the height of the rectangle
	 * @param pixels the array of pixel values
	 * @param offset the index of the first pixels in the <code>pixels</code> array
	 * @param scansize the width to use in extracting pixels from the <code>pixels</code> array
	 */
	public PixelGrabber(ImageProducer ip, int x, int y, int w, int h, int pix[], int off, int scansize) {
		this.ip = ip;
		this.dstX = x;
		this.dstY = y;
		this.dstWidth = w;
		this.dstHeight = h;
		this.intPixels = pix;
		this.dstOffset = off;
		this.dstScansize = scansize;
		pixel_bufferb = new byte[pix.length * 4];
	}

	/**
	 * Create a PixelGrabber used to grab pixels from the specified Image 
	 * in the specified rectangle
	 *
	 * @param img the Image to grab pixels from
	 * @param x the x coordinate of the rectangle
	 * @param y the y coordinate of the rectangle
	 * @param w the width of the rectangle
	 * @param h the height of the rectangle
	 * @param forceRGB true to force conversion to RGB
	 */
	public PixelGrabber(Image img, int x, int y, int w, int h, boolean forceRGB) {
		this.ip = img.getSource();
		this.dstX = x;
		this.dstY = y;
		this.dstWidth = w;
		this.dstHeight = h;
		this.intPixels = null;
		this.dstOffset = 0;
		this.dstScansize = -1;
		this.pixel_bufferb = null;
	}

	/**
	   Start Grabbing Pixels
	 */
	public synchronized void startGrabbing() {
		if (grabbing == false) {
			grabbing = true;
			ip.startProduction(this);
		}
	}

	/**
	   Abort the grabbing of pixels
	 */
	public synchronized void abortGrabbing() {
		if (grabbing == true) {
			grabbing = false;
			ip.removeConsumer(this);
		}
	}

	/**
	   Grab the Pixels.
	
	   @return true if successful
	
	   @throws InterruptedException if interrupted by another thread.
	 */
	public boolean grabPixels() throws InterruptedException {
		return grabPixels(0);
	}

	/**
	   Grab the Pixels and abort if it takes too long
	   @return true if successful
	   @throws InterruptedException if interrupted by another thread.
	           or time runs out
	 */
	public synchronized boolean grabPixels(long ms) throws InterruptedException {
		if ((status & DONEBITS) != 0) {
			return (status & GRABBEDBITS) != 0;
		}
		long end = ms + System.currentTimeMillis();
		if (!grabbing) {
			grabbing = true;
			status &= ~(ImageObserver.ABORT);
			ip.startProduction(this);
		}
		while (grabbing) {
			long timeout;
			if (ms == 0) {
				timeout = 0;
			} else {
				timeout = end - System.currentTimeMillis();
				if (timeout <= 0) {
					break;
				}
			}
			wait(timeout);
		}
		return (status & GRABBEDBITS) != 0;
	}

	/**
	   Get the status of the pixel grabbing representing by ImageObserver flags
	
	   @return the status
	*/
	public synchronized int getStatus() {
		return status;
	}

	/**
	   Return width of pixel region
	
	   @return width of region
	*/
	public synchronized int getWidth() {
		return dstWidth;
	}

	/**
	   Return height of pixel region
	   
	   @return height of region
	*/
	public synchronized int getHeight() {
		return dstHeight;
	}

	/**
	   Returns the grabbed pixel buffer 
	
	   @return a byte or int array
	*/
	public synchronized Object getPixels() {
		if (intPixels != null)
			return intPixels;
		return pixel_bufferb;
	}

	/**
	   Get the ColorModel of the image
	   
	   @return the ColorModel
	*/
	public synchronized ColorModel getColorModel() {
		return dstModel;
	}

	/**
	 * An <code>ImageProducer</code> indicates the size of the image
	 * being produced using this method.
	 * 
	 * @param width the width of the image
	 * @param height the height of the image 
	 */
	public void setDimensions(int width, int height) {
		System.out.println("setDimension " + width + "," + height);
		if (dstWidth < 0) {
			dstWidth = width - dstX;
		}
		if (dstHeight < 0) {
			dstHeight = height - dstY;
		}
		if (dstWidth <= 0 || dstHeight <= 0) {
			imageComplete(STATICIMAGEDONE);
		} else if (intPixels == null && dstModel == ColorModel.getRGBdefault()) {
			intPixels = new int[dstWidth * dstHeight];
			dstScansize = dstWidth;
		}
		status |= (ImageObserver.WIDTH | ImageObserver.HEIGHT);
	}

	/**
	 * An <code>ImageProducer</code> can set a list of properties
	 * associated with this image by using this method.
	 *
	 * @param props the list of properties associated with this image 
	 */
	public void setProperties(Hashtable props) {
		//this.props = props; //FIXME - DO WE NEED THIS
	}

	/**
	 * This <code>ColorModel</code> should indicate the model used by
	 * the majority of calls to <code>setPixels</code>.  Each call to
	 * <code>setPixels</code> could however indicate a different
	 * <code>ColorModel</code>.
	 *
	 * @param model the color model to be used most often by setPixels
	 * @see ColorModel 
	 */
	public void setColorModel(ColorModel model) {
		this.dstModel = model;
	}

	/**
	 * The <code>ImageProducer</code> should call this method with a
	 * bit mask of hints from any of <code>RANDOMPIXELORDER</code>,
	 * <code>TOPDOWNLEFTRIGHT</code>, <code>COMPLETESCANLINES</code>,
	 * <code>SINGLEPASS</code>, <code>SINGLEFRAME</code>.
	 * 
	 * @param flags a bit mask of hints
	 */
	public void setHints(int flags) {
		//hints = flags; // FIXME - DO NOT KNOW WHAT TO DO WITH THE HINTS
	}

	/**
	 * This function delivers a rectangle of pixels where any
	 * pixel(m,n) is stored in the array as a <code>byte</code> at
	 * index (n * scansize + m + offset).  
	 */
	public void setPixels(int srcX, int srcY, int srcW, int srcH, ColorModel model, byte[] pixels, int srcOffset, int srcScansize) {

		if (srcY < dstY) {
			final int diff = dstY - srcY;
			if (diff >= srcH) {
				return;
			}
			srcOffset += srcScansize * diff;
			srcY += diff;
			srcH -= diff;
		}
		if (srcY + srcH > dstY + dstHeight) {
			srcH = (dstY + dstHeight) - srcY;
			if (srcH <= 0) {
				return;
			}
		}
		if (srcX < dstX) {
			final int diff = dstX - srcX;
			if (diff >= srcW) {
				return;
			}
			srcOffset += diff;
			srcX += diff;
			srcW -= diff;
		}
		if (srcX + srcW > dstX + dstWidth) {
			srcW = (dstX + dstWidth) - srcX;
			if (srcW <= 0) {
				return;
			}
		}

		if (intPixels == null) {
			intPixels = new int[dstWidth * dstHeight];
			dstScansize = dstWidth;
		}

		int dstPtr = dstOffset + (srcY - dstY) * dstScansize + (srcX - dstX);
		final int dstRem = dstScansize - srcW;
		final int srcRem = srcScansize - srcW;
		int lastARGB = -1;
		int lastPixel = -1;
		for (int h = srcH; h > 0; h--) {
			for (int w = srcW; w > 0; w--) {
				final int pixel = pixels[srcOffset++];
				if ((pixel != lastPixel) || (lastARGB == -1)) {
					lastARGB = model.getRGB(pixel);
					lastPixel = pixel;
				}
				intPixels[dstPtr++] = lastARGB;
			}
			srcOffset += srcRem;
			dstPtr += dstRem;
		}
	}

	/**
	 * This function delivers a rectangle of pixels where any
	 * pixel(m,n) is stored in the array as an <code>int</code> at
	 * index (n * scansize + m + offset).  
	 */
	public void setPixels(int srcX, int srcY, int srcW, int srcH, ColorModel model, int[] pixels, int srcOffset, int srcScansize) {

		if (srcY < dstY) {
			final int diff = dstY - srcY;
			if (diff >= srcH) {
				return;
			}
			srcOffset += srcScansize * diff;
			srcY += diff;
			srcH -= diff;
		}
		if (srcY + srcH > dstY + dstHeight) {
			srcH = (dstY + dstHeight) - srcY;
			if (srcH <= 0) {
				return;
			}
		}
		if (srcX < dstX) {
			final int diff = dstX - srcX;
			if (diff >= srcW) {
				return;
			}
			srcOffset += diff;
			srcX += diff;
			srcW -= diff;
		}
		if (srcX + srcW > dstX + dstWidth) {
			srcW = (dstX + dstWidth) - srcX;
			if (srcW <= 0) {
				return;
			}
		}

		if (intPixels == null) {
			intPixels = new int[dstWidth * dstHeight];
			dstScansize = dstWidth;
		}

		int dstPtr = dstOffset + (srcY - dstY) * dstScansize + (srcX - dstX);
		if (model == dstModel) {
			for (int row = 0; row < srcW; row++) {
				System.arraycopy(pixels, srcOffset, intPixels, dstPtr, srcW);
				dstPtr += dstScansize;
				srcOffset += srcScansize;
			}
		} else {
			final int dstRem = dstScansize - srcW;
			final int srcRem = srcScansize - srcW;
			int lastARGB = -1;
			int lastPixel = -1;
			for (int h = srcH; h > 0; h--) {
				for (int w = srcW; w > 0; w--) {
					final int pixel = pixels[srcOffset++];
					if ((pixel != lastPixel) || (lastARGB == -1)) {
						lastARGB = model.getRGB(pixel);
						lastPixel = pixel;
					}
					intPixels[dstPtr++] = lastARGB;
				}
				srcOffset += srcRem;
				dstPtr += dstRem;
			}
		}
	}

	/**
	 * The <code>ImageProducer</code> calls this method to indicate a
	 * single frame or the entire image is complete.  The method is
	 * also used to indicate an error in loading or producing the
	 * image.  
	 */
	public synchronized void imageComplete(int imageStatus) {
		grabbing = false;
		switch (imageStatus) {
			default :
			case IMAGEERROR :
				status |= ImageObserver.ERROR | ImageObserver.ABORT;
				break;
			case IMAGEABORTED :
				status |= ImageObserver.ABORT;
				break;
			case STATICIMAGEDONE :
				status |= ImageObserver.ALLBITS;
				break;
			case SINGLEFRAMEDONE :
				status |= ImageObserver.FRAMEBITS;
				break;
		}
		ip.removeConsumer(this);
		notifyAll();
	}

	/**
	   @deprecated by getStatus
	*/
	public synchronized int status() {
		return getStatus();
	}

}
