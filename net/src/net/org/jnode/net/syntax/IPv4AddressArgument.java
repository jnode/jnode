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
 
package org.jnode.net.syntax;

import java.util.StringTokenizer;

import org.jnode.net.ipv4.IPv4Address;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;

/**
 * This Argument class accepts 4-part IPv4 addresses.  It validates the address, 
 * but does no completion.  The string "default" is a synonym for "0.0.0.0".
 * 
 * @author crawley@jnode.org
 */
public class IPv4AddressArgument extends Argument<IPv4Address> {

    public IPv4AddressArgument(String label, int flags, String description) {
        super(label, flags, new IPv4Address[0], description);
    }

    @Override
    protected String argumentKind() {
        return "IPv4 address";
    }

    @Override
    protected IPv4Address doAccept(Token value) throws CommandSyntaxException {
        if (value.text.equals("default")) {
            return new IPv4Address(new byte[]{0, 0, 0, 0}, 0);
        }
        final StringTokenizer tok = new StringTokenizer(value.text, ".");
        if (tok.countTokens() != 4) {
            throw new CommandSyntaxException("wrong number of components for an IPv4 address");
        }
        try {
            final byte b1 = parseUnsignedByte(tok.nextToken());
            final byte b2 = parseUnsignedByte(tok.nextToken());
            final byte b3 = parseUnsignedByte(tok.nextToken());
            final byte b4 = parseUnsignedByte(tok.nextToken());
            return new IPv4Address(new byte[]{b1, b2, b3, b4}, 0);
        } catch (NumberFormatException ex) {
            throw new CommandSyntaxException("invalid component in IPv4 address");
        }
    }
    
    /**
     * Parse a number and check it is in the range 0 to 255.
     * 
     * @param str
     * @throws NumberFormatException if 'str' is not a number or if it is out of range
     * @return the number cast as a byte.
     */
    private byte parseUnsignedByte(String str) {
        final int v = Integer.parseInt(str);
        if ((v >= 0) && (v < 256)) {
            return (byte) v;
        } else {
            throw new NumberFormatException(str);
        }
    }
}
