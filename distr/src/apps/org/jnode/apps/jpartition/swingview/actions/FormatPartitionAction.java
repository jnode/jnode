/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.apps.jpartition.swingview.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.apps.jpartition.swingview.PartitionView;

public class FormatPartitionAction extends AbstractAction<PartitionView> {
    private static final long serialVersionUID = 4100228292937417784L;

    public FormatPartitionAction(ErrorReporter errorReporter, PartitionView view) {
        super("format the partition", errorReporter, view);
    }

    public void actionPerformed(ActionEvent e) {
        final Component parent = (Component) e.getSource();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Object[] options = UserFacade.getInstance().getFormatters();
                String option =
                        (String) JOptionPane.showInputDialog(parent, "Select a format",
                                "Format selection", JOptionPane.PLAIN_MESSAGE, null, options,
                                options[0]);

                if ((option != null) && (option.length() > 0)) {
                    UserFacade.getInstance().selectFormatter(option);
                    Device dev = UserFacade.getInstance().getSelectedDevice();
                    try {
                        UserFacade.getInstance().formatPartition(dev.getStart());
                    } catch (Exception e1) {
                        errorReporter.reportError(log, FormatPartitionAction.this, e1);
                    }
                    view.update();
                }
            }
        });
    }
}
