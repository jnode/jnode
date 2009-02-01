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
 
package org.jnode.awt.font.truetype.glyph;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import org.jnode.awt.font.spi.ShapedGlyph;
import org.jnode.awt.font.truetype.TTFInput;
import org.jnode.awt.font.truetype.tables.GlyphTable;


public class CompositeGlyph extends TTFGlyph {

    private static final int ARGS_WORDS = 0;
    private static final int ARGS_XY = 1;
    private static final int SCALE = 3;
    private static final int XY_SCALE = 6;
    private static final int TWO_BY_TWO = 7;
    private static final int MORE_COMPONENTS = 5;

    private GeneralPath shape;
    private int noComponents;
    private final GlyphTable table;

    public CompositeGlyph(TTFInput in, GlyphTable table) throws IOException {
        super(in);
        this.table = table;
        read(in);
    }

    public String getType() {
        return "Composite Glyph";
    }

    public GeneralPath getShape() {
        return shape;
    }

    private void read(TTFInput ttf) throws IOException {
        shape = new GeneralPath();

        noComponents = 0;
        boolean more = true;
        while (more) {
            noComponents++;
            ttf.readUShortFlags();
            more = ttf.flagBit(MORE_COMPONENTS);
            int glyphIndex = ttf.readUShort();
            int arg1, arg2;
            if (ttf.flagBit(ARGS_WORDS)) {
                arg1 = ttf.readShort();
                arg2 = ttf.readShort();
            } else {
                arg1 = ttf.readChar();
                arg2 = ttf.readChar();
            }
            AffineTransform t = new AffineTransform();
            if (ttf.flagBit(ARGS_XY)) {
                t.translate(arg1, arg2);
            } else {
                System.err.println("TTFGlyfTable: ARGS_ARE_POINTS not implemented.");
            }

            if (ttf.flagBit(SCALE)) {
                double scale = ttf.readF2Dot14();
                t.scale(scale, scale);
            } else if (ttf.flagBit(XY_SCALE)) {
                double scaleX = ttf.readF2Dot14();
                double scaleY = ttf.readF2Dot14();
                t.scale(scaleX, scaleY);
            } else if (ttf.flagBit(TWO_BY_TWO)) {
                System.err.println("TTFGlyfTable: WE_HAVE_A_TWO_BY_TWO not implemented.");
            }

            ShapedGlyph glyph = (ShapedGlyph) table.getGlyph(glyphIndex);
            GeneralPath appendGlyph = (GeneralPath) glyph.getShape().clone();
            appendGlyph.transform(t);
            shape.append(appendGlyph, false);
        }
    }

    public String toString() {
        return super.toString() + ", " + noComponents + " components";
    }

}
