/*
 * $Id$
 */
package org.jnode.awt;

import org.jnode.driver.video.HardwareCursor;
import org.jnode.driver.video.HardwareCursorImage;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class JNodeCursors {

    /** White */
    private static final int W = 0xFFFFFFFF;
    /** Black */
    private static final int B = 0xFF000000;
    /** Transparent */
    private static final int T = 0x00000000;

    private static final int[] ARROW_IMAGE_16x16 = {
        B, B, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
        B, W, B, T, T, T, T, T, T, T, T, T, T, T, T, T,
        B, W, B, T, T, T, T, T, T, T, T, T, T, T, T, T,
        B, W, W, B, T, T, T, T, T, T, T, T, T, T, T, T,
        B, W, W, B, T, T, T, T, T, T, T, T, T, T, T, T,
        B, W, W, W, B, T, T, T, T, T, T, T, T, T, T, T,
        B, W, W, W, B, T, T, T, T, T, T, T, T, T, T, T,
        B, W, W, W, W, B, T, T, T, T, T, T, T, T, T, T,
        B, W, W, W, W, B, T, T, T, T, T, T, T, T, T, T,
        B, W, W, W, B, T, T, T, T, T, T, T, T, T, T, T,
        B, W, W, B, T, T, T, T, T, T, T, T, T, T, T, T,
        B, W, W, B, T, T, T, T, T, T, T, T, T, T, T, T,
        T, B, W, W, B, T, T, T, T, T, T, T, T, T, T, T,
        T, B, W, W, B, T, T, T, T, T, T, T, T, T, T, T,
        T, T, B, W, B, T, T, T, T, T, T, T, T, T, T, T,
        T, T, B, B, T, T, T, T, T, T, T, T, T, T, T, T,
    };
    // to be used for feature cursors
    /*
    private static final int[] EMPTY_IMAGE_16x16 = {
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
                T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
    };*/
    private static final int[] RESIZE_VERTICAL_IMAGE_16x16 = {
        T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
        T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
        T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
        T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
        T, T, T, B, T, T, T, T, T, T, T, T, B, T, T, T,
        T, T, B, T, T, T, T, T, T, T, T, T, T, B, T, T,
        T, B, T, T, T, T, T, T, T, T, T, T, T, T, B, T,
        B, B, B, B, B, B, B, B, B, B, B, B, B, B, B, B,
        T, B, T, T, T, T, T, T, T, T, T, T, T, T, B, T,
        T, T, B, T, T, T, T, T, T, T, T, T, T, B, T, T,
        T, T, T, B, T, T, T, T, T, T, T, T, B, T, T, T,
        T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
        T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
        T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
        T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
        T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
    };

    private static HardwareCursorImage ARROW_16x16 = new HardwareCursorImage(16, 16, ARROW_IMAGE_16x16, 0, 0);
    private static HardwareCursorImage RESIZE_VERTICAL_16x16 = new HardwareCursorImage(16, 16, RESIZE_VERTICAL_IMAGE_16x16, 0, 0);

    public static HardwareCursor ARROW = new HardwareCursor(new HardwareCursorImage[]{ARROW_16x16});
    public static HardwareCursor RESIZE_VERTICAL = new HardwareCursor(new HardwareCursorImage[]{RESIZE_VERTICAL_16x16});
}
