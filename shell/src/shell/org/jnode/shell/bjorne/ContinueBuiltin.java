/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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

final class ContinueBuiltin extends BjorneBuiltin {
    @SuppressWarnings("deprecation")
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        Iterator<String> it = command.iterator();
        if (!it.hasNext()) {
            throw new BjorneControlException(BjorneInterpreter.BRANCH_CONTINUE,
                    1);
        } else {
            String arg = it.next();
            try {
                int count = Integer.parseInt(arg);
                if (count > 0) {
                    throw new BjorneControlException(
                            BjorneInterpreter.BRANCH_CONTINUE, count);
                }
                error("continue: " + arg + ": loop count out of range", context);
            } catch (NumberFormatException ex) {
                error("continue: " + arg + ": numeric argument required",
                        context);
            }
        }
        return 1;
    }
}
