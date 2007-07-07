/*
 * Copyright 2003-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.font;

import java.lang.ref.Reference;
import java.awt.FontFormatException;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

public abstract class FileFont extends PhysicalFont {

    protected boolean useJavaRasterizer = true;
   
    /* I/O and file operations are always synchronized on the font
     * object. Two threads can be accessing the font and retrieving
     * information, and synchronized only to the extent that filesystem
     * operations require.
     * A limited number of files can be open at a time, to limit the
     * absorption of file descriptors. If a file needs to be opened
     * when there are none free, then the synchronization of all I/O
     * ensures that any in progress operation will complete before some
     * other thread closes the descriptor in order to allocate another one.
     */
    // NB consider using a RAF. FIS has finalize method so may take a
    // little longer to be GC'd. We don't use this stream at all anyway.
    // In fact why increase the size of a FileFont object if the stream
    // isn't needed ..
    //protected FileInputStream stream;
    //protected FileChannel channel;
    protected int fileSize;

    protected FileFontDisposer disposer;

    protected long pScaler;

    /* The following variables are used, (and in the case of the arrays, 
     * only initialised) for select fonts where a native scaler may be
     * used to get glyph images and metrics.
     * glyphToCharMap is filled in on the fly and used to do a reverse
     * lookup when a FileFont needs to get the charcode back from a glyph
     * code so it can re-map via a NativeGlyphMapper to get a native glyph.
     * This isn't a big hit in time, since a boolean test is sufficient
     * to choose the usual default path, nor in memory for fonts which take
     * the native path, since fonts have contiguous zero-based glyph indexes,
     * and these obviously do all exist in the font.
     */
    protected boolean checkedNatives;
    protected boolean useNatives;
    protected NativeFont[] nativeFonts;
    protected char[] glyphToCharMap;
    /*
     * @throws FontFormatException - if the font can't be opened
     */
    FileFont(String platname, Object nativeNames)
	throws FontFormatException {

	super(platname, nativeNames);
    }
    
    FontStrike createStrike(FontStrikeDesc desc) {
        if (!checkedNatives) {
           checkUseNatives();
        }
        return new FileFontStrike(this, desc);
    }

    protected boolean checkUseNatives() {
        checkedNatives = true;
	return useNatives;
    }

    /* This method needs to be accessible to FontManager if there is
     * file pool management. It may be a no-op.
     */
    protected abstract void close();

    /* 
     * This is the public interface. The subclasses need to implement
     * this. The returned block may be longer than the requested length.
     */
    abstract ByteBuffer readBlock(int offset, int length);

    public boolean canDoStyle(int style) {
	return true;
    }

    void setFileToRemove(File file) {
	Disposer.addObjectRecord(this,
				 new CreatedFontFileDisposerRecord(file));
    }

    /* This is called when a font scaler is determined to
     * be unusable (ie bad). We want to free all references to it, so
     * that its not used again.
     * This means updating all strikes to point to the null scaler so that
     * if they are currently referenced they will not attempt to use
     * the freed native scaler.
     * It also appears desirable to remove all the entries from the
     * cache so no other code will pick them up. But we can't just
     * 'delete' them as code may be using them. And simply dropping
     * the reference to the cache will make the reference objects
     * unreachable and so they will not get disposed.
     * Since a strike may hold (via java arrays) native pointers to many
     * rasterised glyphs, this would be a memory leak.
     * The solution is :
     * - to move all the entries to another map where they
     *   are no longer locatable
     * - update FontStrikeDisposer to be able to distinguish which
     * map they are held in via a boolean flag
     * Since this isn't expected to be anything other than an extremely
     * rare maybe it is not worth doing this last part.
     */
    synchronized void deregisterFontAndClearStrikeCache() {
        FontManager.deRegisterBadFont(this);

        pScaler = getNullScaler();

        for (Reference strikeRef : strikeCache.values()) {
            if (strikeRef != null) {
                /* NB we know these are all FileFontStrike instances
                 * because the cache is on this FileFont
                 */
                FileFontStrike strike = (FileFontStrike)strikeRef.get();
                if (strike != null && strike.pScalerContext != 0L) {
                    setNullScaler(strike.pScalerContext);
                }
            }
        }
    }

    /* These methods defined in scalerMethods.c */

    /* set the null scaler in an already existing native scaler context.
     * The effect of this is that existing references to a strike which
     * holds a pointer to that native struct, on subsequent calls down
     * into native will use that null scaler. This is used when the
     * original scaler is 'bad' and can no longer be used.
     */
    native synchronized void setNullScaler(long pScalerContext);

    /* freeScaler is called by a disposer on a reference queue */
    static native void freeScaler(long pScaler);

    /* Retrieves a singleton "null" scaler instance which must
     * not be freed.
     */
    static synchronized native long getNullScaler();

    native synchronized	StrikeMetrics getFontMetrics(long pScalerContext);

    native synchronized float getGlyphAdvance(long pScalerContext,
					      int glyphCode);

    native synchronized void getGlyphMetrics(long pScalerContext,
					     int glyphCode,
					     Point2D.Float metrics);

    native synchronized long getGlyphImage(long pScalerContext,
					   int glyphCode);

    /* These methods defined in t2kscalerMethods.cpp */
    native synchronized Rectangle2D.Float getGlyphOutlineBounds(long pContext,
								int glyphCode);

    native synchronized GeneralPath getGlyphOutline(long pScalerContext,
						    int glyphCode,
						    float x, float y);

    native synchronized	GeneralPath getGlyphVectorOutline(long pScalerContext,
							  int[] glyphs,
							  int numGlyphs,
							  float x, float y);

    /* T1 & TT implementation differ so this method is abstract */
    protected abstract long getScaler();

//     protected synchronized void freeScaler() {
// 	if (pScaler != 0L) {
// 	    freeScaler(pScaler);
// 	    pScaler = 0L;
// 	}
//     }

    protected static class FileFontDisposer implements DisposerRecord {

	long pScaler = 0L;
	boolean disposed = false;

	public FileFontDisposer(long pScaler) {
	    this.pScaler = pScaler;
	}

	public synchronized void dispose() {
	    if (!disposed) {
		FileFont.freeScaler(pScaler);
		pScaler = 0L;
		disposed = true;
	    }
	}
    }

    private static class CreatedFontFileDisposerRecord implements DisposerRecord {
	
	File fontFile = null;

	private CreatedFontFileDisposerRecord(File file) {
	    fontFile = file;
	}

	public void dispose() {
	    java.security.AccessController.doPrivileged(
	         new java.security.PrivilegedAction() {
	              public Object run() {
			  if (fontFile != null) {
			      try {
				  /* REMIND: is it possible that the file is
				   * still open? It will be closed when the
				   * font2D is disposed but could this code
				   * execute first? If so the file would not
				   * be deleted on MS-windows.
				   */
				  fontFile.delete();
				  /* remove from delete on exit hook list : */
				  FontManager.tmpFontFiles.remove(fontFile);
			      } catch (Exception e) {
			      }
			  }
			  return null;
		      }
	    }); 
	}
    }
}


   


    

    
