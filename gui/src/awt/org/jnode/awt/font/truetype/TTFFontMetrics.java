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

import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import org.jnode.awt.font.truetype.tables.CMapTable;
import org.jnode.awt.font.truetype.tables.HorizontalMetricsTable;

/**
 * @author epr
 */
public class TTFFontMetrics extends FontMetrics {

    private final TTFFontData fontData;
    private final double scale;
    private final int fontSize;

    /**
     * @param font
     * @param fontData
     * @throws IOException
     */
    public TTFFontMetrics(Font font, TTFFontData fontData)
        throws IOException {
        super(font);
        if (font == null) {
            throw new IllegalArgumentException("font cannot be null");
        }
        if (fontData == null) {
            throw new IllegalArgumentException("fontData cannot be null");
        }
        this.fontData = fontData;
        this.fontSize = font.getSize();
        final double ascent = fontData.getHorizontalHeaderTable().getAscent();
        this.scale = fontSize / ascent;
        //System.out.println("Font=" + font.getName() + ", size=" + fontSize + ",
        // scale=" + scale + ", ascent=" + ascent);
    }

    /**
     * @param ch
     * @return The width
     * @see java.awt.FontMetrics#charWidth(char)
     */
    public int charWidth(char ch) {
        try {
            final CMapTable cmapTable = fontData.getCMapTable();
            final CMapTable.EncodingTable encTable = cmapTable.getEncodingTable(0);
            final HorizontalMetricsTable hmTable = fontData.getHorizontalMetricsTable();
            final int index = encTable.getTableFormat().getGlyphIndex(ch);
            return (int) (hmTable.getAdvanceWidth(index) * scale);
        } catch (IOException ex) {
            return 0;
        }
    }

    /**
     * @return The ascent
     * @see java.awt.FontMetrics#getAscent()
     */
    public int getAscent() {
        try {
            final int ascent = (int) (fontData.getHorizontalHeaderTable().getAscent() * scale);
            return ascent;
        } catch (IOException ex) {
            return 0;
        }
    }

    /**
     * @return The descent
     * @see java.awt.FontMetrics#getDescent()
     */
    public int getDescent() {
        try {
            final int descent = Math.abs((int) (fontData.getHorizontalHeaderTable().getDescent() * scale));
            return descent;
        } catch (IOException ex) {
            return 0;
        }
    }

    /**
     * @return The maximum advance
     * @see java.awt.FontMetrics#getMaxAdvance()
     */
    public int getMaxAdvance() {
        try {
            return (int) (fontData.getHorizontalHeaderTable().getMaxAdvance() * scale);
        } catch (IOException ex) {
            return 0;
        }
    }

    /**
     * @return The maximum ascent
     * @see java.awt.FontMetrics#getMaxAscent()
     */
    public int getMaxAscent() {
        return getAscent();
    }

    /**
     * @return The maximum descent
     * @see java.awt.FontMetrics#getMaxDescent()
     */
    public int getMaxDescent() {
        return getDescent();
    }
}
