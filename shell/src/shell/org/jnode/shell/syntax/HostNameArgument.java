/*
 * $Id$
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

package org.jnode.shell.syntax;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jnode.shell.CommandLine.Token;

/**
 * This class implements hostname/ip address valued command line arguments.  At the moment, it performs
 * no special syntax checking and does no completion.
 * 
 * @author crawley@jnode.org
 */
public class HostNameArgument extends Argument<String> {
    // We won't make the Argument type parameter InetAddress because that would
    // require us to resolve DNS addresses while we are parsing / completing.

    public HostNameArgument(String label, int flags, String description) {
        super(label, flags, new String[0], description);
    }

    public HostNameArgument(String label, int flags) {
        this(label, flags, null);
    }

    public HostNameArgument(String label) {
        this(label, 0, null);
    }

    @Override
    protected void doAccept(Token token) throws CommandSyntaxException {
        addValue(token.token);
    }
    
    @Override
    protected String argumentKind() {
        return "host name/ip address";
    }

    /**
     * Resolve the hostname / IP address string to a network address.
     * @return the network address.
     * @throws UnknownHostException if the hostname does not resolve to an address
     */
    public InetAddress getAsInetAddress() throws UnknownHostException {
        return InetAddress.getByName(getValue());
    }
    
    /**
     * Resolve the ith hostname / IP address string to a network address.
     * @param i the index of the hostname we want the address
     * @return the network address.
     * @throws UnknownHostException if the hostname does not resolve to an address
     */
    public InetAddress getAsInetAddress(int i) throws UnknownHostException {
        return InetAddress.getByName(getValues()[i]);
    }
}
