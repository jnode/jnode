package org.jnode.apps.jpartition;

public interface ViewFactory {
    Object createDeviceView(ErrorReporter errorReporter, Object cmdProcessorView, boolean install)
        throws Exception;

    Object createCommandProcessorView();

    ErrorReporter createErrorReporter();
}
