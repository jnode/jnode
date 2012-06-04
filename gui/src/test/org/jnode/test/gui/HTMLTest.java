/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * HTML rendering test.
 *
 * @author Levente S\u00e1ntha
 */
public class HTMLTest {
    public static void main(String[] argv) throws Exception {
        JFrame f = new JFrame("HTML test");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setSize(400, 400);
        JEditorPane ep = new JEditorPane();
        f.add(new JScrollPane(ep));
        if (argv.length == 0) {
            ep.setText(
                "No arguments found on command line\n\norg.jnode.test.gui.HTMLTest <url>\n\turl\tURL of page to load.");
        } else {
            ep.setPage(argv[0]);
        }
        f.setVisible(true);
    }
}
