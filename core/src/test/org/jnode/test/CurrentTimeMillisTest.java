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

/**
 * @author epr
 */
public class CurrentTimeMillisTest {

    public static void main(String[] args) {

        System.out.println("Testing System.currentTimeMillis please wait");

        for (int i = 0; i < 1000; i++) {
            final long start = System.currentTimeMillis();

            final long end = System.currentTimeMillis();

            int count = 0;
            for (int j = 0; j < 50000; j++) {
                count += j;
            }

            final long diff = end - start;

            if (diff < 0) {
                System.out.println("Oops currentTimeMillis goes back in time " + diff + "ms");
            }

        }

        System.out.println("done");
    }
}
