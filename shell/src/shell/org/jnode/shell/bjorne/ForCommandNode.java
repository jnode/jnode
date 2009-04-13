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

import org.jnode.shell.CommandRunnable;
import org.jnode.shell.CommandShell;
import org.jnode.shell.CommandThread;
import org.jnode.shell.CommandThreadImpl;
import org.jnode.shell.ShellException;

/**
 * ForCommandNode is the parse tree node class for 'for' statements.
 * 
 * @author crawley@jnode.org
 */
public class ForCommandNode extends CommandNode {
    private final CommandNode body;

    private final BjorneToken var;

    private final BjorneToken[] words;

    /**
     * Construct a ForCommandNode
     * @param var the variable
     * @param words the words
     * @param body the loop body
     */
    public ForCommandNode(BjorneToken var, BjorneToken[] words, CommandNode body) {
        super(BjorneInterpreter.CMD_FOR);
        this.body = body;
        this.var = var;
        this.words = words;
    }

    /**
     * Get the body of the for loop
     * @return the 'body' CommandNode
     */
    public CommandNode getBody() {
        return body;
    }

    /**
     * The for loop's variable.
     * @return the token for the variable.
     */
    public BjorneToken getVar() {
        return var;
    }

    /**
     * The words that are used to supply values for the loop variable.
     * @return the words.
     */
    public BjorneToken[] getWords() {
        return words;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LoopCommand{").append(super.toString());
        sb.append(",var=").append(var);
        if (words != null) {
            sb.append(",words=");
            appendArray(sb, words);
        }
        sb.append(",body=").append(body);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) throws ShellException {
        int rc = 0;
        List<BjorneToken> expanded = context.expandAndSplit(words);
        for (BjorneToken word : expanded) {
            context.setVariable(var.getText(), word.getText());
            rc = body.execute(context);
        }
        return rc;
    }
    
    @Override
    public CommandThread fork(CommandShell shell, final BjorneContext context) 
        throws ShellException {
        
        CommandRunnable cr = new BjorneSubshellRunner(context) {
            @Override
            public int doRun() throws ShellException {
                return ForCommandNode.this.execute(context);
            }
        };
        return new CommandThreadImpl(cr, context.getName());
    }
}
