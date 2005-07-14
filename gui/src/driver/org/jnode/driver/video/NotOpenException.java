/*
 * $Id$
 */
package org.jnode.driver.video;

public class NotOpenException extends FrameBufferException {

    /**
     * 
     */
    public NotOpenException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public NotOpenException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param s
     */
    public NotOpenException(String s) {
        super(s);
    }

    /**
     * @param cause
     */
    public NotOpenException(Throwable cause) {
        super(cause);
    }
}
