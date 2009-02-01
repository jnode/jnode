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
 
package org.jnode.test.core;

import org.jnode.util.NumberUtils;
import org.jnode.vm.performance.PerformanceCounterEvent;
import org.jnode.vm.performance.PerformanceCounters;
import org.jnode.vm.scheduler.VmProcessor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PerfCtrTest {

    /**
     * @param args
     */
    public static void main(String[] args) {

        PerformanceCounters perfCtr = VmProcessor.current()
            .getPerformanceCounters();
        if (perfCtr.getMaximumCounters() == 0) {
            System.out.println("No counters available");
            return;
        }

        if (args.length == 0) {
            System.out.println("Available events:");
            for (PerformanceCounterEvent evt : perfCtr.getAvailableEvents()) {
                System.out.println(evt);
            }
            System.out.println();
        } else {
            PerformanceCounterEvent[] events = new PerformanceCounterEvent[perfCtr
                .getMaximumCounters()];
            int i = 0;
            for (String arg : args) {
                if (i >= events.length) {
                    break;
                }
                PerformanceCounterEvent evt = perfCtr.getAvailableEvent(arg);
                if (evt != null) {
                    events[i++] = evt;
                }
            }

            // for (PerformanceCounterEvent evt : perfCtr.getAvailableEvents())
            // {
            // if (i >= events.length) {
            // break;
            // }
            // events[i++] = evt;
            // }

            if (i < events.length) {
                PerformanceCounterEvent[] evt2 = new PerformanceCounterEvent[i];
                System.arraycopy(events, 0, evt2, 0, i);
                events = evt2;
            }

            long[] counters = new long[events.length];
            perfCtr.startCounters(events);
            ArithOpt.main(new String[0]);

            perfCtr.getCounterValues(counters);
            perfCtr.stopCounters();
            for (i = 0; i < events.length; i++) {
                System.out.println(events[i].getId() + " 0x"
                    + NumberUtils.hex(counters[i]));
            }
        }
    }
}
