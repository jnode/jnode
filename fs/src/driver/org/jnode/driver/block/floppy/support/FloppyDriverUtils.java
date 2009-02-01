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
 
package org.jnode.driver.block.floppy.support;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.jnode.naming.InitialNaming;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class FloppyDriverUtils {

    public static final FloppyDeviceFactory getFloppyDeviceFactory() throws NamingException {
        try {
            return InitialNaming.lookup(FloppyDeviceFactory.NAME);
        } catch (NameNotFoundException ex) {
            final FloppyDeviceFactory fac = new DefaultFloppyDeviceFactory();
            InitialNaming.bind(FloppyDeviceFactory.NAME, fac);
            return fac;
        }
    }
}
