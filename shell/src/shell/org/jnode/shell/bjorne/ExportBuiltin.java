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

import java.util.Iterator;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;

/**
 * This class implements the 'export' built-in.
 * 
 * @author crawley@jnode.org
 */
final class ExportBuiltin extends BjorneBuiltin {
    @SuppressWarnings("deprecation")
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        Iterator<String> args = command.iterator();
        if (!args.hasNext()) {
            throw new BjorneControlException(BjorneInterpreter.BRANCH_EXIT,
                    context.getParent().getLastReturnCode());
        }
        while (args.hasNext()) {
            String arg = args.next();
            int pos = arg.indexOf('=');
            if (pos == -1) {
                context.getParent().setExported(arg, true);
            } else if (pos == 0) { 
                error("export: " + arg + ": not a valid identifier", context);
            } else {
                String name = arg.substring(0, pos);
                String value = arg.substring(pos + 1);
                if (!BjorneToken.isName(name)) {
                    error("export: " + name + ": not a valid identifier", context);
                }
                context.getParent().setVariable(name, value);
                context.getParent().setExported(name, true);
            }
        }
        return 0;
    }
}
