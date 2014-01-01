/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

import org.jnode.shell.CommandRunnable;
import org.jnode.shell.CommandShell;
import org.jnode.shell.CommandThread;
import org.jnode.shell.CommandThreadImpl;
import org.jnode.shell.ShellException;

/**
 * IfCommandNode is the parse tree node class for 'if' statements and 'elif' sub-statements.
 * 
 * @author crawley@jnode.org
 */
public class IfCommandNode extends CommandNode {

    private final CommandNode cond;

    private final CommandNode thenPart;

    private final CommandNode elsePart;

    /**
     * Construct an IfCommandNode.
     * 
     * @param commandType this should be {@link BjorneInterpreter#CMD_IF} 
     *     or {@link BjorneInterpreter#CMD_ELIF}.
     * @param cond the CommandNode providing the 'condition' for the statement.
     * @param thenPart the statement's 'then' CommandNode or {code null}
     * @param elsePart the statement's 'else' CommandNode or {code null}
     */
    public IfCommandNode(int commandType, CommandNode cond,
            CommandNode thenPart, CommandNode elsePart) {
        super(commandType);
        this.cond = cond;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    /**
     * The command node that provides the 'condition' for the 'if' statement.
     * @return the 'condition' node.
     */
    public CommandNode getCond() {
        return cond;
    }

    /**
     * The command node that provides the 'else' part of the 'if' statement.  In
     * the case of an 'elif', this will be another IfCommandNode.  If there is no
     * 'else' part, this will be or {@code null}.
     * 
     * @return the 'else' node, or {@code null}.
     */
    public CommandNode getElsePart() {
        return elsePart;
    }

    /**
     * The command node that provides the 'then' part of the 'if' statement.
     * 
     * @return the 'then' node, or {@code null}.
     */
    public CommandNode getThenPart() {
        return thenPart;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
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
        try {
            context.evaluateRedirectionsAndPushHolders(getRedirects());
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
        } finally {
            context.popHolders();
        }
    }
    
    @Override
    public CommandThread fork(CommandShell shell, final BjorneContext context) 
        throws ShellException {
        
        CommandRunnable cr = new BjorneSubshellRunner(context) {
            @Override
            public int doRun() throws ShellException {
                return IfCommandNode.this.execute(context);
            }
        };
        return new CommandThreadImpl(cr, context.getName());
    }
}
