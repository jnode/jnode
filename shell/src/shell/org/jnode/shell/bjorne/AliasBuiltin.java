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

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;

/**
 * This class implements the 'alias' built-in.
 * 
 * @author crawley@jnode.org
 */
final class AliasBuiltin extends BjorneBuiltin {
    @SuppressWarnings("deprecation")
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        Iterator<String> args = command.iterator();
        context = context.getParent();
        PrintStream ps = context.resolvePrintStream(context.getIO(Command.STD_ERR));
        int rc = 0;
        if (!args.hasNext()) {
            printAliases(ps, context.getAliases());
        } else {
            while (args.hasNext()) {
                String arg = args.next();
                int pos = arg.indexOf('=');
                String aliasName;
                String alias;
                if (pos <= 0) {
                    aliasName = arg;
                    alias = null;
                } else {
                    aliasName = arg.substring(0, pos);
                    alias = arg.substring(pos + 1);
                }
                if (alias == null) {
                    alias = context.getAlias(aliasName);
                    if (alias == null) {
                        error("alias: " + aliasName + " not found", context);
                        rc = 1;
                    } else {
                        printAlias(ps, aliasName, alias);
                    }
                } else {
                    if (!BjorneToken.isName(aliasName)) {
                        error("alias: " + aliasName + ": not a valid alias name", context);
                    }
                    context.defineAlias(aliasName, alias);
                }
            }
        }
        return rc;
    }

    private void printAliases(PrintStream ps, TreeMap<String, String> aliases) {
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            printAlias(ps, entry.getKey(), entry.getValue());
        }
    }

    private void printAlias(PrintStream ps, String aliasName, String alias) {
        ps.println(aliasName + "=" + alias);
    }
   
    
}
