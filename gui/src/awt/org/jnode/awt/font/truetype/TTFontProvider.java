/*
 * $Id$
 */
package org.jnode.awt.font.truetype;

import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jnode.awt.font.FontProvider;
import org.jnode.awt.font.TextRenderer;

/**
 * @author epr
 */
public class TTFontProvider implements FontProvider {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	/** Cache font renderers */
	private final HashMap renderers = new HashMap();
	/** All loaded fonts (name, TTFFont) */
	private final HashMap fontsByName = new HashMap();
	/** Have the system fonts been loaded yet */
	private boolean fontsLoaded = false;
	/** All system fonts */
	private static final String SYSTEM_FONTS[] = { "bhm.ttf", "bhmbd.ttf", "bhmbi.ttf", "bhmi.ttf", "luxisr.ttf", "roman.ttf", "times.ttf", "velehrad.ttf" };

	/**
	 * Does this provides provide the given font?
	 * 
	 * @param font
	 * @return True if this provider provides the given font, false otherwise
	 */
	public boolean provides(Font font) {
		if (!fontsLoaded) {
			loadFonts();
		}
		final Font f = (Font) fontsByName.get(font.getFamily());
		return (f != null);
	}

	/**
	 * Returns a set containing a one-point size instance of all fonts available in this provider. Typical usage would be to allow a user to select a particular font. Then, the application can size
	 * the font and set various font attributes by calling the deriveFont method on the choosen instance. This method provides for the application the most precise control over which Font instance is
	 * used to render text. If a font in this provider has multiple programmable variations, only one instance of that Font is returned in the set, and other variations must be derived by the
	 * application. If a font in this provider has multiple programmable variations, such as Multiple-Master fonts, only one instance of that font is returned in the Font set. The other variations
	 * must be derived by the application.
	 * 
	 * @return The set containing all fonts provides by this provider.
	 */
	public Set getAllFonts() {
		if (!fontsLoaded) {
			loadFonts();
		}
		return new HashSet(fontsByName.values());
	}

	/**
	 * Gets a text renderer for the given font.
	 * 
	 * @param font
	 * @return The renderer
	 */
	public TextRenderer getTextRenderer(Font font) {
		TextRenderer r = (TextRenderer) renderers.get(font);
		if (r == null) {
			r = new TTFTextRenderer(getFontData(font), font.getSize());
			renderers.put(font, r);
		}
		return r;
	}

	/**
	 * Gets the font metrics for the given font.
	 * 
	 * @param font
	 * @return The metrics
	 */
	public FontMetrics getFontMetrics(Font font) {
		return null;
	}

	/**
	 * Gets the font data for the given font
	 * 
	 * @param font
	 * @return The font data
	 */
	private TTFFontData getFontData(Font font) {
		if (font instanceof TTFFont) {
			return ((TTFFont) font).getFontData();
		}
		return null;
	}

	/**
	 * Load all default fonts.
	 */
	private final void loadFonts() {
		final String[] fontNames = SYSTEM_FONTS;
		final int max = fontNames.length;
		for (int i = 0; i < max; i++) {
			loadFont("/" + fontNames[i]);
		}
		fontsLoaded = true;
	}

	private final void loadFont(String resName) {
		try {
			final ClassLoader cl = getClass().getClassLoader();
			final URL url = cl.getResource(resName);
			if (url != null) {
				final TTFFontData fontData = new TTFFontDataFile(url);
				final TTFFont font = new TTFFont(fontData, 10);
				fontsByName.put(font.getName(), font);
			} else {
				log.error("Cannot find font resource " + resName);
			}
		} catch (IOException ex) {
			log.error("Cannot find font " + resName + ": " + ex.getMessage());
		} catch (Throwable ex) {
			log.error("Cannot find font " + resName, ex);
		}
	}
}
