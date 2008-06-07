/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
import java.util.List;

/**
 * Common interface for both resident and non-resident attribute list
 * attributes.
 *
 * @author Daniel Noll (daniel@nuix.com.au)
 */
interface AttributeListAttribute {

    /**
     * Gets an entry from the attribute list.
     *
     * XXX: What if there are multiple?  In the case I've seen, there are multiple but only the first
     *      one contains any data.
     *
     * @param attrTypeID the type of attribute to find.
     * @return the attribute entry.
     * @throws IOException if there is an error reading the attribute's non-resident data.
     */
    List<AttributeListEntry> getEntries(int attrTypeID) throws IOException;
}
