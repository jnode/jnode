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

package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AutoBoxTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        testBoolean();
        testByte();
        testChar();
        testShort();
        testInt();
        testLong();
        testFloat();
        testDouble();
    }

    private static void testBoolean() {
        System.out.println("Boolean");
        final boolean[] a = new boolean[]{true, false};

        Boolean i = a[0];
        System.out.println("i=" + i);
        a[1] = new Boolean(true);

        for (int k = 0; k < a.length; k++) {
            final Boolean j = a[k];
            System.out.println("j=" + j + " , class=" + j.getClass().getName());
        }
    }

    private static void testByte() {
        System.out.println("Byte");
        final byte[] a = new byte[]{1, 2, 3, 4, 5};

        Byte i = a[3];
        System.out.println("i=" + i);
        a[2] = new Byte((byte) 85);

        for (int k = 0; k < a.length; k++) {
            final Byte j = a[k];
            System.out.println("j=" + j + " , class=" + j.getClass().getName());
        }
    }

    private static void testChar() {
        System.out.println("Character");
        final char[] a = new char[]{'1', '2', '3', '4', '5'};

        Character i = a[3];
        System.out.println("i=" + i);
        a[2] = new Character('T');

        for (int k = 0; k < a.length; k++) {
            final Character j = a[k];
            System.out.println("j=" + j + " , class=" + j.getClass().getName());
        }
    }

    private static void testShort() {
        System.out.println("Short");
        final short[] a = new short[]{1, 2, 3, 4, 5};

        Short i = a[3];
        System.out.println("i=" + i);
        a[2] = new Short((short) 85);

        for (int k = 0; k < a.length; k++) {
            final Short j = a[k];
            System.out.println("j=" + j + " , class=" + j.getClass().getName());
        }
    }

    private static void testInt() {
        System.out.println("Int");
        final int[] a = new int[]{1, 2, 3, 4, 5};

        Integer i = a[3];
        System.out.println("i=" + i);
        a[2] = new Integer(85);

        for (int k = 0; k < a.length; k++) {
            final Integer j = a[k];
            System.out.println("j=" + j + " , class=" + j.getClass().getName());
        }
    }

    private static void testLong() {
        System.out.println("Long");
        final long[] a = new long[]{1, 2, 3, 4, 5};

        Long i = a[3];
        System.out.println("i=" + i);
        a[2] = new Long(85);

        for (int k = 0; k < a.length; k++) {
            final Long j = a[k];
            System.out.println("j=" + j + " , class=" + j.getClass().getName());
        }
    }

    private static void testFloat() {
        System.out.println("Float");
        final float[] a = new float[]{1, 2, 3, 4, 5};

        Float i = a[3];
        System.out.println("i=" + i);
        a[2] = new Float(85);

        for (int k = 0; k < a.length; k++) {
            final Float j = a[k];
            System.out.println("j=" + j + " , class=" + j.getClass().getName());
        }
    }

    private static void testDouble() {
        System.out.println("Double");
        final double[] a = new double[]{1, 2, 3, 4, 5};

        Double i = a[3];
        System.out.println("i=" + i);
        a[2] = new Double(85);

        for (int k = 0; k < a.length; k++) {
            final Double j = a[k];
            System.out.println("j=" + j + " , class=" + j.getClass().getName());
        }
    }
}
