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

package org.jnode.util;

import org.jnode.vm.VmSystem;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TimeUtils {

    /**
     * Sleep for ms milliseconds.
     *
     * @return True if return normal, false on InterruptedException.
     */
    public static boolean sleep(long ms) {
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException ex) {
            return false;
        }
    }

    /**
     * Wait for ms milliseconds in a busy waiting loop.
     * This method is very CPU intensive, so be carefull.
     *
     * @param ms
     */
    public static void loop(long ms) {
        final long start = VmSystem.currentKernelMillis();
        while (true) {
            if ((start + ms) <= VmSystem.currentKernelMillis()) {
                break;
            }
        }
    }
}
