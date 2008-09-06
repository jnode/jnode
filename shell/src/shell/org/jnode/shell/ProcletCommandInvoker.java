/*
 * $Id: ThreadCommandInvoker.java 3374 2007-08-02 18:15:27Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2003-2007 JNode.org
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

package org.jnode.shell;

import org.jnode.shell.io.CommandIO;
import org.jnode.shell.proclet.ProcletContext;
import org.jnode.shell.proclet.ProcletIOContext;
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

    CommandThreadImpl createThread(CommandLine cmdLine, CommandRunner cr) {
        CommandIO[] ios = cmdLine.getStreams();
        return ProcletContext.createProclet(cr, null, null, 
                new Object[] {
                    ios[0].getInputStream(), 
                    ios[1].getOutputStream(),
                    ios[2].getOutputStream()}, 
                cmdLine.getCommandName());
    }
    
    private static synchronized void init() {
        if (!initialized) {
            VmSystem.switchToExternalIOContext(new ProcletIOContext());
            initialized = true;
        }
    }
}
