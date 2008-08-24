package org.jnode.emu;

public class EmuException extends Exception {

    public EmuException(String message) {
        super(message);
    }

    public EmuException(String message, Throwable cause) {
        super(message, cause);
    }
}
