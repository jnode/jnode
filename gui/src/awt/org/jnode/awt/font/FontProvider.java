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

package org.jnode.awt.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public interface FontProvider<F extends Font> {
    /**
     * Give the name of the font (used for setting the first provider to use
     * among all available ones)
     */
    public String getName();

    /**
     * Does this provides provide the given font?
     *
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
    public Set<F> getAllFonts();

    /**
     * Gets a text renderer for the given font.
     *
     * @param font
     * @return The text renderer for the given font
     */
    public TextRenderer getTextRenderer(Font font);

    /**
     * Gets the font metrics for the given font.
     *
     * @param font
     * @return The font metrics for the given font
     */
    public FontMetrics getFontMetrics(Font font);
    
    /**
     * Translates the font into a font that is provided by this provider.
     *
     * @param font
     * @return
     */
    public F getCompatibleFont(Font font);
    
    /**
     * Creates a font peer from the given name or return null if not supported/provided.
     * As said in {@link org.jnode.awt.JNodeToolkit#getClasspathFontPeer(String, java.util.Map)} javadoc :
     * "We don't know what kind of "name" the user requested (logical, face, family)".
     * 
     * @param name
     * @param attrs
     * @return
     */
    public JNodeFontPeer<? extends FontProvider<F>, F> createFontPeer(String name, Map attrs);

    /**
     * Read and create a Font from the given InputStream
     * @param stream
     * @return
     */
    public F createFont(InputStream stream) throws FontFormatException, IOException;    
}
