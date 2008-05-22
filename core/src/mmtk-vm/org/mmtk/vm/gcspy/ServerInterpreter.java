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
 
package org.mmtk.vm.gcspy;

import org.vmmagic.pragma.Uninterruptible;

/**
 * VM-neutral stub file for generic GCspy server interpreter This class
 * implements the GCspy server. Mostly it forwards calls to the C gcspy library.
 * $Id$
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author <a href="http://www.ukc.ac.uk/people/staff/rej">Richard Jones</a>
 * @version $Revision$
 */
public class ServerInterpreter implements Uninterruptible {
    public static void init(String name, int port, String[] eventNames,
            boolean verbose, String generalInfo) {
    }

    public static boolean isConnected(int event) {
        return false;
    }

    public static void startServer(boolean wait) {
    }

    public static boolean shouldTransmit(int event) {
        return false;
    }

    public static void startCompensationTimer() {
    }

    public static void stopCompensationTimer() {
    }

    public static void serverSafepoint(int event) {
    }

    public static int computeHeaderSize() {
        return 0;
    }
}
