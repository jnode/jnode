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
 
package org.jnode.test.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;

import org.jnode.awt.font.renderer.FontScaleOp;
import org.jnode.awt.font.renderer.GlyphRenderer;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GlyphTest {

	public static void main(String[] args) {
		Font f = new Font("Verdana", Font.PLAIN, 64);

		FontRenderContext ctx = new FontRenderContext(null, false, false);
		GlyphVector gv = f.createGlyphVector(ctx, "Hoi");
		Rectangle2D bounds = gv.getVisualBounds();
		Shape shape = gv.getOutline(0.0f, (float) bounds.getHeight());

		Area area = new Area(shape);
		final double scale = 5.0;
		area.transform(AffineTransform.getScaleInstance(scale, scale));
		final Rectangle2D b = area.getBounds2D();
		Dimension size = new Dimension((int) b.getMaxX(), (int) b.getMaxY());

		BufferedImage image = new BufferedImage(size.width + 20,
				size.height + 20, BufferedImage.TYPE_INT_RGB);
		fill(area, image);

		Kernel mtx3 = createKernel(3);
		printSum(mtx3);

		Kernel mtx5 = createKernel(5);
		printSum(mtx5);

		Kernel mtx7 = createKernel(7);
		printSum(mtx7);

		Kernel mtx15 = createKernel(15);
		printSum(mtx15);

		double fscale = 0.1;
		FontScaleOp fsOp3 = new FontScaleOp((int) (size.width * fscale),
				(int) (size.height * fscale), mtx3);
		FontScaleOp fsOp5 = new FontScaleOp((int) (size.width * fscale),
				(int) (size.height * fscale), mtx5);
		FontScaleOp fsOp7 = new FontScaleOp((int) (size.width * fscale),
				(int) (size.height * fscale), mtx7);
		FontScaleOp fsOp15 = new FontScaleOp((int) (size.width * fscale),
				(int) (size.height * fscale), mtx15);
		BufferedImage fs3Img = fsOp3.filter(image, null);
		BufferedImage fs5Img = fsOp5.filter(image, null);
		BufferedImage fs7Img = fsOp7.filter(image, null);
		BufferedImage fs15Img = fsOp15.filter(image, null);

		JFrame frm;

		if (false) {
			frm = new JFrame("GlyphTest - FontScaleOp");
			frm.getContentPane().setBackground(Color.BLACK);
			frm.getContentPane().setLayout(new GridLayout(3, 1));
			frm.getContentPane().add(
					new ImageViewer(new BufferedImage[] { image }));
			frm.getContentPane().add(
					new ImageViewer(5.0, new BufferedImage[] { fs3Img, fs5Img,
							fs7Img, fs15Img }));
			frm.getContentPane().add(
					new ImageViewer(1.0, new BufferedImage[] { fs3Img, fs5Img,
							fs7Img, fs15Img }));
			// frm.getContentPane().add(new ShapeViewer(area));
			// frm.setSize(size.height * 4 + 10, size.width * 2 + 10);
			frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frm.pack();
			frm.show();
		} else {

			final int[] sizes = { 8, 9, 10, 11, 12, 14, 16, 18, 20, 24, 28 };
			final GlyphRenderer renderer = new GlyphRenderer(area);

			final Raster[] fonts = new Raster[sizes.length];
			for (int i = 0; i < sizes.length; i++) {
				fonts[i] = renderer.createGlyphRaster(sizes[i]);
			}

			frm = new JFrame("GlyphTest - SumAreaTable");
			frm.getContentPane().setBackground(Color.BLACK);
			frm.getContentPane().setLayout(new GridLayout(3, 1));
			frm.getContentPane().add(
					new ImageViewer(new BufferedImage[] { image }));
			frm.getContentPane().add(new RasterViewer(5.0, fonts));
			frm.getContentPane().add(new RasterViewer(1.0, fonts));
			frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frm.pack();
			frm.show();
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
			for (int i = 0; i < images.length; i++) {
				g1.drawImage(images[i], (int) (dx * i / scale), 0, null);
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
				ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
				ColorModel cm = new ComponentColorModel(cs, false, true,
						Transparency.OPAQUE, src[i].getDataBuffer()
								.getDataType());
				imgs[i] = new BufferedImage(cm, (WritableRaster) src[i], true,
						null);
			}
			return imgs;
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
