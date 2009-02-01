/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.test.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogTest extends Dialog {


    /**
     * @param title
     */
    public DialogTest(String title) {
        super(new Frame(), title);
        setModal(true);
        final Button ok = new Button("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                DialogTest.this.dispose();
            }

        });
        add(new TextField(), BorderLayout.CENTER);
        add(ok, BorderLayout.EAST);

        setSize(200, 100);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        DialogTest dlg = new DialogTest("Dialog test");
        dlg.setVisible(true);
        System.exit(0);
    }

}
