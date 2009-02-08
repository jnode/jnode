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
 
package org.jnode.shell.bjorne;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellSyntaxException;

final class ShiftBuiltin extends BjorneBuiltin {
    
    @SuppressWarnings("deprecation")
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        context = context.getParent();
        String[] args = command.getArguments();
        int nos;
        if (args.length == 0) {
            nos = 1;
        } else if (args.length == 1) {
            try {
                nos = Integer.parseInt(args[0]);
                if (nos < 0) {
                    new ShellSyntaxException("Argument for 'shift' is negative: " + args[0]);
                }
            } catch (NumberFormatException ex) {
                throw new ShellSyntaxException("Nonnumeric argument for 'shift': " + args[0]);
            }
        } else {
            throw new ShellSyntaxException("Too many arguments for 'shift'");
        }
        if (nos == 0) {
            return 0;
        } 
        int nosOldArgs = context.nosArgs();
        if (nos >= nosOldArgs) {
            context.setArgs(new String[0]);
            return nos == nosOldArgs ? 0 : 1;
        }
        String[] oldArgs = context.getArgs();
        String[] newArgs = new String[oldArgs.length - nos];
        System.arraycopy(oldArgs, nos, newArgs, 0, newArgs.length);
        context.setArgs(newArgs);
        return 0;
    }
}
