/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
