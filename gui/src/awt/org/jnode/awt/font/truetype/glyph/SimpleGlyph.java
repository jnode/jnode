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

import java.awt.geom.GeneralPath;
import java.io.IOException;
import org.jnode.awt.font.truetype.TTFInput;

public class SimpleGlyph extends TTFGlyph {

    private static final int ON_CURVE = 0;
    private static final int X_SHORT = 1;
    private static final int Y_SHORT = 2;
    private static final int REPEAT_FLAG = 3;
    private static final int X_SAME = 4;
    private static final int Y_SAME = 5;
    private static final int X_POSITIVE = 4;
    private static final int Y_POSITIVE = 5;

    private int numberOfContours;
    private int[] endPtsOfContours;
    private int[] instructions;
    private int[] flags;
    private int[] xCoordinates, yCoordinates;
    private boolean[] onCurve;
    private GeneralPath shape;

    public SimpleGlyph(TTFInput in, int numberOfContours) throws IOException {
        super(in);
        this.numberOfContours = numberOfContours;
        this.endPtsOfContours = new int[numberOfContours];
        read(in);
    }

    public String getType() {
        return "Simple Glyph";
    }

    private void read(TTFInput ttf) throws IOException {
        // read Array of last points of each contour
        for (int i = 0; i < endPtsOfContours.length; i++) {
            endPtsOfContours[i] = ttf.readUShort();
        }
        // read the number of instructions and allocate memory for them
        instructions = new int[ttf.readUShort()];
        // read all the instructions
        for (int i = 0; i < instructions.length; i++) {
            instructions[i] = ttf.readByte();
        }

        int numberOfPoints = endPtsOfContours[endPtsOfContours.length - 1] + 1;
        // allocate memory for flags, xCoordinates[], yCoordinates[]
        flags = new int[numberOfPoints];
        xCoordinates = new int[numberOfPoints];
        yCoordinates = new int[numberOfPoints];
        onCurve = new boolean[numberOfPoints];
        // if should be repeted than return allways the last byte of
        // repeatCountTimes
        int repeatCount = 0;
        int repeatFlag = 0;
        // read the flags
        for (int i = 0; i < numberOfPoints; i++) {
            // if repeatCount was seted than return the same byte and
            // decrementthe number of repeats
            if (repeatCount > 0) {
                flags[i] = repeatFlag;
                repeatCount--;
            } else {
                // read the flag
                flags[i] = ttf.readRawByte();
                // if repeat is seted than read how many times
                if (TTFInput.flagBit(flags[i], REPEAT_FLAG)) {
                    repeatCount = ttf.readByte();
                    repeatFlag = flags[i];
                }
            }
            TTFInput.checkZeroBit(flags[i], 6, "flags");
            TTFInput.checkZeroBit(flags[i], 7, "flags");
            onCurve[i] = TTFInput.flagBit(flags[i], ON_CURVE);
        }

        int last = 0;
        // read xCoordinates
        for (int i = 0; i < numberOfPoints; i++) {
            if (TTFInput.flagBit(flags[i], X_SHORT)) {
                if (TTFInput.flagBit(flags[i], X_POSITIVE)) {
                    last = xCoordinates[i] = last + ttf.readByte();
                } else {
                    last = xCoordinates[i] = last - ttf.readByte();
                }
            } else {
                if (TTFInput.flagBit(flags[i], X_SAME)) {
                    last = xCoordinates[i] = last;
                } else {
                    last = xCoordinates[i] = last + ttf.readShort();
                }
            }
        }

        last = 0;
        // read yCoordinates
        for (int i = 0; i < numberOfPoints; i++) {
            if (TTFInput.flagBit(flags[i], Y_SHORT)) {
                if (TTFInput.flagBit(flags[i], Y_POSITIVE)) {
                    last = yCoordinates[i] = last + ttf.readByte();
                } else {
                    last = yCoordinates[i] = last - ttf.readByte();
                }
            } else {
                if (TTFInput.flagBit(flags[i], Y_SAME)) {
                    last = yCoordinates[i] = last;
                } else {
                    last = yCoordinates[i] = last + ttf.readShort();
                }
            }
        }
    }

    public String toString() {
        String str = super.toString() + ", " + numberOfContours + " contours, endPts={";
        for (int i = 0; i < numberOfContours; i++)
            str += (i == 0 ? "" : ",") + endPtsOfContours[i];
        str += "}, " + instructions.length + " instructions";
        return str;
    }

    public String toDetailedString() {
        String str = toString() + "\n  instructions = {";
        for (int i = 0; i < instructions.length; i++) {
            str += Integer.toHexString(instructions[i]) + " ";
        }
        return str + "}";
    }

    public GeneralPath getShape() {
        if (shape != null) {
            return shape;
        }

        shape = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        int p = 0;
        int x = 0;
        int y = 0;
        for (int i = 0; i < endPtsOfContours.length; i++) {
            boolean first = true;
            while (p <= endPtsOfContours[i]) {
                x = xCoordinates[p];
                y = yCoordinates[p];
                //System.out.print(p+": ("+x+","+y+")");
                if (first) {
                    shape.moveTo(x, y);
                    //System.out.println(" m");
                    if (!onCurve[p]) {
                        //System.err.println("First point of contour not on curve!");
                    } else {
                        //TODO fix it, see bellow
                    }
                } else if (onCurve[p]) {
                    shape.lineTo(x, y);
                    //System.out.println(" l");
                } else {
                    int pIndex = 0;
                    // when we are at the end of a contour
                    if (p == endPtsOfContours[i])
                        // look for the endpoint of the curve at the beginning
                        if (i > 0)
                            pIndex = endPtsOfContours[i - 1] + 1;
                        else
                            pIndex = 0;
                    else
                        pIndex = ++p; // else take the next point
                    int x1 = xCoordinates[pIndex];
                    int y1 = yCoordinates[pIndex];
                    //System.out.print("("+x1+","+y1+")");
                    if (onCurve[p]) {
                        shape.quadTo(x, y, x1, y1);
                        //System.out.println(" q");
                    } else {
                        if (p == endPtsOfContours[i])
                            if (i > 0)
                                pIndex = endPtsOfContours[i - 1] + 1;
                            else
                                pIndex = 0;
                        else
                            pIndex = ++p;
                        int x2 = xCoordinates[pIndex];
                        int y2 = yCoordinates[pIndex];
                        //System.out.println("("+y2+","+y2+") c");
                        shape.curveTo(x, y, x1, y1, x2, y2);

                        // FIXME: Find out how to construct a cubic or quadratic
                        // Bezier curve from a Bezier spline with an arbitrary number
                        // of off-curve points
                        if (!onCurve[p]) {
                            //System.err.println("Three points in a row not on curve!");
                        } else {
                            //TODO see above
                        }
                    }
                }
                first = false;
                p++;
            }
            shape.closePath();
            //System.out.println(".");
        }
        return shape;
    }

}
