/*
 * $Id$
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
