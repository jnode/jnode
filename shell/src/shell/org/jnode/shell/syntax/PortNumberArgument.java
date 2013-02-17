/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.shell.syntax;

/**
 * This Argument class captures port numbers.  No completion is performed at the moment.
 * 
 * @author crawley@jnode.org
 *
 */
public class PortNumberArgument extends IntegerArgument {

    public PortNumberArgument(String label, int flags, String description) {
        super(label, flags, 0, 65535, description);
    }

    @Override
    protected String argumentKind() {
        return "port number";
    }
}
