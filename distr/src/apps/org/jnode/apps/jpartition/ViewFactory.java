package org.jnode.apps.jpartition;


public interface ViewFactory {
	Object createDeviceView(ErrorReporter errorReporter, Object cmdProcessorView)
				throws Exception;

	Object createCommandProcessorView();

	ErrorReporter createErrorReporter();
}
