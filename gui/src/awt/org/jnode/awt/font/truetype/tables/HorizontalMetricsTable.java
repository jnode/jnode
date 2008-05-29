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

package org.jnode.awt.font.truetype.tables;

import java.io.IOException;
import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFInput;

/**
 * HMTX Table.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public final class HorizontalMetricsTable extends TTFTable {

    private int[] advanceWidth;
    private short[] leftSideBearing;
    private short[] leftSideBearing2;


    /**
     * @param font
     * @param input
     */
    HorizontalMetricsTable(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    public String getTag() {
        return "hmtx";
    }

    protected void readTable(TTFInput ttf) throws IOException {
        final TTFFontData font = getFont();
        final int numberOfHMetrics = font.getHorizontalHeaderTable().getNumberOfHMetrics();
        final int numGlyphs = font.getMaxPTable().numGlyphs;

        advanceWidth = new int[numberOfHMetrics];
        leftSideBearing = new short[numberOfHMetrics];
        for (int i = 0; i < numberOfHMetrics; i++) {
            advanceWidth[i] = ttf.readUFWord();
            leftSideBearing[i] = ttf.readFWord();
        }

        leftSideBearing2 = ttf.readShortArray(numGlyphs - numberOfHMetrics);
    }

    public String toString() {
        String str = super.toString();
        str += "\n  hMetrics[" + advanceWidth.length + "] = {";
        for (int i = 0; i < advanceWidth.length; i++) {
            if (i % 8 == 0)
                str += "\n    ";
            str += "(" + advanceWidth[i] + "," + leftSideBearing[i] + ") ";
        }
        str += "\n  }";
        str += "\n  lsb[" + leftSideBearing2.length + "] = {";
        for (int i = 0; i < leftSideBearing2.length; i++) {
            if (i % 16 == 0)
                str += "\n    ";
            str += leftSideBearing2[i] + " ";
        }
        str += "\n  }";
        return str;
    }

    /**
     * @param index
     * @return The advance width for a given index
     */
    public int getAdvanceWidth(int index) {
        return this.advanceWidth[index];
    }

    /**
     * @param index
     * @return The left side bearing for a given index
     */
    public int getLeftSideBearing(int index) {
        return this.leftSideBearing[index];
    }

}
