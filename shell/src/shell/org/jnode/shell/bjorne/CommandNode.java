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

import org.jnode.shell.CommandShell;
import org.jnode.shell.CommandThread;
import org.jnode.shell.ShellException;

/**
 * This is the base class for parse tree nodes that represent simple
 * and compound bjorne commands.
 * 
 * @author crawley@jnode.org
 */
public abstract class CommandNode {
    private RedirectionNode[] redirects;

    private int nodeType;

    private int flags;

    /**
     * Construct with its initial parse tree node type
     * @param nodeType the parse tree node type
     */
    public CommandNode(int nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Get the redirection parse tree nodes for this command node
     * @return the redirection nodes or {@code null}
     */
    public RedirectionNode[] getRedirects() {
        return redirects;
    }

    /**
     * Set the redirection parse tree nodes for this command node
     * @param redirects the redirection nodes or {@code null}
     */
    public void setRedirects(RedirectionNode[] redirects) {
        this.redirects = redirects;
    }

    /** 
     * Get the parse tree node type
     * @return the node type
     */
    public int getNodeType() {
        return nodeType;
    }

    /**
     * Get the node's flags.  The meaning is specific to each CommandNode subclass.
     * @return the flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Set (OR) one or more flags
     * @param flag
     */
    public void setFlag(int flag) {
        flags |= flag;
    }

    /**
     * Update the node type.
     * @param nodeType
     */
    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Execute this command node with the given bjorne shell context
     * @param context the bjorne context
     * @return the return code from executing the command node
     * @throws ShellException
     */
    public abstract int execute(BjorneContext context) throws ShellException;

    /**
     * Create a CommandThread that will run this command node with the given 
     * bjorne shell context.
     * @param shell our parent CommandShell instance
     * @param context the bjorne context
     * @return a CommandThread that will run the command node when 
     *        {@link Thread#start()} is called.
     * @throws ShellException
     */
    public abstract CommandThread fork(CommandShell shell, BjorneContext context)
        throws ShellException;

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

    /**
     * A helper method for toString() implementations that renders an
     * array in a human readable form.
     * @param sb the destination for rendering
     * @param array the object array to be rendered.
     */
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

    /**
     * Build a pipeline descriptor for this node.
     * 
     * @param context the bjorne shell context supplying variables, etc
     * @return the assembled descriptor or {@code null} if none is needed.
     * @throws ShellException
     */
    public BjornePipeline buildPipeline(BjorneContext context) throws ShellException {
        // FIXME ... I think I need to implement this for all CommandNode subtypes ...
        return null;
    }

}
