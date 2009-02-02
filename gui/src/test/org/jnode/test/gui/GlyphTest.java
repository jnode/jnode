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
 
package org.jnode.test.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jnode.awt.font.renderer.GlyphRenderer;
import org.jnode.awt.font.renderer.RenderContext;
import org.jnode.awt.font.spi.ShapedGlyph;
import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFFontDataFile;
import org.jnode.awt.font.truetype.tables.HorizontalHeaderTable;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GlyphTest {

    public static void main(String[] args) throws IOException {
        // Font f = new Font("Verdana", Font.PLAIN, 64);

        TTFFontData fdata = loadFont("luxisr.ttf");

        JFrame frm;

        frm = new JFrame("GlyphTest - SumAreaTable");
        frm.getContentPane().setBackground(Color.LIGHT_GRAY);

        final String text = "Hello world";
        final RenderContext ctx = new RenderContext();
        final JPanel scale1 = new JPanel(new FlowLayout());
        final JPanel scale3 = new JPanel(new FlowLayout());
        scale1.setOpaque(false);
        scale3.setOpaque(false);
        frm.getContentPane().setLayout(new GridLayout(2, 1));
        frm.getContentPane().add(scale1);
        frm.getContentPane().add(scale3);
        for (int i = 0; i < text.length(); i++) {
            Raster r = renderChar(text.charAt(i), fdata, ctx);
            scale1.add(new RasterViewer(1.0, r));
            scale3.add(new RasterViewer(3.0, r));
        }
        // frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setSize(600, 400);
        frm.show();
    }

    private static Raster renderChar(char ch, TTFFontData fdata, RenderContext ctx)
        throws IOException {
        final int idx = fdata.getCMapTable().getEncodingTable(0).getTableFormat().getGlyphIndex(ch);
        final ShapedGlyph g = (ShapedGlyph) fdata.getGlyphTable().getGlyph(idx);
        Shape shape = g.getShape();
//        System.out.println("shape.bounds " + shape.getBounds());
        final HorizontalHeaderTable hheadTable = fdata
            .getHorizontalHeaderTable();
        final double ascent = hheadTable.getAscent();

        Area area = new Area(shape);
        GlyphRenderer gr = new GlyphRenderer(new RenderContext(), area, ascent);
        final WritableRaster r = GlyphRenderer.createRaster(32, 32);
        gr.createGlyphRaster(r, 16);
        return r;
    }

    private static TTFFontData loadFont(String name) {
        String resName = name;
        try {
            // final URL url = ClassLoader.getSystemResource(name);
            final ClassLoader cl = Thread.currentThread()
                .getContextClassLoader();
            final URL url = cl.getResource(resName);
            if (url != null) {
                return new TTFFontDataFile(url);
            } else {
                throw new Error("Cannot find font resource " + resName);
            }
        } catch (IOException ex) {
            throw new Error("Cannot find font " + resName + ": " + ex.getMessage());
        } catch (Throwable ex) {
            throw new Error("Cannot find font " + resName, ex);
        }
    }

    private static Kernel createKernel(int width) {
        final float[] mtx = new float[width * width];
        final int center = width - 1 / 2;

        double total = 0.0;
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < width; x++) {
                int d2 = Math.abs(center - x) * Math.abs(center - y);
                double d = Math.sqrt(d2);
                double v = 1 / Math.pow(2.0, d);
                mtx[y * width + x] = (float) v;
                total += v;
            }
        }

        for (int i = 0; i < mtx.length; i++) {
            mtx[i] /= total;
        }

        return new Kernel(width, width, mtx);
    }

    private static void printSum(Kernel kernel) {
        float v = 0.0f;
        float[] mtx = kernel.getKernelData(null);
        for (int i = 0; i < mtx.length; i++) {
            v += mtx[i];
        }
        System.out.println("mtx " + v);
    }

    private static void fill(Shape shape, BufferedImage img) {
        final Graphics2D g = (Graphics2D) img.getGraphics();
        // g.draw(shape);

        final int w = img.getWidth();
        final int h = img.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (shape.contains(x, y)) {
                    g.fillRect(x, y, 1, 1);
                }
            }
        }
    }

    public static class ImageViewer extends Component {

        private final BufferedImage[] images;

        private final double scale;

        public ImageViewer(BufferedImage[] images) {
            this(1.0, images);
        }

        public ImageViewer(double scale, BufferedImage[] images) {
            this.scale = scale;
            this.images = images;
        }

        /**
         * @see java.awt.Component#paint(java.awt.Graphics)
         */
        public void paint(Graphics g1) {
            Graphics2D g = (Graphics2D) g1;
            int dx = getWidth() / images.length;
            g.scale(scale, scale);
            final Color bgColor = getBackground();
            for (int i = 0; i < images.length; i++) {
                g1.drawImage(images[i], (int) (dx * i / scale), 0, bgColor, null);
            }
        }

        /**
         * @see java.awt.Component#getPreferredSize()
         */
        public Dimension getPreferredSize() {
            int w = 0;
            int h = 0;
            for (int i = 0; i < images.length; i++) {
                w = Math.max(w, (int) (images[i].getWidth() * scale));
                h = Math.max(h, (int) (images[i].getHeight() * scale));
            }
            return new Dimension(w * images.length + 10, h);
        }
    }

    public static class RasterViewer extends ImageViewer {

        /**
         * @param image
         */
        public RasterViewer(Raster image) {
            this(new Raster[]{image});
        }

        /**
         * @param image
         */
        public RasterViewer(double scale, Raster image) {
            this(scale, new Raster[]{image});
        }

        /**
         * @param images
         */
        public RasterViewer(Raster[] images) {
            this(1.0, images);
        }

        /**
         * @param scale
         * @param images
         */
        public RasterViewer(double scale, Raster[] images) {
            super(scale, toImages(images));
        }

        private static BufferedImage[] toImages(Raster[] src) {
            BufferedImage[] imgs = new BufferedImage[src.length];
            for (int i = 0; i < src.length; i++) {
                imgs[i] = toImage(src[i]);
            }
            return imgs;
        }

        private static BufferedImage toImage(Raster src) {
            final int w = src.getWidth();
            final int h = src.getHeight();
//            System.out.println("image size " + w + "x" + h);
            final BufferedImage img = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);
            final int c = Color.BLACK.getRGB() & 0xFFFFFF;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    final int v = src.getSample(x, y, 0) & 0xFF;
                    img.setRGB(x, y, (v << 24) | c);
                }
            }
            return img;
        }

    }

    public static class MasterViewer extends ImageViewer {

        /**
         * @param images
         */
        public MasterViewer(GlyphRenderer.Master master) {
            super(1.0, new BufferedImage[]{toImage(master)});
        }

        private static BufferedImage toImage(GlyphRenderer.Master master) {
            final int w = master.width;
            final int h = master.height;
            final BufferedImage img = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    img.setRGB(x, y, master.bits.get(y * w + x) ? 0xFFFFFF : 0);
                }
            }
            return img;
        }
    }

    public static class ShapeViewer extends Component {

        private final Shape shape;

        public ShapeViewer(Shape shape) {
            this.shape = shape;
        }

        /**
         * @see java.awt.Component#paint(java.awt.Graphics)
         */
        public void paint(Graphics g1) {
            Graphics2D g = (Graphics2D) g1;
            g.setColor(Color.BLACK);
            ((Graphics2D) g).draw(shape);
        }

        /**
         * @see java.awt.Component#getPreferredSize()
         */
        public Dimension getPreferredSize() {
            return shape.getBounds().getSize();
        }
    }
}
