/*
 * $Id$
 */
package org.jnode.debugger;

import java.io.PrintStream;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DebuggerUtils {

    public static void showThreadHeading(PrintStream out, Thread thread) {
        out.print(thread.getName() + ", " + thread.getPriority() + ", "
                + thread.getVmThread().getThreadStateName());
    }
}
