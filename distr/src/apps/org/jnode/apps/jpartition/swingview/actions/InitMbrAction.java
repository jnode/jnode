package org.jnode.apps.jpartition.swingview.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.jnode.apps.jpartition.commands.framework.CommandProcessor;
import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.driver.bus.ide.IDEDevice;

public class InitMbrAction extends AbstractAction
{
	public InitMbrAction(IDEDevice device, CommandProcessor processor) {
		super("init MBR");
	}

	public void actionPerformed(ActionEvent e) {
		//UserFacade.getInstance().initMbr();
	}
}
