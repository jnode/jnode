package org.jnode.apps.jpartition.swingview;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.jnode.apps.jpartition.ViewFactory;
import org.jnode.apps.jpartition.controller.MainController;

public class SwingViewFactory implements ViewFactory {
	public Object createDeviceView(MainController controller, 
			Object fileDeviceView, Object cmdProcessorView) throws Exception
	{
		return new MainView(controller, (JFrame)fileDeviceView, (JComponent)cmdProcessorView);
	}
	
	public Object createFileDeviceView(MainController controller) throws Exception
	{
		return new FileDeviceView(controller);
	}

	public Object createCommandProcessorView(MainController mainController) {
		return new CommandProcessorView(mainController);
	}
}
