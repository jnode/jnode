/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.test.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFFontDataFile;
import org.jnode.awt.font.truetype.glyph.TTFGlyph;
import org.jnode.awt.font.truetype.tables.CMapTable;
import org.jnode.awt.font.truetype.tables.GlyphTable;
import org.jnode.awt.font.truetype.tables.HorizontalHeaderTable;
import org.jnode.awt.font.truetype.tables.HorizontalMetricsTable;

/**
 *
 * @author Mark Donszelmann
 * @version $Id$
 */
public class TTFFileTest {

    static TTFFontData ttf;

    public static void main(String[] args) throws Exception {

        String fontName = "luxisr.ttf";
        final String text;
        //String fontName = "AMERIKA.ttf";
        if (args.length >= 1) {
            text = args[0];
        } else {
            text = "a";
        }

        ttf = new TTFFontDataFile(TTFFileTest.class.getResource("/" + fontName));
        //ttf.show();

        System.out.println("Font " + ttf.getNameTable().getFontFamilyName());
        // bbox chars: 188, 375, 198, 353
        //Rectangle maxCharBounds  = ((TTFHeadTable)ttf.getTable(TTFFont.FontHeaderTable)).getMaxCharBounds();
        //    System.out.println(maxCharBounds);
        final Frame frame = new Frame("TTF Test");
        try {
            frame.setBackground(Color.LIGHT_GRAY);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    frame.dispose();
                }
            });
            //frame.getContentPane().add(new GlyphPanel(maxCharBounds));
            frame.pack();
            frame.setSize(500, 500);
            frame.add(new Component() {
                public void paint(Graphics g) {
                    System.out.println("Paint");
                    g.setColor(Color.RED);
                    try {
                        drawString(g, text, 100, 100, 80);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    g.setColor(Color.BLACK);
                    g.drawLine(0, 100, getWidth(), 100);
                    g.drawLine(0, 200, getWidth(), 200);

                    //g.setFont(new Font("Arial", Font.PLAIN, 40));
                    //g.drawString("AWT", 100, 200);
                }
            });
            frame.setVisible(true);
        } finally {
            //Thread.sleep(5000);
            System.in.read();
            frame.dispose();
        }
        //frame.invalidate();
        //ttf.close();

        testGetGlyph(text, 100, 100, 40);
    }

    public static void drawString(Graphics g, String s, int x, int y, double fontSize) throws IOException {

        final GlyphTable glyphTable = ttf.getGlyphTable();
        final CMapTable cmapTable = ttf.getCMapTable();
        final HorizontalHeaderTable hheadTable = ttf.getHorizontalHeaderTable();
        final HorizontalMetricsTable hmTable = ttf.getHorizontalMetricsTable();

        if (!(cmapTable.getNrEncodingTables() > 0)) {
            throw new RuntimeException("No Encoding is found!");
        }
        final CMapTable.EncodingTable encTable = cmapTable.getEncodingTable(0);
        if (encTable.getTableFormat() == null) {
            throw new RuntimeException("The table is NUll!!");
        }
        final int maxAdvance = hheadTable.getMaxAdvance();
        final double ascent = hheadTable.getAscent();
        final int descent = -hheadTable.getDescent();

        final AffineTransform tx = new AffineTransform();
        double scale = fontSize / ascent;

        tx.translate(x, y + fontSize);
        System.out.println("Scale=" + scale);
        tx.scale(scale, -scale);
        tx.translate(0, ascent);

        final Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.GREEN);
        g2.fill(new Rectangle2D.Double(x, y - ascent * scale, maxAdvance * scale, ascent * scale));
        g2.setColor(Color.YELLOW);
        g2.fill(new Rectangle2D.Double(x, y, maxAdvance * scale, descent * scale));
        g2.setColor(Color.RED);
        final GeneralPath gp = new GeneralPath();

        for (int i = 0; i < s.length(); i++) {
            // get the index for the needed glyph
            final int index = encTable.getTableFormat().getGlyphIndex(s.charAt(i));
            final TTFGlyph glyph = (TTFGlyph) glyphTable.getGlyph(index);
            final GeneralPath shape = glyph.getShape();
            gp.append(shape.getPathIterator(tx), false);
            tx.translate(hmTable.getAdvanceWidth(index), 0);
        }
        g2.draw(gp);
    }

    public static void testGetGlyph(String s, int x, int y, int fontSize) throws IOException {

        final GlyphTable glyphTable = ttf.getGlyphTable();
        final CMapTable cmapTable = ttf.getCMapTable();
        final HorizontalHeaderTable hheadTable = ttf.getHorizontalHeaderTable();

        if (!(cmapTable.getNrEncodingTables() > 0)) {
            throw new RuntimeException("No Encoding is found!");
        }
        final CMapTable.EncodingTable encTable = cmapTable.getEncodingTable(0);
        if (encTable.getTableFormat() == null) {
            throw new RuntimeException("The table is NUll!!");
        }

        final double ascent = hheadTable.getAscent();
        final AffineTransform tx = new AffineTransform();
        double scale = fontSize / ascent;
        tx.translate(x, y + fontSize);
        System.out.println("Scale=" + scale + ", ascent=" + ascent);
        tx.scale(scale, -scale);
        tx.translate(0, ascent);

        final String[] types = {"move", "line", "quad", "cubic", "close"};

        for (int i = 0; i < s.length(); i++) {
            // get the index for the needed glyph
            final char ch = s.charAt(i);
            System.out.println("Getting index for char: " + ch);
            final int index = encTable.getTableFormat().getGlyphIndex(ch);
            final TTFGlyph glyph = (TTFGlyph) glyphTable.getGlyph(index); 
            final GeneralPath shape = glyph.getShape();
            PathIterator pi = shape.getPathIterator(null);
            final float[] f = new float[6];
            while (!pi.isDone()) {
                final int type = pi.currentSegment(f);
                System.out.println(types[type] + ",\t(" + f[0] + "," + f[1] + "),\t(" + f[2] + "," + f[3] + "),\t(" + f[4] + "," + f[5] + ")");
                pi.next();
            }
        }
    }

}
