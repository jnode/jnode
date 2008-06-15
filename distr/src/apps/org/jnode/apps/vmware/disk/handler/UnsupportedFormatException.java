package org.jnode.apps.vmware.disk.handler;

import java.io.IOException;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class UnsupportedFormatException extends IOException {

    public UnsupportedFormatException() {
        super();
    }

    public UnsupportedFormatException(String s, Throwable cause) {
        super(s);
        initCause(cause);
    }

    public UnsupportedFormatException(String s) {
        super(s);
    }

    public UnsupportedFormatException(Throwable cause) {
        super();
        initCause(cause);
    }

}
