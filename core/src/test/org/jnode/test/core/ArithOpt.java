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

package org.jnode.test.core;

import org.jnode.util.StopWatch;

/**
 * @author epr
 */
public class ArithOpt {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final int cnt = (args.length > 0) ? Integer.parseInt(args[0]) : 1;
        final StopWatch sw = new StopWatch();
        int i = run();
        for (int x = 1; x < cnt; x++) {
            if (run() != i) {
                throw new RuntimeException("Result differs");
            }
        }
        sw.stop();
        System.out.println("Result : " + i + " took " + sw + " for " + cnt + " loops");
    }

    private static int run() {
        int a, b, c, d, e, f, g, h, i, j, k;
        i = 0;
        for (int z = 0; z < 500000; z++) {
            a = 17;
            b = 67;
            c = 53;
            d = 43;
            e = a + b;
            e = e + e;
            d = d + c;
            f = 5 * a;
            g = 6 * a;
            f = b + 2;
            h = f + g;
            i = g + f * h - 2 * f;
            j = 3 * h;
            k = j / 2;
            i = j * k - e - d + f * c;
        }
        return i;
    }

}
