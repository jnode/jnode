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

import gnu.java.awt.peer.ClasspathFontPeer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.jnode.driver.video.Surface;

/**
 * @author epr
 */
public interface FontManager {

    public static final Class<FontManager> NAME = FontManager.class; //"FontManager";

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
     *
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
    public void drawText(Surface g, Shape clip, AffineTransform tx, CharSequence text, Font font, int x, int y,
                         Color color);

    /**
     * @throws IOException 
     * @throws FontFormatException 
     *
     */
    public Font createFont(int format, InputStream stream) throws FontFormatException, IOException;

    public ClasspathFontPeer createFontPeer(String name, Map attrs);
}
