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
 * OS/2 Table.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public final class OS2Table extends VersionTable {

    private int version;
    short xAvgCharWidth;
    int usWeightClass, usWidthClass;
    short fsType;
    short ySubscriptXSize, ySubscriptYSize, ySubscriptXOffset, ySubscriptYOffset;
    short ySuperscriptXSize, ySuperscriptYSize, ySuperscriptXOffset, ySuperscriptYOffset;
    short yStrikeoutSize, yStrikeoutPosition;
    short sFamilyClass;
    private byte[] panose = new byte[10];
    private long[] ulUnicode = new long[4];
    private byte[] achVendID = new byte[4];
    private int fsSelection;
    int usFirstCharIndex, usLastCharIndes;
    int sTypoAscender, sTzpoDescender, sTypoLineGap;
    int usWinAscent, usWinDescent;
    private long[] ulCodePageRange = new long[2];

    /**
     * @param font
     * @param input
     */
    OS2Table(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    public String getTag() {
        return "OS/2";
    }

    protected void readTable(TTFInput in) throws IOException {

        version = in.readUShort();
        xAvgCharWidth = in.readShort();
        usWeightClass = in.readUShort();
        usWidthClass = in.readUShort();
        fsType = in.readShort();

        ySubscriptXSize = in.readShort();
        ySubscriptYSize = in.readShort();
        ySubscriptXOffset = in.readShort();
        ySubscriptYOffset = in.readShort();
        ySuperscriptXSize = in.readShort();
        ySuperscriptYSize = in.readShort();
        ySuperscriptXOffset = in.readShort();
        ySuperscriptYOffset = in.readShort();
        yStrikeoutSize = in.readShort();
        yStrikeoutPosition = in.readShort();

        sFamilyClass = in.readShort();

        in.readFully(panose);

        for (int i = 0; i < ulUnicode.length; i++) {
            ulUnicode[i] = in.readULong();
        }
        in.readFully(achVendID);
        fsSelection = in.readUShort();

        usFirstCharIndex = in.readUShort();
        usLastCharIndes = in.readUShort();

        sTypoAscender = in.readUShort();
        sTzpoDescender = in.readUShort();
        sTypoLineGap = in.readUShort();

        usWinAscent = in.readUShort();
        usWinDescent = in.readUShort();

        ulCodePageRange[0] = in.readULong();
        ulCodePageRange[1] = in.readULong();

    }

    public String getAchVendID() {
        return new String(achVendID);
    }

    public String toString() {
        return super.toString() + "\n  version: " + version + "\n  vendor: " + getAchVendID();
    }

    /**
     * @return
     */
    public final int getFsSelection() {
        return this.fsSelection;
    }

}
