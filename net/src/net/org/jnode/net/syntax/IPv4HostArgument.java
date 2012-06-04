/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.net.syntax;

import org.jnode.net.ipv4.IPv4Address;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;

/**
 * This Argument class accepts a host name or IPv4 addresses. No validation or
 * completion is performed,
 * 
 * @author crawley@jnode.org
 */
public class IPv4HostArgument extends Argument<IPv4Address> {

    public IPv4HostArgument(String label, int flags, String description) {
        super(label, flags, new IPv4Address[0], description);
    }

    @Override
    protected String argumentKind() {
        return "hostname/IPv4 address";
    }

    @Override
    protected IPv4Address doAccept(Token value, int flags) throws CommandSyntaxException {
        try {
            return new IPv4Address(value.text);
        } catch (IllegalArgumentException ex) {
            throw new CommandSyntaxException("invalid hostname or IPv4 address");
        }
    }
}
