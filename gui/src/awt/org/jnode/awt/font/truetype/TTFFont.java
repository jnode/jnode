/*
 * $Id$
 */
package org.jnode.awt.font.truetype;

import java.awt.Font;
import java.io.IOException;

/**
 * @author epr
 */
public class TTFFont extends Font {

	private final TTFFontData fontData;

	/**
	 * @param fontData
	 * @param size
	 * @throws IOException
	 */
	public TTFFont(TTFFontData fontData, int size) throws IOException {
		super(fontData.getNameTable().getFontFamilyName(), fontData.getStyle(), size);
		this.fontData = fontData;
	}

	/**
	 * @return The font data
	 */
	final TTFFontData getFontData() {
		return this.fontData;
	}
	
	/**
	 * @param size
	 * @see java.awt.Font#deriveFont(float)
	 * @return The derived font
	 */
	public Font deriveFont(float size) {
		try {
			return new TTFFont(fontData, (int) size);
		} catch (IOException ex) {
			return super.deriveFont(size);
		}
	}
}
