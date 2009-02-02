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
 
package org.jnode.test;

import java.io.PrintStream;

/**
 * Test of floating point operations
 *
 * @author epr
 */
public class FPTest {

    public static void main(String[] args) throws InterruptedException {

        Runnable r = new Runnable() {
            public void run() {
                final PrintStream out = System.out;

                testFloat(out);
                Thread.yield();
                testDouble(out);
                Thread.yield();
                testTan(out);
                Thread.yield();
            }
        };
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    private static void testTan(PrintStream out) {
        out.println("testTan");

        for (double i = -5; i < 5; i += 0.5) {

            final double jvmResult = Math.tan(i);
            final double cpResult = StrictMathTest.tan(i);

            if (jvmResult != cpResult) {
                out.println("tan(" + i + ")=" + jvmResult + "\t" + cpResult);
            }
        }
        final double angle = Math.PI / 4.0;
        final double a = 1.0 - Math.cos(angle);
        final double b = Math.tan(angle);
        final double c = Math.sqrt(1.0 + b * b) - 1 + a;
        final float cv = (float) (4.0 / 3.0 * a * b / c);
        out.println("JVM: " + Math.tan(angle) + ", CP: " + StrictMathTest.tan(angle));
        out.println("CV: " + cv);
    }


    private static void testFloat(PrintStream out) {
        out.println("testFloat");
        final float a = 45.5f;
        final float b = -7.11f;
        final double da = a;
        final double db = b;
        final int ia = (int) a;
        final int ib = (int) b;

        out.println("a      " + a);
        out.println("b      " + b);
        out.println("-a     " + (-a));
        out.println("-b     " + (-b));
        out.println("a + b  " + (a + b));
        out.println("a - b  " + (a - b));
        out.println("a * b  " + (a * b));
        out.println("a / b  " + (a / b));
        out.println("sin(a) " + Math.sin(a));
        out.println("asin(a) " + Math.asin(a));
        out.println("cos(a) " + Math.cos(a));
        out.println("acos(a) " + Math.acos(a));
        out.println("tan(a) " + Math.tan(a));
        out.println("atan(a) " + Math.atan(a));
        out.println("sqrt(a) " + Math.sqrt(a));

        out.println("da    " + da);
        out.println("db    " + db);
        out.println("ia    " + ia);
        out.println("ib    " + ib);

        out.println("a == a " + (a == a));
        out.println("a != a " + (a != a));
        out.println("a >  a " + (a > a));
        out.println("a >= a " + (a >= a));
        out.println("a <  a " + (a < a));
        out.println("a <= a " + (a <= a));

        out.println("a == b " + (a == b));
        out.println("a != b " + (a != b));
        out.println("a >  b " + (a > b));
        out.println("a >= b " + (a >= b));
        out.println("a <  b " + (a < b));
        out.println("a <= b " + (a <= b));

        out.println();
    }

    private static void testDouble(PrintStream out) {
        out.println("testDouble");
        final double a = 45.5;
        final double b = -7.11;
        final float fa = (float) a;
        final float fb = (float) b;
        final int ia = (int) a;
        final int ib = (int) b;

        out.println("a      " + a);
        out.println("b      " + b);
        out.println("-a     " + (-a));
        out.println("-b     " + (-b));
        out.println("a + b  " + (a + b));
        out.println("a - b  " + (a - b));
        out.println("a * b  " + (a * b));
        out.println("a / b  " + (a / b));
        out.println("sin(a) " + Math.sin(a));
        out.println("asin(a) " + Math.asin(a));
        out.println("cos(a) " + Math.cos(a));
        out.println("acos(a) " + Math.acos(a));
        out.println("tan(a) " + Math.tan(a));
        out.println("atan(a) " + Math.atan(a));
        out.println("sqrt(a) " + Math.sqrt(a));

        out.println("fa    " + fa);
        out.println("fb    " + fb);
        out.println("ia    " + ia);
        out.println("ib    " + ib);

        out.println("a == a " + (a == a));
        out.println("a != a " + (a != a));
        out.println("a >  a " + (a > a));
        out.println("a >= a " + (a >= a));
        out.println("a <  a " + (a < a));
        out.println("a <= a " + (a <= a));

        out.println("a == b " + (a == b));
        out.println("a != b " + (a != b));
        out.println("a >  b " + (a > b));
        out.println("a >= b " + (a >= b));
        out.println("a <  b " + (a < b));
        out.println("a <= b " + (a <= b));

        out.println();
    }
}
