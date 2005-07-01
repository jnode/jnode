/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.awt.font.truetype;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.Raster;

import org.apache.log4j.Logger;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.renderer.GlyphRenderer;
import org.jnode.awt.font.truetype.glyph.Glyph;
import org.jnode.awt.font.truetype.tables.CMapTable;
import org.jnode.awt.font.truetype.tables.GlyphTable;
import org.jnode.awt.font.truetype.tables.HorizontalHeaderTable;
import org.jnode.awt.font.truetype.tables.HorizontalMetricsTable;
import org.jnode.driver.video.Surface;

/**
 * @author epr
 */
public class TTFTextRenderer implements TextRenderer {

    /** My logger */
    private static final Logger log = Logger.getLogger(TTFTextRenderer.class);

    private final TTFFontData fontData;

    private final double fontSize;

    private final RenderCache renderCache;

    /**
     * Create a new instance
     * 
     * @param fontData
     * @param fontSize
     */
    public TTFTextRenderer(RenderCache renderCache, TTFFontData fontData,
            int fontSize) {
        this.renderCache = renderCache;
        this.fontData = fontData;
        this.fontSize = fontSize;
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
            String text, int x, int y, Color color) {
        try {
            final GlyphTable glyphTable = fontData.getGlyphTable();
            final CMapTable cmapTable = fontData.getCMapTable();

            if (!(cmapTable.getNrEncodingTables() > 0)) {
                throw new RuntimeException("No Encoding is found!");
            }
            final CMapTable.EncodingTable encTable = cmapTable
                    .getEncodingTable(0);
            if (encTable.getTableFormat() == null) {
                throw new RuntimeException("The table is NUll!!");
            }
            final HorizontalHeaderTable hheadTable = fontData
                    .getHorizontalHeaderTable();
            final double ascent = hheadTable.getAscent();
            final HorizontalMetricsTable hmTable = fontData
                    .getHorizontalMetricsTable();
            final double scale = fontSize / ascent;
//            System.out.println("scale=" + scale);

            for (int i = 0; i < text.length(); i++) {
                // get the index for the needed glyph
                final int index = encTable.getTableFormat().getGlyphIndex(
                        text.charAt(i));
                final Glyph g = glyphTable.getGlyph(index);
                final GlyphRenderer renderer = renderCache.getRenderer(g,
                        ascent);
                final Raster alphaRaster = renderer
                        .createGlyphRaster((int) fontSize);
                final int w = alphaRaster.getWidth();
                final int h = alphaRaster.getHeight();

                final Point2D minLoc = renderer.getMinLocation((int) fontSize);
                final double dstX = x + minLoc.getX();
                final double dstY = y - fontSize + minLoc.getY();

                surface.drawAlphaRaster(alphaRaster, tx, 0, 0, (int) dstX,
                        (int) dstY, w, h, color);
                final double aw = hmTable.getAdvanceWidth(index);
//                System.out.println("idx=" + index + " x=" + x + " w=" + w
//                        + " aw=" + aw + " aw*sc=" + (aw * scale));
                x += (aw * scale);
//                x += alphaRaster.getWidth();
                System.out.println("aw*sc=" + (aw * scale) + " ar.w=" + alphaRaster.getWidth());
            }
        } catch (Exception ex) {
            log.error("Error drawing text", ex);
        }
    }
}
