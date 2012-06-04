/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.command.system;

import java.io.PrintWriter;

import javax.isolate.Isolate;
import javax.isolate.IsolateStatus;
import javax.isolate.Link;
import javax.isolate.LinkMessage;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.vm.isolate.VmIsolate;

/**
 * Terminates an isolate.
 * The isolate to terminate is specified by its integer identifier.
 *
 * @author Levente S\u00e1ntha
 */
public class TerminateCommand extends AbstractCommand {
    
    private static final String help_id = "The ID of the isolate to terminate";
    private static final String help_super = "Terminate an Isolate";
    private static final String err_root = "The root isolate cannot be terminated with this command";
    private static final String err_not_found = "Isolate not found: %d%n";
    private static final String str_done = "Done.";
    
    private final IntegerArgument argID;

    public TerminateCommand() {
        super(help_super);
        argID = new IntegerArgument("id", Argument.MANDATORY | Argument.SINGLE, help_id);
        registerArguments(argID);
    }
    
    public void execute() throws Exception {
        PrintWriter err = getError().getPrintWriter();
        PrintWriter out = getOutput().getPrintWriter();
        Integer id = argID.getValue();
        if (id.equals(VmIsolate.getRoot().getId())) {
            err.println(err_root);
            exit(1);
        }

        VmIsolate[] iso_arr = VmIsolate.getVmIsolates();
        Isolate iso = null;
        for (VmIsolate vmi : iso_arr) {
            if (id.equals(vmi.getId())) {
                iso = vmi.getIsolate();
                break;
            }
        }

        if (iso == null) {
            err.format(err_not_found, id);
            exit(1);
        }

        Link link = iso.newStatusLink();
        iso.exit(-1);
        while (true) {
            LinkMessage msg = link.receive();
            if (msg.containsStatus()) {
                IsolateStatus is = msg.extractStatus();
                if (is.getState().equals(IsolateStatus.State.EXITED)) {
                    out.println(str_done);
                    break;
                }
            }
        }
    }
}
