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

public class RedirectionNode {
    public final int redirectionType;

    public final BjorneToken io;

    public final BjorneToken arg;

    public RedirectionNode(final int redirectionType, BjorneToken io,
            BjorneToken arg) {
        super();
        this.redirectionType = redirectionType;
        this.io = io;
        this.arg = arg;
    }

    public BjorneToken getArg() {
        return arg;
    }

    public BjorneToken getIo() {
        return io;
    }

    public int getRedirectionType() {
        return redirectionType;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Redirect{");
        sb.append("redirectionType=").append(redirectionType);
        if (io != null) {
            sb.append(",io=").append(io);
        }
        if (arg != null) {
            sb.append(",arg=").append(arg);
        }
        sb.append("}");
        return sb.toString();
    }
}
