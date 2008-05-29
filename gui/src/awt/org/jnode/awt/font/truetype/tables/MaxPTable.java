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
 * MAXP Table.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public final class MaxPTable extends VersionTable {

    public int numGlyphs;

    public int maxPoints, maxContours;

    public int maxCompositePoints, maxCompositeContours;

    public int maxZones;

    public int maxTwilightPoints;

    public int maxStorage;

    public int maxFunctionDefs;

    public int maxInstructionDefs;

    public int maxStackElements;

    public int maxSizeOfInstructions;

    public int maxComponentElements;

    public int maxComponentDepth;

    /**
     * @param font
     * @param input
     */
    MaxPTable(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    public String getTag() {
        return "maxp";
    }

    protected void readTable(TTFInput in) throws IOException {
        readVersion(in);

        numGlyphs = in.readUShort();

        maxPoints = in.readUShort();
        maxContours = in.readUShort();
        maxCompositePoints = in.readUShort();
        maxCompositeContours = in.readUShort();
        maxZones = in.readUShort();
        maxTwilightPoints = in.readUShort();
        maxStorage = in.readUShort();
        maxFunctionDefs = in.readUShort();
        maxInstructionDefs = in.readUShort();
        maxStackElements = in.readUShort();
        maxSizeOfInstructions = in.readUShort();
        maxComponentElements = in.readUShort();
        maxComponentDepth = in.readUShort();
    }

    public String toString() {
        return super.toString() + "\n" + "  numGlyphs: " + numGlyphs;
    }
}
