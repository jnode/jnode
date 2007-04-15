package org.jnode.apps.jpartition;

import org.jnode.apps.jpartition.controller.MainController;

public interface ViewFactory {
	Object createDeviceView(MainController controller, Object fileDeviceView, 
						Object cmdProcessorView) throws Exception;
	
	Object createFileDeviceView(MainController controller) throws Exception;

	Object createCommandProcessorView(MainController mainController);
}
