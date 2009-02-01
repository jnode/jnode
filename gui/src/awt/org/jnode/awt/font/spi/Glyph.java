/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.awt.font.spi;

import java.awt.Rectangle;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public abstract class Glyph {

    private final int xMin;
    private final int yMin;
    private final int xMax;
    private final int yMax;

    /**
     * Initialize this instance;
     *
     * @param in
     * @throws java.io.IOException
     */
    protected Glyph(int xMin, int yMin, int xMax, int yMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    public final Rectangle getBBox() {
        return new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
    }

    public String toString() {
        return "(" + xMin + "," + yMin + "):(" + xMax + "," + yMax + ")";
    }
}
