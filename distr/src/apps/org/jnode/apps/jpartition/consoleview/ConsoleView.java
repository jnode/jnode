package org.jnode.apps.jpartition.consoleview;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.Context;
import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.consoleview.components.Component;
import org.jnode.apps.jpartition.consoleview.components.Labelizer;
import org.jnode.apps.jpartition.consoleview.components.NumberField;
import org.jnode.apps.jpartition.consoleview.components.Options;
import org.jnode.apps.jpartition.consoleview.components.YesNo;
import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.Partition;
import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.util.NumberUtils;

class ConsoleView extends Component {
	private static final Logger log = Logger.getLogger(ConsoleView.class);
	
	private final boolean install;
	private Partition selectedPartition;
	
	ConsoleView(InputStream in, PrintStream out, ErrorReporter errorReporter, boolean install)
	{
		super(new Context(in, out, errorReporter));
		this.install = install;
		
		try {
			start();
		} catch (Throwable e) {
			errorReporter.reportError(log, this, e);
		}
		
		println();
		print("selectedPartition="+PartitionLabelizer.INSTANCE.getLabel(selectedPartition));
		print(" on device "+DeviceLabelizer.INSTANCE.getLabel(UserFacade.getInstance().getSelectedDevice()));
	}
	
	private void start() throws Exception
	{
		selectDevice();
		selectPartition();
		
		if(UserFacade.getInstance().hasChanges())
		{
			YesNo yesNo = new YesNo(context);
			boolean apply = yesNo.show("There is pending modifications. Would you like to apply them ?");
			if(apply)
			{
				UserFacade.getInstance().applyChanges();
			}
		}
	}
	
	private void selectDevice() throws IOException
	{
		List<Device> devices = UserFacade.getInstance().getDevices();
		Options devicesOpt = new Options(context);
		int choice = (int) devicesOpt.show("Select a device", devices, DeviceLabelizer.INSTANCE);
		
		String device = devices.get(choice - 1).getName();
		UserFacade.getInstance().selectDevice(device);
		println("device="+device);
	}

	private void selectPartition() throws Exception
	{
		selectedPartition = null;
		if(install)
		{
			selectedPartition = selectPartitionForInstall();
		}
		
		if(selectedPartition == null)
		{
			selectedPartition = selectPartitionForDevice();
		}
	}
	
	private Partition selectPartitionForDevice() throws Exception {
		List<Partition> partitions = UserFacade.getInstance().getPartitions();
		
		Options partitionsOpt = new Options(context);
		int choice = (int) partitionsOpt.show("Select a partition", partitions, PartitionLabelizer.INSTANCE);
		
		return partitions.get(choice - 1);
	}

	private Partition selectPartitionForInstall() throws Exception
	{
		List<Partition> partitions = UserFacade.getInstance().getPartitions();
		Partition partition = null;
		if((partitions.size() == 1) && !partitions.get(0).isUsed())
		{
			YesNo yesNo = new YesNo(context);
			boolean create = yesNo.show("There is no partition. Would you like to create one ?");
			if(create)
			{
				partition = createPartition(partitions.get(0));
			}
		}
		
		return partition;
	}

	private Partition createPartition(Partition freePart) throws Exception {
		long size = freePart.getSize();
		String space = NumberUtils.toBinaryByte(size);
		YesNo yesNo = new YesNo(context);
		boolean allSpace = yesNo.show("Would you like to use all the free space ("+space+") ?");
		
		if(!allSpace)
		{
			NumberField sizeField = new NumberField(context);
			size = sizeField.show("Size of the new partition ");
		}
		
		return UserFacade.getInstance().createPartition(freePart.getStart(), size);
	}
}
