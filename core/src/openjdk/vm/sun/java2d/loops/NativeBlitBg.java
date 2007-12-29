/*
 * $
 */
package sun.java2d.loops;

import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;
import java.awt.*;

/**
 * @author Levente S\u00e1ntha
 */
class NativeBlitBg {
    /**
     * @see sun.java2d.loops.BlitBg#BlitBg(sun.java2d.SurfaceData, sun.java2d.SurfaceData, java.awt.Composite, sun.java2d.pipe.Region, java.awt.Color, int, int, int, int, int, int)
     */
    private static void BlitBg(BlitBg instance, SurfaceData src, SurfaceData dst,
			      Composite comp, Region clip,
			      Color bgColor,
			      int srcx, int srcy,
			      int dstx, int dsty,
			      int width, int height){
        //todo implement it
    }
}
