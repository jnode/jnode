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

public class ForCommandNode extends CommandNode {
    private final CommandNode body;

    private final BjorneToken var;

    private final BjorneToken[] words;

    public ForCommandNode(BjorneToken var, BjorneToken[] words, CommandNode body) {
        super(BjorneInterpreter.CMD_FOR);
        this.body = body;
        this.var = var;
        this.words = words;
    }

    public CommandNode getBody() {
        return body;
    }

    public BjorneToken getVar() {
        return var;
    }

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
        for (BjorneToken word : words) {
            context.setVariable(var.getText(), word.getText());
            rc = body.execute(context);
        }
        return rc;
    }
}
