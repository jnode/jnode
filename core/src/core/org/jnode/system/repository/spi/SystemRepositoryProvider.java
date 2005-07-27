/*
 * $Id$
 */
package org.jnode.system.repository.spi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.Set;

import javax.naming.Name;

public interface SystemRepositoryProvider {

    /**
     * Does this part of the repository contain an element with the given name.
     * @param elementName
     * @return
     */
    public boolean contains(Name elementName);
    
    /**
     * Gets all elementnames contained in the part of the repository identified
     * by the given element name.
     * @return The set of names, never null
     */
    public Set<Name> names(Name elementName);
    
    /**
     * Remove an element with the given name from the repository.
     * If the indicated element contains sub-elements, they will also be removed.
     * @param elementName
     */
    public void remove(Name elementName);
    
    /**
     * Map a read-only version of the element identified by the given name
     * into a buffer.
     * @param elementName
     * @throws IOException
     */
    public MappedByteBuffer map(Name elementName)
    throws IOException;
    
    /**
     * Put an element into repository.
     * @param elementName
     * @param dst
     * @throws IOException
     */
    public void put(Name elementName, ByteBuffer src)
    throws IOException;
    
}
