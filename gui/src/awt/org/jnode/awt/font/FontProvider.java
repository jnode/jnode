/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.awt.font;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Set;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface FontProvider {

	/**
	 * Does this provides provide the given font?
	 * @param font
	 * @return True if this provider provides the given font, false otherwise
	 */
	public boolean provides(Font font);

	/**
	 * Returns a set containing a one-point size instance of all fonts 
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
	 * is returned in the Font set. 
	 * The other variations must be derived by the application. 
	 * 
	 * @return All fonts this provider can provide
	 */
	public Set<Font> getAllFonts();

	/**
	 * Gets a text renderer for the given font.
	 * @param font
	 * @return The text renderen for the given font
	 */
	public TextRenderer getTextRenderer(Font font);

	/**
	 * Gets the font metrics for the given font.
	 * @param font
	 * @return The font metrics for the given font
	 */
	public FontMetrics getFontMetrics(Font font);
}
