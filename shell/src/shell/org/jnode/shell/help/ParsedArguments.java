/*
 * $Id$
 */
package org.jnode.shell.help;

import java.util.Map;

/**
 * @author qades
 */
public class ParsedArguments {

    private final Map args;

    ParsedArguments(Map args) {
        this.args = args;
    }

    public final int size() {
        return args.size();
    }

    final String[] getValues(Argument arg) {
        return (String[]) args.get(arg);
    }

    final boolean isSet(Parameter param) {
        return args.containsKey(param);
    }
}