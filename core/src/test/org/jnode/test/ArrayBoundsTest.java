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
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ArrayBoundsTest {

    public static void main(String[] args) {
        test(args, -1, false);
        test(args, args.length, false);
        if (args.length > 0) {
            test(args, 0, true);
            test(args, args.length - 1, true);
        }
    }

    private static void test(String[] arr, int index, boolean ok) {
        try {
            System.out.println(arr[index]);
            if (!ok) {
                throw new RuntimeException("Test should fail at index " + index);
            } else {
                System.out.println("Ok");
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (ok) {
                throw ex;
            } else {
                System.out.println("Ok: " + ex.getMessage());
            }
        }
    }

}
