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
 
package org.jnode.shell.command;

import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MemoryCommand extends AbstractCommand {
    public static void main(String[] args) throws Exception {
        new MemoryCommand().execute(args);
    }

    public MemoryCommand() {
        super("shows current JNode memory usage");
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        final Runtime rt = Runtime.getRuntime();
        out.println("Total memory " + NumberUtils.toBinaryByte(rt.totalMemory()));
        out.println("Used memory  " + NumberUtils.toBinaryByte(rt.totalMemory() - rt.freeMemory()));
        out.println("Free memory  " + NumberUtils.toBinaryByte(rt.freeMemory()));
    }
}
