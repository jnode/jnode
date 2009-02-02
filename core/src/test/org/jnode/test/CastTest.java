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

import java.util.Vector;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CastTest {

    public static void main(String[] args) {
        test(args);
        test2("Hello world");
    }

    private StaticClass field;

    public void test1(Vector<?> entries, int i) {
        field = (StaticClass) entries.get(i);
    }

    static class StaticClass {
        static {
            System.out.println(1);
        }
    }

    public static void test(Object args) {
        System.out.println("args.class=" + args.getClass().getName());
        Object[] arr = (Object[]) args;
        System.out.println(arr);

        if (args instanceof String[]) {
            System.out.println("Instanceof");
        } else {
            System.out.println("Not instanceof: " + args.getClass().getName());
        }
    }

    public static void test2(Object arg) {
        try {
            ((Integer) arg).intValue();
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }
}
