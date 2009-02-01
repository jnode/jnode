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
 
package org.jnode.driver.bus.ide;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.jnode.naming.InitialNaming;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class IDEDriverUtils {

    /**
     * search and return the IDEDeviceFactory responsible for JNode
     *
     * @return the IDEDeviceFactory from JNode
     * @throws NamingException
     */
    public static final IDEDeviceFactory getIDEDeviceFactory() throws NamingException {
        try {
            return InitialNaming.lookup(IDEDeviceFactory.NAME);
        } catch (NameNotFoundException ex) {
            final IDEDeviceFactory fac = new DefaultIDEDeviceFactory();
            InitialNaming.bind(IDEDeviceFactory.NAME, fac);
            return fac;
        }
    }
}
