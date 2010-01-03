/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import org.jnode.vm.MathSupport;

public class LongTest {
    public static void main(String[] argv) {
        divtest(Long.MIN_VALUE, Long.MIN_VALUE);
        divtest(Long.MIN_VALUE, Long.MAX_VALUE);
        divtest(Long.MAX_VALUE, Long.MIN_VALUE);
        divtest(Long.MAX_VALUE, Long.MAX_VALUE);

        divtest(0, Long.MIN_VALUE);
        divtest(0, Long.MAX_VALUE);

        divtest(1, Long.MIN_VALUE);
        divtest(1, Long.MAX_VALUE);

        divtest(-1, Long.MIN_VALUE);
        divtest(-1, Long.MAX_VALUE);

        int bound = 1000;

        divtestLoop(Long.MIN_VALUE, Long.MIN_VALUE + bound);
        divtestLoop(Long.MAX_VALUE - bound, Long.MAX_VALUE);
        divtestLoop(-bound, bound);
    }

    private static void divtestLoop(long min, long max) {
        for (long i = min; i < max; i++) {
            divtest(Long.MIN_VALUE, i);
        }

        for (long i = min; i < max; i++) {
            divtest(Long.MAX_VALUE, i);
        }

        for (long i = min; i < max; i++) {
            divtest(0, i);
        }

        for (long i = min; i < max; i++) {
            divtest(1, i);
        }

        for (long i = min; i < max; i++) {
            divtest(-1, i);
        }
    }

    private static void divtest(long num, long den) {
        if (den == 0) return;
        long q1 = num / den;
        long q2 = MathSupport.ldiv(num , den);
        if (q1 != q2) {
            System.out.println("error for " + num + " / " + den);
            System.out.println(q1);
            System.out.println(q2);
        }
        q1 = num % den;
        q2 = MathSupport.lrem(num, den);
        if (q1 != q2) {
            System.out.println("error for " + num + " % " + den);
            System.out.println(q1);
            System.out.println(q2);
        }
    }
}
