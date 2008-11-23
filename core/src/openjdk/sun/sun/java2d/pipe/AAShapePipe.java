/*
 * Copyright 1997-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import sun.awt.SunHints;
import sun.java2d.SunGraphics2D;

/**
 * This class is used to convert raw geometry into 8-bit alpha tiles
 * using an AATileGenerator for application by the next stage of
 * the pipeline.
 * This class sets up the Generator and computes the alpha tiles
 * and then passes them on to a CompositePipe object for painting.
 */
public class AAShapePipe implements ShapeDrawPipe {
    static RenderingEngine renderengine = RenderingEngine.getInstance();

    CompositePipe outpipe;

    public AAShapePipe(CompositePipe pipe) {
        outpipe = pipe;
    }

    public void draw(SunGraphics2D sg, Shape s) {
        BasicStroke bs;

        if (sg.stroke instanceof BasicStroke) {
            bs = (BasicStroke) sg.stroke;
        } else {
            s = sg.stroke.createStrokedShape(s);
            bs = null;
        }

        renderPath(sg, s, bs);
    }

    public void fill(SunGraphics2D sg, Shape s) {
        renderPath(sg, s, null);
    }

    private static byte[] theTile;

    public synchronized static byte[] getAlphaTile(int len) {
        byte[] t = theTile;
        if (t == null || t.length < len) {
            t = new byte[len];
        } else {
            theTile = null;
        }
        return t;
    }

    public synchronized static void dropAlphaTile(byte[] t) {
        theTile = t;
    }

    public void renderPath(SunGraphics2D sg, Shape s, BasicStroke bs) {
        boolean adjust = (bs != null &&
                          sg.strokeHint != SunHints.INTVAL_STROKE_PURE);
        boolean thin = (sg.strokeState <= sg.STROKE_THINDASHED);
        Object context = null;
        byte alpha[] = null;

        Region clip = sg.getCompClip();
        int abox[] = new int[4];
        AATileGenerator aatg =
            renderengine.getAATileGenerator(s, sg.transform, clip,
                                            bs, thin, adjust, abox);
        if (aatg == null) {
            // Nothing to render
            return;
        }

        try {
            context = outpipe.startSequence(sg, s,
                                            new Rectangle(abox[0], abox[1],
                                                          abox[2] - abox[0],
                                                          abox[3] - abox[1]),
                                            abox);

            int tw = aatg.getTileWidth();
            int th = aatg.getTileHeight();
            alpha = getAlphaTile(tw * th);

            byte[] atile;

            for (int y = abox[1]; y < abox[3]; y += th) {
                for (int x = abox[0]; x < abox[2]; x += tw) {
                    int w = Math.min(tw, abox[2] - x);
                    int h = Math.min(th, abox[3] - y);

                    int a = aatg.getTypicalAlpha();
                    if (a == 0x00 ||
                        outpipe.needTile(context, x, y, w, h) == false)
                    {
                        aatg.nextTile();
                        outpipe.skipTile(context, x, y);
                        continue;
                    }
                    if (a == 0xff) {
                        atile = null;
                        aatg.nextTile();
                    } else {
                        atile = alpha;
                        aatg.getAlpha(alpha, 0, tw);
                    }

                    outpipe.renderPathTile(context, atile, 0, tw,
                                           x, y, w, h);
                }
            }
        } finally {
            aatg.dispose();
            if (context != null) {
                outpipe.endSequence(context);
            }
            if (alpha != null) {
                dropAlphaTile(alpha);
            }
        }
    }
}
