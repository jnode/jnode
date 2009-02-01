/*
 * $Id$
 *
 * JNode.org
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.jnode.driver.video.util.Curves;

//import org.jnode.awt.geom.Curves;

/**
 * @author epr
 * @author peda
 */
public class CurvesTest {

    private static final int SCALE = 500;
    private static final int LOOPS = 1000000;
    private static final Random r;

    private static double x0, y0, x1, y1, x2, y2, x3, y3;


    static {
        r = new Random(System.currentTimeMillis());
        x0 = r.nextDouble() * SCALE;
        y0 = r.nextDouble() * SCALE;
        x1 = r.nextDouble() * SCALE;
        y1 = r.nextDouble() * SCALE;
        x2 = r.nextDouble() * SCALE;
        y2 = r.nextDouble() * SCALE;
        x3 = r.nextDouble() * SCALE;
        y3 = r.nextDouble() * SCALE;
    }


    public static void main(String[] args) {

        compareCubicCurveImpl();

        compareQuadCurveImpl();

        new CurvesTest().testOptic();

    }

    private void testOptic() {

        System.out.println("Startframe");


        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MyPanel panel1 = new MyPanel("Cubic-Test");
        MyPanel panel2 = new MyPanel("Quad-Test");

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, panel2);
        pane.setDividerLocation(0.5);

        frame.add(pane);

        frame.setSize(1000, 600);
        frame.setVisible(true);

        double[] temp1 = new double[42];
        double[] temp2 = new double[42];

        // cubic test
        for (int i = 0; i < 5; i++) {
            x0 = r.nextDouble() * SCALE;
            y0 = r.nextDouble() * SCALE;
            x1 = r.nextDouble() * SCALE;
            y1 = r.nextDouble() * SCALE;
            x2 = r.nextDouble() * SCALE;
            y2 = r.nextDouble() * SCALE;
            x3 = r.nextDouble() * SCALE;
            y3 = r.nextDouble() * SCALE;
            Curves.calculateCubicCurveOpt2(x0, y0, x1, y1, x2, y2, x3, y3, temp1);
            panel1.addPoints1(temp1);
            Curves.calculateCubicCurve(x0, y0, x1, y1, x2, y2, x3, y3, temp2);
            panel1.addPoints2(temp2);
        }

        // quad test
        for (int i = 0; i < 5; i++) {
            x0 = r.nextDouble() * SCALE;
            y0 = r.nextDouble() * SCALE;
            x1 = r.nextDouble() * SCALE;
            y1 = r.nextDouble() * SCALE;
            x2 = r.nextDouble() * SCALE;
            y2 = r.nextDouble() * SCALE;
            Curves.calculateQuadCurveOpt2(x0, y0, x1, y1, x2, y2, temp1);
            panel2.addPoints1(temp1);
            Curves.calculateQuadCurve(x0, y0, x1, y1, x2, y2, temp2);
            panel2.addPoints2(temp2);
        }


    }

    public static void compareQuadCurveImpl() {

        long start;

        start = System.currentTimeMillis();
        quadOld();
        System.out.println("quadOld took " + (System.currentTimeMillis() - start) + "ms.");

        start = System.currentTimeMillis();
        quadNew();
        System.out.println("quadNew took " + (System.currentTimeMillis() - start) + "ms.");


    }

    private static void quadOld() {

        double[] temp = new double[42];

        for (int i = 0; i < LOOPS; i++)
            Curves.calculateQuadCurve(x0, y0, x1, y1, x2, y2, temp);
    }

    private static void quadNew() {

        double[] temp = new double[42];

        for (int i = 0; i < LOOPS; i++)
            Curves.calculateQuadCurveOpt2(x0, y0, x1, y1, x2, y2, temp);

    }


    public static void compareCubicCurveImpl() {

        long start;

        start = System.currentTimeMillis();
        cubicOld();
        System.out.println("cubicOld took " + (System.currentTimeMillis() - start) + "ms.");

        start = System.currentTimeMillis();
        cubicTest();
        System.out.println("cubicTest took " + (System.currentTimeMillis() - start) + "ms.");
    }

    public static void cubicOld() {

        double[] temp = new double[42];

        for (int i = 0; i < LOOPS; i++)
            Curves.calculateCubicCurve(x0, y0, x1, y1, x2, y2, x3, y3, temp);
    }

    public static void cubicTest() {

        double[] temp = new double[42];

        for (int i = 0; i < LOOPS; i++)
            Curves.calculateCubicCurveOpt2(x0, y0, x1, y1, x2, y2, x3, y3, temp);
    }


    private class MyPanel extends JPanel {

        GeneralPath gp1 = new GeneralPath();
        GeneralPath gp2 = new GeneralPath();
        String name = "";

        public MyPanel(String n) {
            name = n;
        }

        public void addPoints1(double[] v) {
            gp1.moveTo((float) v[0], (float) v[1]);

            for (int i = 1; i < v.length / 2; i++)
                gp1.lineTo((float) v[2 * i], (float) v[2 * i + 1]);
        }

        public void addPoints2(double[] v) {
            gp2.moveTo((float) v[0], (float) v[1]);

            for (int i = 1; i < v.length / 2; i++)
                gp2.lineTo((float) v[2 * i], (float) v[2 * i + 1]);
        }

        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            g2d.setColor(Color.BLACK);
            g2d.drawString(name, 10, 10);

            g2d.setColor(Color.RED);
            g2d.drawString("Opt", 10, 50);
            g2d.draw(gp1);

            g2d.setColor(Color.BLUE);
            g2d.drawString("Normal", 10, 100);
            g2d.draw(gp2.createTransformedShape(AffineTransform.getTranslateInstance(2, 2)));
        }

    }
}
