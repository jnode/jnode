/*
 * $Id$
 */
package org.jnode.driver.video.vmware;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.DriverException;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCI_IDs;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.util.AbstractSurface;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Address;

/**
 * @author epr
 */
public class VMWareCore extends AbstractSurface implements VMWareConstants, PCI_IDs {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private final VMWareDriver driver;
	private final int indexPort;
	private final int valuePort;
	private final IOResource ports;
	private final MemoryResource fifo;
	private final MemoryResource videoRam;
	private final int videoRamSize;
	private final int maxWidth;
	private final int maxHeight;
	private final int bitsPerPixel;
	private final int redMask;
	private final int greenMask;
	private final int blueMask;
	private final int redMaskShift;
	private final int greenMaskShift;
	private final int blueMaskShift;
	private final int capabilities;
	private int bytesPerLine;
	private int offset;
	private int displayWidth;
	private boolean fifoDirty = false;
	private BitmapGraphics bitmapGraphics;
	private ColorModel model;

	/**
	 * Create a new instance
	 * 
	 * @param driver
	 * @param device
	 */
	public VMWareCore(VMWareDriver driver, PCIDevice device) throws ResourceNotFreeException, DriverException {
		super(640, 480);
		this.driver = driver;
		final int basePort;
		if (device.getConfig().getDeviceID() == PCI_DEVICE_ID_VMWARE_SVGA) {
			basePort = SVGA_LEGACY_BASE_PORT;
			this.indexPort = SVGA_LEGACY_BASE_PORT + SVGA_INDEX_PORT * 4;
			this.valuePort = SVGA_LEGACY_BASE_PORT + SVGA_VALUE_PORT * 4;
		} else {
			basePort = device.getConfig().getBaseAddresses()[0].getIOBase();
			this.indexPort = basePort + SVGA_INDEX_PORT;
			this.valuePort = basePort + SVGA_VALUE_PORT;
		}

		log.debug("Found VMWare SVGA device using ports 0x" + NumberUtils.hex(indexPort) + " and 0x" + NumberUtils.hex(valuePort));

		try {
			final ResourceManager rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
			ports = rm.claimIOResource(device, basePort, SVGA_NUM_PORTS * 4);
			final int id = getVMWareID();
			if (id == SVGA_ID_0 || id == SVGA_ID_INVALID) {
				dumpState();
				throw new DriverException("No supported VMWare SVGA found, found id 0x" + NumberUtils.hex(id));
			} else {
				log.debug("VMWare SVGA ID: 0x" + NumberUtils.hex(id));
			}
			fifo = initFifo(device, rm);
			this.capabilities = getReg32(SVGA_REG_CAPABILITIES);
			this.videoRamSize = getReg32(SVGA_REG_FB_MAX_SIZE);
			final int videoRamBase = getReg32(SVGA_REG_FB_START);
			this.maxWidth = getReg32(SVGA_REG_MAX_WIDTH);
			this.maxHeight = getReg32(SVGA_REG_MAX_HEIGHT);
			final int bitsPerPixel = getReg32(SVGA_REG_BITS_PER_PIXEL);
			this.bytesPerLine = getReg32(SVGA_REG_BYTES_PER_LINE);
			this.videoRam = rm.claimMemoryResource(device, Address.valueOf(videoRamBase), videoRamSize, ResourceManager.MEMMODE_NORMAL);
			this.bitsPerPixel = bitsPerPixel;
			switch (bitsPerPixel) {
				case 8 :
					{
						bitmapGraphics = BitmapGraphics.create8bppInstance(videoRam, width, height, bytesPerLine, 0);
					}
					break;
				case 16 :
					{
						bitmapGraphics = BitmapGraphics.create16bppInstance(videoRam, width, height, bytesPerLine, 0);
					}
					break;
				case 24 :
					{
						bitmapGraphics = BitmapGraphics.create24bppInstance(videoRam, width, height, bytesPerLine, 0);
					}
					break;
				case 32 :
					{
						bitmapGraphics = BitmapGraphics.create32bppInstance(videoRam, width, height, bytesPerLine, 0);
					}
					break;
				default :
					throw new DriverException("Unknown bits/pixel value " + bitsPerPixel);
			}
			this.redMask = getReg32(SVGA_REG_RED_MASK);
			this.greenMask = getReg32(SVGA_REG_GREEN_MASK);
			this.blueMask = getReg32(SVGA_REG_BLUE_MASK);
			this.redMaskShift = getMaskShift(redMask);
			this.greenMaskShift = getMaskShift(greenMask);
			this.blueMaskShift = getMaskShift(blueMask);
		} catch (NameNotFoundException ex) {
			throw new ResourceNotFreeException(ex);
		}
	}

	/**
	 * Release all resources
	 */
	public final void release() {
		ports.release();
		fifo.release();
		videoRam.release();
	}

	/**
	 * Open a given configuration
	 * 
	 * @param config
	 */
	public void open(FrameBufferConfiguration config) {
		final int w = config.getScreenWidth();
		final int h = config.getScreenHeight();
		setMode(w, h, config.getColorModel());
		fillRect(0, 0, w, h, 0, PAINT_MODE);
		//dumpState(); // For debugging purposes
	}

	/**
	 * Close the SVGA screen
	 * 
	 * @see org.jnode.driver.video.Surface#close()
	 */
	public synchronized void close() {
		disableSVGA();
		driver.close(this);
		super.close();
	}

	/**
	 * Initialize the grahpics mode
	 * 
	 * @param width
	 * @param height
	 */
	public final void setMode(int width, int height, ColorModel model) {
		setReg32(SVGA_REG_WIDTH, width);
		setReg32(SVGA_REG_HEIGHT, height);
		setReg32(SVGA_REG_ENABLE, 1);
		this.model = model;
		this.offset = getReg32(SVGA_REG_FB_OFFSET);
		setReg32(SVGA_REG_GUEST_ID, GUEST_OS_OTHER);
		this.displayWidth = (getReg32(SVGA_REG_BYTES_PER_LINE) * 8) / ((bitsPerPixel + 7) & ~7);
		this.bytesPerLine = getReg32(SVGA_REG_BYTES_PER_LINE);
		setSize(width, height);
		this.width = width;
		this.height = height;
		switch (bitsPerPixel) {
			case 8 :
				{
					bitmapGraphics = BitmapGraphics.create8bppInstance(videoRam, width, height, bytesPerLine, 0);
				}
				break;
			case 16 :
				{
					bitmapGraphics = BitmapGraphics.create16bppInstance(videoRam, width, height, bytesPerLine, 0);
				}
				break;
			case 24 :
				{
					bitmapGraphics = BitmapGraphics.create24bppInstance(videoRam, width, height, bytesPerLine, 0);
				}
				break;
			case 32 :
				{
					bitmapGraphics = BitmapGraphics.create32bppInstance(videoRam, width, height, bytesPerLine, 0);
				}
				break;
		}
	}

	public FrameBufferConfiguration[] getConfigs() {
		final FrameBufferConfiguration[] r = new FrameBufferConfiguration[3];
		final ColorModel cm = new DirectColorModel(bitsPerPixel, redMask, greenMask, blueMask);
		r[0] = new VMWareConfiguration(640, 480, cm);
		r[1] = new VMWareConfiguration(800, 600, cm);
		r[2] = new VMWareConfiguration(1024, 768, cm);
		return r;
	}

	/**
	 * Disable the SVGA mode.
	 */
	public final void disableSVGA() {
		setReg32(SVGA_REG_ENABLE, 0);
	}

	/**
	 * Update the entire screen
	 */
	public final void updateScreen() {
		updateScreen(0, 0, width, height);
	}

	/**
	 * Update the given region of the screen
	 */
	public final void updateScreen(int x, int y, int width, int height) {
		writeWordToFIFO(SVGA_CMD_UPDATE);
		writeWordToFIFO(x);
		writeWordToFIFO(y);
		writeWordToFIFO(width);
		writeWordToFIFO(height);
	}

	/**
	 * Enable/Disable the cursor.
	 * 
	 * @param enable
	 */
	public final void setCursorEnabled(boolean enable) {
		setReg32(SVGA_REG_CURSOR_ON, (enable) ? 1 : 0);
	}

	/**
	 * Draw the given shape
	 * 
	 * @param shape
	 * @param color
	 * @param mode
	 */
	public final synchronized void draw(Shape shape, AffineTransform tx, Color color, int mode) {
		syncFIFO();
		super.draw(shape, tx, color, mode);
		final Rectangle r = getBounds(shape, tx);
		updateScreen(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
	}

	/**
	 * Draw an image to this surface
	 * 
	 * @param src
	 * @param srcX
	 * @param srcY
	 * @param x
	 *            The upper left x coordinate
	 * @param y
	 *            The upper left y coordinate
	 * @param w
	 * @param h
	 * @param bgColor
	 *            The background color to use for transparent pixels. If null, no transparent
	 *            pixels are unmodified on the destination
	 */
	public void drawCompatibleRaster(Raster src, int srcX, int srcY, int x, int y, int w, int h, Color bgColor) {
		if (bgColor != null) {
			bitmapGraphics.drawImage(src, srcX, srcY, x, y, w, h, convertColor(bgColor));
		} else {
			bitmapGraphics.drawImage(src, srcX, srcY, x, y, w, h);
		}
		updateScreen(x, y, w, h);
	}

	/**
	 * @see org.jnode.driver.video.Surface#fill(Shape, AffineTransform, Color, int)
	 */
	public final synchronized void fill(Shape shape, AffineTransform tx, Color color, int mode) {
		syncFIFO();
		super.fill(shape, tx, color, mode);
		final Rectangle b = getBounds(shape, tx);
		updateScreen(b.x, b.y, b.width, b.height);
	}

	/**
	 * Fill a given rectangle with a given color
	 */
	public final synchronized void fillRectangle(int x1, int y1, int x2, int y2, Color color, int mode) {
		fillRect(x1, y1, x2 - x1, y2 - y1, convertColor(color), mode);
	}

	/**
	 * Fill a given rectangle with a given color
	 * 
	 * @param x1
	 * @param y1
	 * @param width
	 * @param height
	 * @param color
	 * @param mode
	 */
	protected final void fillRect(int x, int y, int width, int height, int color, int mode) {
		if (x < 0) {
			width = Math.max(0, x + width);
			x = 0;
		}
		if (y < 0) {
			height = Math.max(0, y + height);
			y = 0;
		}
		if ((width > 0) && (height > 0)) {
			if (mode == Surface.XOR_MODE) {
				writeWordToFIFO(SVGA_CMD_RECT_ROP_FILL);
				writeWordToFIFO(color);
				writeWordToFIFO(x);
				writeWordToFIFO(y);
				writeWordToFIFO(width);
				writeWordToFIFO(height);
				writeWordToFIFO(SVGA_ROP_XOR);
			} else {
				writeWordToFIFO(SVGA_CMD_RECT_FILL);
				writeWordToFIFO(color);
				writeWordToFIFO(x);
				writeWordToFIFO(y);
				writeWordToFIFO(width);
				writeWordToFIFO(height);
			}
		}
	}

	/**
	 * Dump the state of the SVGA to log.
	 */
	public final void dumpState() {
		log.debug("Max. Resolution " + maxWidth + "*" + maxHeight);
		log.debug("Cur. Resolution " + width + "*" + height);
		log.debug("Bits/Pixel      " + bitsPerPixel);
		log.debug("Bytes/Line      " + bytesPerLine);
		log.debug("Offset          " + offset);
		log.debug("Display width   " + displayWidth);
		log.debug("Red mask        0x" + NumberUtils.hex(getReg32(SVGA_REG_RED_MASK)));
		log.debug("Green mask      0x" + NumberUtils.hex(getReg32(SVGA_REG_GREEN_MASK)));
		log.debug("Blue mask       0x" + NumberUtils.hex(getReg32(SVGA_REG_BLUE_MASK)));
		log.debug("Capabilities    0x" + NumberUtils.hex(capabilities));
	}

	/**
	 * Set the pixel at the given location to the given color.
	 * 
	 * @param x
	 * @param y
	 * @param color
	 */
	protected final void drawPixel(int x, int y, int color, int mode) {
		bitmapGraphics.drawPixels(x, y, 1, color, mode);
		/*
		 * if ((x >= 0) && (x < width) && (y > = 0) && (y < height)) { y; final int ofs; if (mode ==
		 * Surface.XOR_MODE) { switch (bitsPerPixel) { case 8 : ofs = ofsY + x;
		 * videoRam.xorByte(ofs, (byte) color, 1); break; case 16 : ofs = ofsY + (x << 1);
		 * videoRam.xorShort(ofs, (short) color, 1); break; case 24 : 3); videoRam.xorShort(ofs,
		 * (short) (color & 0xFFFF), 1); videoRam.xorByte(ofs + 2, (byte) ((color >> 16) & 0xFF),
		 * 1); break; case 32 : ofs = ofsY + (x << 2); videoRam.xorInt(ofs, color, 1); break;
		 * default : throw new RuntimeException("Unknown bitsPerPixel"); } } else { switch
		 * (bitsPerPixel) { case 8 : ofs = ofsY + x; videoRam.setByte(ofs, (byte) color); break;
		 * case 16 : ofs = ofsY + (x << 1); videoRam.setShort(ofs, (short) color); break; case 24 :
		 * 3); videoRam.setShort(ofs, (short) (color & 0xFFFF)); videoRam.setByte(ofs + 2, (byte)
		 * ((color >> 16) & 0xFF)); break; case 32 : ofs = ofsY + (x << 2); videoRam.setInt(ofs,
		 * color); break; default : throw new RuntimeException("Unknown bitsPerPixel"); } }
		 */
	}

	/**
	 * Low level draw line method. This method does not call updateScreen.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param c
	 * @param mode
	 */
	protected final void drawLine(int x1, int y1, int x2, int y2, int c, int mode) {
		if (x1 == x2) {
			// Vertical line
			fillRect(x1, Math.min(y1, y2), 1, Math.abs(y2 - y1), c, mode);
		} else if (y1 == y2) {
			// Horizontal line
			//drawHorizontalLine(Math.min(x1, x2), y1, Math.abs(x2 - x1)+1, c, mode);
			fillRect(Math.min(x1, x2), y1, Math.abs(x2 - x1) + 1, 1, c, mode);
		} else {
			super.drawLine(x1, y1, x2, y2, c, mode);
		}
	}

	protected final void drawHorizontalLine(int x, int y, int w, int color, int mode) {
		if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) {
			w = Math.min(width - x, w);
			final int ofsY = bytesPerLine * y;
			int ofs;
			if (mode == Surface.XOR_MODE) {
				switch (bitsPerPixel) {
					case 8 :
						ofs = ofsY + x;
						videoRam.xorByte(ofs, (byte) color, w);
						break;
					case 16 :
						ofs = ofsY + (x << 1);
						videoRam.xorShort(ofs, (short) color, w);
						break;
					case 24 :
						ofs = ofsY + (x * 3);
						while (w > 0) {
							videoRam.xorShort(ofs, (short) (color & 0xFFFF), 1);
							videoRam.xorByte(ofs + 2, (byte) ((color >> 16) & 0xFF), 1);
							w--;
							ofs += 3;
						}
						break;
					case 32 :
						ofs = ofsY + (x << 2);
						videoRam.xorInt(ofs, color, w);
						break;
					default :
						throw new RuntimeException("Unknown bitsPerPixel");
				}
			} else {
				switch (bitsPerPixel) {
					case 8 :
						ofs = ofsY + x;
						videoRam.setByte(ofs, (byte) color, w);
						break;
					case 16 :
						ofs = ofsY + (x << 1);
						videoRam.setShort(ofs, (short) color, w);
						break;
					case 24 :
						ofs = ofsY + (x * 3);
						while (w > 0) {
							videoRam.setShort(ofs, (short) (color & 0xFFFF));
							videoRam.setByte(ofs + 2, (byte) ((color >> 16) & 0xFF));
							w--;
							ofs += 3;
						}
						break;
					case 32 :
						ofs = ofsY + (x << 2);
						videoRam.setInt(ofs, color, w);
						break;
					default :
						throw new RuntimeException("Unknown bitsPerPixel");
				}
			}
		}
	}

	/**
	 * Gets the contents of a 32-bit register
	 * 
	 * @param nIndex
	 * @return
	 */
	private final int getReg32(int index) {
		ports.outPortDword(indexPort, index);
		return ports.inPortDword(valuePort);
	}

	/**
	 * Sets the contents of a 32-bit register
	 * 
	 * @param nIndex
	 * @param nValue
	 */
	private final void setReg32(int index, int value) {
		ports.outPortDword(indexPort, index);
		ports.outPortDword(valuePort, value);
	}

	/**
	 * Write the given word to the FIFO.
	 * 
	 * @param nValue
	 */
	private final void writeWordToFIFO(int value) {
		fifoDirty = true;
		//Debug.out.println ("VMWare::WriteWordToFIFO(" + Integer.toHexString(nValue) + ") pos: "
		// + ReadFIFO (SVGA_FIFO_NEXT_CMD));
		/* Need to sync? */
		if ((getFIFO(SVGA_FIFO_NEXT_CMD) + 4 == getFIFO(SVGA_FIFO_STOP))
			|| (getFIFO(SVGA_FIFO_NEXT_CMD) == getFIFO(SVGA_FIFO_MAX) - 4 && getFIFO(SVGA_FIFO_STOP) == getFIFO(SVGA_FIFO_MIN))) {
			log.debug("VMWare::WriteWordToFIFO() syncing FIFO");
			setReg32(SVGA_REG_SYNC, 1);
			while (getReg32(SVGA_REG_BUSY) != 0) {
				Thread.yield();
			}
		}
		setFIFO(getFIFO(SVGA_FIFO_NEXT_CMD) / 4, value);
		setFIFO(SVGA_FIFO_NEXT_CMD, getFIFO(SVGA_FIFO_NEXT_CMD) + 4);
		if (getFIFO(SVGA_FIFO_NEXT_CMD) == getFIFO(SVGA_FIFO_MAX)) {
			setFIFO(SVGA_FIFO_NEXT_CMD, getFIFO(SVGA_FIFO_MIN));
		}
	}

	/**
	 * Wait until the SVGA is ready
	 */
	private final void syncFIFO() {
		if (fifoDirty) {
			setReg32(SVGA_REG_SYNC, 1);
			while (getReg32(SVGA_REG_BUSY) != 0) {
				Thread.yield();
			}
			fifoDirty = false;
		}
	}

	/**
	 * Gets a FIFO entry
	 * 
	 * @param index
	 * @return
	 */
	private final int getFIFO(int index) {
		return fifo.getInt(index * 4);
	}

	/**
	 * Sets a FIFO entry
	 * 
	 * @param index
	 * @param value
	 */
	private final void setFIFO(int index, int value) {
		fifo.setInt(index * 4, value);
	}

	/**
	 * Convert the given color to a value suitable for VMWare
	 * 
	 * @param color
	 */
	protected final int convertColor(Color color) {
		final int r = color.getRed();
		final int g = color.getGreen();
		final int b = color.getBlue();
		return ((r << redMaskShift) & redMask) | ((g << greenMaskShift) & greenMask) | ((b << blueMaskShift) & blueMask);
	}

	/**
	 * Gets the SVGA_ID of the VMware SVGA adapter. This function should hide any backward
	 * compatibility mess.
	 */
	private final int getVMWareID() {
		int vmware_svga_id;

		/*******************************************************************************************
		 * Any version with any SVGA_ID_ to SVGA_ID_0 to support versions of this driver with
		 * SVGA_ID_0.
		 * 
		 * Versions of SVGA_ID_0 ignore writes to the SVGA_REG_ID register.
		 * 
		 * Versions of SVGA_ID_1 will allow us to overwrite the content of the SVGA_REG_ID register
		 * only with the values SVGA_ID_0 or SVGA_ID_1.
		 * 
		 * Versions of SVGA_ID_2 will allow us to overwrite the content of the SVGA_REG_ID register
		 * only with the values SVGA_ID_0 or SVGA_ID_1 or SVGA_ID_2.
		 */

		setReg32(SVGA_REG_ID, SVGA_ID_2);
		vmware_svga_id = getReg32(SVGA_REG_ID);

		if (vmware_svga_id == SVGA_ID_2) {
			return SVGA_ID_2;
		}

		setReg32(SVGA_REG_ID, SVGA_ID_1);
		vmware_svga_id = getReg32(SVGA_REG_ID);
		if (vmware_svga_id == SVGA_ID_1) {
			return SVGA_ID_1;
		}

		if (vmware_svga_id == SVGA_ID_0) {
			return SVGA_ID_0;
		}

		/* No supported VMware SVGA devices found */
		return SVGA_ID_INVALID;
	}

	/**
	 * Claim and initialize the FIFO.
	 * 
	 * @param owner
	 * @param rm
	 * @return
	 */
	private final MemoryResource initFifo(ResourceOwner owner, ResourceManager rm) throws ResourceNotFreeException {
		final int physBase = getReg32(SVGA_REG_MEM_START);
		final int size = getReg32(SVGA_REG_MEM_SIZE);
		final Address address = Address.valueOf(physBase);

		log.debug("Found FIFO at 0x" + NumberUtils.hex(physBase) + ", size 0x" + NumberUtils.hex(size));

		final MemoryResource res = rm.claimMemoryResource(owner, address, size, ResourceManager.MEMMODE_NORMAL);
		res.setInt(SVGA_FIFO_MIN * 4, 16);
		res.setInt(SVGA_FIFO_MAX * 4, size);
		res.setInt(SVGA_FIFO_NEXT_CMD * 4, 16);
		res.setInt(SVGA_FIFO_STOP * 4, 16);
		setReg32(SVGA_REG_CONFIG_DONE, 1);

		return res;
	}

	/**
	 * Gets the size of the video ram in bytes.
	 */
	final int getVideoRamSize() {
		return this.videoRamSize;
	}

	/**
	 * Gets the maximum screen height in pixels
	 */
	final int getMaxHeight() {
		return this.maxHeight;
	}

	/**
	 * Gets the maximum screen width in pixels
	 */
	final int getMaxWidth() {
		return this.maxWidth;
	}

	/**
	 * Gets the number of bits per pixel
	 */
	final int getBitsPerPixel() {
		return this.bitsPerPixel;
	}

	/**
	 * Gets the number of bytes per line
	 */
	final int getBytesPerLine() {
		return this.bytesPerLine;
	}

	/**
	 * Gets the number of shift needed for the given mask.
	 * 
	 * E.g. getMaskShift(0xFF00) == 8
	 * 
	 * @param mask
	 * @return
	 */
	private final int getMaskShift(int mask) {
		int count = 0;
		while ((mask & 1) == 0) {
			count++;
			mask = mask >> 1;
		}
		return count;
	}

	/**
	 * @see org.jnode.driver.video.Surface#getColorModel()
	 */
	public ColorModel getColorModel() {
		return model;
	}

}
