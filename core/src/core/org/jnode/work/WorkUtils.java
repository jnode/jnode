/*
 * $Id$
 */
package org.jnode.work;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.system.BootLog;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class WorkUtils {

    /**
     * Add the given work to the workmanager.
     * @param work
     * @return True if the work was added to the workmanager, false if
     * the workmanager could not be found.
     */
    public static boolean add(Work work) {
        final WorkManager wm;
        try {
            wm = (WorkManager)InitialNaming.lookup(WorkManager.NAME);
            wm.add(work);
            return true;
        } catch (NameNotFoundException ex) {
            BootLog.error("Cannot find workmanager", ex);
            return false;
        }
    }
}
