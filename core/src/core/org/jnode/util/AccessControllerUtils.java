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
 
package org.jnode.util;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AccessControllerUtils {

    /**
     * Calls AccessController.doPrivileged and unwraps any exception wrapped
     * in the PrivilegedActionException.
     *
     * @param action a typed action 
     * @return an instance of the action's type.
     * @throws Exception
     */
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws Exception {
        try {
            return AccessController.doPrivileged(action);
        } catch (PrivilegedActionException ex) {
            throw ex.getException();
        }
    }

}
