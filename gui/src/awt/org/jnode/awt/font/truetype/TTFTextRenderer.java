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

package org.jnode.awt.font.truetype;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.WritableRaster;
import org.jnode.awt.font.renderer.GlyphRenderer;
import org.jnode.awt.font.renderer.RenderCache;
import org.jnode.awt.font.spi.AbstractTextRenderer;
import org.jnode.awt.font.spi.FontData;
import org.jnode.awt.font.spi.Glyph;
import org.jnode.awt.font.truetype.tables.CMapTable;
import org.jnode.awt.font.truetype.tables.GlyphTable;
import org.jnode.awt.font.truetype.tables.HorizontalHeaderTable;
import org.jnode.awt.font.truetype.tables.HorizontalMetricsTable;
import org.jnode.driver.video.Surface;

/**
 * @author epr
 */
public class TTFTextRenderer extends AbstractTextRenderer {
    /**
     * Create a new instance
     *
     * @param renderCache
     * @param fontMetrics
     * @param fontData
     */
    public TTFTextRenderer(RenderCache renderCache, FontMetrics fontMetrics,
                           FontData fontData) {
        super(renderCache, fontMetrics, fontData);
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
            final TTFFontData fd = (TTFFontData) fontData;
            final int fontSize = fontMetrics.getFont().getSize();
            final GlyphTable glyphTable = fd.getGlyphTable();
            final CMapTable cmapTable = fd.getCMapTable();

            if (!(cmapTable.getNrEncodingTables() > 0)) {
                throw new RuntimeException("No Encoding is found!");
            }
            final CMapTable.EncodingTable encTable = cmapTable
                .getEncodingTable(0);
            if (encTable.getTableFormat() == null) {
                throw new RuntimeException("The table is NUll!!");
            }
            final HorizontalHeaderTable hheadTable = fd
                .getHorizontalHeaderTable();
            final double ascent = hheadTable.getAscent();
            final HorizontalMetricsTable hmTable = fd
                .getHorizontalMetricsTable();
            final double scale = fontSize / ascent;

            final int textLength = text.length();
            final WritableRaster alphaRaster = createAlphaRaster();
            for (int i = 0; i < textLength; i++) {
                // get the index for the needed glyph
                final char ch = text.charAt(i);
                final int index = encTable.getTableFormat().getGlyphIndex(ch);
                if (ch != ' ') {
                    final Glyph g = glyphTable.getGlyph(index);
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
                x += (scale * (double) hmTable.getAdvanceWidth(index));
            }
        } catch (Exception ex) {
            log.error("Error drawing text", ex);
        }
    }
}
