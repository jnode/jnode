/*
 * $Id$
 */
package org.jnode.vm.bytecode;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class StackException extends RuntimeException {

    /**
     *  
     */
    public StackException() {
        super();
    }

    /**
     * @param s
     */
    public StackException(String s) {
        super(s);
    }

    /**
     * @param s
     * @param cause
     */
    public StackException(String s, Throwable cause) {
        super(s, cause);
    }

    /**
     * @param cause
     */
    public StackException(Throwable cause) {
        super(cause);
    }
}