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
 
package org.jnode.command.system;

import org.jnode.shell.AbstractCommand;
import org.jnode.vm.VmSystem;

/**
 * Halts the system
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class HaltCommand extends AbstractCommand {
    
    private static final String help_super = "Stop all services and devices, so that the computer can be turned off";
    public HaltCommand() {
        super(help_super);
    }

    public static void main(String[] args) throws Exception {
        new HaltCommand().execute(args);
    }

    @Override
    public void execute() {
        VmSystem.halt(false);
    }
}
