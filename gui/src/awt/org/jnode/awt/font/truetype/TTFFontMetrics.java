/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
