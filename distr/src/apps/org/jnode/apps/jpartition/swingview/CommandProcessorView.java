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
 
package org.jnode.apps.jpartition.swingview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jnode.apps.jpartition.commands.framework.Command;
import org.jnode.apps.jpartition.commands.framework.CommandProcessor;
import org.jnode.apps.jpartition.commands.framework.CommandProcessorListener;
import org.jnode.apps.jpartition.model.UserFacade;

public class CommandProcessorView extends JPanel implements CommandProcessorListener {
    /**
     * 
     */
    private static final long serialVersionUID = 4411987954528000167L;

    private DefaultListModel commands = new DefaultListModel();
    private JList commandsUI = new JList(commands);
    private JButton btnApply = new JButton("Apply");

    public CommandProcessorView() {
        setBorder(BorderFactory.createTitledBorder("pending commands"));

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.add(btnApply, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(btnPanel, BorderLayout.EAST);
        panel.add(new JScrollPane(commandsUI), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        btnApply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae_) {
                CommandProcessorView.this.setEnabled(false);
                UserFacade.getInstance().applyChanges();
            }
        });

        UserFacade.getInstance().addCommandProcessorListener(this);
    }

    protected void refreshCommand(Object command) {
        int index = commands.indexOf(command);
        commands.set(index, command);
    }

    public void commandAdded(CommandProcessor processor, Command command) {
        commands.addElement(command);
    }

    public void commandFinished(CommandProcessor processor, Command command) {
        refreshCommand(command);
    }

    public void commandRemoved(CommandProcessor processor, Command command) {
        commands.removeElement(command);
    }

    public void commandStarted(CommandProcessor processor, Command command) {
        refreshCommand(command);
    }
}
