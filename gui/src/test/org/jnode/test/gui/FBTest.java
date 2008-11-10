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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.Surface;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * @author epr
 */
public class FBTest extends AbstractCommand {

    private final DeviceArgument ARG_DEVICE = new DeviceArgument(
        "device", Argument.OPTIONAL, "the FrameBuffer device to use", FrameBufferAPI.class);

    private final IntegerArgument ARG_LOOPS = new IntegerArgument(
        "loops", Argument.OPTIONAL, "how many loops each test should perform");

    private final StringArgument ARG_TESTS = new StringArgument(
        "tests", Argument.OPTIONAL, "tests to be perform (lREAQ)");

    public FBTest() {
        super("Performs tests on the FrameBuffer implementation and outputs performance data");
        registerArguments(ARG_DEVICE, ARG_LOOPS, ARG_TESTS);
    }

    private static final Logger log = Logger.getLogger(FBTest.class);

    Surface g;
    private int width;
    private int height;
    private final Color[] colors =
        new Color[]{Color.RED, Color.BLUE, Color.GRAY, Color.ORANGE, Color.CYAN, Color.MAGENTA};
    private int cIndex;
    int paintMode;
    private int count;
    private String tests;
    final AffineTransform tx = new AffineTransform();

    protected void perform() {
        log.info("Loop count          " + count);
        if (tests.indexOf('l') >= 0) {
            log.info("Shape Line  PAINT   " + performTest(new DrawShapeLineTest(), Surface.PAINT_MODE));
            log.info("Shape Line  XOR     " + performTest(new DrawShapeLineTest(), Surface.XOR_MODE));
        }
        if (tests.indexOf('R') >= 0) {
            log.info("Shape Rect  PAINT   " + performTest(new DrawShapeRectTest(), Surface.PAINT_MODE));
            log.info("Shape Rect  XOR     " + performTest(new DrawShapeRectTest(), Surface.XOR_MODE));
        }
        if (tests.indexOf('E') >= 0) {
            log.info("Shape Ellipse PAINT " + performTest(new DrawShapeEllipseTest(), Surface.PAINT_MODE));
            //log.info("Shape Ellipse XOR   " + performTest(new DrawShapeEllipseTest(), Surface.XOR_MODE));
        }
        if (tests.indexOf('A') >= 0) {
            log.info("Shape Arc PAINT     " + performTest(new DrawShapeArcTest(), Surface.PAINT_MODE));
            //log.info("Shape Arc XOR       " + performTest(new DrawShapeArcTest(), Surface.XOR_MODE));
        }
        if (tests.indexOf('Q') >= 0) {
            log.info("Shape QuadCurve PAINT " + performTest(new DrawShapeQuadTest(), Surface.PAINT_MODE));
            //log.info("Shape Arc XOR       " + performTest(new DrawShapeArcTest(), Surface.XOR_MODE));
        }
        if (tests.indexOf('C') >= 0) {
            log.info("Colors " + performTest(new ColorsTest(), Surface.PAINT_MODE));
        }
    }

    public static void main(String[] args) throws Exception {
        new FBTest().execute(args);
    }

    public void execute() {

        Device dev = ARG_DEVICE.getValue();
        count = ARG_LOOPS.isSet() ? ARG_LOOPS.getValue() : 100;
        tests = ARG_TESTS.isSet() ? ARG_TESTS.getValue() : "lREAQC";

        try {
            if (dev == null) {
                final Collection<Device> devs = DeviceUtils.getDevicesByAPI(FrameBufferAPI.class);
                if (devs.size() == 0) {
                    getError().getPrintWriter().println("No framebuffer devices to test");
                    exit(1);
                }
                dev = new ArrayList<Device>(devs).get(0);
            }

            log.info("Using device " + dev.getId());
            final FrameBufferAPI api = dev.getAPI(FrameBufferAPI.class);
            final FrameBufferConfiguration conf = api.getConfigurations()[0];

            g = api.open(conf);
            this.width = conf.getScreenWidth();
            this.height = conf.getScreenHeight();

            perform();

            Thread.sleep(30000);
        } catch (Throwable ex) {
            log.error("Error in FBTest", ex);
        } finally {
            if (g != null) {
                log.info("Close graphics");
                g.close();
            }
            log.info("End of FBTest");
        }

        CurvesTest.compareCubicCurveImpl();

        CurvesTest.compareQuadCurveImpl();
    }

    final int randomX() {
        return (int) (Math.random() * (width - 1));
    }

    final int randomY() {
        return (int) (Math.random() * (height - 1));
    }

    final Color randomColor() {
        cIndex = (cIndex + 1) % colors.length;
        return colors[cIndex];    // not too random
    }

    final long performTest(Test test, int paintMode) {
        this.paintMode = paintMode;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            test.perform();
        }
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
            g.draw(new Line2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(
                y2 - y1)), null, tx, randomColor(), paintMode);
        }
    }

    class DrawShapeRectTest implements Test {
        public void perform() {
            final int x1 = randomX();
            final int y1 = randomY();
            final int x2 = randomX();
            final int y2 = randomY();
            g.draw(new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(
                y2 - y1)), null, tx, randomColor(), paintMode);
        }
    }

    class DrawShapeEllipseTest implements Test {
        public void perform() {
            final int x1 = randomX();
            final int y1 = randomY();
            final int x2 = randomX();
            final int y2 = randomY();
            g.draw(new Ellipse2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(
                y2 - y1)), null, tx, randomColor(), paintMode);
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
            g.draw(new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2), null, tx, randomColor(), paintMode);
        }
    }

    class ColorsTest implements Test {
        public void perform() {
            int x = 0;
            final int width = 50;
            for (Color color : colors) {
                for (int i = 0; i < 10; i++) {
                    final int w = width - 2 * i;
                    g.draw(new Rectangle2D.Double(x + i, i, w, w), null, tx, color, paintMode);
                }
                x += width;
            }
        }
    }

    class DrawShapeArcTest implements Test {
        public void perform() {
            final int x1 = randomX();
            final int y1 = randomY();
            final int x2 = randomX();
            final int y2 = randomY();
            g.draw(new Arc2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(
                y2 - y1), 40, 300, Arc2D.PIE), null, tx, randomColor(), paintMode);
        }
    }

}
