package org.jnode.apps.jpartition;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.apps.jpartition.model.UserFacade;

public class JPartition {
	final private ViewFactory viewFactory;
	final private InputStream in;
	final private PrintStream out;
	final private PrintStream err;
	final private boolean install;
	
	public JPartition(ViewFactory viewFactory, InputStream in,
			PrintStream out, PrintStream err, boolean install)
	{
		this.viewFactory = viewFactory;
		this.in = in;
		this.out = out;
		this.err = err;
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
