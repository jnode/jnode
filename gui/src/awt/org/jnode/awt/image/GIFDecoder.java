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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * @author epr
 */
public class GIFDecoder implements ImageProducer {

	private final LinkedList consumers = new LinkedList();

	private byte bytePixels[]; // image data in index model
	private int intPixels[]; // image data in RGB model
	private int width; // image size
	private int height;
	ColorModel colorModel;
	private int imageStatus;

	//private String fullInfo; // Format: field in info box
	//private String shortInfo; // short format info
	private String comment; // comment text

	private InputStream input;
	private boolean closeWhenFinished;

	/**
	 * Creates an instance of a GIF decoder for further reading from <code>input</code>. Image
	 * reading from the stream starts only at <code>startProduction ()</code> or <code>addConsumer ()</code>
	 * call. By default, <code>input</code> isn't closed once image reading is done.
	 * 
	 * @param input
	 */
	public GIFDecoder(InputStream input) {
		this(input, false);
	}

	/**
	 * Creates an instance of a GIF decoder for further reading from <code>input</code>. Image
	 * reading from the stream starts only at <code>startProduction ()</code> or <code>addConsumer ()</code>
	 * call.
	 * 
	 * @param input
	 *            an input stream
	 * @param closeWhenFinished
	 *            if <code>true</code> then <code>input</code> will be closed once image
	 *            reading will be done.
	 * @since PJA2.4
	 */
	public GIFDecoder(InputStream input, boolean closeWhenFinished) {
		this.input = input;
		this.closeWhenFinished = closeWhenFinished;
	}

	/**
	 * <code>ImageProducer</code> implementation.
	 * 
	 * @param ic
	 */
	public void startProduction(ImageConsumer ic) {
		addConsumer(ic);
	}

	/**
	 * <code>ImageProducer</code> implementation.
	 * 
	 * @param ic
	 */
	public void addConsumer(ImageConsumer ic) {
		if (ic != null && isConsumer(ic))
			return;

		synchronized (this) {
			if (imageStatus == 0)
				try {
					loadGIF(input);
					imageStatus = ImageConsumer.STATICIMAGEDONE;
				} catch (IOException e) {
					imageStatus = ImageConsumer.IMAGEERROR;
				}
		}

		if (imageStatus != ImageConsumer.IMAGEERROR) {
			ic.setDimensions(width, height);
			ic.setHints(ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME | ImageConsumer.TOPDOWNLEFTRIGHT);
			if (colorModel != null) {
				ic.setColorModel(colorModel);
				ic.setPixels(0, 0, width, height, colorModel, bytePixels, 0, width);
			} else
				// If colorModel can't be instantiated send image in default RGB format
				ic.setPixels(0, 0, width, height, null, intPixels, 0, width);
			ic.imageComplete(imageStatus);
		} else
			ic.imageComplete(imageStatus);
	}

	/**
	 * <code>ImageProducer</code> implementation.
	 * 
	 * @param ic
	 * @return True or false
	 */
	public boolean isConsumer(ImageConsumer ic) {
		return consumers.contains(ic);
	}

	/**
	 * <code>ImageProducer</code> implementation.
	 * 
	 * @param ic
	 */
	public void removeConsumer(ImageConsumer ic) {
		consumers.remove(ic);
	}

	/**
	 * <code>ImageProducer</code> implementation.
	 * 
	 * @param ic
	 */
	public void requestTopDownLeftRightResend(ImageConsumer ic) {
		// Useless, already sent in that order
	}

	// xvgif.c translation starts here

	// Transformed most of global variables to local variables
	// These variables are used only once when image is loaded
	// (loadGIF () is called in a synchronized block)
	private int bitOffset = 0; // Bit Offset of next code
	private int XC = 0; // Output X and Y coords of current pixel
	private int YC = 0;
	private int pass = 0; // Used by output routine if interlaced pixels
	private int ptr = 0;
	private int oldYC = -1;

	private byte r[] = new byte[256];
	private byte g[] = new byte[256];
	private byte b[] = new byte[256]; // colormap, if PIC8
	private int transparentIndex = -1;

	private final static String id87 = "GIF87a";
	private final static String id89 = "GIF89a";

	private final static short EGA_PALETTE[][] = { { 0, 0, 0 }, {
			0, 0, 128 }, {
			0, 128, 0 }, {
			0, 128, 128 }, {
			128, 0, 0 }, {
			128, 0, 128 }, {
			128, 128, 0 }, {
			200, 200, 200 }, {
			100, 100, 100 }, {
			100, 100, 255 }, {
			100, 255, 100 }, {
			100, 255, 255 }, {
			255, 100, 100 }, {
			255, 100, 255 }, {
			255, 255, 100 }, {
			255, 255, 255 }
	};

	private final static byte EXTENSION = 0x21;
	private final static byte IMAGESEP = 0x2c;
	private final static byte TRAILER = 0x3b;
	private final static byte INTERLACEMASK = 0x40;
	private final static byte COLORMAPMASK = (byte) 0x80;

	private void loadGIF(InputStream input) throws IOException {
		try {
			if (!(input instanceof BufferedInputStream))
				input = new BufferedInputStream(input);

			// Use a DataInputStream to have EOFException if file is corrupted
			DataInputStream dataInput = new DataInputStream(input);

			// initialize variables
			bitOffset = XC = YC = pass = 0;
			boolean gotimage = false;
			boolean gif89 = false;

			byte[] idBytes = new byte[6];
			for (int i = 0; i < 6; i++)
				idBytes[i] = dataInput.readByte();

			final String id = new String(idBytes);
			if (id.equals(id87)) {
				gif89 = false;
			} else if (id.equals(id89)) {
				gif89 = true;
			} else {
				gifWarning(input, "not a GIF file");
			}

			// Get variables from the GIF screen descriptor
			dataInput.skipBytes(4);
			//byte aByte = dataInput.readByte();
			//int screenWidth = (aByte & 0xFF) + 0x100 * (dataInput.readByte() & 0xFF); // screen
			// dimensions... not used.
			//aByte = dataInput.readByte();
			//int screenHeight = (aByte & 0xFF) + 0x100 * (dataInput.readByte() & 0xFF);

			byte aByte = dataInput.readByte();
			boolean hasColormap = (aByte & COLORMAPMASK) != 0;

			// Bits per pixel, read from GIF header
			int bitsPerPixel = (aByte & 7) + 1;
			// Number of colors
			int colorMapSize = 1 << bitsPerPixel;
			// AND mask for data size
			int bitMask = colorMapSize - 1;

			dataInput.skipBytes(1);
			//int background = dataInput.readByte() & 0xFF; // background color... not used.

			int aspect = dataInput.readByte() & 0xFF;
			if (aspect != 0)
				if (!gif89)
					gifWarning(input, "corrupt GIF file (screen descriptor)");

			// Read in global colormap.
			if (hasColormap)
				for (int i = 0; i < colorMapSize; i++) {
					r[i] = dataInput.readByte();
					g[i] = dataInput.readByte();
					b[i] = dataInput.readByte();
				} else
				// no colormap in GIF file
				// put std EGA palette (repeated 16 times) into colormap, for lack of
				// anything better to do
				for (int i = 0; i < 256; i++) {
					r[i] = (byte) EGA_PALETTE[i & 15][0];
					g[i] = (byte) EGA_PALETTE[i & 15][1];
					b[i] = (byte) EGA_PALETTE[i & 15][2];
				}

			for (int block = 0;(block = dataInput.readByte() & 0xFF) != TRAILER;)
				if (block == EXTENSION) {
					// possible things at this point are:
					//   an application extension block
					//   a comment extension block
					//   an (optional) graphic control extension block
					//       followed by either an image
					//     or a plaintext extension

					// parse extension blocks
					int fn, blocksize, aspnum, aspden;

					// read extension block
					fn = dataInput.readByte() & 0xFF;

					if (fn == 'R') {
						// GIF87 aspect extension
						int blockSize;

						blocksize = dataInput.readByte() & 0xFF;
						if (blocksize == 2) {
							aspnum = dataInput.readByte();
							aspden = dataInput.readByte();
							if (aspden <= 0 || aspnum <= 0) {
								aspnum = aspden = 1;
							}
						} else
							dataInput.skipBytes(blocksize);

						while ((blockSize = dataInput.readByte() & 0xFF) > 0)
							// eat any following data subblocks
							dataInput.skipBytes(blockSize);
					} else if (fn == 0xFE) {
						// Comment Extension
						for (int blockSize = 0;(blockSize = dataInput.readByte() & 0xFF) != 0;) {
							byte commentBytes[] = new byte[blockSize];
							for (int i = 0; i < blockSize; i++)
								commentBytes[i] = dataInput.readByte();

							if (comment != null) {
								comment += "\n" + new String(commentBytes);
							} else {
								comment = new String(commentBytes);
							}
						}
					} else if (fn == 0x01) {
						// PlainText Extension
						int blockSize = dataInput.readByte() & 0xFF;
						int tgLeft = dataInput.readByte() & 0xFF;
						tgLeft += (dataInput.readByte() & 0xFF) << 8;
						int tgTop = dataInput.readByte() & 0xFF;
						tgTop += (dataInput.readByte() & 0xFF) << 8;
						int tgWidth = dataInput.readByte() & 0xFF;
						tgWidth += (dataInput.readByte() & 0xFF) << 8;
						int tgHeight = dataInput.readByte() & 0xFF;
						tgHeight += (dataInput.readByte() & 0xFF) << 8;
						dataInput.skipBytes(4);
						/*
						 * int cWidth = dataInput.readByte() & 0xFF; int cHeight =
						 * dataInput.readByte() & 0xFF; int fg = dataInput.readByte() & 0xFF;
						 */

						dataInput.skipBytes(blockSize - 12); // read rest of first subblock

						// read (and ignore) data sub-blocks
						while ((blockSize = dataInput.readByte() & 0xFF) != 0)
							dataInput.skipBytes(blockSize);
					} else if (fn == 0xF9) {
						// Graphic Control Extension
						for (int blockSize = 0;(blockSize = dataInput.readByte() & 0xFF) != 0;)
							// Added transparent GIF management here
							if (blockSize == 4) {
								int ext1 = (dataInput.readByte() & 0xFF);
								dataInput.skipBytes(2);
								int ext4 = (dataInput.readByte() & 0xFF);

								// v2.1.1 Changed condition for transparent GIFs
								if ((ext1 & 0x01) != 0)
									transparentIndex = ext4;
							} else
								dataInput.skipBytes(blockSize);
					} else if (fn == 0xFF) {
						// Application Extension
						// read (and ignore) data sub-blocks
						for (int blockSize = 0;(blockSize = dataInput.readByte() & 0xFF) != 0;)
							dataInput.skipBytes(blockSize);
					} else {
						// unknown extension
						// read (and ignore) data sub-blocks
						for (int blockSize = 0;(blockSize = dataInput.readByte() & 0xFF) != 0;)
							dataInput.skipBytes(blockSize);
					}
				} else if (block == IMAGESEP) {
					if (gotimage) {
						// just skip over remaining images
						dataInput.skipBytes(8); // left position
						// top position
						// width
						// height
						int misc = dataInput.readByte() & 0xFF; // misc. bits

						if ((misc & 0x80) != 0)
							// image has local colormap. skip it
							for (int i = 0; i < 1 << ((misc & 7) + 1); i++)
								dataInput.skipBytes(3);

						dataInput.skipBytes(1); // minimum code size

						// skip image data sub-blocks
						for (int blockSize = 0;(blockSize = dataInput.readByte() & 0xFF) != 0;)
							dataInput.skipBytes(blockSize);
					} else {
						readImage(dataInput, bitsPerPixel, bitMask, hasColormap, gif89);
						gotimage = true;
					}
				} else {
					// unknown block type
					// don't mention bad block if file was trunc'd, as it's all bogus
					String str = "Unknown block type (0x" + Integer.toString(block, 16) + ")";
					gifWarning(input, str);
					break;
				}

			if (!gotimage)
				gifWarning(input, "no image data found in GIF file");
		} finally {
			// v2.4 image InputStream close when finished
			if (closeWhenFinished)
				try {
					input.close();
					input = null;
				} catch (IOException e) {
					throw e;
				}

		}
	}

	private void readImage(DataInputStream dataInput, int bitsPerPixel, int bitMask, boolean hasColormap, boolean gif89) throws IOException {
		int npixels = 0;
		int maxpixels = 0;

		// read in values from the image descriptor
		byte aByte;
		dataInput.skipBytes(4);
		//aByte = dataInput.readByte();
		//int leftOffset = (aByte & 0xFF) + 0x100 * (dataInput.readByte() & 0xFF);
		//aByte = dataInput.readByte();
		//int topOffset = (aByte & 0xFF) + 0x100 * (dataInput.readByte() & 0xFF);
		aByte = dataInput.readByte();
		width = (aByte & 0xFF) + 0x100 * (dataInput.readByte() & 0xFF);
		aByte = dataInput.readByte();
		height = (aByte & 0xFF) + 0x100 * (dataInput.readByte() & 0xFF);

		int misc = dataInput.readByte(); // miscellaneous bits (interlace, local cmap)
		boolean interlace = (misc & INTERLACEMASK) != 0;

		if ((misc & 0x80) != 0)
			for (int i = 0; i < 1 << ((misc & 7) + 1); i++) {
				r[i] = dataInput.readByte();
				g[i] = dataInput.readByte();
				b[i] = dataInput.readByte();
			}

		if (!hasColormap && (misc & 0x80) == 0) {
			// no global or local colormap
		}

		// Start reading the raster data. First we get the intial code size
		// and compute decompressor constant values, based on this code size.

		// Code size, read from GIF header
		int codeSize = dataInput.readByte() & 0xFF;

		int clearCode = (1 << codeSize); // GIF clear code
		int EOFCode = clearCode + 1; // GIF end-of-information code
		int firstFree = clearCode + 2; // First free code, generated per GIF spec
		int freeCode = firstFree; // Decompressor,next free slot in hash table

		// The GIF spec has it that the code size is the code size used to
		// compute the above values is the code size given in the file, but the
		// code size used in compression/decompression is the code size given in
		// the file plus one. (thus the ++).
		codeSize++;
		int initCodeSize = codeSize; // Starting code size, used during Clear
		int maxCode = (1 << codeSize); // limiting value for current code size
		int readMask = maxCode - 1; // Code AND mask for current code size

		// UNBLOCK:
		// Read the raster data. Here we just transpose it from the GIF array
		// to the raster array, turning it from a series of blocks into one long
		// data stream, which makes life much easier for readCode ().
		byte[] raster = null;
		for (int blockSize = 0;(blockSize = dataInput.readByte() & 0xFF) != 0;) {
			int start = 0;
			if (raster == null)
				raster = new byte[blockSize];
			else {
				byte oldData[] = raster;
				raster = new byte[oldData.length + blockSize];
				System.arraycopy(oldData, 0, raster, 0, oldData.length);
				start = oldData.length;
			}

			while (blockSize-- > 0)
				raster[start++] = dataInput.readByte();
		}

		// Allocate the 'pixels'
		maxpixels = width * height;
		bytePixels = new byte[maxpixels];
		int picptr = 0;

		// The hash table used by the decompressor
		int prefix[] = new int[4096];
		int suffix[] = new int[4096];
		// An output array used by the decompressor
		int outCode[] = new int[4097];
		int outCount = 0; // Decompressor output 'stack count'

		int currentCode; // Decompressor variables
		int oldCode = 0;
		int inCode;
		int finChar = 0;
		// Decompress the file, continuing until you see the GIF EOF code.
		// One obvious enhancement is to add checking for corrupt files here.
		int code = readCode(dataInput, raster, codeSize, readMask);
		while (code != EOFCode) {
			// Clear code sets everything back to its initial value, then reads the
			// immediately subsequent code as uncompressed data.
			if (code == clearCode) {
				codeSize = initCodeSize;
				maxCode = (1 << codeSize);
				readMask = maxCode - 1;
				freeCode = firstFree;
				code = readCode(dataInput, raster, codeSize, readMask);
				currentCode = oldCode = code;
				finChar = currentCode & bitMask;
				if (!interlace)
					bytePixels[picptr++] = (byte) finChar;
				else
					doInterlace(finChar);
				npixels++;
			} else {
				// If not a clear code, must be data: save same as currentCode and inCode

				// if we're at maxcode and didn't get a clear, stop loading
				if (freeCode >= 4096)
					break;

				currentCode = inCode = code;

				// If greater or equal to freeCode, not in the hash table yet;
				// repeat the last character decoded
				if (currentCode >= freeCode) {
					currentCode = oldCode;
					if (outCount > 4096)
						break;
					outCode[outCount++] = finChar;
				}

				// Unless this code is raw data, pursue the chain pointed to by currentCode
				// through the hash table to its end; each code in the chain puts its
				// associated output code on the output queue.
				while (currentCode > bitMask) {
					if (outCount > 4096)
						break; // corrupt file
					outCode[outCount++] = suffix[currentCode];
					currentCode = prefix[currentCode];
				}

				if (outCount > 4096)
					break;

				// The last code in the chain is treated as raw data.
				finChar = currentCode & bitMask;
				outCode[outCount++] = finChar;

				// Now we put the data out to the Output routine.
				// It's been stacked LIFO, so deal with it that way...

				// safety thing: prevent exceeding range of 'bytePixels'
				if (npixels + outCount > maxpixels)
					outCount = maxpixels - npixels;

				npixels += outCount;
				if (!interlace)
					for (int i = outCount - 1; i >= 0; i--)
						bytePixels[picptr++] = (byte) outCode[i];
				else
					for (int i = outCount - 1; i >= 0; i--)
						doInterlace(outCode[i]);
				outCount = 0;

				// Build the hash table on-the-fly. No table is stored in the file.
				prefix[freeCode] = oldCode;
				suffix[freeCode] = finChar;
				oldCode = inCode;

				// Point to the next slot in the table. If we exceed the current
				// maxCode value, increment the code size unless it's already 12. If it
				// is, do nothing: the next code decompressed better be CLEAR

				freeCode++;
				if (freeCode >= maxCode) {
					if (codeSize < 12) {
						codeSize++;
						maxCode *= 2;
						readMask = (1 << codeSize) - 1;
					}
				}
			}

			code = readCode(dataInput, raster, codeSize, readMask);
			if (npixels >= maxpixels)
				break;
		}

		if (npixels != maxpixels) {
			if (!interlace) // clear.EOBuffer
				for (int i = 0; i < maxpixels - npixels; i++)
					bytePixels[npixels + i] = 0;
		}

		// fill in the GifImage structure
		colorModel = new IndexColorModel(8, 256, r, g, b, transparentIndex);

		//fullInfo = "GIF" + ((gif89) ? "89" : "87") + ", " + bitsPerPixel + " bit" +
		// ((bitsPerPixel == 1) ? "" : "s") + "per pixel, " + (interlace ? "" : "non-") +
		// "interlaced.";

		//shortInfo = width + "x" + height + " GIF" + ((gif89) ? "89" : "87");

		// comment gets handled in main LoadGIF() block-reader
	}

	/**
	 * Fetch the next code from the raster data stream. The codes can be any length from 3 to 12
	 * bits, packed into 8-bit bytes, so we have to maintain our location in the raster array as a
	 * BIT Offset. We compute the byte Offset into the raster array by dividing this by 8, pick up
	 * three bytes, compute the bit Offset into our 24-bit chunk, shift to bring the desired code
	 * to the bottom, then mask it off and return it.
	 * 
	 * @param input
	 * @param raster
	 * @param codeSize
	 * @param readMask
	 * @return int
	 * @throws IOException
	 */
	private int readCode(DataInputStream input, byte raster[], int codeSize, int readMask) throws IOException {
		int byteOffset = bitOffset / 8;
		int inWordOffset = bitOffset % 8;
		// v2.2
		// Alan Dix modification to fix raster over-run errors
		// int rawCode = (raster [byteOffset] & 0xFF)
		//              + ((raster [byteOffset + 1] & 0xFF) << 8);
		int rawCode = (raster[byteOffset] & 0xFF);
		if (byteOffset + 1 < raster.length)
			rawCode += ((raster[byteOffset + 1] & 0xFF) << 8);
		else if (codeSize + inWordOffset > 8)
			gifWarning(input, "short raster ?  raster.length = " + raster.length + ", codeSize = " + codeSize + ", readMask = " + readMask);
		// end of modification

		if (codeSize >= 8 && byteOffset + 2 < raster.length)
			rawCode += (raster[byteOffset + 2] & 0xFF) << 16;
		rawCode >>= (bitOffset % 8);
		bitOffset += codeSize;
		return rawCode & readMask;
	}

	private void doInterlace(int index) {
		if (oldYC != YC) {
			ptr = YC * width;
			oldYC = YC;
		}

		if (YC < height)
			bytePixels[ptr++] = (byte) index;

		// Update the X-coordinate, and if it overflows, update the Y-coordinate
		if (++XC == width) {
			// deal with the interlace as described in the GIF
			// spec. Put the decoded scan line out to the screen if we haven't gone
			// past the bottom of it
			XC = 0;

			switch (pass) {
				case 0 :
					YC += 8;
					if (YC >= height) {
						pass++;
						YC = 4;
					}
					break;

				case 1 :
					YC += 8;
					if (YC >= height) {
						pass++;
						YC = 2;
					}
					break;

				case 2 :
					YC += 4;
					if (YC >= height) {
						pass++;
						YC = 1;
					}
					break;

				case 3 :
					YC += 2;
					break;

				default :
					break;
			}
		}
	}

	private void gifWarning(InputStream input, String st) throws IOException {
		throw new IOException("Warning ! " + input + " : " + st);
	}
}
