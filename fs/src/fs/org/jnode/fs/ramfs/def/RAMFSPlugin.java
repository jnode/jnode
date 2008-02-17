package org.jnode.fs.ramfs.def;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.driver.DeviceException;
import org.jnode.driver.virtual.VirtualDevice;
import org.jnode.driver.virtual.VirtualDeviceFactory;
import org.jnode.fs.*;
import org.jnode.fs.service.FileSystemService;
import org.jnode.fs.ramfs.RAMFileSystem;
import org.jnode.fs.ramfs.RAMFileSystemType;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * This plugin creates a new Ram filesystem and mounts it to /jnode/
 *
 * @author peda
 */
public class RAMFSPlugin extends Plugin {

	/** RAMFS logger */
	private static final Logger log = Logger.getLogger(RAMFSPlugin.class);

	/**
	 * @param descriptor
	 */
	public RAMFSPlugin(PluginDescriptor descriptor) {
		super(descriptor);
        log.debug("RAMFSPlugin created.");
	}

	@Override
	protected void startPlugin() throws PluginException {

    	log.info("start ramfs");

        try {

        	FileSystemService fSS = InitialNaming.lookup(FileSystemService.NAME);
        	RAMFileSystemType type = RAMFileSystemType.getInstance();

            try {

                VirtualDevice dev = VirtualDeviceFactory.createDevice(RAMFileSystemType.VIRTUAL_DEVICE_NAME);

                log.info(dev.getId() + " registered");

            	final RAMFileSystem fs = type.create(dev, true);
                fSS.registerFileSystem(fs);

                final String mountPath = "jnode";

                fSS.mount(mountPath, fs, null);

                log.info("Mounted " + type.getName() + " on " + mountPath);

                FSDirectory root_dir = fs.getRootEntry().getDirectory();
                root_dir.addDirectory("home");
                root_dir.addDirectory("tmp");
                //adding files to /jnode/lib/ required by thecore classes
                FSDirectory libDir = (FSDirectory) root_dir.addDirectory("lib");
                InputStream is = RAMFSPlugin.class.getResourceAsStream("flavormap.properties");
                if(is != null){
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int c;
                    while((c = is.read(buf)) > -1)
                        baos.write(buf, 0, c);

                    is.close();
                    baos.flush();
                    baos.close();

                    ByteBuffer data = ByteBuffer.wrap(baos.toByteArray());
                    FSFile fmp = (FSFile)libDir.addFile("flavormap.properties");
                    fmp.write(0, data);
                    fmp.flush();                    
                }
            } catch (DeviceAlreadyRegisteredException ex){
            	log.error("RAMFS is allready running.");
            } catch (FileSystemException ex) {
            	log.error("Cannot mount " + type.getName() + " filesystem ", ex);
            } catch (DeviceException ex){
            	log.debug("DeviceExeption.", ex);
            } catch (IOException ex) {
            	log.error("Cannot mount RAMFS", ex);
            }
        } catch (NameNotFoundException e){
    	   	log.error("filsystemservice / filesystemtype not found");
        }
	}

	@Override
	protected void stopPlugin() throws PluginException {

		log.info("stop RAMFS");

		try {

			FileSystemService fSS = InitialNaming.lookup(FileSystemService.NAME);

			final DeviceManager dm = DeviceUtils.getDeviceManager();

            VirtualDevice dev = (VirtualDevice) dm.getDevice(RAMFileSystemType.VIRTUAL_DEVICE_NAME);
            fSS.unregisterFileSystem(dev);

    		log.info("RAMFS unmounted");

    		dm.unregister(dev);

    		log.info("RAMFS unregistered");

		} catch (NameNotFoundException e){
    	   	log.error("filsystemservice / filesystemtype not found");
        } catch (DeviceNotFoundException ex){
        	log.info("no ramfs present");
        } catch (DriverException ex){
        	log.error(ex);
        }
	}
}
