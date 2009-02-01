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
 
package org.jnode.driver.bus.pci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * @author epr
 */
public class PCIDescriptors {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(PCIDescriptors.class);
    private final Map<Integer, VendorDescriptor> vendors;
    private static final PCIDescriptors instance = new PCIDescriptors();

    private PCIDescriptors() {
        try {
            vendors = readDevices();
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Gets my single instance
     */
    public static PCIDescriptors getInstance() {
        return instance;
    }

    /**
     * Gets the descriptor of the vendor with the given ID.
     *
     * @param vendorId
     */
    public VendorDescriptor findVendor(int vendorId) {
        VendorDescriptor result;
        result = (VendorDescriptor) vendors.get(new Integer(vendorId));
        if (result == null) {
            result = new VendorDescriptor(vendorId, "? (" + vendorId + ")");
        }
        return result;
    }

    private Map<Integer, VendorDescriptor> readDevices()
        throws IOException {
        log.debug("Loading PCI device info");
        InputStream is = ClassLoader.getSystemResourceAsStream("org/jnode/driver/pci/pci.ids");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader in = new BufferedReader(isr);
        HashMap<Integer, VendorDescriptor> vendors = new HashMap<Integer, VendorDescriptor>();
        VendorDescriptor lastVendor = null;
        DeviceDescriptor lastDevice = null;

        String line;

        while ((line = in.readLine()) != null) {
            line = stripComment(line);

            if (line.length() > 0) {
                int tabs = countTabs(line);
                line = line.trim();
                if (tabs == 0) {
                    // Vendor ID
                    lastVendor = parseVendor(line);
                    vendors.put(lastVendor.getId(), lastVendor);
                } else if (tabs == 1) {
                    // Device
                    lastDevice = parseDevice(line);
                    lastVendor.addDevice(lastDevice);
                } else {
                    // Subclass
                }
            }

        }

        return vendors;
    }

    private VendorDescriptor parseVendor(String line) {
        int idx = line.indexOf(' ');
        int id = Integer.parseInt(line.substring(0, idx), 16);
        return new VendorDescriptor(id, line.substring(idx + 1).trim());
    }

    private DeviceDescriptor parseDevice(String line) {
        int idx = line.indexOf(' ');
        int id = Integer.parseInt(line.substring(0, idx), 16);
        return new DeviceDescriptor(id, line.substring(idx + 1).trim());
    }

    private String stripComment(String line) {
        int idx = line.indexOf('#');
        if (idx >= 0) {
            line = line.substring(0, idx);
        }
        return line;
    }

    private int countTabs(String line) {
        final int len = line.length();
        int count = 0;
        for (int i = 0; i < len; i++) {
            if (line.charAt(i) == '\t') {
                count++;
            } else {
                return count;
            }
        }
        return count;
    }
}
