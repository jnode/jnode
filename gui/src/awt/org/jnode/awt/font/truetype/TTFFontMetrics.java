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

	/**
	 * @param font
	 * @param fontData
	 * @throws IOException
	 */
	public TTFFontMetrics(Font font, TTFFontData fontData)
	throws IOException {
		super(font);
		this.fontData = fontData;
		this.scale = font.getSize() / fontData.getHorizontalHeaderTable().getAscent();
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
			return (int)(fontData.getHorizontalHeaderTable().getAscent() * scale);
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
			return Math.abs((int)(fontData.getHorizontalHeaderTable().getDescent() * scale));
		} catch (IOException ex) {
			return 0;
		}
	}

	/**
	 * @see java.awt.FontMetrics#getHeight()
	 * @return The height
	 */
	public int getHeight() {
		// TODO Auto-generated method stub
		return super.getHeight();
	}

	/**
	 * @see java.awt.FontMetrics#getLeading()
	 * @return The leading
	 */
	public int getLeading() {
		return super.getLeading();
		// TODO Implement me
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

	/**
	 * @see java.awt.FontMetrics#getWidths()
	 * @return The widths
	 */
	public int[] getWidths() {
		// TODO Auto-generated method stub
		return super.getWidths();
	}
}
