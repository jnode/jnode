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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.spi.ShapedGlyph;
import org.jnode.awt.font.truetype.tables.CMapTable;
import org.jnode.awt.font.truetype.tables.GlyphTable;
import org.jnode.awt.font.truetype.tables.HorizontalHeaderTable;
import org.jnode.awt.font.truetype.tables.HorizontalMetricsTable;
import org.jnode.driver.video.Surface;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TTFSimpleTextRenderer implements TextRenderer {

    /**
     * My logger
     */
    private final Logger log = Logger.getLogger(getClass());

    private final TTFFontData fontData;

    private final double fontSize;

    /**
     * Create a new instance
     *
     * @param fontData
     * @param fontSize
     */
    public TTFSimpleTextRenderer(TTFFontData fontData, int fontSize) {
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
    public void render(Surface sf, Shape clip, AffineTransform tx2, CharSequence text, int x, int y, Color c) {
        try {
            final GeneralPath gp = new GeneralPath();
            gp.moveTo(x, y);

            final GlyphTable glyphTable = fontData.getGlyphTable();
            final CMapTable cmapTable = fontData.getCMapTable();
            final HorizontalHeaderTable hheadTable = fontData
                .getHorizontalHeaderTable();
            final HorizontalMetricsTable hmTable = fontData
                .getHorizontalMetricsTable();

            if (!(cmapTable.getNrEncodingTables() > 0)) {
                throw new RuntimeException("No Encoding is found!");
            }
            final CMapTable.EncodingTable encTable = cmapTable
                .getEncodingTable(0);
            if (encTable.getTableFormat() == null) {
                throw new RuntimeException("The table is NUll!!");
            }
            final double ascent = hheadTable.getAscent();

            final AffineTransform tx = new AffineTransform();
            final double scale = fontSize / ascent;

            tx.translate(x, y + fontSize);
            tx.scale(scale, -scale);
            tx.translate(0, ascent);

            for (int i = 0; i < text.length(); i++) {
                // get the index for the needed glyph
                final char character = text.charAt(i);
                final int index = encTable.getTableFormat().getGlyphIndex(character);
                if (character != ' ') {
                    Shape shape = ((ShapedGlyph) glyphTable.getGlyph(index)).getShape();
                    gp.append(shape.getPathIterator(tx), false);
                }
                tx.translate(hmTable.getAdvanceWidth(index), 0);
            }
            sf.draw(gp, clip, tx2, c, Surface.PAINT_MODE);
        } catch (IOException ex) {
            log.error("Error drawing text", ex);
        }
    }
}
