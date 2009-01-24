package org.jnode.emu;

public class EmuException extends Exception {

    private static final long serialVersionUID = 1L;

    public EmuException(String message) {
        super(message);
    }

    public EmuException(String message, Throwable cause) {
        super(message, cause);
    }
}
