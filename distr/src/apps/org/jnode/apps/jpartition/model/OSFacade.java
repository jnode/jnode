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
 
package org.jnode.apps.jpartition.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.block.PartitionableBlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.fs.FileSystem;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;

public class OSFacade {
    private static final Logger LOG = Logger.getLogger(OSFacade.class);

    private static Comparator<Partition> PARTITION_COMPARATOR = new Comparator<Partition>() {
        public int compare(Partition p1, Partition p2) {
            // we assume here that the partition doesn't intersect
            return (int) (p1.getStart() - p2.getStart());
        }
    };

    @SuppressWarnings("unchecked")
    private static final Class<PartitionableBlockDeviceAPI> REQUIRED_API =
            PartitionableBlockDeviceAPI.class;

    static {
        LOG.setLevel(Level.DEBUG);
    }

    private static final OSFacade INSTANCE;
    static {
        INSTANCE = new OSFacade();
    }

    private OSFacade() {
    }

    static final OSFacade getInstance() {
        return INSTANCE;
    }

    final void setOSListener(final OSListener listener) throws OSFacadeException {
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }

        try {
            DeviceUtils.getDeviceManager().addListener(new DeviceListener() {
                public void deviceStarted(org.jnode.driver.Device device) {
                    if (device instanceof IDEDevice) {
                        Device dev = null;
                        try {
                            dev = createDevice(device);
                            if (dev != null) {
                                listener.deviceAdded(dev);
                            }
                        } catch (OSFacadeException e) {
                            listener.errorHappened(e);
                        }
                    }
                }

                public void deviceStop(org.jnode.driver.Device device) {
                    if (device instanceof IDEDevice) {
                        Device dev = null;
                        try {
                            dev = createDevice(device);
                            if (dev != null) {
                                listener.deviceRemoved(dev);
                            }
                        } catch (OSFacadeException e) {
                            listener.errorHappened(e);
                        }
                    }
                }
            });
        } catch (NameNotFoundException e) {
            throw new OSFacadeException("error in setOSListener", e);
        }
    }

    final List<Device> getDevices() throws OSFacadeException {
        List<Device> devices = new ArrayList<Device>();
        try {
            DeviceManager devMan = org.jnode.driver.DeviceUtils.getDeviceManager();
            for (org.jnode.driver.Device dev : devMan.getDevicesByAPI(REQUIRED_API)) {
                Device device = createDevice(dev);
                if (device != null) {
                    devices.add(device);
                }
            }
        } catch (NameNotFoundException e) {
            throw new OSFacadeException("error in getDevices", e);
        }

        return devices;
    }

    private Device createDevice(org.jnode.driver.Device dev) throws OSFacadeException {
        LOG.debug("createDevice: wrapping device " + dev.getId());

        Device device = null;
        List<IBMPartitionTableEntry> partitions = getPartitions(dev);
        if (partitions != null) { // null if not supported
            LOG.debug("createDevice: nbPartitions=" + partitions.size());

            long devSize = 0;
            try {
                devSize = dev.getAPI(REQUIRED_API).getLength();
            } catch (ApiNotFoundException e) {
                throw new OSFacadeException("error in createDevice", e);
            } catch (IOException e) {
                throw new OSFacadeException("error in createDevice", e);
            }

            // one empty partition taking all place
            List<Partition> devPartitions = new ArrayList<Partition>(partitions.size());
            devPartitions.add(new Partition(0L, devSize, false));

            // add used partitions
            device = new Device(dev.getId(), devSize, dev, devPartitions);
            for (IBMPartitionTableEntry e : partitions) {
                IBMPartitionTableEntry pte = (IBMPartitionTableEntry) e;

                long start = pte.getStartLba();
                long size = pte.getNrSectors() * IDEConstants.SECTOR_SIZE;
                device.addPartition(start, size); // add a non-empty partition
            }
        }

        LOG.debug("createDevice: return device=" + device);
        return device;
    }

    private List<IBMPartitionTableEntry> getPartitions(org.jnode.driver.Device dev)
        throws OSFacadeException {
        boolean supported = false;
        List<IBMPartitionTableEntry> partitions = new ArrayList<IBMPartitionTableEntry>();

        try {
            if (dev.implementsAPI(REQUIRED_API)) {
                PartitionableBlockDeviceAPI<?> api = dev.getAPI(REQUIRED_API);
                boolean supportedPartitions = true;

                for (PartitionTableEntry e : api.getPartitionTable()) {
                    if (!(e instanceof IBMPartitionTableEntry)) {
                        // non IBM partition tables are not handled for now
                        supportedPartitions = false;
                        break;
                    }

                    IBMPartitionTableEntry entry = (IBMPartitionTableEntry) e;
                    if (entry.isValid() && !entry.isEmpty()) {
                        partitions.add(entry);
                    }
                }

                supported = supportedPartitions;
            }
        } catch (ApiNotFoundException e) {
            throw new OSFacadeException("error in getPartitions", e);
        } catch (IOException e) {
            throw new OSFacadeException("error in getPartitions", e);
        }

        return supported ? partitions : null;
    }

    private FileSystem<?> getFileSystem(org.jnode.driver.Device dev, PartitionTableEntry pte) {
        /*
         * DeviceManager devMan = InitialNaming.lookup(DeviceManager.NAME);
         * FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
         * Collection<Device> devices = devMan.getDevices(); FileSystem fs =
         * null;
         * 
         * for (Device device : devices) { if (device instanceof
         * IDEDiskPartitionDevice) { IDEDiskPartitionDevice partition =
         * (IDEDiskPartitionDevice)device; if ((partition.getParent() == dev) &&
         * (partition.getPartitionTableEntry() == pte)) { //fs =
         * fss.getFileSystem(device).get; break; } } }
         * 
         * return fs;
         */
        return null; // TODO
    }
}
