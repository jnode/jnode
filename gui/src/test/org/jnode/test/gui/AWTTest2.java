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

import java.awt.BorderLayout;
import java.awt.Frame;

/**
 * @author epr
 */
public class AWTTest2 {

    public static void main(String[] args) throws InterruptedException {
        int sleep = 5;
        try {
            sleep = Integer.parseInt(args[0]);
        } catch (Exception e) {
            //ignore
        }

        Frame wnd = new Frame();
        try {
            wnd.setSize(600, 400);
            BoxWorld bw = new BoxWorld();
            bw.init();
            wnd.add(bw, BorderLayout.CENTER);
            wnd.setVisible(true);
            bw.requestFocus();
            Thread.sleep(sleep * 1000);
            wnd.setVisible(false);
        } finally {
            wnd.dispose();
        }
    }
}
