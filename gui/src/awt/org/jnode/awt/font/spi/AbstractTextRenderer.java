/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.awt.font.spi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.WritableRaster;
import org.apache.log4j.Logger;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.renderer.GlyphRenderer;
import org.jnode.awt.font.renderer.RenderCache;
import org.jnode.awt.font.renderer.RenderContext;
import org.jnode.driver.video.Surface;
import org.jnode.vm.Vm;

/**
 * @author epr
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public abstract class AbstractTextRenderer implements TextRenderer {

    /**
     * My logger
     */
    protected static final Logger log = Logger.getLogger(AbstractTextRenderer.class);

    protected final FontMetrics fontMetrics;

    protected final RenderCache renderCache;

    protected final FontData fontData;

    /**
     * Key of the alpha raster in the render context
     */
    protected static final String ALPHA_RASTER = AbstractTextRenderer.class.getName() + "AR";

    /**
     * Create a new instance
     *
     * @param fontData
     * @param fontSize
     */
    public AbstractTextRenderer(RenderCache renderCache, FontMetrics fontMetrics,
                                FontData fontData) {
        this.renderCache = renderCache;
        this.fontMetrics = fontMetrics;
        this.fontData = fontData;
    }

    /**
     * Create/get the alpha raster used for rendering.
     *
     * @return
     */
    protected final WritableRaster createAlphaRaster() {
        final RenderContext ctx = renderCache.getContext();
        WritableRaster r = (WritableRaster) ctx.getObject(ALPHA_RASTER);
        final int fontSizeUp = (int) (fontMetrics.getFont().getSize() + 0.5);
        if ((r == null) || (r.getWidth() < fontSizeUp) || (r.getHeight() < fontSizeUp)) {
            r = GlyphRenderer.createRaster(fontSizeUp, fontSizeUp);
            ctx.setObject(ALPHA_RASTER, r);
            Vm.getVm().getCounter(ALPHA_RASTER).inc();
        }
        return r;
    }

    /**
     * Render a given text to the given graphics at the given location.
     *
     * @param g
     * @param text
     * @param x
     * @param y
     */
    public void render(Surface surface, Shape clip, AffineTransform tx,
                       CharSequence text, int x, int y, Color color) {
        try {
            final double ascent = fontMetrics.getAscent();
            final int fontSize = fontMetrics.getFont().getSize();
            final double scale = fontSize / ascent;

            final int textLength = text.length();
            final WritableRaster alphaRaster = createAlphaRaster();
            for (int i = 0; i < textLength; i++) {
                final char ch = text.charAt(i);
                if (ch != ' ') {
                    final Glyph g = fontData.getGlyph(ch);
                    final GlyphRenderer renderer = renderCache.getRenderer(g,
                        ascent);
                    final Dimension d;
                    d = renderer.createGlyphRaster(alphaRaster, fontSize);

                    final Point2D minLoc = renderer.getMinLocation(fontSize);
                    final int dstX = x + (int) minLoc.getX();
                    final int dstY = y - d.height + (int) minLoc.getY();

                    surface.drawAlphaRaster(alphaRaster, tx, 0, 0, dstX, dstY,
                        d.width, d.height, color);
                }
                x += fontMetrics.charWidth(ch);
            }
        } catch (Exception ex) {
            log.error("Error drawing text", ex);
        }
    }
}
