/*
 * $Id$
 */
package org.jnode.awt.font;

import java.awt.Graphics2D;

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
	public void render(Graphics2D g, String text, int x, int y);

}
