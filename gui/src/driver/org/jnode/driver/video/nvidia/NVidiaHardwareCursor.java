/*
 * $Id$
 */
package org.jnode.driver.video.nvidia;

import org.jnode.driver.video.CursorImage;
import org.jnode.driver.video.HardwareCursorAPI;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NVidiaHardwareCursor implements NVidiaConstants, HardwareCursorAPI {
	
	private final NVidiaVgaIO vgaIO;
	private final int architecture;
	
	public NVidiaHardwareCursor(NVidiaVgaIO vgaIO, int architecture) {
		this.vgaIO = vgaIO;
		this.architecture = architecture;
	}

	final void initCursor() {
		// cursor bitmap will be stored at the start of the framebuffer
		final int curadd = 0;

		/* set cursor bitmap adress ... */
		if (architecture <= NV04A) /* or laptop */ {
			/* must be used this way on pre-NV10 and on all 'Go' cards! */
			/* cursorbitmap must start on 2Kbyte boundary: */
			/* set adress bit11-16, and set 'no doublescan' (registerbit 1 = 0) */
			vgaIO.setCRT(NVCRTCX_CURCTL0, ((curadd & 0x0001f800) >> 9));
			/* set adress bit17-23, and set graphics mode cursor(?) (registerbit 7 = 1) */
			vgaIO.setCRT(NVCRTCX_CURCTL1, (((curadd & 0x00fe0000) >> 17) | 0x80));
			/* set adress bit24-31 */
			vgaIO.setCRT(NVCRTCX_CURCTL2, ((curadd & 0xff000000) >> 24));
		}
		else
		{
			/* upto 4Gb RAM adressing:
			 * can be used on NV10 and later (except for 'Go' cards)! */
			/* NOTE:
			 * This register does not exist on pre-NV10 and 'Go' cards. */

			/* cursorbitmap must still start on 2Kbyte boundary: */
			vgaIO.setReg32(NV32_NV10CURADD32, curadd & 0xfffff800);
		}

		/* set cursor colour: not needed because of direct nature of cursor bitmap. */

		/*clear cursor*/
		vgaIO.getVideoMem().setShort(curadd, (short)0x7fff, 1024);

		/* select 32x32 pixel, 16bit color cursorbitmap, no doublescan */
		vgaIO.setReg32(NV32_CURCONF, 0x02000100);

		/* de-activate hardware cursor for now */
		setCursorVisible(false);
	}
	
	public void setCursorVisible(boolean visible) {
		int temp = vgaIO.getCRT(NVCRTCX_CURCTL0);
		if (visible) {
			temp |= 0x01;
		} else {
			temp &= 0xfe;
		}
		vgaIO.setCRT(NVCRTCX_CURCTL0, temp);
	}
	
	public void setCursorPosition(int x, int y) {
		vgaIO.setReg32(NVDAC_CURPOS, ((x & 0x0fff) | ((y & 0x0fff) << 16)));
	}

	
	/**
	 * Sets the cursor image.
	 */
	public void setCursorImage(CursorImage cursor) {
		
		// TODO implement me
		
	}
}
