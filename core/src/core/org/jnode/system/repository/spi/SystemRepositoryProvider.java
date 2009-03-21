/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
     *
     * @param elementName
     * @return {@code true} if it does, {@code false} otherwise.
     */
    public boolean contains(Name elementName);

    /**
     * Gets all element names contained in the part of the repository identified
     * by the given element name.
     *
     * @return The set of names, never null
     */
    public Set<Name> names(Name elementName);

    /**
     * Remove an element with the given name from the repository.
     * If the indicated element contains sub-elements, they will also be removed.
     *
     * @param elementName
     */
    public void remove(Name elementName);

    /**
     * Map a read-only version of the element identified by the given name
     * into a buffer.
     *
     * @param elementName
     * @throws IOException
     */
    public MappedByteBuffer map(Name elementName)
        throws IOException;

    /**
     * Put an element into repository.
     *
     * @param elementName
     * @param src
     * @throws IOException
     */
    public void put(Name elementName, ByteBuffer src)
        throws IOException;

}
