package org.jnode.fs.ramfs.def;

import java.io.IOException;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.fs.ramfs.RAMFSDevice;
import org.jnode.fs.ramfs.RAMFSDriver;
import org.jnode.fs.ramfs.RAMFileSystemType;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

public class RAMFSPlugin extends Plugin {

	/** RAMFS logger */
	private static final Logger log = Logger.getLogger(RAMFSPlugin.class);

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
         	
            	final RAMFSDevice dev = new RAMFSDevice();
            	dev.setDriver(new RAMFSDriver());
            	
            	final DeviceManager dm = DeviceUtils.getDeviceManager();
            	dm.register(dev);
			    
            	log.info(dev.getId() + " registered");
         		
            	final FileSystem fs = type.create(dev, true);
                fSS.registerFileSystem(fs);

                final String mountPath = "ramfs";
                fSS.mount(mountPath, fs, null);
                
                log.info("Mounted " + type.getName() + " on " + mountPath);
                            
            } catch (DeviceAlreadyRegisteredException ex){
            	log.error("RAMFS is allready running.");
            } catch (FileSystemException ex) {
            	log.error("Cannot mount " + type.getName() + " filesystem ", ex);
            } catch (DriverException e){
            	log.debug("DriverExeption.");
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
    		
			RAMFSDevice dev = (RAMFSDevice) dm.getDevice("nulldevice");
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
