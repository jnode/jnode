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
import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFInput;

/**
 * LOCA Table.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public final class LocationsTable extends TTFTable {

    public long offset[];

    /**
     * @param font
     * @param input
     */
    LocationsTable(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    public String getTag() {
        return "loca";
    }

    protected void readTable(TTFInput ttf) throws IOException {
        final TTFFontData font = getFont();
        final short format = font.getHeaderTable().indexToLocFormat;
        final int numGlyphs = font.getMaxPTable().numGlyphs + 1;
        offset = new long[numGlyphs];
        for (int i = 0; i < numGlyphs; i++) {
            offset[i] = (format == HeadTable.ITLF_LONG ? ttf.readULong() : ttf.readUShort() * 2);
        }
    }

    public String toString() {
        String str = super.toString();
        for (int i = 0; i < offset.length; i++) {
            if (i % 16 == 0)
                str += "\n  ";
            str += offset[i] + " ";
        }
        return str;
    }
}
