/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.vm.scheduler;

import org.jnode.annotation.Inline;
import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.Uninterruptible;
import org.jnode.vm.LoadCompileService;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmStackReader;

/**
 * This is the Kernel Debugger (also known as kdb).
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class KernelDebugger {
    private final VmScheduler vmScheduler;
    
    KernelDebugger(VmScheduler vmScheduler) {
        this.vmScheduler = vmScheduler;
    }
    
    /**
     * Process all waiting KDB commands.
     */
    @Uninterruptible
    @KernelSpace
    @Inline
    final void processAllKdbInput() {
        int ch;
        while ((ch = readKdbInput()) >= 0) {
            processKdbInput(ch);
        }
    }

    /**
     * Process the input from the kernel debugger.
     *
     * @param input
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     */
    @Uninterruptible
    @KernelSpace
    private final void processKdbInput(int input) {
        switch ((char) input) {
            case '?':
            case 'h':
                debug("Commands:\n");
                debug("l   Show Load/Compile queue\n");
                debug("p   Ping\n");
                debug("q   Print thread queues\n");
                debug("r   Print stacktraces of ready-queue\n");
                debug("t   Print current thread\n");
                debug("v   Verify thread\n");
                debug("w   Print waiting threads\n");
                debug("W   Print stacktraces of waiting threads\n");
                break;
            case 'l':
                debug("<load-compile-service: ");
                debug("\n");
                LoadCompileService.showInfo();
                debug("/>\n");
                break;
            case 'p':
                debug("<ping/>");
                break;
            case 'q': {
                final VmThread currentThread = VmMagic.currentProcessor().currentThread;
                debug("<queues: current-thread name='");
                debug(currentThread.getName());
                debug("' state='");
                debug(currentThread.getThreadStateName());
                debug("\n");
                vmScheduler.getReadyQueue().dump(false, null);
                vmScheduler.getSleepQueue().dump(false, null);
                debug("/>\n");
                break;
            }
            case 'r':
                debug("<traces: ");
                debug("\n");
                vmScheduler.getReadyQueue().dump(true, vmScheduler.getStackReader());
                debug("/>\n");
                break;
            case 'v':
                debug("<verify: ");
                debug("\n");
                verifyThreads();
                debug("/>\n");
                break;
            case 'w':
                debug("<waiting: ");
                debug("\n");
                dumpWaitingThreads(false, null);
                debug("/>\n");
                break;
            case 'W':
                debug("<waiting: ");
                debug("\n");
                dumpWaitingThreads(true, vmScheduler.getStackReader());
                debug("/>\n");
                break;
            case 't': {
                final VmThread currentThread = VmMagic.currentProcessor().currentThread;
                debug("<currentthread name='");
                debug(currentThread.getName());
                debug("' state='");
                debug(currentThread.getThreadStateName());
                debug("'/>\n");
                break;
            }
            case 'T': {
                final VmThread currentThread = VmMagic.currentProcessor().currentThread;
                debug("<currentthread name='");
                debug(currentThread.getName());
                debug("' state='");
                debug(currentThread.getThreadStateName());
                vmScheduler.getStackReader().debugStackTrace(currentThread);
                debug("'/>\n");
                break;
            }
            case '#':
                debug("Halt for ever\n");
                while (true)
                    ;

                // default:
                // debug(input);
        }
    }

    /**
     * Dump the status of this queue on debug.
     */
    @KernelSpace
    private final void dumpWaitingThreads(boolean dumpStack, VmStackReader stackReader) {
        VmThreadQueueEntry e = vmScheduler.getAllThreadsQueue().first;
        while (e != null) {
            if (e.thread.isWaiting()) {
                debug(e.thread.getName());
                debug(" id0x");
                debug(e.thread.getId());
                debug(" s0x");
                debug(e.thread.getThreadState());
                debug(" p0x");
                debug(e.thread.priority);
                debug(" wf:");
                VmThread waitFor = e.thread.getWaitForThread();
                debug((waitFor != null) ? waitFor.getName() : "none");
                debug("\n");
                if (dumpStack && (stackReader != null)) {
                    stackReader.debugStackTrace(e.thread);
                    debug("\n");
                }
            }
            e = e.next;
        }
    }
    
    /**
     * Dump the status of this queue on debug.
     */
    @KernelSpace
    private final void verifyThreads() {
        VmThreadQueueEntry e = vmScheduler.getAllThreadsQueue().first;
        while (e != null) {
            e.thread.verifyState();
            e = e.next;
        }
    }
    
    /**
     * Print the given string to the output.
     */
    @Inline
    private final void debug(String str) {
        Unsafe.debug(str);
    }
    
    /**
     * Print the given integer to the output.
     */
    @Inline
    private final void debug(int i) {
        Unsafe.debug(i);
    }
    
    /**
     * Read a keystroke from the input.
     */
    @Inline
    private final int readKdbInput() {
        return Unsafe.readKdbInput();        
    }    
}
