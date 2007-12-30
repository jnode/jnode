package sun.font;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;

/**
 * @see sun.font.FileFont
 * @author Levente S\u00e1ntha
 */
class NativeFileFont {
    /**
     * @see sun.font.FileFont#freeScaler(long)
     */
    private static void freeScaler(long arg1) {
        //todo implement it
    }
    /**
     * @see sun.font.FileFont#getNullScaler()
     */
    private static long getNullScaler() {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.font.FileFont#getFontMetrics(long)
     */
    private static StrikeMetrics getFontMetrics(FileFont instance, long arg1) {
        //todo implement it
        return null;
    }
    /**
     * @see sun.font.FileFont#getGlyphAdvance(long, int)
     */
    private static float getGlyphAdvance(FileFont instance, long arg1, int arg2) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.font.FileFont#getGlyphMetrics(long, int, java.awt.geom.Point2D.Float)
     */
    private static void getGlyphMetrics(FileFont instance, long arg1, int arg2, Point2D.Float arg3) {
        //todo implement it
    }
    /**
     * @see sun.font.FileFont#getGlyphImage(long, int)
     */
    private static long getGlyphImage(FileFont instance, long arg1, int arg2) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.font.FileFont#getGlyphOutlineBounds(long, int)
     */
    private static Rectangle2D.Float getGlyphOutlineBounds(FileFont instance, long arg1, int arg2) {
        //todo implement it
        return null;
    }
    /**
     * @see sun.font.FileFont#getGlyphOutline(long, int, float, float)
     */
    private static GeneralPath getGlyphOutline(FileFont instance, long arg1, int arg2, float arg3, float arg4) {
        //todo implement it
        return null;
    }
    /**
     * @see sun.font.FileFont#getGlyphVectorOutline(long, int[], int, float, float)
     */
    private static GeneralPath getGlyphVectorOutline(FileFont instance, long arg1, int[] arg2, int arg3, float arg4, float arg5) {
        //todo implement it
        return null;
    }

    /**
     * @see sun.font.FileFont#setNullScaler(long) 
     */
    private static void setNullScaler(FileFont instance, long pScalerContext){
        //todo implement it
    }
}
