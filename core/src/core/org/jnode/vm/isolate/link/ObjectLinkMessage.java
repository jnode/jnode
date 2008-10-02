/*
 * $Id: VmIsolate.java 4592 2008-09-30 12:00:11Z crawley $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
package org.jnode.vm.isolate.link;

/**
 * This message type passes an object by reference.  This is probably a bad idea
 * because it 'breaks' the isolation of isolates.  Use sparingly if at all.
 * 
 * @author crawley@jnode.org
 */
public class ObjectLinkMessage extends LinkMessageImpl {

    private final Object obj;

    private ObjectLinkMessage(Object cr) {
        this.obj = cr;
    }

    public static ObjectLinkMessage newMessage (Object obj) {
        return new ObjectLinkMessage(obj);
    }

    @Override
    public Object extract() {
        return obj;
    }

    @Override
    LinkMessageImpl cloneMessage() {
        return new ObjectLinkMessage(obj);
    }
}
