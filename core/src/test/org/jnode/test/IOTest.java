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

import java.io.File;

/**
 * @author epr
 */
public class IOTest {

    public static void main(String[] args) {
        File[] roots = File.listRoots();

        System.out.println("FS Roots:");
        for (int i = 0; i < roots.length; i++) {
            System.out.println("[" + i + "]: " + roots[i]);
            printDir(roots[i], 1);
        }
    }

    public static void printDir(File dir, int level) {
        final File[] list = dir.listFiles();
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                final File f = list[i];
                if (f.isDirectory()) {
                    System.out.println(tabs(level) + "[" + f.getName() + "]");
                    printDir(f, level + 1);
                } else {
                    System.out.println(tabs(level) + f.getName() + " " + f.length());
                }
            }
            System.out.println(tabs(level) + " -- total of " + list.length + " files --");
        } else {
            System.out.println("list == null in (" + dir + ").list");
        }
    }

    private static String tabs(int level) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < level; i++) {
            b.append("  ");
        }
        return b.toString();
    }

}
 
