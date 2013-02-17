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
 
package org.jnode.vm.objects;



public abstract class VmSystemObject implements BootableObject {
    /**
     * Verify this object, just before it is written to the boot image during
     * the build process.
     */
    public void verifyBeforeEmit() {
    }

    /**
     * This method is called in the build process to get extra information
     * on this object. This extra information is added to the listing file.
     *
     * @return String
     */
    public String getExtraInfo() {
        return null;
    }
}
