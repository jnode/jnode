/*
 * $Id$
 */
package org.jnode.awt.font.truetype;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.awt.font.TextRenderer;

/**
 * @author epr
 */
public class TTFTextRenderer implements TextRenderer {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private final TTFFontData fontData;
	private final double fontSize;

	/**
	 * Create a new instance
	 * @param fontData
	 * @param fontSize
	 */
	public TTFTextRenderer(TTFFontData fontData, int fontSize) {
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
	public void render(Graphics2D g, String text, int x, int y) {
		try {
			final GeneralPath gp = new GeneralPath();
			gp.moveTo(x, y);
			
			final TTFGlyphTable glyphTable = fontData.getGlyphTable();
			final TTFCMapTable cmapTable = fontData.getCMapTable();
			final TTFHorizontalHeaderTable hheadTable = fontData.getHorizontalHeaderTable();
			final TTFHorizontalMetricsTable hmTable = fontData.getHorizontalMetricsTable();

			if (!(cmapTable.getNrEncodingTables() > 0)) {
				throw new RuntimeException("No Encoding is found!");
			}
			final TTFCMapTable.EncodingTable encTable = cmapTable.getEncodingTable(0);
			if (encTable.getTableFormat() == null) {
				throw new RuntimeException("The table is NUll!!");
			}
			final double ascent = hheadTable.getAscent();

			final AffineTransform tx = new AffineTransform();
			final double scale = fontSize / ascent;

			tx.translate(x, y + fontSize);
			tx.scale(scale, -scale);
			tx.translate(0, ascent);
			
			for (int i = 0; i < text.length(); i++) {
				// get the index for the needed glyph
				final int index = encTable.getTableFormat().getGlyphIndex(text.charAt(i));
				Shape shape = glyphTable.getGlyph(index).getShape();
				if(text.charAt(i) != ' ')
					gp.append(shape.getPathIterator(tx), false);
				tx.translate(hmTable.getAdvanceWidth(index), 0);
			}
			g.draw(gp);
			g.fill(gp);
		} catch (IOException ex) {
			log.error("Error drawing text", ex);
		}
	}
}
