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
 
package org.jnode.fs.jifs.command;

import javax.naming.NameNotFoundException;

import org.jnode.fs.jifs.*;

import java.io.InputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.FileSystem;
import org.jnode.naming.InitialNaming;
import org.jnode.fs.service.FileSystemService;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.driver.DeviceAlreadyRegisteredException;

/**
 * Just mounts initial JIFS on /Jifs
 * 
 * @author Andreas H\u00e4nel
 */
public class createJIFS implements Command{

    private static final Logger log = Logger.getLogger(createJIFS.class);

	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		log.info("Create jifs");
        try {
         	FileSystemService fSS = (FileSystemService) InitialNaming.lookup(FileSystemService.NAME);
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
}
