package org.jnode.net.syntax;

import java.net.InetAddress;
import java.util.StringTokenizer;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.net.ipv4.IPv4Address;

import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;

/**
 * This Argument class accepts 4-part IPv4 addresses.  It validates the address, 
 * but does no completion.
 * 
 * @author crawley@jnode.org
 */
public class IPv4AddressArgument extends Argument<IPv4Address> {

    public IPv4AddressArgument(String label, int flags,
            String description) {
        super(label, flags, new IPv4Address[0], description);
    }

    @Override
    protected String argumentKind() {
        return "IPv4 address";
    }

    @Override
    protected IPv4Address doAccept(Token value) throws CommandSyntaxException {
        final StringTokenizer tok = new StringTokenizer(value.token, ".");
        if (tok.countTokens() != 4) {
            throw new CommandSyntaxException("Wrong number of components for an IPv4 address");
        }
        try {
            final byte b1 = parseUnsignedByte(tok.nextToken());
            final byte b2 = parseUnsignedByte(tok.nextToken());
            final byte b3 = parseUnsignedByte(tok.nextToken());
            final byte b4 = parseUnsignedByte(tok.nextToken());
            return new IPv4Address(new byte[]{b1, b2, b3, b4}, 0);
        } catch (NumberFormatException ex) {
            throw new CommandSyntaxException("Invalid component in IPv4 address");
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
