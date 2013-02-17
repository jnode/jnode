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
 
package org.jnode.bootlog;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;


/**
 * Class holding the {@link BootLog} instance used by the system.
 *
 * @author Fabien DUMINY
 */
public final class BootLogInstance {
    private BootLogInstance() {
    }

    /**
     * Get the system's {@link BootLog}.
     *
     * @return the system's {@link BootLog}.
     */
    public static BootLog get() {
        try {
            return InitialNaming.lookup(BootLog.class);
        } catch (NameNotFoundException e) {
            throw new Error("unable to find a BootLog instance", e);
        }
    }

    /**
     * Set the system's {@link BootLog}.
     *
     * @param bootLog the system's {@link BootLog}.
     * @throws NamingException
     * @throws NameAlreadyBoundException
     */
    public static void set(BootLog bootLog) throws NameAlreadyBoundException, NamingException {
        InitialNaming.bind(BootLog.class, bootLog);
    }
}
