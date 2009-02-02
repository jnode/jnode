/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.awt.font.truetype;

import java.awt.Font;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jnode.awt.font.spi.FontData;
import org.jnode.awt.font.spi.Glyph;
import org.jnode.awt.font.truetype.tables.CMapTable;
import org.jnode.awt.font.truetype.tables.GlyphTable;
import org.jnode.awt.font.truetype.tables.HeadTable;
import org.jnode.awt.font.truetype.tables.HorizontalHeaderTable;
import org.jnode.awt.font.truetype.tables.HorizontalMetricsTable;
import org.jnode.awt.font.truetype.tables.LocationsTable;
import org.jnode.awt.font.truetype.tables.MaxPTable;
import org.jnode.awt.font.truetype.tables.NameTable;
import org.jnode.awt.font.truetype.tables.OS2Table;
import org.jnode.awt.font.truetype.tables.TTFTable;

/**
 * TrueType Font with all its tables.
 *
 * @author Simon Fischer
 * @version $Id$
 */
public abstract class TTFFontData implements FontData {

    /**
     * Tables indexed by tag
     */
    private final Map<TableClass, TTFTable> tables = new HashMap<TableClass, TTFTable>();

    /**
     * Initialize this instance.
     *
     * @param input
     */
    protected TTFFontData() {
    }

    public Glyph getGlyph(char c) throws IOException {
        final GlyphTable glyphTable = getGlyphTable();
        final CMapTable cmapTable = getCMapTable();

        if (!(cmapTable.getNrEncodingTables() > 0)) {
            throw new RuntimeException("No Encoding is found!");
        }

        final CMapTable.EncodingTable encTable = cmapTable.getEncodingTable(0);
        if (encTable.getTableFormat() == null) {
            throw new RuntimeException("The table is NUll!!");
        }

        //get the index for the needed glyph
        final int index = encTable.getTableFormat().getGlyphIndex(c);
        return glyphTable.getGlyph(index);
    }

    /**
     * Gets the version of this font data.
     *
     * @return
     */
    public abstract int getFontVersion();


    /**
     * Create a new table with a given table.
     *
     * @param tag
     * @param input
     * @throws IOException
     */
    protected final void newTable(TableClass tc, TTFInput input) throws IOException {
        final TTFTable tbl = tc.create(this, input);
        tables.put(tc, tbl);
    }

    /**
     * Show the contents of this font data.
     */
    public void show() {
        System.out.println("Tables:");
        for (Object v : tables.values()) {
            System.out.println(v);
        }
    }

    /**
     * ************************************************************************
     * Returns the table with the given tag and reads it if necessary.
     *
     * @param tag
     * @return The table
     * @throws IOException
     */
    private final TTFTable getTable(TableClass tag) throws IOException {
        final TTFTable table = tables.get(tag);

        if (table == null) throw new IOException("table is null for tag " + tag.getTag());
        table.read();
        return table;
    }

    /**
     * Returns the glyph table and reads it if necessary.
     *
     * @return The glyph table
     * @throws IOException
     */
    public GlyphTable getGlyphTable() throws IOException {
        return (GlyphTable) getTable(TableClass.GLYF);
    }

    /**
     * Gets the maximum profile table
     *
     * @return The maximum profile table
     * @throws IOException
     */
    public MaxPTable getMaxPTable() throws IOException {
        return (MaxPTable) getTable(TableClass.MAXP);
    }

    /**
     * Gets the name table
     *
     * @return The name table
     * @throws IOException
     */
    public NameTable getNameTable() throws IOException {
        return (NameTable) getTable(TableClass.NAME);
    }

    /**
     * Gets the horizontal header table
     *
     * @return The horizontal header table
     * @throws IOException
     */
    public HorizontalHeaderTable getHorizontalHeaderTable()
        throws IOException {
        return (HorizontalHeaderTable) getTable(TableClass.HHEA);
    }

    /**
     * Gets the horizontal metrics table
     *
     * @return The horizontal metrics table
     * @throws IOException
     */
    public HorizontalMetricsTable getHorizontalMetricsTable()
        throws IOException {
        return (HorizontalMetricsTable) getTable(TableClass.HMTX);
    }

    /**
     * Gets the font header table
     *
     * @return The header table
     * @throws IOException
     */
    public HeadTable getHeaderTable() throws IOException {
        return (HeadTable) getTable(TableClass.HEAD);
    }

    /**
     * Gets the OS/2 / Windows metrics table
     *
     * @return The OS/2 / Windows metrics table
     * @throws IOException
     */
    public OS2Table getOS2Table() throws IOException {
        return (OS2Table) getTable(TableClass.OS2);
    }

    /**
     * Gets the location table
     *
     * @return The location table
     * @throws IOException
     */
    public LocationsTable getLocationsTable() throws IOException {
        return (LocationsTable) getTable(TableClass.LOCA);
    }

    /**
     * Gets the character to glyph mapping table
     *
     * @return The CMap table
     * @throws IOException
     */
    public CMapTable getCMapTable() throws IOException {
        return (CMapTable) getTable(TableClass.CMAP);
    }

    /**
     * Reads all tables. This method does not need to be called since the tables
     * are read on demand ( <tt>getTable()</tt>. It might be useful to call
     * it in order to print out all available information.
     *
     * @throws IOException
     */
    public void readAll() throws IOException {
        for (TTFTable table : tables.values()) {
            if (table != null) {
                table.read();
            }
        }
    }

    public void close() throws IOException {
    }

    /**
     * Gets the style (in AWT terms) of this font
     *
     * @return The style
     * @throws IOException
     * @see Font#BOLD
     * @see Font#ITALIC
     * @see Font#PLAIN
     */
    public int getStyle() throws IOException {
        final OS2Table t = getOS2Table();
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
