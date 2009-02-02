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
 
package org.jnode.shell.proclet;

import org.jnode.shell.AsyncCommandInvoker;
import org.jnode.shell.Command;
import org.jnode.shell.CommandInvoker;
import org.jnode.shell.CommandRunner;
import org.jnode.shell.CommandShell;
import org.jnode.shell.CommandThreadImpl;
import org.jnode.shell.io.CommandIO;
import org.jnode.vm.VmSystem;

/**
 * This command invoker runs commands in their own proclet, giving each one its
 * own stdin,out,err etcetera.
 * 
 * @author crawley@jnode.org
 */
public class ProcletCommandInvoker extends AsyncCommandInvoker {

    public static final Factory FACTORY = new Factory() {
        public CommandInvoker create(CommandShell shell) {
            return new ProcletCommandInvoker(shell);
        }

        public String getName() {
            return "proclet";
        }
    };
    
    private static boolean initialized;

    public ProcletCommandInvoker(CommandShell commandShell) {
        super(commandShell);
        init();
    }

    public String getName() {
        return "proclet";
    }

    protected CommandThreadImpl createThread(CommandRunner cr) {
        CommandIO[] ios = cr.getIos();
        return ProcletContext.createProclet(cr, null, null, 
                new Object[] {
                    ios[Command.STD_IN].getInputStream(), 
                    ios[Command.STD_OUT].getPrintStream(),
                    ios[Command.STD_ERR].getPrintStream()}, 
                cr.getCommandName());
    }
    
    private static synchronized void init() {
        if (!initialized) {
            VmSystem.switchToExternalIOContext(new ProcletIOContext());
            initialized = true;
        }
    }
}
