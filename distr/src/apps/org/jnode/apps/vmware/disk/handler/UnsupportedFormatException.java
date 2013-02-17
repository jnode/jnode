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
 
package org.jnode.apps.vmware.disk.handler;

import java.io.IOException;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class UnsupportedFormatException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public UnsupportedFormatException() {
        super();
    }

    /**
     * 
     * @param s
     * @param cause
     */
    public UnsupportedFormatException(String s, Throwable cause) {
        super(s);
        initCause(cause);
    }

    /**
     * 
     * @param s
     */
    public UnsupportedFormatException(String s) {
        super(s);
    }

    /**
     * 
     * @param cause
     */
    public UnsupportedFormatException(Throwable cause) {
        super();
        initCause(cause);
    }

}
