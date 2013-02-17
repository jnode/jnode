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
 
package org.jnode.vm.classmgr;

import org.jnode.vm.objects.VmSystemObject;

/**
 * This class holds the exceptions a method has declared to throw.
 * It is read from the Exceptions attribute of a method.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmExceptions extends VmSystemObject {

    private final VmConstClass[] exceptions;
    private final char pragmaFlags;

    /**
     * Create a new, empty instance
     */
    public VmExceptions() {
        this.exceptions = null;
        this.pragmaFlags = 0;
    }

    /**
     * Create a new instance
     *
     * @param exceptions
     */
    public VmExceptions(VmConstClass[] exceptions, char pragmaFlags) {
        this.exceptions = exceptions;
        this.pragmaFlags = pragmaFlags;
    }

    /**
     * Gets the number of exceptions in this list.
     *
     * @return int
     */
    public final int getLength() {
        if (exceptions != null) {
            return exceptions.length;
        } else {
            return 0;
        }
    }

    /**
     * Gets the exception at the given index
     *
     * @param index
     * @return Exception class reference
     */
    public final VmConstClass getException(int index) {
        if (exceptions != null) {
            return exceptions[index];
        } else {
            throw new IndexOutOfBoundsException("exceptions is empty; index " + index);
        }
    }

    /**
     * @return Returns the pragmaFlags.
     */
    final int getPragmaFlags() {
        return pragmaFlags;
    }
}
