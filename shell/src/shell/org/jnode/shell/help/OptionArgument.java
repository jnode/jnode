/*
 * $Id$
 */

package org.jnode.shell.help;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qades
 */
public class OptionArgument extends Argument {

    private final Option[] options;

    public OptionArgument(String name, String description, Option[] options,
            boolean multi) {
        super(name, description, multi);
        this.options = options;
    }

    public OptionArgument(String name, String description, Option[] options) {
        this(name, description, options, SINGLE);
    }

    public OptionArgument(String name, String description, Option o1) {
        this(name, description, new Option[] { o1}, SINGLE);
    }

    public OptionArgument(String name, String description, Option o1, Option o2) {
        this(name, description, new Option[] { o1, o2}, SINGLE);
    }

    public OptionArgument(String name, String description, Option o1,
            Option o2, Option o3) {
        this(name, description, new Option[] { o1, o2, o3}, SINGLE);
    }

    public OptionArgument(String name, String description, Option o1,
            Option o2, Option o3, Option o4) {
        this(name, description, new Option[] { o1, o2, o3, o4}, SINGLE);
    }

    public OptionArgument(String name, String description, Option o1,
            Option o2, Option o3, Option o4, Option o5) {
        this(name, description, new Option[] { o1, o2, o3, o4, o5}, SINGLE);
    }

    public String format() {
        if (options.length == 0) return "";
        String result = options[ 0].getName();
        for (int i = 1; i < options.length; i++)
            result += "|" + options[ i].getName();
        return result;
    }

    public void describe(Help help) {
        for (int i = 0; i < options.length; i++)
            options[ i].describe(help);
    }

    public String complete(String partial) {
        List opts = new ArrayList();
        for (int i = 0; i < options.length; i++) {
            if (options[ i].getName().startsWith(partial))
                    opts.add(options[ i].getName());
        }

        return complete(partial, opts);
    }

    /**
     * Override this method to check if a given value "fits" this argument.
     * 
     * @param value
     * @return true if value, false otherwise.
     */
    protected boolean isValidValue(String value) {
        final int length = options.length;
        for (int i = 0; i < length; i++) {
            if (options[ i].getName().equals(value)) { return true; }
        }
        return false;
    }
    
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Options: ");
        final int length = options.length;
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(options[i].getName());
        }
        return sb.toString();
    }

    public static class Option extends Parameter {

        public Option(String name, String description) {
            super(name, description, MANDATORY);
        }
    }
}