/*
 * $Id$
 */
package org.jnode.partitions;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PartitionTableException extends Exception {

    /**
     * 
     */
    public PartitionTableException() {
        super();
    }

    /**
     * @param s
     * @param cause
     */
    public PartitionTableException(String s, Throwable cause) {
        super(s, cause);
    }

    /**
     * @param s
     */
    public PartitionTableException(String s) {
        super(s);
    }

    /**
     * @param cause
     */
    public PartitionTableException(Throwable cause) {
        super(cause);
    }

}
