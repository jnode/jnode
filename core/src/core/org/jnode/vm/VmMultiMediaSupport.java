/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.vm;

import org.vmmagic.unboxed.Address;

/**
 * Class used to implement multi media support for
 * {@link org.jnode.system.MultiMediaMemoryResource}.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmMultiMediaSupport {

    /**
     * Merge 32-bit ARGB values at the given memory address.
     *
     * @param src    The source address (points to 32-bit ARGB int's)
     * @param dst    The destination address (points to 32-bit RGB int's)
     * @param length The number of 32-bit int's to merge.
     */
    public abstract void setARGB32bpp(Address src, Address dst, int length);
}
