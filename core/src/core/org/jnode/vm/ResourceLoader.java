/*
 * $Id$
 */
package org.jnode.vm;

import java.net.URL;
import java.nio.ByteBuffer;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ResourceLoader {

    /**
     * Gets a resource with a given name as a byte buffer.
     * 
     * @param resourceName
     * @return null if not found
     */
    public ByteBuffer getResourceAsBuffer(String resourceName);

    /**
     * Does this loader contain the resource with the given name.
     * 
     * @param resourceName
     * @return boolean
     */
    public boolean containsResource(String resourceName);

    /**
     * Does this loader contain the resource with the given name.
     * 
     * @param resourceName
     * @return boolean
     */
    public URL getResource(String resourceName);
}
