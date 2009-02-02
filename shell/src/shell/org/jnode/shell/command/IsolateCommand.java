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
 
package org.jnode.shell.command;

import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.vm.isolate.VmIsolate;

/**
 * The IsolateCommand provides information about the current isolates in the system.
 * @author Levente S\u00e1ntha
 */
public class IsolateCommand extends AbstractCommand {
    @Override
    public void execute() throws Exception {
        final PrintWriter out = getOutput().getPrintWriter();
        out.println("      Id " + " Creator " + "State    " + "Main class");
        VmIsolate root = VmIsolate.getRoot();
        if (root != null) {
            out.println(format(String.valueOf(root.getId()), 8, false) + " " +
                format("0", 8, false) + " "  +
                format(String.valueOf(root.getState()), 8, true));
        }
        for (VmIsolate iso : VmIsolate.getVmIsolates()) {
            out.println(format(String.valueOf(iso.getId()), 8, false) + " " +
                format(String.valueOf(iso.getCreator().getId()), 8, false) + " " +
                format(String.valueOf(iso.getState()), 8, true) + " " +
                iso.getMainClassName());
        }
    }

    public String format(String value, int width, boolean left) {
        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < width - value.length(); i++) {
            sb.append(' ');
        }
        if (left) {
            sb.insert(0, value);
        } else {
            sb.append(value);
        }
        return sb.toString();
    }
}
