package org.jnode.apps.jpartition.swingview;

import it.battlehorse.stamps.annotations.ModelDependent;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.controller.MainController;
import org.jnode.partitions.ibm.IBMPartitionTypes;

public class PartitionView extends DiskAreaView 
{
	private static final Logger log = Logger.getLogger(PartitionView.class);

	private DeviceView deviceView;
	
	public PartitionView(MainController controller, DeviceView deviceView)
	{
		super(controller);
		
		this.deviceView = deviceView;
	}

	@ModelDependent(modelKey = "partition", propertyKey = "empty")	
	public void setEmpty(boolean empty) {
		lblInfos.setText(empty ? "empty" : "");
	}

	@ModelDependent(modelKey = "partition", propertyKey = "bootable")	
	public void setBootable(boolean bootable) {
		lblInfos.setText(bootable ? "B" : "");
	}

	@ModelDependent(modelKey = "partition", propertyKey = "type")	
	public void setType(IBMPartitionTypes type) {
		lblInfos.setText(type.getName());
	}

	@ModelDependent(modelKey = "partition", propertyKey = "start")	
	public void setStart(long start) {
		
	}

	@ModelDependent(modelKey = "partition", propertyKey = "size")	
	public void setSize(long size) {
	}
	
/*
	public PartitionModel getPartition() {
		return partition;
	}

	public void setPartition(PartitionModel partition) {
		if(this.partition != partition)
		{
			this.partition = partition;
			refreshFromModel();
		}
	}
	
	public void refreshFromModel()
	{
		StringBuilder sb = new StringBuilder();
				
		String name = partition.getType().getName();
		log.debug("refreshFromModel: name="+name);
		if(name.length() > 10)
		{
			name = name.substring(0, 10);
		}
		String boot = partition.isBootable() ? "(B)" : "(-)";
		log.debug("refreshFromModel: boot="+boot);
		
		sb.append(boot).append(name);
		if(!partition.isEmpty())
		{
			sb.append("-").append(partition.getStart());
			sb.append("-").append(partition.getSize());
		}
		lblInfos.setText(sb.toString());
		
		log.debug("refreshFromModel: description="+sb.toString());
	}
*/	
}
