/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.jnode.awt.font.FontProvider;
import org.jnode.awt.font.TextRenderer;
import org.jnode.awt.font.bdf.BDFFont;
import org.jnode.awt.font.bdf.BDFFontProvider;
import org.jnode.driver.video.Surface;

/**
 * @author Levente S\u00e1ntha
 */
public class Graphics2DSurface implements Surface {
    private Graphics2D graph;

    public Graphics2DSurface(Graphics2D g) {
        this.graph = g;
    }

    public void close() {
        graph.dispose();
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        graph.copyArea(x, y, width, height, dx, dy);
    }

    public void draw(Shape shape, Shape clip, AffineTransform tx, Color color, int mode) {
        graph.setClip(clip);
        graph.transform(tx);
        graph.setColor(color);
        //mode?
        graph.draw(shape);
    }

    public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX, int srcY, int dstX, int dstY, int width,
                                int height, Color color) {

    }

    public void drawCompatibleRaster(Raster raster, int srcX, int srcY, int dstX, int dstY, int width, int height,
                                     Color bgColor) {

    }

    public void fill(Shape shape, Shape clip, AffineTransform tx, Color color, int mode) {

    }

    public ColorModel getColorModel() {
        return null;
    }

    public int getRGBPixel(int x, int y) {
        return 0;
    }

    public int[] getRGBPixels(Rectangle region) {
        return new int[0];
    }

    public void setRGBPixel(int x, int y, int color) {
        graph.setColor(new Color(color));
        graph.drawLine(x, y, x, y);
    }

    public void update(int x, int y, int width, int height) {
        //do nothing
    }

    public static void main(String[] argv) {
        FontProvider<BDFFont> fp = new BDFFontProvider();
        Set<? extends Font> obj = fp.getAllFonts();
        System.out.println(obj);
        final TextRenderer tr = fp.getTextRenderer(obj.iterator().next());
        final AffineTransform tf = AffineTransform.getTranslateInstance(0, 0);
        JFrame f = new JFrame("BDF Test");
        f.add(new JComponent() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2DSurface gs = new Graphics2DSurface((Graphics2D) g);

                tr.render(gs, null, tf, "JNode", 100, 100, Color.BLACK);
            }
        });
        f.setSize(400, 400);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

    }
}
