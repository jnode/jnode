/*
 * $Id$
 */
package org.jnode.awt.font.truetype;

import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jnode.awt.font.FontProvider;
import org.jnode.awt.font.TextRenderer;

/**
 * @author epr
 */
public class TTFontProvider implements FontProvider {
	
	private final HashMap renderers = new HashMap();
	private final HashMap fontsByName = new HashMap();
	
	/**
	 * Does this provides provide the given font?
	 * @param font
	 * @return True if this provider provides the given font, false otherwise
	 */
	public boolean provides(Font font) {
		final Font f = (Font)fontsByName.get(font.getFamily()); 
		return (f != null);
	}

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
	 * @return The set containing all fonts provides by this provider.
	 */
	public Set getAllFonts() 
	{
		if(fontsByName.size() == 0)
		{
			// load the luxisr.ttf
			TTFFontData fontData;
			try {
				fontData = new TTFFontDataFile(this.getClass().getClassLoader().getResource("/" + "luxisr.ttf"));
				TTFFont font = new TTFFont(fontData,10 );
				fontsByName.put(font.getName(),font);
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return new HashSet(fontsByName.values());
	}

	/**
	 * Gets a text renderer for the given font.
	 * @param font
	 * @return The renderer
	 */
	public TextRenderer getTextRenderer(Font font) {
		TextRenderer r = (TextRenderer)renderers.get(font);
		if (r == null) {
			r = new TTFTextRenderer(getFontData(font), font.getSize());
			renderers.put(font, r);
		}
		return r;
	}

	/**
	 * Gets the font metrics for the given font.
	 * @param font
	 * @return The metrics
	 */
	public FontMetrics getFontMetrics(Font font) {
		return null;
	}
	
	/**
	 * Gets the font data for the given font
	 * @param font
	 * @return The font data
	 */
	private TTFFontData getFontData(Font font) 
	{
		if(font instanceof TTFFont)
			return ((TTFFont)font).getFontData();
		return null;
	}
}
