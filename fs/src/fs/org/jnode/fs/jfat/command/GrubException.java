package org.jnode.fs.jfat.command;

public class GrubException extends Exception {
    private static final long serialVersionUID = 1L;

    public GrubException(String message) {
        super(message);
    }

    public GrubException(String message, Throwable cause) {
        super(message, cause);
    }
}
