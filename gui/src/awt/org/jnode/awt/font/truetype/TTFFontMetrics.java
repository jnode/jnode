/*
 * $Id$
 */
package org.jnode.awt.font.truetype;

import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;

/**
 * @author epr
 */
public class TTFFontMetrics extends FontMetrics {

	private final TTFFontData fontData;
	private final double scale;
	private final int fontSize;

	/**
	 * @param font
	 * @param fontData
	 * @throws IOException
	 */
	public TTFFontMetrics(Font font, TTFFontData fontData)
	throws IOException {
		super(font);
		if (font == null) {
			throw new IllegalArgumentException("font cannot be null");
		}
		if (fontData == null) {
			throw new IllegalArgumentException("fontData cannot be null");
		}
		this.fontData = fontData;
		this.fontSize = font.getSize();
		final double ascent = fontData.getHorizontalHeaderTable().getAscent();
		this.scale = fontSize / ascent;
		//System.out.println("Font=" + font.getName() + ", size=" + fontSize + ", scale=" + scale + ", ascent=" + ascent);
	}

	/**
	 * @param ch
	 * @see java.awt.FontMetrics#charWidth(char)
	 * @return The width
	 */
	public int charWidth(char ch) {
		try {
			final TTFCMapTable cmapTable = fontData.getCMapTable();
			final TTFCMapTable.EncodingTable encTable = cmapTable.getEncodingTable(0);
			final TTFHorizontalMetricsTable hmTable = fontData.getHorizontalMetricsTable();
			final int index = encTable.getTableFormat().getGlyphIndex(ch);
			return (int)(hmTable.getAdvanceWidth(index) * scale);
		} catch (IOException ex) {
			return 0;
		}
	}

	/**
	 * @see java.awt.FontMetrics#getAscent()
	 * @return The ascent
	 */
	public int getAscent() {
		try {
			final int ascent = (int)(fontData.getHorizontalHeaderTable().getAscent() * scale);
			return ascent;
		} catch (IOException ex) {
			return 0;
		}
	}

	/**
	 * @see java.awt.FontMetrics#getDescent()
	 * @return The descent
	 */
	public int getDescent() {
		try {
			final int descent = Math.abs((int)(fontData.getHorizontalHeaderTable().getDescent() * scale));
			return descent;
		} catch (IOException ex) {
			return 0;
		}
	}

	/**
	 * @see java.awt.FontMetrics#getMaxAdvance()
	 * @return The maximum advance
	 */
	public int getMaxAdvance() {
		try {
			return (int)(fontData.getHorizontalHeaderTable().getMaxAdvance() * scale);
		} catch (IOException ex) {
			return 0;
		}
	}

	/**
	 * @see java.awt.FontMetrics#getMaxAscent()
	 * @return The maximum ascent
	 */
	public int getMaxAscent() {
		return getAscent();
	}

	/**
	 * @see java.awt.FontMetrics#getMaxDescent()
	 * @return The maximum descent
	 */
	public int getMaxDescent() {
		return getDescent();
	}
}
