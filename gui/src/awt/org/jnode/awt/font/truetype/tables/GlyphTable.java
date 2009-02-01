/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.awt.font.truetype.tables;

import java.io.IOException;
import org.jnode.awt.font.spi.Glyph;
import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFInput;
import org.jnode.awt.font.truetype.glyph.CompositeGlyph;
import org.jnode.awt.font.truetype.glyph.SimpleGlyph;

/**
 * GLYPH Table.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public final class GlyphTable extends VersionTable {

    /**
     * If this variable is set to false then the glyphs will not be read
     * until they are retrieved with <tt>getGlyph(int)</tt>.
     */
    private static final boolean READ_GLYPHS = false;

    private Glyph[] glyphs;

    private long[] offsets;

    /**
     * @param font
     * @param input
     */
    GlyphTable(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    public String getTag() {
        return "glyf";
    }

    protected final void readTable(TTFInput ttf) throws IOException {
        final TTFFontData font = getFont();
        glyphs = new Glyph[font.getMaxPTable().numGlyphs];
        offsets = font.getLocationsTable().offset;

        if (READ_GLYPHS) {
            for (int i = 0; i < glyphs.length; i++) {
                if ((i > 0) && (offsets[i - 1] == offsets[i])) {
                    glyphs[i] = glyphs[i - 1];
                } else {
                    try {
                        getGlyph(i);
                    } catch (IOException e) {
                        System.err.println("While reading glyph #" + i + " (offset " + offsets[i] + "):");
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public Glyph getGlyph(int i) throws IOException {
        if (glyphs[i] != null) {
            return glyphs[i];
        } else {
            final TTFInput in = getInput();
            in.pushPos();
            in.seek(offsets[i]);
            int numberOfContours = in.readShort();
            if (numberOfContours >= 0) {
                glyphs[i] = new SimpleGlyph(in, numberOfContours);
            } else {
                glyphs[i] = new CompositeGlyph(in, this);
            }
            in.popPos();
            return glyphs[i];
        }
    }

    public String toString() {
        String str = super.toString();
        for (int i = 0; i < glyphs.length; i++) {
            str += "\n  #" + i + ": " + glyphs[i];
        }
        return str;
    }
}
