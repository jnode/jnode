package org.jnode.apps.jpartition.model;

public class DeviceException extends RuntimeException {

    private static final long serialVersionUID = -6289552400638465023L;

    public DeviceException() {
        super();
    }

    public DeviceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceException(String message) {
        super(message);
    }

    public DeviceException(Throwable cause) {
        super(cause);
    }

}
