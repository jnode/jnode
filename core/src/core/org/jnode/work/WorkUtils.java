/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
     *
     * @param work
     * @return True if the work was added to the workmanager, false if
     *         the workmanager could not be found.
     */
    public static boolean add(Work work) {
        final WorkManager wm;
        try {
            wm = InitialNaming.lookup(WorkManager.NAME);
            wm.add(work);
            return true;
        } catch (NameNotFoundException ex) {
            BootLog.error("Cannot find workmanager", ex);
            return false;
        }
    }
}
