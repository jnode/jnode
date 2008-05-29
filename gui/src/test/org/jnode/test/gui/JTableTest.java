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

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * @author Levente S\u00e1ntha
 */
public class JTableTest {
    public static void main(String[] argv) {
        JFrame f = new JFrame("JTable Test");
        f.setSize(400, 400);
        f.add(new JScrollPane(new JTable(new DefaultTableModel(
            new Object[][]{
                {1, 2, 3, 4},
                {'a', 'b', 'c', 'd'},
                {5, 6, 7, 8},
                {11, 22, 33, 44},
                {55, 66, 66, 88},
            },
            new Object[]{'A', 'B', 'C', 'D'}))));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setVisible(true);
    }
}
