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

package org.jnode.test.gui;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

/**
 * @author epr
 */
public class GeneralPathTest {

    public static void main(String[] args) {

        System.out.println("Simple");
        GeneralPath p1 = new GeneralPath();
        p1.moveTo(5, 5);
        for (int i = 0; i < 4000; i++) {
            p1.lineTo(i, 10);
        }

        System.out.println("Append");
        GeneralPath p2 = new GeneralPath();
        p2.append(p1, false);
        p1.lineTo(10, 10);

        System.out.println("Transform");
        p2.transform(AffineTransform.getScaleInstance(5, 5));
    }
}
