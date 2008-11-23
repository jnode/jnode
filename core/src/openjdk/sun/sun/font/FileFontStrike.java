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

import java.lang.ref.SoftReference;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.ConcurrentHashMap;
import static sun.awt.SunHints.*;


public class FileFontStrike extends PhysicalStrike {

    /* fffe and ffff are values we specially interpret as meaning
     * invisible glyphs.
     */
    static final int INVISIBLE_GLYPHS = 0x0fffe;

    private FileFont fileFont;

    /* REMIND: replace this scheme with one that installs a cache
     * instance of the appropriate type. It will require changes in
     * FontStrikeDisposer and NativeStrike etc.
     */
    private static final int UNINITIALISED = 0;
    private static final int INTARRAY      = 1;
    private static final int LONGARRAY     = 2;
    private static final int SEGINTARRAY   = 3;
    private static final int SEGLONGARRAY  = 4;
 
    private int glyphCacheFormat = UNINITIALISED;

    /* segmented arrays are blocks of 256 */
    private static final int SEGSHIFT = 8;
    private static final int SEGSIZE  = 1 << SEGSHIFT;

    private boolean segmentedCache;
    private int[][] segIntGlyphImages;
    private long[][] segLongGlyphImages;

    /* The "metrics" information requested by clients is usually nothing
     * more than the horizontal advance of the character.
     * In most cases this advance and other metrics information is stored
     * in the glyph image cache.
     * But in some cases we do not automatically retrieve the glyph
     * image when the advance is requested. In those cases we want to
     * cache the advances since this has been shown to be important for
     * performance.
     * The segmented cache is used in cases when the single array
     * would be too large.
     */
    private float[] horizontalAdvances;
    private float[][] segHorizontalAdvances;

    /* Outline bounds are used when printing and when drawing outlines
     * to the screen. On balance the relative rarity of these cases
     * and the fact that getting this requires generating a path at
     * the scaler level means that its probably OK to store these
     * in a Java-level hashmap as the trade-off between time and space.
     * Later can revisit whether to cache these at all, or elsewhere.
     * Should also profile whether subsequent to getting the bounds, the
     * outline itself is also requested. The 1.4 implementation doesn't
     * cache outlines so you could generate the path twice - once to get
     * the bounds and again to return the outline to the client.
     * If the two uses are coincident then also look into caching outlines.
     * One simple optimisation is that we could store the last single
     * outline retrieved. This assumes that bounds then outline will always
     * be retrieved for a glyph rather than retrieving bounds for all glyphs
     * then outlines for all glyphs.
     */
    ConcurrentHashMap<Integer, Rectangle2D.Float> boundsMap;
    SoftReference<ConcurrentHashMap<Integer, Point2D.Float>>
        glyphMetricsMapRef;

    AffineTransform invertDevTx;

    boolean useNatives;
    NativeStrike[] nativeStrikes;

    FileFontStrike(FileFont fileFont, FontStrikeDesc desc) {
	super(fileFont, desc);
	this.fileFont = fileFont;

	if (desc.style != fileFont.style) {
	  /* If using algorithmic styling, the base values are
	   * boldness = 1.0, italic = 0.0. The superclass constructor
	   * initialises these.
	   */
	    if ((desc.style & Font.ITALIC) == Font.ITALIC &&
		(fileFont.style & Font.ITALIC) == 0) {
		algoStyle = true;
		italic = 0.7f;
	    }
	    if ((desc.style & Font.BOLD) == Font.BOLD &&
		((fileFont.style & Font.BOLD) == 0)) {
		algoStyle = true;
		boldness = 1.33f;
	    }
	}
	double[] matrix = new double[4];
	AffineTransform at = desc.glyphTx;
	at.getMatrix(matrix);
	if (!desc.devTx.isIdentity() &&
	    desc.devTx.getType() != AffineTransform.TYPE_TRANSLATION) {
	    try {
		invertDevTx = desc.devTx.createInverse();
	    } catch (NoninvertibleTransformException e) {
	    }
	}

        /* If any of the values is NaN then substitute the null scaler context.
         * This will return null images, zero advance, and empty outlines
         * as no rendering need take place in this case.
         * We pass in the null scaler as the singleton null context
         * requires it. However 
         */
        if (Double.isNaN(matrix[0]) || Double.isNaN(matrix[1]) ||
            Double.isNaN(matrix[2]) || Double.isNaN(matrix[3]) ||
            fileFont.getScaler() == null) {
            pScalerContext = NullFontScaler.getNullScalerContext();
        } else {
            pScalerContext = fileFont.getScaler().createScalerContext(matrix,
                                    fileFont instanceof TrueTypeFont,
                                    desc.aaHint, desc.fmHint,
                                    boldness, italic);
        }

	mapper = fileFont.getMapper();
	int numGlyphs = mapper.getNumGlyphs();

        /* Always segment for fonts with > 2K glyphs, but also for smaller
         * fonts with non-typical sizes and transforms.
         * Segmenting for all non-typical pt sizes helps to minimise memory
         * usage when very many distinct strikes are created.
         * The size range of 0->5 and 37->INF for segmenting is arbitrary
         * but the intention is that typical GUI integer point sizes (6->36)
         * should not segment unless there's another reason to do so.
         */
        float ptSize = (float)matrix[3]; // interpreted only when meaningful.
        int iSize = (int)ptSize;
        boolean isSimpleTx = (at.getType() & complexTX) == 0;
        segmentedCache =
            (numGlyphs > SEGSIZE << 3) ||
            ((numGlyphs > SEGSIZE << 1) &&
             (!isSimpleTx || ptSize != iSize || iSize < 6 || iSize > 36));

        /* This can only happen if we failed to allocate memory for context.
         * NB: in such case we may still have some memory in java heap
         *     but subsequent attempt to allocate null scaler context
         *     may fail too (cause it is allocate in the native heap).
         *     It is not clear how to make this more robust but on the
         *     other hand getting NULL here seems to be extremely unlikely.
	 */
	if (pScalerContext == 0L) {
            /* REMIND: when the code is updated to install cache objects
             * rather than using a switch this will be more efficient.
             */
            this.disposer = new FontStrikeDisposer(fileFont, desc);
            initGlyphCache();
            pScalerContext = NullFontScaler.getNullScalerContext();
	    FontManager.deRegisterBadFont(fileFont);
	    return;
	}

        if (fileFont.checkUseNatives() && desc.aaHint==0 && !algoStyle) {
            /* Check its a simple scale of a pt size in the range
	     * where native bitmaps typically exist (6-36 pts) */
            if (matrix[1] == 0.0 && matrix[2] == 0.0 &&
                matrix[0] >= 6.0 && matrix[0] <= 36.0 &&
		matrix[0] == matrix[3]) {
		useNatives = true;
                int numNatives = fileFont.nativeFonts.length;
                nativeStrikes = new NativeStrike[numNatives];
                /* Maybe initialise these strikes lazily?. But we
                 * know we need at least one
                 */
                for (int i=0; i<numNatives; i++) {
                    nativeStrikes[i] =
			new NativeStrike(fileFont.nativeFonts[i], desc, false);
                }
            }
        }

	this.disposer = new FontStrikeDisposer(fileFont, desc, pScalerContext);

        /* Always get the image and the advance together for smaller sizes
         * that are likely to be important to rendering performance.
         * The pixel size of 48.0 can be thought of as
         * "maximumSizeForGetImageWithAdvance".
         * This should be no greater than OutlineTextRender.THRESHOLD.
	 */
        getImageWithAdvance = at.getScaleY() <= 48.0;

        /* Some applications request advance frequently during layout.
         * If we are not getting and caching the image with the advance,
         * there is a potentially significant performance penalty if the
         * advance is repeatedly requested before requesting the image.
         * We should at least cache the horizontal advance.
         * REMIND: could use info in the font, eg hmtx, to retrieve some
         * advances. But still want to cache it here.
         */

        if (!getImageWithAdvance) {
            if (!segmentedCache) {
	    horizontalAdvances = new float[numGlyphs];
	    /* use max float as uninitialised advance */
	    for (int i=0; i<numGlyphs; i++) {
		horizontalAdvances[i] = Float.MAX_VALUE;
	    }
            } else {
                int numSegments = (numGlyphs + SEGSIZE-1)/SEGSIZE;
                segHorizontalAdvances = new float[numSegments][];
            }
	}
    }

    /* A number of methods are delegated by the strike to the scaler
     * context which is a shared resource on a physical font.
     */

    public int getNumGlyphs() {
	return fileFont.getNumGlyphs();
    }

    /* Try the native strikes first, then try the fileFont strike */
    long getGlyphImageFromNative(int glyphCode) {
	long glyphPtr;
	char charCode = fileFont.glyphToCharMap[glyphCode];
	for (int i=0;i<nativeStrikes.length;i++) {
	    CharToGlyphMapper mapper = fileFont.nativeFonts[i].getMapper();
	    int gc = mapper.charToGlyph(charCode)&0xffff;
	    if (gc != mapper.getMissingGlyphCode()) {
		glyphPtr = nativeStrikes[i].getGlyphImagePtrNoCache(gc);
		if (glyphPtr != 0L) {
		    return glyphPtr;
		}
	    }
	}
        return fileFont.getGlyphImage(pScalerContext, glyphCode);
    }

    long getGlyphImagePtr(int glyphCode) {
	if (glyphCode >= INVISIBLE_GLYPHS) {
            return StrikeCache.invisibleGlyphPtr;
	}
	long glyphPtr;
        if ((glyphPtr = getCachedGlyphPtr(glyphCode)) != 0L) {
            return glyphPtr;
        } else {
            if (useNatives) {
                glyphPtr = getGlyphImageFromNative(glyphCode);
            } else {
                glyphPtr = fileFont.getGlyphImage(pScalerContext,
                                                  glyphCode);
            }
            return setCachedGlyphPtr(glyphCode, glyphPtr);
        }
    }

    void getGlyphImagePtrs(int[] glyphCodes, long[] images, int  len) {

        for (int i=0; i<len; i++) {
            int glyphCode = glyphCodes[i];
            if (glyphCode >= INVISIBLE_GLYPHS) {
                images[i] = StrikeCache.invisibleGlyphPtr;
                continue;
            } else if ((images[i] = getCachedGlyphPtr(glyphCode)) != 0L) {
                continue;
            } else {
                long glyphPtr;
                if (useNatives) {
                    glyphPtr = getGlyphImageFromNative(glyphCode);
                } else {
                    glyphPtr = fileFont.getGlyphImage(pScalerContext,
                                                      glyphCode);
                }
                images[i] = setCachedGlyphPtr(glyphCode, glyphPtr);
            }
        }
    }

    /* The following method is called from CompositeStrike as a special case.
     */
    private static final int SLOTZEROMAX = 0xffffff;
    int getSlot0GlyphImagePtrs(int[] glyphCodes, long[] images, int len) {

	int convertedCnt = 0;

        for (int i=0; i<len; i++) {
            int glyphCode = glyphCodes[i];
            if (glyphCode >= SLOTZEROMAX) {
                return convertedCnt;
            } else {
                convertedCnt++;
            }
            if (glyphCode >= INVISIBLE_GLYPHS) {
                images[i] = StrikeCache.invisibleGlyphPtr;
                continue;
            } else if ((images[i] = getCachedGlyphPtr(glyphCode)) != 0L) {
                continue;
            } else {
                long glyphPtr;
                if (useNatives) {
                    glyphPtr = getGlyphImageFromNative(glyphCode);
                } else {
                    glyphPtr = fileFont.getGlyphImage(pScalerContext,
                                                      glyphCode);
                }
                images[i] = setCachedGlyphPtr(glyphCode, glyphPtr);
            }
	}
	return convertedCnt;
    }

    /* Only look in the cache */
    long getCachedGlyphPtr(int glyphCode) {
        switch (glyphCacheFormat) {
            case INTARRAY:
                return intGlyphImages[glyphCode] & INTMASK;
            case SEGINTARRAY:
                int segIndex = glyphCode >> SEGSHIFT;
                if (segIntGlyphImages[segIndex] != null) {
                    int subIndex = glyphCode % SEGSIZE;
                    return segIntGlyphImages[segIndex][subIndex] & INTMASK;
                } else {
                    return 0L;
                }
            case LONGARRAY:
                return longGlyphImages[glyphCode];
            case SEGLONGARRAY:
                segIndex = glyphCode >> SEGSHIFT;
                if (segLongGlyphImages[segIndex] != null) {
                    int subIndex = glyphCode % SEGSIZE;
                    return segLongGlyphImages[segIndex][subIndex];
                } else {
                    return 0L;
                }
        }
        /* If reach here cache is UNINITIALISED. */
        return 0L;
    }

    private synchronized long setCachedGlyphPtr(int glyphCode, long glyphPtr) {
        switch (glyphCacheFormat) {
            case INTARRAY:
                if (intGlyphImages[glyphCode] == 0) {
                    intGlyphImages[glyphCode] = (int)glyphPtr;
                    return glyphPtr;
                } else {
                    StrikeCache.freeIntPointer((int)glyphPtr);
                    return intGlyphImages[glyphCode] & INTMASK;
                }

            case SEGINTARRAY: 
                int segIndex = glyphCode >> SEGSHIFT;
                int subIndex = glyphCode % SEGSIZE;
                if (segIntGlyphImages[segIndex] == null) {
                    segIntGlyphImages[segIndex] = new int[SEGSIZE];
                }
                if (segIntGlyphImages[segIndex][subIndex] == 0) {
                    segIntGlyphImages[segIndex][subIndex] = (int)glyphPtr;
                    return glyphPtr;
                } else {
                    StrikeCache.freeIntPointer((int)glyphPtr);
                    return segIntGlyphImages[segIndex][subIndex] & INTMASK;
                }

            case LONGARRAY:
                if (longGlyphImages[glyphCode] == 0L) {
                    longGlyphImages[glyphCode] = glyphPtr;
                    return glyphPtr;
                } else {
                    StrikeCache.freeLongPointer(glyphPtr);
                    return longGlyphImages[glyphCode];
                }

           case SEGLONGARRAY: 
                segIndex = glyphCode >> SEGSHIFT;
                subIndex = glyphCode % SEGSIZE;
                if (segLongGlyphImages[segIndex] == null) {
                    segLongGlyphImages[segIndex] = new long[SEGSIZE];
                }
                if (segLongGlyphImages[segIndex][subIndex] == 0L) {
                    segLongGlyphImages[segIndex][subIndex] = glyphPtr;
                    return glyphPtr;
                } else {
                    StrikeCache.freeLongPointer(glyphPtr);
                    return segLongGlyphImages[segIndex][subIndex];
                }
        }

        /* Reach here only when the cache is not initialised which is only
         * for the first glyph to be initialised in the strike.
         * Initialise it and recurse. Note that we are already synchronized.
         */
        initGlyphCache();
        return setCachedGlyphPtr(glyphCode, glyphPtr);
    }

    /* Called only from synchronized code or constructor */
    private void initGlyphCache() {
        
        int numGlyphs = mapper.getNumGlyphs();

        if (segmentedCache) {
            int numSegments = (numGlyphs + SEGSIZE-1)/SEGSIZE;
            if (FontManager.longAddresses) {
                glyphCacheFormat = SEGLONGARRAY;
                segLongGlyphImages = new long[numSegments][];
                this.disposer.segLongGlyphImages = segLongGlyphImages;
             } else {
                 glyphCacheFormat = SEGINTARRAY;
                 segIntGlyphImages = new int[numSegments][];
                 this.disposer.segIntGlyphImages = segIntGlyphImages;
             }
        } else {
            if (FontManager.longAddresses) {
                glyphCacheFormat = LONGARRAY;
                longGlyphImages = new long[numGlyphs];
                this.disposer.longGlyphImages = longGlyphImages;
            } else {
                glyphCacheFormat = INTARRAY;
                intGlyphImages = new int[numGlyphs];
                this.disposer.intGlyphImages = intGlyphImages;
            }
        }
    }

    /* Metrics info is always retrieved. If the GlyphInfo address is non-zero
     * then metrics info there is valid and can just be copied.
     * This is in user space coordinates.
     */
    float getGlyphAdvance(int glyphCode) {
	float advance;

	if (glyphCode >= INVISIBLE_GLYPHS) {
	    return 0f;
	}
	if (horizontalAdvances != null) {
	    advance = horizontalAdvances[glyphCode];
	    if (advance != Float.MAX_VALUE) {
		return advance;
	    }
        } else if (segmentedCache && segHorizontalAdvances != null) {
            int segIndex = glyphCode >> SEGSHIFT;
            float[] subArray = segHorizontalAdvances[segIndex];
            if (subArray != null) {
                advance = subArray[glyphCode % SEGSIZE];
                if (advance != Float.MAX_VALUE) {
                    return advance;
                }
            }
	}

	if (invertDevTx != null) {
	    /* If there is a device transform need x & y advance to
	     * transform back into user space.
	     */
	    advance = getGlyphMetrics(glyphCode).x;
	} else {
	    long glyphPtr;
	    if (getImageWithAdvance) {
                /* A heuristic optimisation says that for most cases its
		 * worthwhile retrieving the image at the same time as the
		 * advance. So here we get the image data even if its not
		 * already cached.
		 */	    
		glyphPtr = getGlyphImagePtr(glyphCode);
	    } else {
		glyphPtr = getCachedGlyphPtr(glyphCode);
	    }
	    if (glyphPtr != 0L) {
		advance = StrikeCache.unsafe.getFloat
		    (glyphPtr + StrikeCache.xAdvanceOffset);

	    } else {
		advance = fileFont.getGlyphAdvance(pScalerContext, glyphCode);
	    }
	}

	if (horizontalAdvances != null) {
	    horizontalAdvances[glyphCode] = advance;
        } else if (segmentedCache && segHorizontalAdvances != null) {
            int segIndex = glyphCode >> SEGSHIFT;
            int subIndex = glyphCode % SEGSIZE;
            if (segHorizontalAdvances[segIndex] == null) {
                segHorizontalAdvances[segIndex] = new float[SEGSIZE];
                for (int i=0; i<SEGSIZE; i++) {
                     segHorizontalAdvances[segIndex][i] = Float.MAX_VALUE;
                }
            }
            segHorizontalAdvances[segIndex][subIndex] = advance;
	}
	return advance;
    }

    float getCodePointAdvance(int cp) {
	return getGlyphAdvance(mapper.charToGlyph(cp));
    }

    /**
     * Result and pt are both in device space.
     */
    void getGlyphImageBounds(int glyphCode, Point2D.Float pt,
			     Rectangle result) {

        long ptr = getGlyphImagePtr(glyphCode);
        float topLeftX, topLeftY;

        /* With our current design NULL ptr is not possible
           but if we eventually allow scalers to return NULL pointers
           this check might be actually useful. */
        if (ptr == 0L) {
            result.x = (int) Math.floor(pt.x);
            result.y = (int) Math.floor(pt.y);
            result.width = result.height = 0;
            return;
        }

        topLeftX = StrikeCache.unsafe.getFloat(ptr+StrikeCache.topLeftXOffset);
        topLeftY = StrikeCache.unsafe.getFloat(ptr+StrikeCache.topLeftYOffset);

	result.x = (int)Math.floor(pt.x + topLeftX);
	result.y = (int)Math.floor(pt.y + topLeftY);
        result.width =
	    StrikeCache.unsafe.getShort(ptr+StrikeCache.widthOffset)  &0x0ffff;
        result.height =
	    StrikeCache.unsafe.getShort(ptr+StrikeCache.heightOffset) &0x0ffff;

        /* HRGB LCD text may have padding that is empty. This is almost always
         * going to be when topLeftX is -2 or less.
         * Try to return a tighter bounding box in that case.
         * If the first three bytes of every row are all zero, then
         * add 1 to "x" and reduce "width" by 1.
         */
        if ((desc.aaHint == INTVAL_TEXT_ANTIALIAS_LCD_HRGB ||
             desc.aaHint == INTVAL_TEXT_ANTIALIAS_LCD_HBGR)
            && topLeftX <= -2.0f) {
            int minx = getGlyphImageMinX(ptr, (int)result.x);
            if (minx > result.x) {
                result.x += 1;
                result.width -=1;
            }
        }
    }

    private int getGlyphImageMinX(long ptr, int origMinX) {

        int width = StrikeCache.unsafe.getChar(ptr+StrikeCache.widthOffset);
        int height = StrikeCache.unsafe.getChar(ptr+StrikeCache.heightOffset);
        int rowBytes =
            StrikeCache.unsafe.getChar(ptr+StrikeCache.rowBytesOffset);

        if (rowBytes == width) {
            return origMinX;
        }

        long pixelData;
        if (StrikeCache.nativeAddressSize == 4) {
            pixelData = 0xffffffff &
                StrikeCache.unsafe.getInt(ptr + StrikeCache.pixelDataOffset);
        } else {
            pixelData =
                StrikeCache.unsafe.getLong(ptr + StrikeCache.pixelDataOffset);
        }
        if (pixelData == 0L) {
            return origMinX;
        }

        for (int y=0;y<height;y++) {
            for (int x=0;x<3;x++) {
                if (StrikeCache.unsafe.getByte(pixelData+y*rowBytes+x) != 0) {
                    return origMinX;
                }
            }
        }
        return origMinX+1;
    }

    /* These 3 metrics methods below should be implemented to return
     * values in user space.
     */
    StrikeMetrics getFontMetrics() {
	if (strikeMetrics == null) {
	    strikeMetrics =
		fileFont.getFontMetrics(pScalerContext);
	    if (invertDevTx != null) {
		strikeMetrics.convertToUserSpace(invertDevTx);
	    }
	}
	return strikeMetrics;
    }

    Point2D.Float getGlyphMetrics(int glyphCode) {
	Point2D.Float metrics = new Point2D.Float();

	// !!! or do we force sgv user glyphs?
	if (glyphCode >= INVISIBLE_GLYPHS) {
	    return metrics;
        }
	long glyphPtr;
	if (getImageWithAdvance) {
            /* A heuristic optimisation says that for most cases its
	     * worthwhile retrieving the image at the same time as the
	     * metrics. So here we get the image data even if its not
	     * already cached.
	     */
	    glyphPtr = getGlyphImagePtr(glyphCode);
	} else {
	     glyphPtr = getCachedGlyphPtr(glyphCode);
	}
	if (glyphPtr != 0L) {
	    metrics = new Point2D.Float();
	    metrics.x = StrikeCache.unsafe.getFloat
		(glyphPtr + StrikeCache.xAdvanceOffset);
	    metrics.y = StrikeCache.unsafe.getFloat
		(glyphPtr + StrikeCache.yAdvanceOffset);
	    /* advance is currently in device space, need to convert back
	     * into user space.
	     * This must not include the translation component. */
	    if (invertDevTx != null) {
		invertDevTx.deltaTransform(metrics, metrics);
	    }
	} else {
	    /* We sometimes cache these metrics as they are expensive to 
	     * generate for large glyphs.
	     * We never reach this path if we obtain images with advances.
	     * But if we do not obtain images with advances its possible that
	     * we first obtain this information, then the image, and never
	     * will access this value again.
	     */
	    Integer key = new Integer(glyphCode);
	    Point2D.Float value = null;
            ConcurrentHashMap<Integer, Point2D.Float> glyphMetricsMap = null;
	    if (glyphMetricsMapRef != null) {
                glyphMetricsMap = glyphMetricsMapRef.get();
	    }
	    if (glyphMetricsMap != null) {
                value = glyphMetricsMap.get(key);
                if (value != null) {
                    metrics.x = value.x;
                    metrics.y = value.y;
                    /* already in user space */
                    return metrics;
		}
	    }
	    if (value == null) {
		fileFont.getGlyphMetrics(pScalerContext, glyphCode, metrics);
		/* advance is currently in device space, need to convert back
		 * into user space.
		 */
		if (invertDevTx != null) {
		    invertDevTx.deltaTransform(metrics, metrics);
		}		
	        value = new Point2D.Float(metrics.x, metrics.y);
                /* We aren't synchronizing here so it is possible to
                 * overwrite the map with another one but this is harmless.
                 */
                if (glyphMetricsMap == null) {
                    glyphMetricsMap =
                        new ConcurrentHashMap<Integer, Point2D.Float>();
                    glyphMetricsMapRef =
                        new SoftReference<ConcurrentHashMap<Integer,
                        Point2D.Float>>(glyphMetricsMap);
                }
                glyphMetricsMap.put(key, value);
	    }
	}
	return metrics;
    }

    Point2D.Float getCharMetrics(char ch) {
	return getGlyphMetrics(mapper.charToGlyph(ch));
    }

    /* The caller of this can be trusted to return a copy of this
     * return value rectangle to public API. In fact frequently it
     * can't use use this return value directly anyway.
     * This returns bounds in device space. Currently the only
     * caller is SGV and it converts back to user space.
     * We could change things so that this code does the conversion so
     * that all coords coming out of the font system are converted back
     * into user space even if they were measured in device space.
     * The same applies to the other methods that return outlines (below)
     * But it may make particular sense for this method that caches its
     * results.
     * There'd be plenty of exceptions, to this too, eg getGlyphPoint needs
     * device coords as its called from native layout and getGlyphImageBounds
     * is used by GlyphVector.getGlyphPixelBounds which is specified to
     * return device coordinates, the image pointers aren't really used
     * up in Java code either.
     */
    Rectangle2D.Float getGlyphOutlineBounds(int glyphCode) {

        if (boundsMap == null) {
            boundsMap = new ConcurrentHashMap<Integer, Rectangle2D.Float>();
        }

        Integer key = new Integer(glyphCode);
        Rectangle2D.Float bounds = boundsMap.get(key);

	if (bounds == null) {
	    bounds = fileFont.getGlyphOutlineBounds(pScalerContext, glyphCode);
            boundsMap.put(key, bounds);
	}
	return bounds;
    }

    public Rectangle2D getOutlineBounds(int glyphCode) {
	return fileFont.getGlyphOutlineBounds(pScalerContext, glyphCode);
    }

    GeneralPath getGlyphOutline(int glyphCode, float x, float y) {
	return fileFont.getGlyphOutline(pScalerContext,	glyphCode, x, y);
    }

    GeneralPath getGlyphVectorOutline(int[] glyphs, float x, float y) {
	return fileFont.getGlyphVectorOutline(pScalerContext,
					      glyphs, glyphs.length, x, y);
    }

    protected void adjustPoint(Point2D.Float pt) {
	if (invertDevTx != null) {
      	    invertDevTx.deltaTransform(pt, pt);
	}
    }
}
