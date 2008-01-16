package org.jnode.apps.jpartition.consoleview;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.Context;
import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.consoleview.components.Options;
import org.jnode.apps.jpartition.consoleview.components.YesNo;
import org.jnode.apps.jpartition.model.Partition;
import org.jnode.apps.jpartition.model.UserFacade;

class ConsoleView {
	private static final Logger log = Logger.getLogger(ConsoleView.class);
	
	private final Context context;
	private final boolean install;
	
	ConsoleView(InputStream in, PrintStream out, ErrorReporter errorReporter, boolean install)
	{
		this.context = new Context(in, out, errorReporter);
		this.install = install;
		
		try {
			start();
		} catch (Throwable e) {
			errorReporter.reportError(log, this, e);
		}
	}
	
	private void start() throws Exception
	{
		selectDevice();
		selectPartition();
	}
	
	private void selectDevice() throws IOException
	{
		String[] devices = UserFacade.getInstance().getDevices();
		Options devicesOpt = new Options(context);
		int choice = devicesOpt.show("Select a device", devices);
		
		String device = devices[choice - 1];
		UserFacade.getInstance().selectDevice(device);
		System.err.println("device="+device);
	}

	private void selectPartition() throws Exception
	{
		List<Partition> partitions = UserFacade.getInstance().getPartitions();
		
		Partition partition = null;
		if(install)
		{
			if(partitions.isEmpty())
			{
				//TODO
				YesNo createPart = new YesNo(context);
				boolean create = createPart.show("There is no partition. Would you liek to create one ?");
				
				partition = null; //TODO
			}
		}
		
		if(partition == null)
		{
			Options partitionsOpt = new Options(context);
			int choice = partitionsOpt.show("Select a partition", partitions);
			
			partition = partitions.get(choice - 1);
			//UserFacade.getInstance().selectDevice(device);
		}
		
		//TODO return result of selection
	}
}
