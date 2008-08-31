package org.jnode.shell.io;

public class CommandIOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CommandIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandIOException(String message) {
        super(message);
    }

}
