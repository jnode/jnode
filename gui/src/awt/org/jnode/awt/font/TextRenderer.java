/*
 * $Id$
 */
package org.jnode.awt.font;

import java.awt.Color;
import java.awt.geom.AffineTransform;

import org.jnode.driver.video.Surface;

/**
 * @author epr
 */
public interface TextRenderer {

	/**
	 * Render a given text to the given graphics at the given location.
	 * @param g
	 * @param text
	 * @param x
	 * @param y
	 */
	public void render(Surface g, AffineTransform tx, String text, int x, int y, Color color);

}
