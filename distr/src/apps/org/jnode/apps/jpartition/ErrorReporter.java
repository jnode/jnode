package org.jnode.apps.jpartition;

import org.apache.log4j.Logger;

public class ErrorReporter {
    public final void reportError(Logger log, Object source, Throwable t) {
        reportError(log, source, (Object) t);
    }

    public final void reportError(Logger log, Object source, String message) {
        reportError(log, source, (Object) message);
    }

    protected void displayError(Object source, String message) {
        // by default display nothing
    }

    private final void reportError(Logger log, Object source, Object message) {
        Throwable t = (message instanceof Throwable) ? (Throwable) message : null;

        String msg = (t == null) ? String.valueOf(message) : t.getMessage();
        displayError(source, msg);

        if (log != null) {
            if (t != null) {
                log.error(msg, t);
            } else {
                log.error(msg);
            }
        }
    }
}
