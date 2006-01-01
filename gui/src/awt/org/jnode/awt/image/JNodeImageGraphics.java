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
 
package org.jnode.awt.image;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Shape;

import org.jnode.awt.util.AbstractGraphics;

/**
 * @author epr
 */
public class JNodeImageGraphics extends AbstractGraphics {

	/**
	 * @param src
	 */
	public JNodeImageGraphics(JNodeImageGraphics src) {
		super(src);
	}

	/**
	 * @param width
	 * @param height
	 */
	public JNodeImageGraphics(int width, int height) {
		super(width, height);
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param dx
	 * @param dy
	 * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
	 */
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.Graphics#create()
	 * @return The graphics
	 */
	public Graphics create() {
		return new JNodeImageGraphics(this);
	}

	/**
	 * @param shape
	 * @see java.awt.Graphics2D#draw(java.awt.Shape)
	 */
	public void draw(Shape shape) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param shape
	 * @see java.awt.Graphics2D#fill(java.awt.Shape)
	 */
	public void fill(Shape shape) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.Graphics2D#getDeviceConfiguration()
	 * @return The configuration
	 */
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * @param text
     * @param x
     * @param y
     * @see java.awt.Graphics#drawString(java.lang.String,int,int)
     */
    public void drawString(String text, int x, int y) {
        // Not implemented
    }
}
