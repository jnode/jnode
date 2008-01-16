package org.jnode.apps.jpartition.swingview;

import javax.swing.JComponent;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.ViewFactory;

public class SwingViewFactory implements ViewFactory {
	public Object createDeviceView(ErrorReporter errorReporter,
			Object cmdProcessorView, boolean install) throws Exception
	{
		return new MainView(errorReporter, (JComponent)cmdProcessorView);
	}

	public Object createCommandProcessorView() {
		return new CommandProcessorView();
	}

	public ErrorReporter createErrorReporter() {
		return new SwingErrorReporter();
	}
}
