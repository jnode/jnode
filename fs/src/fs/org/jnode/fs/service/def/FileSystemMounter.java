/*
 * $Id$
 */
package org.jnode.fs.service.def;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.RemovableDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginException;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;

/**
 * A FileSystemMounter listens to the DeviceManager and once a Device that
 * implements the BlockDeviceAPI is started, it tries to mount a FileSystem on
 * that device.
 * 
 * @author epr
 */
public class FileSystemMounter implements DeviceListener, QueueProcessor {

    /** My logger */
    private static final Logger log = Logger.getLogger(FileSystemMounter.class);

    /** The DeviceManager i'm listening to */
    private DeviceManager devMan;

    /** The FileSystemService i'm using */
    private FileSystemService fileSystemService;

    /** Mapping between a device and a mounted FileSystem */
    private final HashMap devices2FS = new HashMap();

    private QueueProcessorThread asynchronousMounterThread;

    private final Queue devicesWaitingToBeMounted = new Queue();

    /** Number of devices added to the mount queue */
    private int devicesAdded;

    /** Number of devices actually processed by the mount processor */
    private int devicesProcessed;

    /**
     * Start the FS mounter.
     * 
     * @throws PluginException
     */
    public void start() throws PluginException {
        try {
            devMan = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);
            devMan.addListener(this);
            fileSystemService = (FileSystemService) InitialNaming
                    .lookup(FileSystemService.NAME);
            asynchronousMounterThread = new QueueProcessorThread(
                    "Asynchronous-FS-Mounter", devicesWaitingToBeMounted, this);
            asynchronousMounterThread.start();
        } catch (NameNotFoundException ex) {
            throw new PluginException("Cannot find DeviceManager", ex);
        }

    }

    /**
     * Stop the FS mounter.
     */
    public void stop() {
        devMan.removeListener(this);
        asynchronousMounterThread.stopProcessor();
        asynchronousMounterThread = null;
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
     */
    public final void deviceStarted(Device device) {
        // add it to the queue of devices to be mounted only if the action is
        // not
        // already pending
        synchronized (devicesWaitingToBeMounted) {
            devicesWaitingToBeMounted.add(device);
            devicesAdded++;
        }
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
     */
    public final void deviceStop(Device device) {
        final FileSystem fs;
        synchronized (this) {
            fs = (FileSystem) devices2FS.get(device);
            devices2FS.remove(device);
        }
        if (fs != null) {
            try {
                fs.close();
            } catch (IOException ex) {
                log.error("Cannot close filesystem", ex);
            }
        }
    }

    /**
     * Try to mount a filesystem on the given device.
     * 
     * @param device
     * @param api
     */
    protected void tryToMount(Device device, FSBlockDeviceAPI api,
            boolean removable) {

        synchronized (this) {
            if (devices2FS.containsKey(device)) {
                log.info("device already mounted...");
                return;
            }
        }

        //if (removable) {
        //	log.error("Not mounting removable devices yet...");
        // TODO Implement mounting of removable devices
        //	return;
        //}

        log.info("Try to mount " + device.getId());
        // Read the first sector
        try {
            final byte[] bs = new byte[ api.getSectorSize()];
            api.read(0, bs, 0, bs.length);
            for (Iterator i = fileSystemService.fileSystemTypes().iterator(); i
                    .hasNext();) {
                final FileSystemType fst = (FileSystemType) i.next();
                // 
                if (fst.supports(api.getPartitionTableEntry(), bs)) {
                    try {
                        final FileSystem fs = fst.create(device);
                        fileSystemService.registerFileSystem(fs);
                        log.info("Mounted " + fst.getName() + " on "
                                + device.getId());
                        devices2FS.put(device, fs);
                        return;
                    } catch (FileSystemException ex) {
                        log.error("Cannot mount " + fst.getName()
                                + " filesystem on " + device.getId(), ex);
                    }
                }
            }
            log.info("No filesystem found for " + device.getId());
        } catch (IOException ex) {
            log.error("Cannot read bootsector of " + device.getId());
        }
    }

    /**
     * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
     */
    public void process(Object queuedObject) throws Exception {
        Device device = (Device) queuedObject;
        try {
            FSBlockDeviceAPI api = (FSBlockDeviceAPI) device
                    .getAPI(FSBlockDeviceAPI.class);
            if (device.implementsAPI(RemovableDeviceAPI.class)) {
                tryToMount(device, api, true);
            } else {
                tryToMount(device, api, false);
            }
        } catch (ApiNotFoundException ex) {
            // Just ignore this device.
        } finally {
            devicesProcessed++;
        }
    }
    
    /**
     * Is the mounter ready.
     * @return
     */
    public boolean isReady() {
        return (devicesAdded == devicesProcessed);
    }
}
