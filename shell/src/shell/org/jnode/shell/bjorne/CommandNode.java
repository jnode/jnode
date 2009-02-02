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

import org.jnode.shell.ShellException;

public abstract class CommandNode {
    private RedirectionNode[] redirects;

    private int nodeType;

    private int flags;

    public CommandNode(int nodeType) {
        this.nodeType = nodeType;
    }

    public RedirectionNode[] getRedirects() {
        return redirects;
    }

    public void setRedirects(RedirectionNode[] redirects) {
        this.redirects = redirects;
    }

    public int getNodeType() {
        return nodeType;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlag(int flag) {
        flags |= flag;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    public abstract int execute(BjorneContext context) throws ShellException;

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("nodeType=").append(nodeType);
        if (flags != 0) {
            sb.append(",flags=0x").append(Integer.toHexString(flags));
        }
        if (redirects != null) {
            sb.append(",redirects=");
            appendArray(sb, redirects);
        }
        return sb.toString();
    }

    protected static void appendArray(StringBuffer sb, Object[] array) {
        sb.append('[');
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(array[i]);
        }
        sb.append(']');
    }
}
