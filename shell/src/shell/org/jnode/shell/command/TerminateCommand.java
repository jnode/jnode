/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.shell.command;

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
    private final IntegerArgument ARG_ID = new IntegerArgument(
            "id", Argument.MANDATORY | Argument.SINGLE,
            "the identifier of the isolate to terminate");

    public TerminateCommand() {
        super("Terminate an isolate");
        registerArguments(ARG_ID);
    }

    public void execute() throws Exception {
        PrintWriter err = getError().getPrintWriter();
        PrintWriter out = getOutput().getPrintWriter();
        Integer id = ARG_ID.getValue();
        if (id.equals(VmIsolate.getRoot().getId())) {
            err.println("The root isolate cannot be terminated with this command.");
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
            err.println("Isolate not found: " + id);
            exit(1);
        }

        Link link = iso.newStatusLink();
        iso.exit(-1);
        while (true) {
            LinkMessage msg = link.receive();
            if (msg.containsStatus()) {
                IsolateStatus is = msg.extractStatus();
                if (is.getState().equals(IsolateStatus.State.EXITED)) {
                    out.println("Done.");
                    break;
                }
            }
        }
    }
}
