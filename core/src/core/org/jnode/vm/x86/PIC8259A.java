/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm.x86;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.system.resource.IOResource;
import org.jnode.system.resource.ResourceManager;
import org.jnode.system.resource.ResourceNotFreeException;
import org.jnode.system.resource.SimpleResourceOwner;
import org.jnode.annotation.Inline;
import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.PrivilegedActionPragma;
import org.jnode.annotation.Uninterruptible;
import org.jnode.vm.scheduler.ProcessorLock;

/**
 * Wrapper for the 8259A Programmable Interrupt Controller.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class PIC8259A {

    /**
     * PIC Access lock
     */
    private final ProcessorLock lock = new ProcessorLock();

    /** PIC Master IO resource */
    private final IOResource io8259_A;
    /** PIC Slave IO resource */
    private final IOResource io8259_B;

    /**
     * Create and initialize.
     */
    @PrivilegedActionPragma
    public PIC8259A() {
        final SimpleResourceOwner owner = new SimpleResourceOwner("PIC8259A");

        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            io8259_A = rm.claimIOResource(owner, 0x20, 1); // 0xA0
            io8259_B = rm.claimIOResource(owner, 0xA0, 1);
        } catch (NameNotFoundException ex) {
            throw new Error("Cannot find ResourceManager", ex);
        } catch (ResourceNotFreeException ex) {
            throw new Error("Cannot claim PIC0-IO ports", ex);
        }
    }

    /**
     * Set an End Of Interrupt message to the 8259 interrupt controller(s).
     *
     * @param irq
     */
    @Uninterruptible
    @KernelSpace
    @Inline
    final void eoi(int irq) {
        try {
            // Get access
            lock.lock();

            // Perform EOI
            if (irq < 8) {
                io8259_A.outPortByte(0x20, 0x60 + irq);
            } else {
                /*
                 * if (irq == 10) { Screen.debug(" <EOI 10/> ");
                 */
                io8259_B.outPortByte(0xA0, 0x60 + irq - 8);
                /* EOI of cascade */
                io8259_A.outPortByte(0x20, 0x60 + 2);
            }
        } finally {
            // Return access
            lock.unlock();
        }
    }
}
