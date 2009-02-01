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
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.utils.device.DeviceUtils;
import org.jnode.driver.bus.ide.IDEDevice;

public class FileDeviceView extends JFrame {
    private static final Logger log = Logger.getLogger(FileDeviceView.class);

    private final ErrorReporter errorReporter;

    private JList devicesList = new JList(new DefaultListModel());
    private JPanel buttons = new JPanel();
    private JButton addVMWareDiskButton = new JButton("add VMWare disk");
    private JButton addFakeDiskButton = new JButton("add fake disk");
    private JButton removeButton = new JButton("remove device");
    private List<IDEDevice> fileDevices = new ArrayList<IDEDevice>();

    public FileDeviceView(ErrorReporter errorReporter) throws Exception {
        this.errorReporter = errorReporter;

        setTitle("File devices");
        setLayout(new BorderLayout());
        add(new JScrollPane(devicesList), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        buttons.add(addVMWareDiskButton);
        buttons.add(addFakeDiskButton);
        buttons.add(removeButton);

        addFakeDiskButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                FileDeviceView.this.fileDevices.add(DeviceUtils
                        .createFakeDevice(FileDeviceView.this.errorReporter));
            }
        });
        addVMWareDiskButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                FileDeviceView.this.fileDevices.add(DeviceUtils
                        .createVMWareDevice(FileDeviceView.this.errorReporter));
            }
        });
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (devicesList.getSelectedIndex() >= 0) {
                    Object device = devicesList.getSelectedValue();
                    if (device != null) {
                        FileDeviceView.this.fileDevices.remove(device);
                    }
                }
            }
        });

        setSize(600, 300);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void addDevice(Object device) {
        ((DefaultListModel) devicesList.getModel()).addElement(device);
    }

    public void removeDevice(Object device) {
        ((DefaultListModel) devicesList.getModel()).removeElement(device);
    }
}
