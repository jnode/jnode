/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.Surface;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.DeviceArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;


/**
 * @author epr
 */
public class FBTest {

        public static final Help.Info HELP_INFO = new Help.Info(
		"FBTest",
		"Performs tests on the FrameBuffer implementation and outputs performance data",
		new Parameter[] {
			new Parameter(new DeviceArgument("framebuffer", "the FrameBuffer device to use"), Parameter.OPTIONAL),
			new Parameter(new Argument("loops", "how many loops each test should perform"), Parameter.OPTIONAL)
		}
	);


	final Surface g;
	private final int width;
	private final int height;
	private final Color[] colors = new Color[]{Color.RED, Color.BLUE, Color.GRAY, Color.ORANGE, Color.CYAN, Color.MAGENTA};
	private int cIndex;
	int paintMode;
	private final int count;
	private final String tests;
	final AffineTransform tx = new AffineTransform();

	protected FBTest(Surface g, int width, int height, int count, String tests) {
		this.g = g;
		this.width = width;
		this.height = height;
		this.count = count;
		this.tests = tests;
	}

	protected void perform() {
		System.out.println("Loop count          " + count);
		if (tests.indexOf('l') >= 0) {
			System.out.println("Shape Line  PAINT   " + performTest(new DrawShapeLineTest(), Surface.PAINT_MODE));
			System.out.println("Shape Line  XOR     " + performTest(new DrawShapeLineTest(), Surface.XOR_MODE));
		}
		if (tests.indexOf('R') >= 0) {
			System.out.println("Shape Rect  PAINT   " + performTest(new DrawShapeRectTest(), Surface.PAINT_MODE));
			System.out.println("Shape Rect  XOR     " + performTest(new DrawShapeRectTest(), Surface.XOR_MODE));
		}
		if (tests.indexOf('E') >= 0) {
			System.out.println("Shape Ellipse PAINT " + performTest(new DrawShapeEllipseTest(), Surface.PAINT_MODE));
			//System.out.println("Shape Ellipse XOR   " + performTest(new DrawShapeEllipseTest(), Surface.XOR_MODE));
		}
		if (tests.indexOf('A') >= 0) {
			System.out.println("Shape Arc PAINT     " + performTest(new DrawShapeArcTest(), Surface.PAINT_MODE));
			//System.out.println("Shape Arc XOR       " + performTest(new DrawShapeArcTest(), Surface.XOR_MODE));
		}
		if (tests.indexOf('Q') >= 0) {
			System.out.println("Shape QuadCurve PAINT " + performTest(new DrawShapeQuadTest(), Surface.PAINT_MODE));
			//System.out.println("Shape Arc XOR       " + performTest(new DrawShapeArcTest(), Surface.XOR_MODE));
		}
	}

	public static void main(String[] args) throws Exception {

		final String devId = (args.length > 0) ? args[0] : "fb0";
		final int count = (args.length > 1) ? Integer.parseInt(args[1]) : 100;
		final String tests = (args.length > 2) ? args[2] : "plrREQ";
		

		try {
			final DeviceManager dm = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);
			final Device dev = dm.getDevice(devId);
			final FrameBufferAPI api = (FrameBufferAPI) dev.getAPI(FrameBufferAPI.class);
			final FrameBufferConfiguration conf = api.getConfigurations()[0];

			final Surface g = api.open(conf);
			try {
				new FBTest(g, conf.getScreenWidth(), conf.getScreenHeight(), count, tests).perform();
				Thread.sleep(3000);
			} finally {
				g.close();
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	final int randomX() {
		return (int) (Math.random() * (width-1));
	}
	final int randomY() {
		return (int) (Math.random() * (height-1));
	}
	final Color randomColor() {
		cIndex = (cIndex + 1) % colors.length;
		return colors[cIndex];	// not too random
	}
	final long performTest(Test test, int paintMode) {
		this.paintMode = paintMode;
		long start = System.currentTimeMillis();
		for( int i = 0; i < count; i++ )
			test.perform();
		return System.currentTimeMillis() - start;
	}

	interface Test {
		void perform();
	}

	class DrawShapeLineTest implements Test {
		public void perform() {
			final int x1 = randomX();
			final int y1 = randomY();
			final int x2 = randomX();
			final int y2 = randomY();
			g.draw(new Line2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2-x1), Math.abs(y2-y1)), tx, randomColor(), paintMode);
		}
	}

	class DrawShapeRectTest implements Test {
		public void perform() {
			final int x1 = randomX();
			final int y1 = randomY();
			final int x2 = randomX();
			final int y2 = randomY();
			g.draw(new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2-x1), Math.abs(y2-y1)), tx, randomColor(), paintMode);
		}
	}

	class DrawShapeEllipseTest implements Test {
		public void perform() {
			final int x1 = randomX();
			final int y1 = randomY();
			final int x2 = randomX();
			final int y2 = randomY();
			g.draw(new Ellipse2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2-x1), Math.abs(y2-y1)), tx, randomColor(), paintMode);
		}
	}

	class DrawShapeQuadTest implements Test {
		public void perform() {
			final int x1 = randomX();
			final int y1 = randomY();
			final int x2 = randomX();
			final int y2 = randomY();
			final int cx = randomX();
			final int cy = randomY();
			g.draw(new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2), tx, randomColor(), paintMode);
		}
	}

	class DrawShapeArcTest implements Test {
		public void perform() {
			final int x1 = randomX();
			final int y1 = randomY();
			final int x2 = randomX();
			final int y2 = randomY();
			g.draw(new Arc2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2-x1), Math.abs(y2-y1), 40, 300, Arc2D.PIE), tx, randomColor(), paintMode);
		}
	}

}
