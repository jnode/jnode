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
 
package org.jnode.apps.jpartition.swingview;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.apps.jpartition.model.UserListener;

public class MainView extends JFrame {
    private static final long serialVersionUID = -7377987617244339590L;

    private final DefaultComboBoxModel devices;
    private final DeviceView deviceView;

    public MainView(ErrorReporter errorReporter, JComponent cmdProcessorView) throws Exception {
        setTitle("JPartition");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(cmdProcessorView, BorderLayout.SOUTH);

        deviceView = new DeviceView(errorReporter);
        add(deviceView, BorderLayout.CENTER);

        devices = new DefaultComboBoxModel(UserFacade.getInstance().getDeviceNames());
        final JComboBox cboDevices = new JComboBox(devices);
        cboDevices.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
                String item = selected ? String.valueOf(event.getItem()) : null;
                UserFacade.getInstance().selectDevice(item);
                deviceView.update();
            }
        });

        JPanel cboPanel = new JPanel(new BorderLayout());
        cboPanel.add(cboDevices, BorderLayout.CENTER);
        cboPanel.add(new JLabel("Device : "), BorderLayout.WEST);
        add(cboPanel, BorderLayout.NORTH);

        setSize(600, 300);
        setVisible(true);
        setLocation(300, 300);

        UserFacade.getInstance().setUserListener(new UserListener() {
            public void deviceAdded(String name) {
                devices.addElement(name);
                deviceView.update();
            }

            public void deviceRemoved(String name) {
                devices.removeElement(name);
                deviceView.update();
            }

            public void selectionChanged(Device selectedDevice) {
                cboDevices.setSelectedItem(selectedDevice.getName());
                deviceView.update();
            }
        });
    }
}
