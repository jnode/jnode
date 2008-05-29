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

import java.awt.Rectangle;
import java.io.IOException;
import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFInput;

/**
 * HEAD Table.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public final class HeadTable extends VersionTable {

    public static final int FDH_MIXED = 0;
    public static final int FDH_LEFT_TO_RIGHT = 1;
    public static final int FDH_LEFT_TO_RIGHT_NEUTRAL = 2;
    public static final int FDH_RIGHT_TO_LEFT = -1;
    public static final int FDH_RIGHT_TO_LEFT_NEUTRAL = -2;

    public static final int ITLF_SHORT = 0;
    public static final int ITLF_LONG = 1;

    public int fontRevisionMinor, fontRevisionMajor;

    public long checkSumAdjustment;
    public long magicNumber;
    public boolean baseline0, sidebearing0, instrDependOnSize, forcePPEM2Int, instrAlterAdvance;
    public int unitsPerEm;
    public byte[] created = new byte[8];
    public byte[] modified = new byte[8];
    public short xMin, yMin, xMax, yMax;
    public boolean macBold, macItalic;
    public int lowestRecPPEM;
    public short fontDirectionHint;
    public short indexToLocFormat, glyphDataFormat;

    /**
     * @param font
     * @param input
     */
    HeadTable(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    public String getTag() {
        return "head";
    }

    public void readTable(TTFInput in) throws IOException {
        readVersion(in);

        fontRevisionMajor = in.readUShort();
        fontRevisionMinor = in.readUShort();

        checkSumAdjustment = in.readULong();
        magicNumber = in.readULong();

        in.readUShortFlags(); // flags
        baseline0 = in.flagBit(0);
        sidebearing0 = in.flagBit(1);
        instrDependOnSize = in.flagBit(2);
        forcePPEM2Int = in.flagBit(3);
        instrAlterAdvance = in.flagBit(4);

        unitsPerEm = in.readUShort();

        in.readFully(created);
        in.readFully(modified);

        xMin = in.readShort();
        yMin = in.readShort();
        xMax = in.readShort();
        yMax = in.readShort();

        in.readUShortFlags(); // macstyle
        macBold = in.flagBit(0);
        macItalic = in.flagBit(1);

        lowestRecPPEM = in.readUShort();
        fontDirectionHint = in.readShort();
        indexToLocFormat = in.readShort();
        if ((indexToLocFormat != ITLF_LONG) && (indexToLocFormat != ITLF_SHORT))
            System.err.println("Unknown value for indexToLocFormat: " + indexToLocFormat);
        glyphDataFormat = in.readShort();
    }

    public String toString() {
        String str = super.toString() + "\n" + "  magicNumber: 0x" + Integer.toHexString((int) magicNumber) + " (" +
            (magicNumber == 0x5f0f3cf5 ? "ok" : "wrong") + ")\n";
        str += "  indexToLocFormat: " + indexToLocFormat + " ";
        if (indexToLocFormat == ITLF_LONG)
            str += " (long)\n";
        else if (indexToLocFormat == ITLF_SHORT)
            str += "(short)\n";
        else
            str += "(illegal value)\n";
        str += "  bbox: (" + xMin + "," + yMin + ") : (" + xMax + "," + yMax + ")";
        return str;
    }

    public Rectangle getMaxCharBounds() {
        return new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
    }
}
