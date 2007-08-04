package org.jnode.apps.jpartition.swingview.actions;

import java.awt.event.ActionEvent;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.commands.BaseDeviceCommand;
import org.jnode.apps.jpartition.commands.framework.Command;
import org.jnode.apps.jpartition.commands.framework.CommandProcessor;
import org.jnode.apps.jpartition.swingview.ErrorReporter;
import org.jnode.driver.bus.ide.IDEDevice;

abstract public class BaseDeviceAction extends AbstractAction 
{	
	private static final Logger log = Logger.getLogger(BaseDeviceAction.class);
	
	final protected CommandProcessor processor;
	final protected IDEDevice device;
	
	public BaseDeviceAction(IDEDevice device, CommandProcessor processor) {
		super();
		this.processor = processor;
		this.device = device;
	}

	public BaseDeviceAction(IDEDevice device, CommandProcessor processor, String name, Icon icon) {
		super(name, icon);
		this.processor = processor;
		this.device = device;
	}

	public BaseDeviceAction(IDEDevice device, CommandProcessor processor, String name) {
		super(name);
		this.processor = processor;
		this.device = device;
	}

	public void actionPerformed(ActionEvent event)
	{
		try
		{
			Command command = getCommand(device);
			processor.addCommand(command);
		}
		catch(Throwable t)
		{
			ErrorReporter.reportError(log, this, t);
		}
	}
	
	abstract protected BaseDeviceCommand getCommand(IDEDevice device);
}
