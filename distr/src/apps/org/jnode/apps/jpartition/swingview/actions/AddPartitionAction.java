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
 
package org.jnode.apps.jpartition.swingview.actions;

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.apps.jpartition.swingview.PartitionView;

public class AddPartitionAction extends AbstractAction<PartitionView> {
    private static final long serialVersionUID = 1240081461305707910L;

    public AddPartitionAction(ErrorReporter errorReporter, PartitionView view) {
        super("add a partition", errorReporter, view);
    }

    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final Device dev = UserFacade.getInstance().getSelectedDevice();

                final JDialog dlg = new JDialog((Dialog) null, "create new partition", true);
                final SpinnerNumberModel spn = new SpinnerNumberModel(10, 1, dev.getSize(), 10);
                dlg.getContentPane().setLayout(new FlowLayout());
                dlg.getContentPane().add(new JLabel("size : "));
                dlg.getContentPane().add(new JSpinner(spn));
                JButton btn = new JButton("Add partition");
                btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        long size = spn.getNumber().longValue();
                        try {
                            UserFacade.getInstance().createPartition(
                                    view.getPartition().getStart(), size);
                        } catch (Exception e1) {
                            errorReporter.reportError(log, AddPartitionAction.this, e1);
                        }
                        dlg.dispose();
                        view.getDeviceView().update();
                    }
                });
                dlg.getContentPane().add(btn);
                dlg.setBounds(200, 200, 300, 100);
                dlg.setVisible(true);
            }
        });
    }
}
