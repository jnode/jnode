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
 
package org.jnode.awt.font;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import org.jnode.driver.video.Surface;

/**
 * @author epr
 */
public interface TextRenderer {

    /**
     * Render a given text to the given graphics at the given location.
     *
     * @param g
     * @param text
     * @param x
     * @param y
     */
    public void render(Surface g, Shape clip, AffineTransform tx, CharSequence text, int x, int y, Color color);

}
