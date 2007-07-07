/*
 * Copyright 1999-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.java2d;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import sun.java2d.loops.RenderCache;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.DrawLine;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.DrawRect;
import sun.java2d.loops.DrawPolygons;
import sun.java2d.loops.DrawPath;
import sun.java2d.loops.FillPath;
import sun.java2d.loops.FillSpans;
import sun.java2d.loops.FontInfo;
import sun.java2d.loops.DrawGlyphList;
import sun.java2d.loops.DrawGlyphListAA;
import sun.java2d.loops.DrawGlyphListLCD;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.CompositePipe;
import sun.java2d.pipe.GeneralCompositePipe;
import sun.java2d.pipe.SpanClipRenderer;
import sun.java2d.pipe.SpanShapeRenderer;
import sun.java2d.pipe.DuctusShapeRenderer;
import sun.java2d.pipe.AlphaPaintPipe;
import sun.java2d.pipe.AlphaColorPipe;
import sun.java2d.pipe.PixelToShapeConverter;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.TextRenderer;
import sun.java2d.pipe.AATextRenderer;
import sun.java2d.pipe.LCDTextRenderer;
import sun.java2d.pipe.SolidTextRenderer;
import sun.java2d.pipe.OutlineTextRenderer;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.DrawImage;
import sun.awt.SunHints;
import sun.awt.image.SurfaceManager;

/**
 * This class provides various pieces of information relevant to a
 * particular drawing surface.  The information obtained from this
 * object describes the pixels of a particular instance of a drawing
 * surface and can only be shared among the various graphics objects
 * that target the same BufferedImage or the same screen Component.
 * <p>
 * Each SurfaceData object holds a StateTrackableDelegate object
 * which tracks both changes to the content of the pixels of this
 * surface and changes to the overall state of the pixels - such
 * as becoming invalid or losing the surface.  The delegate is
 * marked "dirty" whenever the setSurfaceLost() or invalidate()
 * methods are called and should also be marked "dirty" by the
 * rendering pipelines whenever they modify the pixels of this
 * SurfaceData.
 * <p>
 * If you get a StateTracker from a SurfaceData and it reports
 * that it is still "current", then you can trust that the pixels
 * have not changed and that the SurfaceData is still valid and
 * has not lost its underlying storage (surfaceLost) since you
 * retrieved the tracker.
 */
public abstract class SurfaceData
    implements Transparency, DisposerTarget, StateTrackable
{
    private long pData;
    private boolean valid;
    private boolean surfaceLost; // = false;
    private SurfaceType surfaceType;
    private ColorModel colorModel;

    private Object disposerReferent = new Object();

    private static native void initIDs();

    private Object blitProxyKey;
    private StateTrackableDelegate stateDelegate;

    static {
	initIDs();
    }

    protected SurfaceData(SurfaceType surfaceType, ColorModel cm) {
        this(State.STABLE, surfaceType, cm);
    }

    protected SurfaceData(State state, SurfaceType surfaceType, ColorModel cm) {
        this(StateTrackableDelegate.createInstance(state), surfaceType, cm);
    }

    protected SurfaceData(StateTrackableDelegate trackable,
                          SurfaceType surfaceType, ColorModel cm)
    {
        this.stateDelegate = trackable;
	this.colorModel = cm;
	this.surfaceType = surfaceType;
	valid = true;
    }

    protected SurfaceData(State state) {
        this.stateDelegate = StateTrackableDelegate.createInstance(state);
	valid = true;
    }

    /**
     * Subclasses can set a "blit proxy key" which will be used
     * along with the SurfaceManager.getCacheData() mechanism to
     * store acceleration-compatible cached copies of source images.
     * This key is a "tag" used to identify which cached copies
     * are compatible with this destination SurfaceData.
     * The getSourceSurfaceData() method uses this key to manage
     * cached copies of a source image as described below.
     * <p>
     * The Object used as this key should be as unique as it needs
     * to be to ensure that multiple acceleratible destinations can
     * each store their cached copies separately under different keys
     * without interfering with each other or getting back the wrong
     * cached copy.
     * <p>
     * Many acceleratable SurfaceData objects can use their own
     * GraphicsConfiguration as their proxy key as the GC object will
     * typically be unique to a given screen and pixel format, but
     * other rendering destinations may have more or less stringent
     * sharing requirements.  For instance, X11 pixmaps can be
     * shared on a given screen by any GraphicsConfiguration that
     * has the same depth and SurfaceType.  Multiple such GCs with
     * the same depth and SurfaceType can exist per screen so storing
     * a different cached proxy for each would be a waste.  One can
     * imagine platforms where a single cached copy can be created
     * and shared across all screens and pixel formats - such
     * implementations could use a single heavily shared key Object.
     */
    protected void setBlitProxyKey(Object key) {
        // Caching is effectively disabled if we never have a proxy key
        // since the getSourceSurfaceData() method only does caching
        // if the key is not null.
        if (SurfaceDataProxy.isCachingAllowed()) {
            this.blitProxyKey = key;
        }
    }

    /**
     * This method is called on a destination SurfaceData to choose
     * the best SurfaceData from a source Image for an imaging
     * operation, with help from its SurfaceManager.
     * The method may determine that the default SurfaceData was
     * really the best choice in the first place, or it may decide
     * to use a cached surface.  Some general decisions about whether
     * acceleration is enabled are made by this method, but any
     * decision based on the type of the source image is made in
     * the makeProxyFor method below when it comes up with the
     * appropriate SurfaceDataProxy instance.
     * The parameters describe the type of imaging operation being performed.
     * <p>
     * If a blitProxyKey was supplied by the subclass then it is
     * used to potentially override the choice of source SurfaceData.
     * The outline of this process is:
     * <ol>
     * <li> Image pipeline asks destSD to find an appropriate
     *      srcSD for a given source Image object.
     * <li> destSD gets the SurfaceManager of the source Image
     *      and first retrieves the default SD from it using
     *      getPrimarySurfaceData()
     * <li> destSD uses its "blit proxy key" (if set) to look for
     *      some cached data stored in the source SurfaceManager
     * <li> If the cached data is null then makeProxyFor() is used
     *      to create some cached data which is stored back in the
     *      source SurfaceManager under the same key for future uses.
     * <li> The cached data will be a SurfaceDataProxy object.
     * <li> The SurfaceDataProxy object is then consulted to
     *      return a replacement SurfaceData object (typically
     *      a cached copy if appropriate, or the original if not).
     * </ol>
     */
    public SurfaceData getSourceSurfaceData(Image img,
                                            int txtype,
                                            CompositeType comp,
                                            Color bgColor)
    {
        SurfaceManager srcMgr = SurfaceManager.getManager(img);
        SurfaceData srcData = srcMgr.getPrimarySurfaceData();
        if (img.getAccelerationPriority() > 0.0f &&
            blitProxyKey != null)
        {
            SurfaceDataProxy sdp =
                (SurfaceDataProxy) srcMgr.getCacheData(blitProxyKey);
            if (sdp == null || !sdp.isValid()) {
                if (srcData.getState() == State.UNTRACKABLE) {
                    sdp = SurfaceDataProxy.UNCACHED;
                } else {
                    sdp = makeProxyFor(srcData);
                }
                srcMgr.setCacheData(blitProxyKey, sdp);
            }
            srcData = sdp.replaceData(srcData, txtype, comp, bgColor);
        }
        return srcData;
    }

    /**
     * This method is called on a destination SurfaceData to choose
     * a proper SurfaceDataProxy subclass for a source SurfaceData
     * to use to control when and with what surface to override a
     * given image operation.  The argument is the default SurfaceData
     * for the source Image.
     * <p>
     * The type of the return object is chosen based on the
     * acceleration capabilities of this SurfaceData and the
     * type of the given source SurfaceData object.
     * <p>
     * In some cases the original SurfaceData will always be the
     * best choice to use to blit to this SurfaceData.  This can
     * happen if the source image is a hardware surface of the
     * same type as this one and so acceleration will happen without
     * any caching.  It may also be the case that the source image
     * can never be accelerated on this SurfaceData - for example
     * because it is translucent and there are no accelerated
     * translucent image ops for this surface.
     * <p>
     * In those cases there is a special SurfaceDataProxy.UNCACHED
     * instance that represents a NOP for caching purposes - it
     * always returns the original sourceSD object as the replacement
     * copy so no caching is ever performed.
     */
    public SurfaceDataProxy makeProxyFor(SurfaceData srcData) {
        return SurfaceDataProxy.UNCACHED;
    }

    /**
     * Extracts the SurfaceManager from the given Image, and then
     * returns the SurfaceData object that would best be suited as the
     * destination surface in some rendering operation.
     */
    public static SurfaceData getPrimarySurfaceData(Image img) {
        SurfaceManager sMgr = SurfaceManager.getManager(img);
        return sMgr.getPrimarySurfaceData();
    }

    /**
     * Restores the contents of the given Image and then returns the new
     * SurfaceData object in use by the Image's SurfaceManager.
     */
    public static SurfaceData restoreContents(Image img) {
        SurfaceManager sMgr = SurfaceManager.getManager(img);
        return sMgr.restoreContents();
    }

    public State getState() {
        return stateDelegate.getState();
    }

    public StateTracker getStateTracker() {
        return stateDelegate.getStateTracker();
    }

    /**
     * Marks this surface as dirty.
     */
    public final void markDirty() {
        stateDelegate.markDirty();
    }

    /**
     * Sets the value of the surfaceLost variable, which indicates whether
     * something has happened to the rendering surface such that it needs
     * to be restored and re-rendered.
     */
    public void setSurfaceLost(boolean lost) {
        surfaceLost = lost;
        stateDelegate.markDirty();
    }

    public boolean isSurfaceLost() {
        return surfaceLost;
    }

    /**
     * Returns a boolean indicating whether or not this SurfaceData is valid.
     */
    public final boolean isValid() {
	return valid;
    }

    public Object getDisposerReferent() {
	return disposerReferent;
    }

    public long getNativeOps() {
	return pData;
    }

    /**
     * Sets this SurfaceData object to the invalid state.  All Graphics
     * objects must get a new SurfaceData object via the refresh method
     * and revalidate their pipelines before continuing.
     */
    public void invalidate() {
	valid = false;
        stateDelegate.markDirty();
    }

    /**
     * Certain changes in the configuration of a surface require the
     * invalidation of existing associated SurfaceData objects and
     * the creation of brand new ones.  These changes include size,
     * ColorModel, or SurfaceType.  Existing Graphics objects
     * which are directed at such surfaces, however, must continue
     * to render to them even after the change occurs underneath
     * the covers.  The getReplacement() method is called from
     * SunGraphics2D.revalidateAll() when the associated SurfaceData
     * is found to be invalid so that a Graphics object can continue
     * to render to the surface in its new configuration.
     *
     * Such changes only tend to happen to window based surfaces since
     * most image based surfaces never change size or pixel format.
     * Even VolatileImage objects never change size and they only
     * change their pixel format when manually validated against a
     * new GraphicsConfiguration, at which point old Graphics objects
     * are no longer expected to render to them after the validation
     * step.  Thus, only window based surfaces really need to deal
     * with this form of replacement.
     */
    public abstract SurfaceData getReplacement();

    protected static final LoopPipe colorPrimitives;

    public static final TextPipe outlineTextRenderer;
    public static final TextPipe solidTextRenderer;
    public static final TextPipe aaTextRenderer;
    public static final TextPipe lcdTextRenderer;

    protected static final CompositePipe colorPipe;
    protected static final PixelToShapeConverter colorViaShape;
    protected static final TextPipe colorText;
    protected static final CompositePipe clipColorPipe;
    protected static final TextPipe clipColorText;
    protected static final DuctusShapeRenderer AAColorShape;
    protected static final PixelToShapeConverter AAColorViaShape;
    protected static final DuctusShapeRenderer AAClipColorShape;
    protected static final PixelToShapeConverter AAClipColorViaShape;

    protected static final CompositePipe paintPipe;
    protected static final SpanShapeRenderer paintShape;
    protected static final PixelToShapeConverter paintViaShape;
    protected static final TextPipe paintText;
    protected static final CompositePipe clipPaintPipe;
    protected static final TextPipe clipPaintText;
    protected static final DuctusShapeRenderer AAPaintShape;
    protected static final PixelToShapeConverter AAPaintViaShape;
    protected static final DuctusShapeRenderer AAClipPaintShape;
    protected static final PixelToShapeConverter AAClipPaintViaShape;

    protected static final CompositePipe compPipe;
    protected static final SpanShapeRenderer compShape;
    protected static final PixelToShapeConverter compViaShape;
    protected static final TextPipe compText;
    protected static final CompositePipe clipCompPipe;
    protected static final TextPipe clipCompText;
    protected static final DuctusShapeRenderer AACompShape;
    protected static final PixelToShapeConverter AACompViaShape;
    protected static final DuctusShapeRenderer AAClipCompShape;
    protected static final PixelToShapeConverter AAClipCompViaShape;

    protected static final DrawImagePipe imagepipe;

    static {
	colorPrimitives = new LoopPipe();

	outlineTextRenderer = new OutlineTextRenderer();
	solidTextRenderer = new SolidTextRenderer();
	aaTextRenderer = new AATextRenderer();
	lcdTextRenderer = new LCDTextRenderer();

	colorPipe = new AlphaColorPipe();
	// colorShape = colorPrimitives;
	colorViaShape = new PixelToShapeConverter(colorPrimitives);
	colorText = new TextRenderer(colorPipe);
	clipColorPipe = new SpanClipRenderer(colorPipe);
	clipColorText = new TextRenderer(clipColorPipe);
	AAColorShape = new DuctusShapeRenderer(colorPipe);
	AAColorViaShape = new PixelToShapeConverter(AAColorShape);
	AAClipColorShape = new DuctusShapeRenderer(clipColorPipe);
	AAClipColorViaShape = new PixelToShapeConverter(AAClipColorShape);

	paintPipe = new AlphaPaintPipe();
	paintShape = new SpanShapeRenderer.Composite(paintPipe);
	paintViaShape = new PixelToShapeConverter(paintShape);
	paintText = new TextRenderer(paintPipe);
	clipPaintPipe = new SpanClipRenderer(paintPipe);
	clipPaintText = new TextRenderer(clipPaintPipe);
	AAPaintShape = new DuctusShapeRenderer(paintPipe);
	AAPaintViaShape = new PixelToShapeConverter(AAPaintShape);
	AAClipPaintShape = new DuctusShapeRenderer(clipPaintPipe);
	AAClipPaintViaShape = new PixelToShapeConverter(AAClipPaintShape);

	compPipe = new GeneralCompositePipe();
	compShape = new SpanShapeRenderer.Composite(compPipe);
	compViaShape = new PixelToShapeConverter(compShape);
	compText = new TextRenderer(compPipe);
	clipCompPipe = new SpanClipRenderer(compPipe);
	clipCompText = new TextRenderer(clipCompPipe);
	AACompShape = new DuctusShapeRenderer(compPipe);
	AACompViaShape = new PixelToShapeConverter(AACompShape);
	AAClipCompShape = new DuctusShapeRenderer(clipCompPipe);
	AAClipCompViaShape = new PixelToShapeConverter(AAClipCompShape);

	imagepipe = new DrawImage();
    }

    /* Not all surfaces and rendering mode combinations support LCD text. */
    static final int LCDLOOP_UNKNOWN = 0;
    static final int LCDLOOP_FOUND = 1;
    static final int LCDLOOP_NOTFOUND = 2;
    int haveLCDLoop;

    public boolean canRenderLCDText(SunGraphics2D sg2d) {
        // For now the answer can only be true in the following cases:
        if (sg2d.compositeState <= SunGraphics2D.COMP_ISCOPY &&
	    sg2d.paintState <= SunGraphics2D.PAINT_ALPHACOLOR &&
	    sg2d.clipState <= SunGraphics2D.CLIP_RECTANGULAR &&
	    // This last test is a workaround until we fix loop selection
	    // in the pipe validation
	    sg2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON) {
            if (haveLCDLoop == LCDLOOP_UNKNOWN) {
                DrawGlyphListLCD loop =
		    DrawGlyphListLCD.locate(SurfaceType.AnyColor,
					    CompositeType.SrcNoEa,
					    getSurfaceType());
                haveLCDLoop = (loop!= null) ? LCDLOOP_FOUND : LCDLOOP_NOTFOUND;
            }
            return haveLCDLoop == LCDLOOP_FOUND;
        }
        return false; /* for now - in the future we may want to search */
    }

    public void validatePipe(SunGraphics2D sg2d) {
	sg2d.imagepipe = imagepipe;
        if (sg2d.compositeState == sg2d.COMP_XOR) {
	    if (sg2d.paintState > sg2d.PAINT_ALPHACOLOR) {
		sg2d.drawpipe = paintViaShape;
		sg2d.fillpipe = paintViaShape;
		sg2d.shapepipe = paintShape;
		// REMIND: Ideally custom paint mode would use glyph
		// rendering as opposed to outline rendering but the
		// glyph paint rendering pipeline uses MaskBlit which
		// is not defined for XOR.  This means that text drawn
		// in XOR mode with a Color object is different than
		// text drawn in XOR mode with a Paint object.
		sg2d.textpipe = outlineTextRenderer;
	    } else {
		if (sg2d.clipState == sg2d.CLIP_SHAPE) {
		    sg2d.drawpipe = colorViaShape;
		    sg2d.fillpipe = colorViaShape;
		    // REMIND: We should not be changing text strategies
		    // between outline and glyph rendering based upon the
		    // presence of a complex clip as that could cause a
		    // mismatch when drawing the same text both clipped
		    // and unclipped on two separate rendering passes.
		    // Unfortunately, all of the clipped glyph rendering
		    // pipelines rely on the use of the MaskBlit operation
		    // which is not defined for XOR.
		    sg2d.textpipe = outlineTextRenderer;
		} else {
		    if (sg2d.transformState >= sg2d.TRANSFORM_TRANSLATESCALE) {
			sg2d.drawpipe = colorViaShape;
			sg2d.fillpipe = colorViaShape;
		    } else {
			if (sg2d.strokeState != sg2d.STROKE_THIN) {
			    sg2d.drawpipe = colorViaShape;
			} else {
			    sg2d.drawpipe = colorPrimitives;
			}
			sg2d.fillpipe = colorPrimitives;
		    }
		    sg2d.textpipe = solidTextRenderer;
		}
		sg2d.shapepipe = colorPrimitives;
		sg2d.loops = getRenderLoops(sg2d);
		// assert(sg2d.surfaceData == this);
	    }
	} else if (sg2d.compositeState == sg2d.COMP_CUSTOM) {
	    if (sg2d.antialiasHint == SunHints.INTVAL_ANTIALIAS_ON) {
		if (sg2d.clipState == sg2d.CLIP_SHAPE) {
		    sg2d.drawpipe = AAClipCompViaShape;
		    sg2d.fillpipe = AAClipCompViaShape;
		    sg2d.shapepipe = AAClipCompShape;
		    sg2d.textpipe = clipCompText;
		} else {
		    sg2d.drawpipe = AACompViaShape;
		    sg2d.fillpipe = AACompViaShape;
		    sg2d.shapepipe = AACompShape;
		    sg2d.textpipe = compText;
		}
	    } else {
		sg2d.drawpipe = compViaShape;
		sg2d.fillpipe = compViaShape;
		sg2d.shapepipe = compShape;
		if (sg2d.clipState == sg2d.CLIP_SHAPE) {
		    sg2d.textpipe = clipCompText;
		} else {
		    sg2d.textpipe = compText;
		}
	    }
	} else if (sg2d.antialiasHint == SunHints.INTVAL_ANTIALIAS_ON) {
            sg2d.alphafill = getMaskFill(sg2d);
            // assert(sg2d.surfaceData == this);
            if (sg2d.alphafill != null) {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.drawpipe = AAClipColorViaShape;
                    sg2d.fillpipe = AAClipColorViaShape;
                    sg2d.shapepipe = AAClipColorShape;
                    sg2d.textpipe = clipColorText;
                } else {
                    sg2d.drawpipe = AAColorViaShape;
                    sg2d.fillpipe = AAColorViaShape;
                    sg2d.shapepipe = AAColorShape;
                    sg2d.textpipe = colorText;
                }
            } else {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
		    sg2d.drawpipe = AAClipPaintViaShape;
		    sg2d.fillpipe = AAClipPaintViaShape;
		    sg2d.shapepipe = AAClipPaintShape;
		    sg2d.textpipe = clipPaintText;
		} else {
		    sg2d.drawpipe = AAPaintViaShape;
		    sg2d.fillpipe = AAPaintViaShape;
		    sg2d.shapepipe = AAPaintShape;
		    sg2d.textpipe = paintText;
		}
	    }
	} else if (sg2d.paintState > sg2d.PAINT_ALPHACOLOR ||
                   sg2d.compositeState > sg2d.COMP_ISCOPY ||
                   sg2d.clipState == sg2d.CLIP_SHAPE)
	{
	    sg2d.drawpipe = paintViaShape;
	    sg2d.fillpipe = paintViaShape;
	    sg2d.shapepipe = paintShape;
            sg2d.alphafill = getMaskFill(sg2d);
            // assert(sg2d.surfaceData == this);
            if (sg2d.alphafill != null) {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.textpipe = clipColorText;
                } else {
                    sg2d.textpipe = colorText;
                }
            } else {
		if (sg2d.clipState == sg2d.CLIP_SHAPE) {
		    sg2d.textpipe = clipPaintText;
		} else {
		    sg2d.textpipe = paintText;
		}
	    }
	} else {
	    if (sg2d.transformState >= sg2d.TRANSFORM_TRANSLATESCALE) {
		sg2d.drawpipe = colorViaShape;
		sg2d.fillpipe = colorViaShape;
	    } else {
		if (sg2d.strokeState != sg2d.STROKE_THIN) {
		    sg2d.drawpipe = colorViaShape;
		} else {
		    sg2d.drawpipe = colorPrimitives;
		}
		sg2d.fillpipe = colorPrimitives;
	    }

	    /* Try to avoid calling getFontInfo() unless its needed to
	     * resolve one of the new AA types.
	     */
	    switch (sg2d.textAntialiasHint) {
	    case SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT:
		/* equating to OFF which it is for us */
	    case SunHints.INTVAL_TEXT_ANTIALIAS_OFF:
		sg2d.textpipe = solidTextRenderer;
		break;

	    case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
		sg2d.textpipe = aaTextRenderer;
		break;

	    default:
		switch (sg2d.getFontInfo().aaHint) {

		case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB:
		case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VRGB:
		    sg2d.textpipe = lcdTextRenderer;
		    break;

		case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
		    sg2d.textpipe = aaTextRenderer;
		    break;

		default:
		sg2d.textpipe = solidTextRenderer;
		}
	    }
	    sg2d.shapepipe = colorPrimitives;
	    sg2d.loops = getRenderLoops(sg2d);
	    // assert(sg2d.surfaceData == this);
	}
    }

    private static SurfaceType getPaintSurfaceType(SunGraphics2D sg2d) {
        switch (sg2d.paintState) {
        case SunGraphics2D.PAINT_OPAQUECOLOR:
            return SurfaceType.OpaqueColor;
        case SunGraphics2D.PAINT_ALPHACOLOR:
            return SurfaceType.AnyColor;
        case SunGraphics2D.PAINT_GRADIENT:
            if (sg2d.paint.getTransparency() == OPAQUE) {
                return SurfaceType.OpaqueGradientPaint;
            } else {
                return SurfaceType.GradientPaint;
            }
        case SunGraphics2D.PAINT_LIN_GRADIENT:
            if (sg2d.paint.getTransparency() == OPAQUE) {
                return SurfaceType.OpaqueLinearGradientPaint;
            } else {
                return SurfaceType.LinearGradientPaint;
            }
        case SunGraphics2D.PAINT_RAD_GRADIENT:
            if (sg2d.paint.getTransparency() == OPAQUE) {
                return SurfaceType.OpaqueRadialGradientPaint;
            } else {
                return SurfaceType.RadialGradientPaint;
            }
        case SunGraphics2D.PAINT_TEXTURE:
            if (sg2d.paint.getTransparency() == OPAQUE) {
                return SurfaceType.OpaqueTexturePaint;
            } else {
                return SurfaceType.TexturePaint;
            }
        default:
        case SunGraphics2D.PAINT_CUSTOM:
            return SurfaceType.AnyPaint;
        }
    }

    /**
     * Returns a MaskFill object that can be used on this destination
     * with the source (paint) and composite types determined by the given
     * SunGraphics2D, or null if no such MaskFill object can be located.
     * Subclasses can override this method if they wish to filter other
     * attributes (such as the hardware capabilities of the destination
     * surface) before returning a specific MaskFill object.
     */
    protected MaskFill getMaskFill(SunGraphics2D sg2d) {
        return MaskFill.getFromCache(getPaintSurfaceType(sg2d),
                                     sg2d.imageComp,
                                     getSurfaceType());
    }

    private static RenderCache loopcache = new RenderCache(30);

    /**
     * Return a RenderLoops object containing all of the basic
     * GraphicsPrimitive objects for rendering to the destination
     * surface with the current attributes of the given SunGraphics2D.
     */
    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        SurfaceType src = getPaintSurfaceType(sg2d);
	CompositeType comp = (sg2d.compositeState == sg2d.COMP_ISCOPY
                              ? CompositeType.SrcNoEa
                              : sg2d.imageComp);
	SurfaceType dst = sg2d.getSurfaceData().getSurfaceType();
	
	Object o = loopcache.get(src, comp, dst);
	if (o != null) {
	    return (RenderLoops) o;
	}
	
	RenderLoops loops = makeRenderLoops(src, comp, dst);
	loopcache.put(src, comp, dst, loops);
	return loops;
    }

    /**
     * Construct and return a RenderLoops object containing all of
     * the basic GraphicsPrimitive objects for rendering to the
     * destination surface with the given source, destination, and
     * composite types.
     */
    public static RenderLoops makeRenderLoops(SurfaceType src,
					      CompositeType comp,
					      SurfaceType dst)
    {
	RenderLoops loops = new RenderLoops();
	loops.drawLineLoop = DrawLine.locate(src, comp, dst);
	loops.fillRectLoop = FillRect.locate(src, comp, dst);
	loops.drawRectLoop = DrawRect.locate(src, comp, dst);
	loops.drawPolygonsLoop = DrawPolygons.locate(src, comp, dst);
        loops.drawPathLoop = DrawPath.locate(src, comp, dst);
        loops.fillPathLoop = FillPath.locate(src, comp, dst);
	loops.fillSpansLoop = FillSpans.locate(src, comp, dst);
	loops.drawGlyphListLoop = DrawGlyphList.locate(src, comp, dst);
	loops.drawGlyphListAALoop = DrawGlyphListAA.locate(src, comp, dst);
	loops.drawGlyphListLCDLoop = DrawGlyphListLCD.locate(src, comp, dst);
	/*
       	System.out.println("drawLine: "+loops.drawLineLoop);
	System.out.println("fillRect: "+loops.fillRectLoop);
	System.out.println("drawRect: "+loops.drawRectLoop);
	System.out.println("drawPolygons: "+loops.drawPolygonsLoop);
	System.out.println("fillSpans: "+loops.fillSpansLoop);
	System.out.println("drawGlyphList: "+loops.drawGlyphListLoop);
	System.out.println("drawGlyphListAA: "+loops.drawGlyphListAALoop);
	System.out.println("drawGlyphListLCD: "+loops.drawGlyphListLCDLoop);
	*/	
	return loops;
    }

    /**
     * Return the GraphicsConfiguration object that describes this
     * destination surface.
     */
    public abstract GraphicsConfiguration getDeviceConfiguration();

    /**
     * Return the SurfaceType object that describes the destination
     * surface.
     */
    public final SurfaceType getSurfaceType() {
	return surfaceType;
    }

    /**
     * Return the ColorModel for the destination surface.
     */
    public final ColorModel getColorModel() {
	return colorModel;
    }

    /**
     * Returns the type of this <code>Transparency</code>.
     * @return the field type of this <code>Transparency</code>, which is
     *		either OPAQUE, BITMASK or TRANSLUCENT. 
     */
    public int getTransparency() {
	return getColorModel().getTransparency();
    }

    /**
     * Return a readable Raster which contains the pixels for the
     * specified rectangular region of the destination surface.
     * The coordinate origin of the returned Raster is the same as
     * the device space origin of the destination surface.
     * In some cases the returned Raster might also be writeable.
     * In most cases, the returned Raster might contain more pixels
     * than requested.
     *
     * @see useTightBBoxes
     */
    public abstract Raster getRaster(int x, int y, int w, int h);

    /**
     * Does the pixel accessibility of the destination surface
     * suggest that rendering algorithms might want to take
     * extra time to calculate a more accurate bounding box for
     * the operation being performed?
     * The typical case when this will be true is when a copy of
     * the pixels has to be made when doing a getRaster.  The
     * fewer pixels copied, the faster the operation will go.
     *
     * @see getRaster
     */
    public boolean useTightBBoxes() {
	// Note: The native equivalent would trigger on VISIBLE_TO_NATIVE
	// REMIND: This is not used - should be obsoleted maybe
	return true;
    }

    /**
     * Returns the pixel data for the specified Argb value packed
     * into an integer for easy storage and conveyance.
     */
    public int pixelFor(int rgb) {
	return surfaceType.pixelFor(rgb, colorModel);
    }

    /**
     * Returns the pixel data for the specified color packed into an
     * integer for easy storage and conveyance.
     *
     * This method will use the getRGB() method of the Color object
     * and defer to the pixelFor(int rgb) method if not overridden.
     *
     * For now this is a convenience function, but for cases where
     * the highest quality color conversion is requested, this method
     * should be overridden in those cases so that a more direct
     * conversion of the color to the destination color space
     * can be done using the additional information in the Color
     * object.
     */
    public int pixelFor(Color c) {
	return pixelFor(c.getRGB());
    }

    /**
     * Returns the Argb representation for the specified integer value
     * which is packed in the format of the associated ColorModel.
     */
    public int rgbFor(int pixel) {
        return surfaceType.rgbFor(pixel, colorModel);
    }

    /**
     * Returns the bounds of the destination surface.
     */
    public abstract Rectangle getBounds();

    static java.security.Permission compPermission;

    /**
     * Performs Security Permissions checks to see if a Custom
     * Composite object should be allowed access to the pixels
     * of this surface.
     */
    protected void checkCustomComposite() {
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    if (compPermission == null) {
		compPermission =
		    new java.awt.AWTPermission("readDisplayPixels");
	    }
	    sm.checkPermission(compPermission);
	}
    }

    /**
     * Fetches private field IndexColorModel.allgrayopaque
     * which is true when all palette entries in the color
     * model are gray and opaque.
     */
    protected static native boolean isOpaqueGray(IndexColorModel icm);

    /**
     * For our purposes null and NullSurfaceData are the same as
     * they represent a disposed surface.
     */
    public static boolean isNull(SurfaceData sd) {
	if (sd == null || sd == NullSurfaceData.theInstance) {
	    return true;
	}
	return false;
    }

    /**
     * Performs a copyarea within this surface.  Returns
     * false if there is no algorithm to perform the copyarea
     * given the current settings of the SunGraphics2D.
     */
    public boolean copyArea(SunGraphics2D sg2d,
			    int x, int y, int w, int h, int dx, int dy)
    {
	return false;
    }

    /** 
     * Synchronously releases resources associated with this surface.
     */ 
    public void flush() {}

    /**
     * Returns destination associated with this SurfaceData.  This could be
     * either an Image or a Component; subclasses of SurfaceData are
     * responsible for returning the appropriate object.
     */
    public abstract Object getDestination();
}
