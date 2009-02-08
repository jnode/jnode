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

import java.util.ArrayList;
import java.util.List;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellSyntaxException;

final class SetBuiltin extends BjorneBuiltin {
    
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        context = context.getParent();
        boolean optsDone = false;
        boolean forceNewArgs = false;
        List<String> newArgs = new ArrayList<String>();
        String[] args = command.getArguments();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (optsDone) {
                newArgs.add(arg);
            } else if (arg.length() == 0 || 
                    (arg.charAt(0) != '-' && arg.charAt(0) != '+')) {
                optsDone = true;
                newArgs.add(arg);
            } else if (arg.equals("--")) {
                optsDone = true;
                forceNewArgs = true;
            } else {
                boolean set = arg.charAt(0) == '-';
                for (int j = 1; j < arg.length(); j++) {
                    switch (arg.charAt(j)) {
                        case 'x': 
                            context.setEchoExpansions(set);
                            break;
                        case 'f': 
                            context.setGlobbing(!set);
                            break;
                        default:
                            throw new ShellSyntaxException(
                                    "Unknown set option: " + (set ? "-" : "+") + arg.charAt(j));
                    }
                }
            }
        }
        if (forceNewArgs || newArgs.size() > 0) {
            context.setArgs(newArgs.toArray(new String[newArgs.size()]));
        }
        return 0;
    }
}
