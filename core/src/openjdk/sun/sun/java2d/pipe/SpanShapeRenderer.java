/*
 * Copyright 1998-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.java2d.pipe;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import sun.awt.SunHints;
import sun.dc.path.PathConsumer;
import sun.dc.path.PathException;

/**
 * This class is used to convert raw geometry into a span iterator
 * object using a simple flattening polygon scan converter.
 * The iterator can be passed on to special SpanFiller loops to
 * perform the actual rendering.
 */
public abstract class SpanShapeRenderer implements ShapeDrawPipe {
    public static class Composite extends SpanShapeRenderer {
	CompositePipe comppipe;

	public Composite(CompositePipe pipe) {
	    comppipe = pipe;
	}

	public Object startSequence(SunGraphics2D sg, Shape s,
				    Rectangle devR, int[] bbox) {
	    return comppipe.startSequence(sg, s, devR, bbox);
	}

	public void renderBox(Object ctx, int x, int y, int w, int h) {
	    comppipe.renderPathTile(ctx, null, 0, w, x, y, w, h);
	}

	public void endSequence(Object ctx) {
	    comppipe.endSequence(ctx);
	}
    }

    public static class Simple extends SpanShapeRenderer {
	public Object startSequence(SunGraphics2D sg, Shape s,
				    Rectangle devR, int[] bbox) {
	    return sg;
	}

	public void renderBox(Object ctx, int x, int y, int w, int h) {
	    SunGraphics2D sg2d = (SunGraphics2D) ctx;
	    SurfaceData sd = sg2d.getSurfaceData();
	    sg2d.loops.fillRectLoop.FillRect(sg2d, sd, x, y, w, h);
	}

	public void endSequence(Object ctx) {
	}
    }

    public void draw(SunGraphics2D sg, Shape s) {
	if (sg.stroke instanceof BasicStroke) {
	    ShapeSpanIterator sr = new ShapeSpanIterator(sg, true);
	    try {
		drawBasicStroke(sg, s, sr);
	    } finally {
		sr.dispose();
	    }
	} else {
	    renderPath(sg, sg.stroke.createStrokedShape(s));
	}
    }

    public void drawBasicStroke(SunGraphics2D sg, Shape s,
				ShapeSpanIterator sr)
    {
	Region clipRegion = sg.getCompClip();
	sr.setOutputArea(clipRegion);
	sr.setRule(PathIterator.WIND_NON_ZERO);

	BasicStroke bs = (BasicStroke) sg.stroke;
	AffineTransform transform =
	    (sg.transformState >= sg.TRANSFORM_TRANSLATESCALE
	     ? sg.transform : null);
	boolean thin = (sg.strokeState <= sg.STROKE_THINDASHED);

	PathConsumer stroker =
	    DuctusRenderer.createStroker(sr, bs, thin, transform);

	try {
	    transform =
		((sg.transformState != sg.TRANSFORM_ISIDENT)
		 ? sg.transform : null);
	    PathIterator pi = s.getPathIterator(transform);

	    boolean adjust =
		(sg.strokeHint != SunHints.INTVAL_STROKE_PURE);
	    DuctusRenderer.feedConsumer(pi, stroker, adjust, 0.25f);
	} catch (PathException e) {
	    throw new InternalError("Unable to Stroke shape ("+
				    e.getMessage()+")");
	} finally {
	    DuctusRenderer.disposeStroker(stroker, sr);
	}

	renderSpans(sg, clipRegion, s, sr);
    }

    public static final int NON_RECTILINEAR_TRANSFORM_MASK =
	(AffineTransform.TYPE_GENERAL_TRANSFORM |
	 AffineTransform.TYPE_GENERAL_ROTATION);

    public void fill(SunGraphics2D sg, Shape s) {
	if (s instanceof Rectangle2D &&
	    (sg.transform.getType() & NON_RECTILINEAR_TRANSFORM_MASK) == 0)
	{
	    renderRect(sg, (Rectangle2D) s);
	    return;
	}
	renderPath(sg, s);
    }

    public abstract Object startSequence(SunGraphics2D sg, Shape s,
					 Rectangle devR, int[] bbox);

    public abstract void renderBox(Object ctx, int x, int y, int w, int h);

    public abstract void endSequence(Object ctx);

    public void renderRect(SunGraphics2D sg, Rectangle2D r) {
	double corners[] = {
	    r.getX(), r.getY(), r.getWidth(), r.getHeight(),
	};
	corners[2] += corners[0];
	corners[3] += corners[1];
	if (corners[2] <= corners[0] || corners[3] <= corners[1]) {
	    return;
	}
	sg.transform.transform(corners, 0, corners, 0, 2);
	if (corners[2] < corners[0]) {
	    double t = corners[2];
	    corners[2] = corners[0];
	    corners[0] = t;
	}
	if (corners[3] < corners[1]) {
	    double t = corners[3];
	    corners[3] = corners[1];
	    corners[1] = t;
	}
	int abox[] = {
	    (int) corners[0],
	    (int) corners[1],
	    (int) corners[2],
	    (int) corners[3],
	};
	Rectangle devR = new Rectangle(abox[0], abox[1],
				       abox[2] - abox[0],
				       abox[3] - abox[1]);
	Region clipRegion = sg.getCompClip();
	clipRegion.clipBoxToBounds(abox);
	if (abox[0] >= abox[2] || abox[1] >= abox[3]) {
	    return;
	}
	Object context = startSequence(sg, r, devR, abox);
	if (clipRegion.isRectangular()) {
	    renderBox(context, abox[0], abox[1],
		      abox[2] - abox[0],
		      abox[3] - abox[1]);
	} else {
	    SpanIterator sr = clipRegion.getSpanIterator(abox);
	    while (sr.nextSpan(abox)) {
		renderBox(context, abox[0], abox[1],
			      abox[2] - abox[0],
			      abox[3] - abox[1]);
	    }
	}
	endSequence(context);
    }

    public void renderPath(SunGraphics2D sg, Shape s) {
	Region clipRegion = sg.getCompClip();
	
	ShapeSpanIterator sr = new ShapeSpanIterator(sg, false);
	try {
	    sr.setOutputArea(clipRegion);
	    sr.appendPath(s.getPathIterator(sg.transform));
	    renderSpans(sg, clipRegion, s, sr);
	} finally {
	    sr.dispose();
	}
    }

    public void renderSpans(SunGraphics2D sg, Region clipRegion, Shape s,
			    ShapeSpanIterator sr)
    {
	Object context = null;
	int abox[] = new int[4];
	try {
	    sr.getPathBox(abox);
	    Rectangle devR = new Rectangle(abox[0], abox[1],
					   abox[2] - abox[0],
					   abox[3] - abox[1]);
	    clipRegion.clipBoxToBounds(abox);
	    if (abox[0] >= abox[2] || abox[1] >= abox[3]) {
		return;
	    }
	    sr.intersectClipBox(abox[0], abox[1], abox[2], abox[3]);
	    context = startSequence(sg, s, devR, abox);

	    spanClipLoop(context, sr, clipRegion, abox);

	} finally {
	    if (context != null) {
		endSequence(context);
	    }
	}
    }

    public void spanClipLoop(Object ctx, SpanIterator sr,
			     Region r, int[] abox) {
	if (!r.isRectangular()) {
	    sr = r.filter(sr);
	}
	while (sr.nextSpan(abox)) {
	    int x = abox[0];
	    int y = abox[1];
	    renderBox(ctx, x, y, abox[2] - x, abox[3] - y);
	}
    }
}
