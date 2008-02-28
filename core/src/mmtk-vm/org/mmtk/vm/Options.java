/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
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
