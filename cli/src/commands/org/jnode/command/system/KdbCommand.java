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

import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.vm.Unsafe;

/**
 * This command turns kernel debug print capture on and off.  When "on",
 * messages from calls to org.jnode.vm.Unsafe.debug(...) get copied to
 * the serial port.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public class KdbCommand extends AbstractCommand {
    
    private static final String help_on = "Enable the kernel debugger";
    private static final String help_off = "Disable the kernel debugger";
    private static final String help_super = "Control kernel debugging";
    private static final String str_enable = "KDB enabled";
    private static final String str_disable = "KDB disabled";
    private static final String fmt_set = "KDB is %s%n";
    private static final String str_enabled = "enabled";
    private static final String str_disabled = "disabled";
    
    private final FlagArgument argOn;
    private final FlagArgument argOff;

    public KdbCommand() {
        super(help_super);
        argOn  = new FlagArgument("on", Argument.OPTIONAL, help_on);
        argOff = new FlagArgument("off", Argument.OPTIONAL, help_off);
        registerArguments(argOff, argOn);
    }

    public static void main(String[] args) throws Exception {
        new KdbCommand().execute(args);
    }
    
    @Override
    public void execute() {
        PrintWriter out = getOutput().getPrintWriter();
        if (argOff.isSet()) {
            Unsafe.setKdbEnabled(false);
            out.println(str_disable);
        } else if (argOn.isSet()) {
            Unsafe.setKdbEnabled(true);
            out.println(str_enable);
        } else {
            // FIXME ... we shouldn't have to do this ...
            final boolean state = Unsafe.setKdbEnabled(false);
            Unsafe.setKdbEnabled(state);
            out.format(fmt_set, (state ? str_enabled : str_disabled));
        }        
    }
}
