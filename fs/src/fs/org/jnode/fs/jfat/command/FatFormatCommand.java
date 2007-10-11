package org.jnode.fs.jfat.command;

/*
 * $Id: FatFormatCommand.java  2007-07-27 +0100 (s,27 JULY 2007) Tanmoy Deb $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
import java.io.InputStream;
import java.io.PrintStream;
import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.jfat.FatFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.argument.DeviceArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.argument.OptionArgument;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;

/**
 * @author Tango
 * <p>
 * The FAT32 formating  command.
 *
 */
public class FatFormatCommand implements Command{
	 private static final Logger log =
        Logger.getLogger ( FatFormatCommand.class );
	 
	 static final DeviceArgument   ARG_DEVICE     =  new DeviceArgument("device-id","the device to print informations about");
	 
	 static final Parameter        PARAM_DEVICE   =  new Parameter(ARG_DEVICE,Parameter.MANDATORY);
	 
	 static final OptionArgument   TYPE           =  new OptionArgument("action","Type parameter",
			                                         new OptionArgument.Option[] {
			                                         new OptionArgument.Option("-c","Specify Sector Per Cluster Value") });
	 static final Parameter        PARAM_TYPE     =  new Parameter(TYPE, Parameter.OPTIONAL);

	 static final OptionArgument   BS_VAL         =  new OptionArgument("SectorPerClusterValue","Setting The Cluster Size",
			                                         new OptionArgument.Option[] {
			                                         new OptionArgument.Option("1",   "1Kb"),
	                                                 new OptionArgument.Option("2",   "2Kb"),
	                                                 new OptionArgument.Option("4",   "4Kb"),
	                                                 new OptionArgument.Option("8",   "8Kb"),
	                                                 new OptionArgument.Option("16",  "16Kb"),
	                                                 new OptionArgument.Option("32",  "32Kb"),
	                                                 new OptionArgument.Option("64",  "64Kb")});
	
	 static final Parameter       PARAM_BS_VAL    =  new Parameter(BS_VAL,Parameter.OPTIONAL);	 
	 
	 public static Help.Info      HELP_INFO       =  new Help.Info("mkjfat",
	                                                 new Syntax[] {
			                                         new Syntax("Format a block device with a specified type.Enter the Cluster Size as 1 for 1KB. ",
	                                                 new Parameter[] { PARAM_TYPE, PARAM_BS_VAL, PARAM_DEVICE}) });

	 
	 
	 
	 public static void main(String[] args) throws Exception{
		 new FatFormatCommand().execute(new CommandLine(args), System.in, System.out, System.err); 
	 }
	 
	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		    try {		    
            System.out.println("mkjfat:JFAT Formatter.  Version :1.0");
			ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());

            String device = ARG_DEVICE.getValue(cmdLine);            
            Integer bsize;
            try {
                bsize = Integer.valueOf(BS_VAL.getValue(cmdLine));
            } catch (NumberFormatException nfe) {
                bsize=4;//set to the default cluster size
            	log.error("The Cluster of "+bsize+" size not available.");
            }            
            
            String fsTypeName;
            Object params;             
            
            fsTypeName = FatFileSystemType.NAME;            
            params = new Integer(bsize);          
            
            DeviceManager dm;
            dm = InitialNaming.lookup(DeviceManager.NAME);

            Device dev = dm.getDevice(device);
			if(!(dev.getDriver() instanceof FSBlockDeviceAPI)){
				throw new FileSystemException(
                	"Unsupported device by format command");
				}			
			FileSystemService fileSystemService = InitialNaming
            .lookup(FileSystemService.NAME);			
            FileSystemType type = fileSystemService
            .getFileSystemTypeForNameSystemTypes(fsTypeName);
           
            type.format(dev, params);
           
           // restart the device
           dm.stop(dev);          
           dm.start(dev);         
           log.info("Done.");//I will keep it 
		} catch (NameNotFoundException e) {
            log.error("The JFAT name not found.");
        } catch (DeviceNotFoundException e) {
            log.error("The Device not found.");
        } catch (DriverException e) {
            log.error("The Driver Exception.");
        } catch (FileSystemException e) {        	
            log.error("The File System Exception");
            
        }
	}
	

	
	
	
}	
	


