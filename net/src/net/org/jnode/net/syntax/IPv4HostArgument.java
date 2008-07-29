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
    protected IPv4Address doAccept(Token value) throws CommandSyntaxException {
        try {
            return new IPv4Address(value.token);
        } catch (IllegalArgumentException ex) {
            throw new CommandSyntaxException("invalid hostname or IPv4 address");
        }
    }
}
