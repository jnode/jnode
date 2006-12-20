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
import java.util.Collection;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.Surface;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.argument.DeviceArgument;

/**
 * @author epr
 */
public class FBTest {

    public static final Help.Info HELP_INFO = new Help.Info(
            "FBTest",
            "Performs tests on the FrameBuffer implementation and outputs performance data",
            new Parameter[]{
                new Parameter(new DeviceArgument("framebuffer", "the FrameBuffer device to use"), Parameter.OPTIONAL),
                new Parameter(new Argument("loops", "how many loops each test should perform"), Parameter.OPTIONAL)
            }
    );

    private static final Logger log = Logger.getLogger(FBTest.class);

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
    }

    public static void main(String[] args) throws Exception {

        final String devId = (args.length > 0) ? args[0] : "" /*"fb0"*/;
        final int count = (args.length > 1) ? Integer.parseInt(args[1]) : 100;
        final String tests = (args.length > 2) ? args[2] : "plrREQ";

        Surface g = null;
        try {
            Device dev = null;
            if("".equals(devId)){
                final Collection<Device> devs = DeviceUtils.getDevicesByAPI(FrameBufferAPI.class);
                int dev_count = devs.size();
                if(dev_count > 0){
                    Device[] dev_a = devs.toArray(new Device[dev_count]);
                    dev = dev_a[0];
                }
            }

            if(dev == null){
                final DeviceManager dm = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);
                dev = dm.getDevice(devId);
            }

            log.info("Using device " + dev.getId());
            final FrameBufferAPI api = (FrameBufferAPI) dev.getAPI(FrameBufferAPI.class);
            final FrameBufferConfiguration conf = api.getConfigurations()[0];

            g = api.open(conf);
            new FBTest(g, conf.getScreenWidth(), conf.getScreenHeight(), count, tests).perform();
            Thread.sleep(3000);
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
        return colors[cIndex];	// not too random
    }

    final long performTest(Test test, int paintMode) {
        this.paintMode = paintMode;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++)
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
            g.draw(new Line2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1)), null, tx, randomColor(), paintMode);
        }
    }

    class DrawShapeRectTest implements Test {
        public void perform() {
            final int x1 = randomX();
            final int y1 = randomY();
            final int x2 = randomX();
            final int y2 = randomY();
            g.draw(new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1)), null, tx, randomColor(), paintMode);
        }
    }

    class DrawShapeEllipseTest implements Test {
        public void perform() {
            final int x1 = randomX();
            final int y1 = randomY();
            final int x2 = randomX();
            final int y2 = randomY();
            g.draw(new Ellipse2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1)), null, tx, randomColor(), paintMode);
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

    class DrawShapeArcTest implements Test {
        public void perform() {
            final int x1 = randomX();
            final int y1 = randomY();
            final int x2 = randomX();
            final int y2 = randomY();
            g.draw(new Arc2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1), 40, 300, Arc2D.PIE), null, tx, randomColor(), paintMode);
        }
    }

}
