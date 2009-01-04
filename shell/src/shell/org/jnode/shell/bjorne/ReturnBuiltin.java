/*
 * $Id: Command.java 3772 2008-02-10 15:02:53Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

final class ReturnBuiltin extends BjorneBuiltin {
    @SuppressWarnings("deprecation")
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        Iterator<String> it = command.iterator();
        if (!it.hasNext()) {
            throw new BjorneControlException(BjorneInterpreter.BRANCH_RETURN,
                    context.getLastReturnCode());
        } else {
            String arg = it.next();
            try {
                throw new BjorneControlException(
                        BjorneInterpreter.BRANCH_RETURN, Integer.parseInt(arg));
            } catch (NumberFormatException ex) {
                error("return: " + arg + ": numeric argument required", context);
            }
        }
        return 1;
    }
}
