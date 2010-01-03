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
 
package org.jnode.shell.bjorne;

import org.jnode.shell.CommandRunnable;
import org.jnode.shell.CommandShell;
import org.jnode.shell.CommandThread;
import org.jnode.shell.CommandThreadImpl;
import org.jnode.shell.ShellException;

/**
 * LoopCommandNode is the parse tree node class for 'until' and 'while' loop statements.
 * 
 * @author crawley@jnode.org
 */
public class LoopCommandNode extends CommandNode {
    private final CommandNode cond;

    private final CommandNode body;

    /**
     * Construct the LoopCommandNode
     * @param nodeType either {@link BjorneInterpreter#CMD_WHILE} or {@link BjorneInterpreter#CMD_UNTIL}.
     * @param cond the loop's condition CommandNode
     * @param body the loop's body CommandNode
     */
    public LoopCommandNode(int nodeType, CommandNode cond, CommandNode body) {
        super(nodeType);
        this.body = body;
        this.cond = cond;
    }

    /**
     * Get the loop's 'body' node.
     * @return the 'body' node.
     */
    public CommandNode getBody() {
        return body;
    }

    /**
     * Get the loop's 'condition' node
     * @return the 'condition' node.
     */
    public CommandNode getCond() {
        return cond;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LoopCommand{").append(super.toString());
        sb.append(",cond=").append(cond);
        sb.append(",body=").append(body);
        sb.append('}');
        return sb.toString();
    }

    public int execute(BjorneContext context) throws ShellException {
        try {
            int rc = 0;
            context.evaluateRedirectionsAndPushHolders(getRedirects());
            while (true) {
                rc = cond.execute(context);
                if (rc != 0 && getNodeType() == BjorneInterpreter.CMD_WHILE ||
                        rc == 0 && getNodeType() == BjorneInterpreter.CMD_UNTIL) {
                    break;
                }
                try {
                    if (body != null) {
                        body.execute(context);
                    }
                } catch (BjorneControlException ex) {
                    int control = ex.getControl();
                    if (control == BjorneInterpreter.BRANCH_BREAK || 
                            control == BjorneInterpreter.BRANCH_CONTINUE) {
                        if (ex.getCount() > 1) {
                            ex.decrementCount();
                            throw ex;
                        }
                        if (control == BjorneInterpreter.BRANCH_BREAK) {
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        throw ex;
                    }
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
                return LoopCommandNode.this.execute(context);
            }
        };
        return new CommandThreadImpl(cr, context.getName());
    }
}
