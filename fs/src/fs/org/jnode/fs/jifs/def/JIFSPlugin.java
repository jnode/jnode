/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.fs.jifs.def;

import org.apache.log4j.Logger;
import javax.naming.NameNotFoundException;

import org.jnode.fs.jifs.*;

import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceAlreadyRegisteredException;

import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.FileSystem;
import org.jnode.fs.service.FileSystemService;

import org.jnode.naming.InitialNaming;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Andreas H\u00e4nel
 */
public class JIFSPlugin extends Plugin{

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	/** Manager of Extensions */
    private final JIFSExtension jifsExtension;
	
	public JIFSPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        this.jifsExtension = new JIFSExtension(descriptor
                .getExtensionPoint("info"));
        log.debug("JIFSPlugin created.");
    }
	
	/**
     * Start this plugin
     */
    protected void startPlugin() throws PluginException {
    	log.info("start jifs");
        try {
         	FileSystemService fSS = InitialNaming.lookup(FileSystemService.NAME);
         	FileSystemType type = fSS.getFileSystemTypeForNameSystemTypes(JIFileSystemType.NAME);
         	try {
         				final JIFSDevice dev = new JIFSDevice();
            			dev.setDriver(new JIFSDriver());
            			final DeviceManager dm = DeviceUtils.getDeviceManager();
            			dm.register(dev);
			            log.info(dev.getId() + " registered");
         			    final FileSystem fs = type.create(dev, true);
                        fSS.registerFileSystem(fs);
                        log.info("Mounted " + type.getName() + " on "
                                + dev.getId());
                        return;
         			} catch (DeviceAlreadyRegisteredException ex){
         				log.error("jifs is currently running.");
         				return;
         			} catch (FileSystemException ex) {
                        log.error("Cannot mount " + type.getName()
                                + " filesystem ", ex);
                        return;
                    } catch (DriverException e){
                    	log.debug("Got DriverException, maybe jifs is running.");
                    	return;
                    } 
                    
	    } catch (NameNotFoundException e){
    	   	log.error("filsystemservice / filesystemtype not found");
        } catch (FileSystemException e){
        	log.error(e);
        }
    }

    /**
     * Stop this plugin
     */
    protected void stopPlugin() {
    	log.info("stop jifs");
		try {
         	FileSystemService fSS = (FileSystemService) InitialNaming.lookup(FileSystemService.NAME);
    		final DeviceManager dm = DeviceUtils.getDeviceManager();
    		JIFSDevice dev = (JIFSDevice)dm.getDevice("jifs");
    		fSS.unregisterFileSystem(dev);
    		log.info("FIFS unmounted");
    		dm.unregister(dev);
    		log.info("jifs unregistered");
	    } catch (NameNotFoundException e){
    	   	log.error("filsystemservice / filesystemtype not found");
        } catch (DeviceNotFoundException ex){
        	log.info("no jifs present");
        } catch (DriverException ex){
        	log.error(ex);
        }
    }
	
}