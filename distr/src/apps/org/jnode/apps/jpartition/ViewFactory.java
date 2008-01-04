package org.jnode.apps.jpartition;


public interface ViewFactory {
	Object createDeviceView(Object cmdProcessorView)
				throws Exception;

	Object createCommandProcessorView();

	ErrorReporter createErrorReporter();
}
