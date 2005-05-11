/*
 * $Id$
 */
package org.vmmagic.unboxed;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface UnboxedObject {

    /**
     * Convert to an integer.
     * @return
     */
    public int toInt();

    /**
     * Convert to a long.
     * @return
     */
    public long toLong();
}
