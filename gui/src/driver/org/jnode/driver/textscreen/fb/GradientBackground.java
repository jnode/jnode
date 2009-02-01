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
 
package org.jnode.driver.textscreen.fb;

import java.awt.Color;
import java.awt.Graphics;


public class GradientBackground implements Background {
    private final int maxX;
    private final int height;
    private final Color[] colors;
    
    GradientBackground(int width, int height) {
        this.maxX = width - 1;
        this.height = height;        
        this.colors = new Color[height];
        
        final float step = (float) 1.0 / (float) height;        
        float blue = 0f;
        Color color = null;
        Color prevColor = null;
        for (int y = 0; y < height; y++) {
            if ((y > 0) && (prevColor.getBlue() == (int) (blue * 255))) {
                color = prevColor;
            } else {
                color = new Color(0f, 0f, blue);
            }
            colors[y] = color;
            
            blue += step;
            prevColor = color;
        }
    }
    
    
    public void paint(Graphics ig) {
        for (int y = 0; y < height; y++) {
            ig.setColor(colors[y]);        
            ig.drawLine(0, y, maxX, y);
        }
    }

}
