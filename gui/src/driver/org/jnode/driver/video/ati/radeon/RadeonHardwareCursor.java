/*
 * $Id$
 */
package org.jnode.driver.video.ati.radeon;

import java.util.HashMap;

import org.jnode.driver.video.HardwareCursor;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.HardwareCursorImage;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RadeonHardwareCursor implements RadeonConstants, HardwareCursorAPI {

    private final RadeonVgaIO io;
	/** Map between HardwareCursorImage and short[] */
	private final HashMap cursorCache = new HashMap();

    // cursor bitmap will be stored at the start of the framebuffer
	private static final int CURSOR_ADDRESS = 0;

    /**
     * @param io
     */
    public RadeonHardwareCursor(final RadeonVgaIO io) {
        this.io = io;
    }
    
    /**
     * @see org.jnode.driver.video.HardwareCursorAPI#setCursorImage(org.jnode.driver.video.HardwareCursor)
     */
    public void setCursorImage(HardwareCursor cursor) {
        
        // Background color
    	io.setReg32(CUR_CLR0, 0xffffff);
    	// Foreground color
    	io.setReg32(CUR_CLR1, 0);

    	// Set shape
		final short[] cur = getCursor(cursor);
		if (cur != null) {
			io.getVideoMem().setShorts(cur, 0, CURSOR_ADDRESS, 1024);
		}
    }
    
    /**
     * @see org.jnode.driver.video.HardwareCursorAPI#setCursorPosition(int, int)
     */
    public void setCursorPosition(int x, int y) {
    	// if upper-left corner of cursor is outside of
    	// screen, we have to use special registers to clip it
    	int xorigin = 0;
    	int yorigin = 0;
    		
        if( x < 0 ) {
        	xorigin = -x;
        }
        	
        if( y < 0 ) { 
        	yorigin = -y;
        }

    	//Radeon_WaitForFifo( ai, 3 );

        io.setReg32(CUR_HORZ_VERT_OFF, CUR_LOCK
    			| (xorigin << 16)
    			| yorigin );
    	io.setReg32(CUR_HORZ_VERT_POSN, CUR_LOCK
    			| (((xorigin != 0) ? 0 : x) << 16)
    			| ((yorigin != 0) ? 0 : y) );
    	io.setReg32(CUR_OFFSET, CURSOR_ADDRESS + xorigin + yorigin * 16 );
    }
    
    /**
     * @see org.jnode.driver.video.HardwareCursorAPI#setCursorVisible(boolean)
     */
    public void setCursorVisible(boolean visible) {
		int tmp = io.getReg32(CRTC_GEN_CNTL);
		if (visible) {
		    tmp |= CRTC_CUR_EN;
		} else {
		    tmp &= ~CRTC_CUR_EN;		    
		}
		io.setReg32(CRTC_GEN_CNTL, tmp);
    }

    private short[] getCursor(HardwareCursor cursor) {
		final HardwareCursorImage img = cursor.getImage(32, 32);
		if (img == null) {
			return null;
		}
		short[] res = (short[]) cursorCache.get(img);
		if (res == null) {
			res = new short[1024];
			final int[] argb = img.getImage();
			for (int i = 0; i < 1024; i++) {
				final int v = argb[i];
				final int a = (v >>> 24) & 0xFF;
				final int r = ((v >> 16) & 0xFF) >> 3;
				final int g = ((v >> 8) & 0xFF) >> 3;
				final int b = (v & 0xFF) >> 3;

				res[i] = (short) ((r << 10) | (g << 5) | b);
				if (a != 0) {
					res[i] |= 0x8000;
				}
			}
			cursorCache.put(img, res);
		}
		return res;
	}
}
