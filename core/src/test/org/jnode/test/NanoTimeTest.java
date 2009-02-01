/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.test;


/**
 * @author Peter
 */
public class NanoTimeTest {

    public static void main(String[] args) {
        NanoTimeTest ntt = new NanoTimeTest();
        ntt.run(500);
        ntt.run(1000);
        ntt.run(1500);
        ntt.run(2000);
        ntt.run(2500);
        ntt.run(3000);
    }

    public void run(int ms) {
        long start = System.nanoTime();
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.nanoTime();
        long nano = end - start;

        System.out.println("The test ran " + ms + "ms and according to " +
            "System.nanoTime that was " + nano + "ns");
        long p = Math.abs(ms * 1000L - nano) / ms;
        System.out.println("Aberration : " + p + " promill");
    }
}
