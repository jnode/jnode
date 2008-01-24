package org.jnode.apps.jpartition.consoleview;

import org.jnode.apps.jpartition.consoleview.components.Labelizer;
import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.Partition;
import org.jnode.util.NumberUtils;

class DeviceLabelizer implements Labelizer<Device>
{
	static final DeviceLabelizer INSTANCE = new DeviceLabelizer();
	
	public String getLabel(Device device) {
		if(device == null)
		{
			throw new NullPointerException("device is null");
		}

		StringBuilder sb = new StringBuilder();
		
		sb.append(device.getName());
		sb.append(" (").append(NumberUtils.toBinaryByte(device.getSize())).append(')');
		
		return sb.toString();
	}
}
