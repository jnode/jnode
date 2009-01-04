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

import org.jnode.shell.ShellException;

public class IfCommandNode extends CommandNode {

    private final CommandNode cond;

    private final CommandNode thenPart;

    private final CommandNode elsePart;

    public IfCommandNode(int commandType, CommandNode cond,
            CommandNode thenPart, CommandNode elsePart) {
        super(commandType);
        this.cond = cond;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    public CommandNode getCond() {
        return cond;
    }

    public CommandNode getElsePart() {
        return elsePart;
    }

    public CommandNode getThenPart() {
        return thenPart;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("IfCommand{").append(super.toString());
        sb.append(",cond=").append(cond);
        if (thenPart != null) {
            sb.append(",thenPart=").append(thenPart);
        }
        if (elsePart != null) {
            sb.append(",elsePart=").append(elsePart);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) throws ShellException {
        int rc = cond.execute(context);
        if (rc == 0) {
            if (thenPart != null) {
                return thenPart.execute(context);
            }
        } else {
            if (elsePart != null) {
                return elsePart.execute(context);
            }
        }
        if ((getFlags() & BjorneInterpreter.FLAG_BANG) != 0) {
            rc = (rc == 0) ? -1 : 0;
        }
        return rc;
    }
}
