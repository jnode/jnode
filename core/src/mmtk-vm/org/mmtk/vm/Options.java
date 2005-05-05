/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.mmtk.vm;

import org.jnode.system.BootLog;
import org.jnode.vm.Unsafe;
import org.mmtk.utility.options.Option;

/**
 * Skeleton for class to handle command-line arguments and options for GC.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Daniel Frampton
 */
public final class Options {

    /**
     * Map a name into a key in the VM's format
     * 
     * @param name
     *            the space delimited name.
     * @return the VM specific key.
     */
    public static String getKey(String name) {
        return null;
    }

    /**
     * Failure during option processing. This must never return.
     * 
     * @param o
     *            The option that was being set.
     * @param message
     *            The error message.
     */
    public static void fail(Option o, String message) {
        BootLog.warn("Option " + o.getName() + ": " + message);
        Unsafe.die("Failed option");
    }

    /**
     * Warning during option processing.
     * 
     * @param o
     *            The option that was being set.
     * @param message
     *            The warning message.
     */
    public static void warn(Option o, String message) {
        BootLog.warn("Option " + o.getName() + ": " + message);        
    }
}
