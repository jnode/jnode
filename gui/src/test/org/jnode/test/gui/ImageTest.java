/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

/**
 * @author epr
 */
public class ImageTest {

	public static void main(String[] args) throws Exception {
		final boolean loadOnly = (args.length > 0) && args[0].equalsIgnoreCase("loadOnly");

		if (!loadOnly) {
			final Frame wnd = new Frame();
			try {
				wnd.setSize(600, 400);
				wnd.add(new TestComponent());
				wnd.show();

				//Thread.sleep(5000);
				System.in.read();
			} finally {
				wnd.dispose();
			}
		}
	}

	static class TestComponent extends Component implements ImageObserver {

		private final Image img;

		public TestComponent() {
			super();
			img = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("/org/zaval/lw/rs/img/tree.gif"));
		}

		public void paint(Graphics g) {
			System.out.println("Paint called");
			super.paint(g);

			g.setColor(Color.GREEN);
			g.drawRect(0, 0, getWidth(), getHeight());
			g.drawImage(img, 1, 1, this);
		}
		/**
		 * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
		 */
		public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
			System.out.println("imageUpdate " + flags + "," + x + "," + y + "," + w + "," + h);
			return super.imageUpdate(img, flags, x, y, w, h);
		}

	}
}
