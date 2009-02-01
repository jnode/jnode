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

public class FunctionDefinitionNode extends CommandNode {
    private final BjorneToken name;

    private final CommandNode body;

    public FunctionDefinitionNode(final BjorneToken name, final CommandNode body) {
        super(BjorneInterpreter.CMD_FUNCTION_DEF);
        this.name = name;
        this.body = body;
    }

    public CommandNode getBody() {
        return body;
    }

    public BjorneToken getName() {
        return name;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FunctionDefinition{").append(super.toString());
        sb.append(",name=").append(name);
        sb.append(",body=").append(body);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) {
        return -1;
    }
}
