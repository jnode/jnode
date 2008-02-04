package org.jnode.apps.jpartition;

import org.jnode.apps.jpartition.model.UserFacade;

public class JPartition {
	final private ViewFactory viewFactory;
	final private boolean install;

	public JPartition(ViewFactory viewFactory, boolean install)
	{
		this.viewFactory = viewFactory;
		this.install = install;
	}

	public final void launch() throws Exception
	{
		ErrorReporter errorReporter = viewFactory.createErrorReporter();
		UserFacade.getInstance().setErrorReporter(errorReporter);

        // CommandProcessor
        Object cmdProcessorView = viewFactory.createCommandProcessorView();

        // Device
        viewFactory.createDeviceView(errorReporter, cmdProcessorView, install);
	}
}
