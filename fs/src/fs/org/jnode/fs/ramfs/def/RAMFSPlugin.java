package org.jnode.fs.ramfs.def;

import java.io.IOException;

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
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.service.FileSystemService;
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
            FileSystemType type = fSS.getFileSystemTypeForNameSystemTypes(RAMFileSystemType.NAME);

            try {

                VirtualDevice dev = VirtualDeviceFactory.createDevice(RAMFileSystemType.VIRTUAL_DEVICE_NAME);

                log.info(dev.getId() + " registered");
         		
            	final FileSystem fs = type.create(dev, true);
                fSS.registerFileSystem(fs);

                final String mountPath = "jnode";

                fSS.mount(mountPath, fs, null);
                
                log.info("Mounted " + type.getName() + " on " + mountPath);

                FSDirectory root_dir = fs.getRootEntry().getDirectory();
                root_dir.addDirectory("home");
                root_dir.addDirectory("tmp");
                            
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
        } catch (FileSystemException e){
        	log.error(e);
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
