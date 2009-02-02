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
 
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.Iterator;

/**
 * Common interface for both resident and non-resident attribute list
 * attributes.
 *
 * @author Daniel Noll (daniel@noll.id.au)
 */
interface AttributeListAttribute {

    /**
     * Gets an iterator over all the entries in the attribute list.
     *
     * @return an iterator of all attribute list entries.
     * @throws IOException if there is an error reading the attribute's data.
     */
    Iterator<AttributeListEntry> getAllEntries() throws IOException;

}
