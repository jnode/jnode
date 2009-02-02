/*
 * $Id$
 *
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
import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFInput;

/**
 * POST Table.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public final class PostTable extends TTFTable {

    public double format;

    public double italicAngle;

    public short underlinePosition, underlineThickness;

    public long isFixedPitch;

    public long minMemType42, maxMemType42, minMemType1, maxMemType1;

    public int[] glyphNameIndex;

    /**
     * @param font
     * @param input
     */
    PostTable(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    public String getTag() {
        return "post";
    }

    public void readTable(TTFInput in) throws IOException {
        format = in.readFixed();

        italicAngle = in.readFixed();

        underlinePosition = in.readFWord();
        underlineThickness = in.readFWord();

        isFixedPitch = in.readULong();

        minMemType42 = in.readULong();
        maxMemType42 = in.readULong();
        minMemType1 = in.readULong();
        maxMemType1 = in.readULong();

        if (format == 2.0) {
            glyphNameIndex = in.readUShortArray(in.readUShort());
        } else if (format == 2.5) {
            System.err.println("Format 2.5 for post notimplemented yet.");
        }
    }

    public String toString() {
        String str = super.toString() + " format: " + format + "\n  italic:" +
            italicAngle + " ulPos:" + underlinePosition + " ulThick:" +
            underlineThickness + " isFixed:" + isFixedPitch;
        if (glyphNameIndex != null) {
            str += "\n  glyphNamesIndex[" + glyphNameIndex.length + "] = {";
            for (int i = 0; i < glyphNameIndex.length; i++) {
                if (i % 16 == 0)
                    str += "\n    ";
                str += glyphNameIndex[i] + " ";
            }
            str += "\n  }";
        }
        return str;
    }
}
