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

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.Raster;

import org.apache.log4j.Logger;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.renderer.GlyphRenderer;
import org.jnode.driver.video.Surface;

/**
 * @author epr
 */
public class TTFTextRenderer implements TextRenderer {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private final TTFFontData fontData;
	private final double fontSize;
	private final RenderCache renderCache;

	/**
	 * Create a new instance
	 * @param fontData
	 * @param fontSize
	 */
	public TTFTextRenderer(RenderCache renderCache, TTFFontData fontData, int fontSize) {
	    this.renderCache = renderCache;
		this.fontData = fontData;
		this.fontSize = fontSize;
	}

	/**
	 * Render a given text to the given graphics at the given location.
	 * @param g
	 * @param text
	 * @param x
	 * @param y
	 */
	public void render(Surface surface, AffineTransform tx, String text, int x, int y, Color color) {
		try {
			final TTFGlyphTable glyphTable = fontData.getGlyphTable();
			final TTFCMapTable cmapTable = fontData.getCMapTable();

			if (!(cmapTable.getNrEncodingTables() > 0)) {
				throw new RuntimeException("No Encoding is found!");
			}
			final TTFCMapTable.EncodingTable encTable = cmapTable.getEncodingTable(0);
			if (encTable.getTableFormat() == null) {
				throw new RuntimeException("The table is NUll!!");
			}
			
			for (int i = 0; i < text.length(); i++) {
				// get the index for the needed glyph
				final int index = encTable.getTableFormat().getGlyphIndex(text.charAt(i));
				final Glyph g = glyphTable.getGlyph(index);
				final GlyphRenderer renderer = renderCache.getRenderer(g);
				final Raster alphaRaster = renderer.createGlyphRaster((int)fontSize);
				surface.drawAlphaRaster(alphaRaster, tx, 0, 0, x, y, alphaRaster.getWidth(), alphaRaster.getHeight(), color);
				x += alphaRaster.getWidth();
			}
		} catch (Exception ex) {
			log.error("Error drawing text", ex);
		}
	}
}
