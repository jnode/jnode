/**
 * TrueType font data holders.
 * 
 * Copyright 2001, FreeHEP.
 */
package org.jnode.awt.font.truetype;

import java.awt.Font;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * TrueType Font with all its tables.
 * 
 * @author Simon Fischer
 * @version $Id$
 */
public abstract class TTFFontData {

	// this map contains the table entrys mapped on their tag

	private static final String CharToGlyphTable = "cmap";
	private static final String GlyphTable = "glyf";
	private static final String FontHeaderTable = "head";
	private static final String HorizHeaderTable = "hhea";

	/*
	 * 
	 * Required Tables
	 * 
	 * Tag Name ----------- Cmap character to glyph mapping Glyf glyph data Head font header Hhea
	 * horizontal header Hmtx horizontal metrics Loca index to location Maxp maximum profile Name
	 * naming table Post PostScript information OS/2 OS/2 and Windows specific metrics
	 *  
	 */
	private Map entry = new HashMap();

	public abstract int getFontVersion();

	void newTable(String tag, TTFInput input) throws IOException {
		entry.put(tag, initTable(tag, input));
	}

	private Object initTable(String name, TTFInput input) throws IOException {
		for (int i = 0; i < TTFTable.TT_TAGS.length; i++) {
			if (name.equals(TTFTable.TT_TAGS[i])) {
				try {
					final TTFTable table;
					table = (TTFTable) TTFTable.TABLE_CLASSES[i].newInstance();
					table.init(this, input);
					return table;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		System.err.println("Table '" + name + "' ignored.");
		return null;
	}

	public void show() {
		System.out.println("Tables:");
		for (Iterator i = entry.values().iterator(); i.hasNext();) {
			System.out.println(i.next());
		}
	}

	/***********************************************************************************************
	 * Returns the table with the given tag and reads it if necessary.
	 * 
	 * @param tag
	 * @return The table
	 * @throws IOException
	 */
	private final TTFTable getTable(String tag) throws IOException {
		final TTFTable table = (TTFTable) entry.get(tag);
		if (!table.isRead()) {
			table.read();
		}
		return table;
	}

	/**
	 * Returns the glyph table and reads it if necessary.
	 * @return The glyph table
	 * @throws IOException
	 */
	public TTFGlyphTable getGlyphTable() throws IOException {
		return (TTFGlyphTable) getTable(GlyphTable);
	}

	/**
	 * Gets the maximum profile table
	 * 
	 * @return The maximum profile table
	 * @throws IOException
	 */
	public TTFMaxPTable getMaxPTable() throws IOException {
		return (TTFMaxPTable) getTable("maxp");
	}

	/**
	 * Gets the name table
	 * 
	 * @return The name table
	 * @throws IOException
	 */
	public TTFNameTable getNameTable() throws IOException {
		return (TTFNameTable) getTable("name");
	}

	/**
	 * Gets the horizontal header table
	 * 
	 * @return The horizontal header table
	 * @throws IOException
	 */
	public TTFHorizontalHeaderTable getHorizontalHeaderTable() throws IOException {
		return (TTFHorizontalHeaderTable) getTable(HorizHeaderTable);
	}

	/**
	 * Gets the horizontal metrics table
	 * 
	 * @return The horizontal metrics table
	 * @throws IOException
	 */
	public TTFHorizontalMetricsTable getHorizontalMetricsTable() throws IOException {
		return (TTFHorizontalMetricsTable) getTable("hmtx");
	}

	/**
	 * Gets the font header table
	 * 
	 * @return The header table
	 * @throws IOException
	 */
	public TTFHeadTable getHeaderTable() throws IOException {
		return (TTFHeadTable) getTable(FontHeaderTable);
	}

	/**
	 * Gets the OS/2 / Windows metrics table
	 * 
	 * @return The OS/2 / Windows metrics table
	 * @throws IOException
	 */
	public TTFOS_2Table getOS2Table() throws IOException {
		return (TTFOS_2Table) getTable("OS/2");
	}

	/**
	 * Gets the location table
	 * 
	 * @return The location table
	 * @throws IOException
	 */
	public TTFLocationsTable getLocationsTable() throws IOException {
		return (TTFLocationsTable) getTable("loca");
	}

	/**
	 * Gets the character to glyph mapping table
	 * 
	 * @return The CMap table
	 * @throws IOException
	 */
	public TTFCMapTable getCMapTable() throws IOException {
		return (TTFCMapTable) getTable(CharToGlyphTable);
	}

	/**
	 * Reads all tables. This method does not need to be called since the tables are read on demand (
	 * <tt>getTable()</tt>. It might be useful to call it in order to print out all available
	 * information.
	 * @throws IOException
	 */
	public void readAll() throws IOException {
		Iterator i = entry.values().iterator();
		while (i.hasNext()) {
			TTFTable table = (TTFTable) i.next();
			if ((table != null) && (!table.isRead()))
				table.read();
		}
	}

	public void close() throws IOException {
	}

	/**
	 * Gets the style (in AWT terms) of this font
	 * 
	 * @return The style
	 * @see Font#BOLD
	 * @see Font#ITALIC
	 * @see Font#PLAIN
	 * @throws IOException
	 */
	public int getStyle() throws IOException {
		final TTFOS_2Table t = getOS2Table();
		if (t != null) {
			final int fsSel;
			fsSel = t.getFsSelection();
			int style = 0;
			if ((fsSel & 0x01) != 0) {
				style |= Font.ITALIC;
			}
			if ((fsSel & 0x20) != 0) {
				style |= Font.BOLD;
			}
			return style;
		} else {
			return Font.PLAIN;
		}
	}

}
