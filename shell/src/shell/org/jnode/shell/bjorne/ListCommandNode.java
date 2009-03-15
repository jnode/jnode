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

import java.util.List;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandRunnable;
import org.jnode.shell.CommandShell;
import org.jnode.shell.CommandThread;
import org.jnode.shell.CommandThreadImpl;
import org.jnode.shell.Completable;
import org.jnode.shell.ShellException;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.io.CommandIOHolder;


public class ListCommandNode extends CommandNode implements Completable {
    
    private final CommandNode[] commands;

    public ListCommandNode(int nodeType, List<? extends CommandNode> commands) {
        super(nodeType);
        this.commands = commands.toArray(new CommandNode[commands.size()]);
    }

    public CommandNode[] getCommands() {
        return commands;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ListCommand{").append(super.toString());
        sb.append(",commands=");
        appendArray(sb, commands);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) throws ShellException {
        int listFlags = getFlags();
        int rc = 0;
        try {
            if ((listFlags & BjorneInterpreter.FLAG_PIPE) != 0) {
                BjornePipeline pipeline = buildPipeline(context);
                try {
                    pipeline.wire();
                    rc = pipeline.run(context.getShell());
                } finally {
                    pipeline.closeStreams();
                }
            } else {
                if (getNodeType() == BjorneInterpreter.CMD_SUBSHELL) {
                    // This simulates creating a 'subshell'.
                    context = new BjorneContext(context);
                    CommandIOHolder[] holders = context.evaluateRedirections(getRedirects());
                    for (int i = 0; i < holders.length; i++) {
                        context.setIO(i, holders[i]);
                    }
                }
                
                for (CommandNode command : commands) {
                    int commandFlags = command.getFlags();
                    if ((commandFlags & BjorneInterpreter.FLAG_AND_IF) != 0) {
                        if (context.getLastReturnCode() != 0) {
                            break;
                        }
                    }
                    if ((commandFlags & BjorneInterpreter.FLAG_OR_IF) != 0) {
                        if (context.getLastReturnCode() == 0) {
                            break;
                        }
                    }
                    rc = command.execute(context);
                }
            }
        } finally {
            if (getNodeType() == BjorneInterpreter.CMD_SUBSHELL) {
                context.closeIOs();
            }
        }
        if ((listFlags & BjorneInterpreter.FLAG_BANG) != 0) {
            rc = (rc == 0) ? -1 : 0;
        }
        return rc;
    }
    
    @Override
    public CommandThread fork(CommandShell shell, final BjorneContext context) 
        throws ShellException {
        
        CommandRunnable cr = new BjorneSubshellRunner(context) {
            @Override
            public int doRun() throws ShellException {
                return ListCommandNode.this.execute(context);
            }};
        return new CommandThreadImpl(cr, context.getName());
    }

    public BjornePipeline buildPipeline(BjorneContext context) throws ShellException {
        int len = commands.length;
        BjornePipeline pipeline = new BjornePipeline(len);
        for (int i = 0; i < len; i++) {
            CommandNode commandNode = commands[i];
            BjorneContext childContext = new BjorneContext(context);
            pipeline.addStage(commandNode, childContext);
        }
        return pipeline;
    }
    
    @Override
    public void complete(CompletionInfo completion, CommandShell shell) throws CompletionException {
        // TODO Auto-generated method stub

    }
}
