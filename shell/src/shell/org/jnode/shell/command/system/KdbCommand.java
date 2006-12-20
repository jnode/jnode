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
 
package org.jnode.shell.command.system;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.OptionArgument;
import org.jnode.vm.Unsafe;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class KdbCommand {
    static final OptionArgument ARG_ACTION = new OptionArgument("action",
            "action to do on the kernel debugger", new OptionArgument.Option(
                    "on", "Enable the kernel debugger"),
            new OptionArgument.Option("off", "Disable the kernel debugger"));

    static final Parameter PARAM_ACTION = new Parameter(ARG_ACTION);

    public static Help.Info HELP_INFO = new Help.Info("kdb", new Syntax[] {
            new Syntax("Print the state of the kernel debugger"),
            new Syntax("En/Disable the kernel debugger", PARAM_ACTION) });

    public static void main(String[] args) throws Exception {
        final ParsedArguments cmdLine = HELP_INFO.parse(args);
        
        if (PARAM_ACTION.isSet(cmdLine)) {
            final String action = ARG_ACTION.getValue(cmdLine);
            if (action.equals("on")) {
                Unsafe.setKdbEnabled(true);
            } else {
                Unsafe.setKdbEnabled(false);
            }
        } else {
            final boolean state = Unsafe.setKdbEnabled(false);
            Unsafe.setKdbEnabled(state);
            System.out.println("KDB is " + (state ? "enabled" : "disabled"));
        }        
    }
}
