/*
 * $Id$
 */
package org.jnode.shell.help;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class LongArgument extends Argument {
    
    public LongArgument(String name, String description, boolean multi) {
        super(name, description, multi);
    }

    public LongArgument(String name, String description) {
        super(name, description);
    }

    public String complete(String partial) {
        return partial;
    }
    
    public long getLong(ParsedArguments args) {
        return Long.parseLong(this.getValue(args));
    }
}
