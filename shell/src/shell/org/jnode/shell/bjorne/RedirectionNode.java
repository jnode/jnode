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

public class RedirectionNode {
    private final int redirectionType;

    private final BjorneToken io;

    private final BjorneToken arg;

    private String hereDocument;
    
    private boolean expandable = true;

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

    public void setHereDocument(String hereDocument) {
        // FIXME ... should analyze the document and set 'expandable'
        // if there anything that requires expansion.
        this.hereDocument = hereDocument;
    }

    public String getHereDocument() {
        return hereDocument;
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

    public boolean isHereDocumentExpandable() {
        return expandable;
    }
}
