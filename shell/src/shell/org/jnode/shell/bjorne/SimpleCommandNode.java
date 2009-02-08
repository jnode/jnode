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

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellFailureException;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.io.CommandIO;

public class SimpleCommandNode extends CommandNode implements BjorneCompletable {

    private BjorneToken[] assignments;

    private final BjorneToken[] words;

    public SimpleCommandNode(int nodeType, BjorneToken[] words) {
        super(nodeType);
        this.words = words;
    }

    public void setAssignments(BjorneToken[] assignments) {
        this.assignments = assignments;
    }

    public BjorneToken[] getWords() {
        return words;
    }

    public BjorneToken[] getAssignments() {
        return assignments;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SimpleCommand{").append(super.toString());
        if (assignments != null) {
            sb.append(",assignments=");
            appendArray(sb, assignments);
        }
        if (words != null) {
            sb.append(",words=");
            appendArray(sb, words);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int execute(final BjorneContext context) throws ShellException {
        StreamHolder[] holders = null;
        int rc;
        try {
            BjorneToken[] words = getWords();
            if (words.length == 0) {
                // No command to run: assignments are done in the shell's context
                context.performAssignments(assignments);
                // Surprisingly, we still need to perform the redirections
                BjorneContext childContext = new BjorneContext(context);
                childContext.evaluateRedirections(getRedirects());
                rc = 0;
            } else {
                CommandLine command = context.expandAndSplit(words);
                // Assignments and redirections are done in the command's context
                BjorneContext childContext = new BjorneContext(context);
                childContext.performAssignments(assignments);
                holders = childContext.evaluateRedirections(getRedirects());
                CommandIO[] streams = new CommandIO[holders.length];
                for (int i = 0; i < streams.length; i++) {
                    streams[i] = holders[i].getStream();
                }
                if ((getFlags() & BjorneInterpreter.FLAG_ASYNC) != 0) {
                    throw new ShellFailureException(
                            "asynchronous execution (&) not implemented yet");
                } else {
                    rc = childContext.execute(command, streams);
                }
            }
        } finally {
            if (holders != null) {
                for (StreamHolder holder : holders) {
                    holder.close();
                }
            }
        }
        if ((getFlags() & BjorneInterpreter.FLAG_BANG) != 0) {
            rc = (rc == 0) ? -1 : 0;
        }
        context.setLastReturnCode(rc);
        return rc;
    }

    @Override
    public void complete(CompletionInfo completion, BjorneContext context, CommandShell shell)
        throws CompletionException {
        try {
            CommandLine command = context.expandAndSplit(words);
            command.complete(completion, shell);
        } catch (ShellException ex) {
            throw new CompletionException("Shell exception", ex);
        }
    }
}
