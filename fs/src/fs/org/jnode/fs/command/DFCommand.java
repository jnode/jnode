package org.jnode.fs.command;

import java.io.InputStream;
import java.io.PrintStream;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.fs.FileSystem;
import org.jnode.fs.jfat.FatFileSystem;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Syntax;

public class DFCommand extends AbstractCommand {

	 private static final Logger log = Logger.getLogger ( DFCommand.class );
	
	public static Help.Info HELP_INFO =
		new Help.Info(
			"device",
			new Syntax[] {
				new Syntax("Print disk usage about all devices")});
	
	public void execute(CommandLine commandLine, InputStream in,
			PrintStream out, PrintStream err) throws Exception {
		//ParsedArguments cmdLine = HELP_INFO.parse(commandLine);
		print(out);
	}
	
	private void print(PrintStream out) throws NameNotFoundException {
		final DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);
		final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
		StringBuffer b = new StringBuffer();
		b.append("ID")
   	 	.append("\t").append("Total")
   	 	.append("\t").append("Use")
   	 	.append("\t").append("Free")
   	 	.append("\n");
		FileSystem fs;
		for (Device dev : dm.getDevices()) {
			// Is device contains a filesystem ?
			fs = fss.getFileSystem(dev);
			long total, free, use;
	        if (fs != null) {
	        	log.debug("Check device : " + dev.getId());
	        	total = fs.getTotalSpace();
	        	if(total > 0){
					free = fs.getFreeSpace();
					use = total - free;
		        	 b.append(dev.getId())
		        	 .append("\t").append(total)
		        	 .append("\t").append(use)
		        	 .append("\t").append(free) 
		        	 .append("\n");
	        	}
	        }
		}
		out.print(b.toString());
	}

}
