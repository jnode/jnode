/*
 * $Id$
 *
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
 
package org.jnode.shell.command.system;

import org.jnode.shell.AbstractCommand;
import org.jnode.vm.scheduler.IRQManager;
import org.jnode.vm.scheduler.VmProcessor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class LsIRQCommand extends AbstractCommand {
    
    public LsIRQCommand() {
        super("prints IRQ handler info");
    }

    public static void main(String[] args) throws Exception {
        new LsIRQCommand().execute(args);
    }
    
    @Override
    public void execute() {
        final VmProcessor proc = VmProcessor.current();
        final IRQManager irqMgr = proc.getIRQManager();
        final int max = irqMgr.getNumIRQs();
        for (int i = 0; i < max; i++) {
            getOutput().getPrintWriter().println("IRQ" + i + "\t" + irqMgr.getIrqCount(i) + "\t"
                    + irqMgr.getHandlerInfo(i));
        }
    }
}
