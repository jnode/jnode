/*
 * Copyright 1999-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.java2d.loops;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Composite;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;

/**
 * MaskFill
 * 1) fills rectangles of pixels on a surface
 * 2) performs compositing of colors based upon a Composite
 *    parameter
 * 3) blends result of composite with destination using an
 *    alpha coverage mask
 * 4) the mask may be null in which case it should be treated
 *    as if it were an array of all opaque values (0xff)
 */
public class MaskFill extends GraphicsPrimitive
{
    public static final String methodSignature = "MaskFill(...)".toString();

    public static final int primTypeID = makePrimTypeID(); 

    private static RenderCache fillcache = new RenderCache(10);

    public static MaskFill locate(SurfaceType srctype,
				  CompositeType comptype,
				  SurfaceType dsttype)
    {
	return (MaskFill)
	    GraphicsPrimitiveMgr.locate(primTypeID,
					srctype, comptype, dsttype);
    }

    public static MaskFill locatePrim(SurfaceType srctype,
				      CompositeType comptype,
				      SurfaceType dsttype)
    {
	return (MaskFill)
	    GraphicsPrimitiveMgr.locatePrim(primTypeID,
					    srctype, comptype, dsttype);
    }

    /*
     * Note that this uses locatePrim, not locate, so it can return
     * null if there is no specific loop to handle this op...
     */
    public static MaskFill getFromCache(SurfaceType src,
					CompositeType comp,
					SurfaceType dst)
    {
	Object o = fillcache.get(src, comp, dst);
	if (o != null) {
	    return (MaskFill) o;
	}
	MaskFill fill = locatePrim(src, comp, dst);
	if (fill != null) {
	    fillcache.put(src, comp, dst, fill);
	}
	return fill;
    }

    protected MaskFill(SurfaceType srctype,
		       CompositeType comptype,
		       SurfaceType dsttype)
    {
	super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public MaskFill(long pNativePrim,
		    SurfaceType srctype,
		    CompositeType comptype,
		    SurfaceType dsttype)
    {
	super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    /**
     * All MaskFill implementors must have this invoker method
     */
    public native void MaskFill(SunGraphics2D sg2d, SurfaceData sData,
				Composite comp,
				int x, int y, int w, int h,
				byte[] mask, int maskoff, int maskscan);

    static {
	GraphicsPrimitiveMgr.registerGeneral(new MaskFill(null, null, null));
    }

    public GraphicsPrimitive makePrimitive(SurfaceType srctype,
					   CompositeType comptype,
					   SurfaceType dsttype)
    {
	if (SurfaceType.OpaqueColor.equals(srctype) ||
	    SurfaceType.AnyColor.equals(srctype))
	{
            if (CompositeType.Xor.equals(comptype)) {
                throw new InternalError("Cannot construct MaskFill for " +
                                        "XOR mode");
            } else {
                return new General(srctype, comptype, dsttype);
            }
	} else {
	    throw new InternalError("MaskFill can only fill with colors");
	}
    }

    private static class General extends MaskFill {
	FillRect fillop;
	MaskBlit maskop;

	public General(SurfaceType srctype,
		       CompositeType comptype,
		       SurfaceType dsttype)
	{
	    super(srctype, comptype, dsttype);
	    fillop = FillRect.locate(srctype,
				     CompositeType.SrcNoEa,
				     SurfaceType.IntArgb);
	    maskop = MaskBlit.locate(SurfaceType.IntArgb, comptype, dsttype);
	}

	public void MaskFill(SunGraphics2D sg2d,
			     SurfaceData sData,
			     Composite comp,
			     int x, int y, int w, int h,
			     byte mask[], int offset, int scan)
	{
	    BufferedImage dstBI =
		new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    SurfaceData tmpData = BufImgSurfaceData.createData(dstBI);

	    // REMIND: This is not pretty.  It would be nicer if we
	    // passed a "FillData" object to the Pixel loops, instead
	    // of a SunGraphics2D parameter...
	    int pixel = sg2d.pixel;
	    sg2d.pixel = tmpData.pixelFor(sg2d.getColor());
	    fillop.FillRect(sg2d, tmpData, 0, 0, w, h);
	    sg2d.pixel = pixel;

	    maskop.MaskBlit(tmpData, sData, comp, null,
			    0, 0, x, y, w, h,
			    mask, offset, scan);
	}
    }

    public GraphicsPrimitive traceWrap() {
	return new TraceMaskFill(this);
    }

    private static class TraceMaskFill extends MaskFill {
	MaskFill target;

	public TraceMaskFill(MaskFill target) {
	    super(target.getSourceType(),
		  target.getCompositeType(),
		  target.getDestType());
	    this.target = target;
	}

	public GraphicsPrimitive traceWrap() {
	    return this;
	}

	public void MaskFill(SunGraphics2D sg2d, SurfaceData sData,
			     Composite comp,
			     int x, int y, int w, int h,
			     byte[] mask, int maskoff, int maskscan)
	{
	    tracePrimitive(target);
	    target.MaskFill(sg2d, sData, comp, x, y, w, h,
			    mask, maskoff, maskscan);
	}
    }
}
