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
 * HHEA Table.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public final class HorizontalHeaderTable extends VersionTable {

    private int ascender;
    private int descender;
    private int lineGap;
    private int advanceWidthMax;
    private int minLeftSideBearing;
    private int minRightSideBearing;
    private int xMaxExtent;
    private int caretSlopeRise;
    private int caretSlopeRun;
    private int metricDataFormat;
    private int numberOfHMetrics;


    /**
     * @param font
     * @param input
     */
    HorizontalHeaderTable(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    public String getTag() {
        return "hhea";
    }

    protected void readTable(TTFInput in) throws IOException {
        readVersion(in);

        ascender = in.readFWord();
        descender = in.readFWord();
        lineGap = in.readFWord();

        advanceWidthMax = in.readUFWord();
        minLeftSideBearing = in.readFWord();
        minRightSideBearing = in.readFWord();
        xMaxExtent = in.readFWord();

        caretSlopeRise = in.readShort();
        caretSlopeRun = in.readShort();

        for (int i = 0; i < 5; i++) {
            in.checkShortZero();
        }

        metricDataFormat = in.readShort();
        numberOfHMetrics = in.readUShort();
    }

    public String toString() {
        String str = super.toString();
        str += "\n  asc:" + ascender + " desc:" + descender + " lineGap:" + lineGap + " maxAdvance:" + advanceWidthMax;
        str += "\n  metricDataFormat:" + metricDataFormat + " #HMetrics:" + numberOfHMetrics;
        return str;
    }

    /**
     * @return The maximum advance
     */
    public int getMaxAdvance() {
        return this.advanceWidthMax;
    }

    /**
     * @return The ascent
     */
    public int getAscent() {
        return this.ascender;
    }

    /**
     * @return The descent
     */
    public int getDescent() {
        return this.descender;
    }

    /**
     * @return The line gap
     */
    public int getLineGap() {
        return this.lineGap;
    }

    /**
     * @return The X max extent
     */
    public int getXMaxExtent() {
        return this.xMaxExtent;
    }

    /**
     * @return The caret slope rise
     */
    public int getCaretSlopeRise() {
        return this.caretSlopeRise;
    }

    /**
     * @return The caret slope run
     */
    public int getCaretSlopeRun() {
        return this.caretSlopeRun;
    }

    /**
     * @return The metric data format
     */
    public int getMetricDataFormat() {
        return this.metricDataFormat;
    }

    /**
     * @return The minimum left side bearing
     */
    public int getMinLeftSideBearing() {
        return this.minLeftSideBearing;
    }

    /**
     * @return The minimum right side bearing
     */
    public int getMinRightSideBearing() {
        return this.minRightSideBearing;
    }

    /**
     * @return The number of h-metrics
     */
    public int getNumberOfHMetrics() {
        return this.numberOfHMetrics;
    }

}
