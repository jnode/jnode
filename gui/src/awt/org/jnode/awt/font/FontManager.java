/*
 * $Id$
 */
package org.jnode.awt.font;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * @author epr
 */
public interface FontManager {

	public static final Class NAME = FontManager.class;//"FontManager";

	/**
	 * Returns an array containing a one-point size instance of all fonts 
	 * available in this provider. 
	 * Typical usage would be to allow a user to select a particular font. 
	 * Then, the application can size the font and set various font 
	 * attributes by calling the deriveFont method on the choosen instance.
	 * This method provides for the application the most precise control 
	 * over which Font instance is used to render text.
	 * If a font in this provider has multiple programmable variations,
	 * only one instance of that Font is returned in the set, 
	 * and other variations must be derived by the application.
	 * If a font in this provider has multiple programmable variations, 
	 * such as Multiple-Master fonts, only one instance of that font 
	 * is returned in the Font array. 
	 * The other variations must be derived by the application. 
	 * 
	 * @return All fonts
	 */
	public Font[] getAllFonts();

	/**
	 * Gets the font metrics for the given font.
	 * @param font
	 * @return The font metrics for the given font
	 */
	public FontMetrics getFontMetrics(Font font);

	/**
	 * Draw the given text to the given graphics at the given location,
	 * using the given font.
	 * 
	 * @param g
	 * @param text
	 * @param font
	 * @param x
	 * @param y
	 */
	public void drawText(Graphics2D g, String text, Font font, int x, int y);

}
