/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.shell.command.plugin;

import org.jnode.shell.help.Help;
import org.jnode.vm.VmSystem;

/**
 * Halts the system
 * 
 * @author epr
 */
public class HaltCommand {

    public static Help.Info HELP_INFO = new Help.Info("halt",
            "Stop all services and devices, so the computer can be turned off");

    public static void main(String[] args) throws Exception {
        VmSystem.halt(false);
    }
}
