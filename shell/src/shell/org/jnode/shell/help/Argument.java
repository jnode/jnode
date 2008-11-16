/*
 * $Id$
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
 
package org.jnode.shell.help;

import java.io.PrintWriter;

import org.jnode.driver.console.CompletionInfo;

/**
 * @author qades
 */
public class Argument extends CommandLineElement {

    public static final boolean SINGLE = false;

    public static final boolean MULTI = true;

    private static final String[] NO_VALUES = new String[0];

    private final boolean multi;

    public Argument(String name, String description, boolean multi) {
        super(name, description);
        this.multi = multi;
    }

    public Argument(String name, String description) {
        this(name, description, SINGLE);
    }

    public boolean isMulti() {
        return multi;
    }

    public String format() {
        return "<" + getName() + ">" + (isMulti() ? " ..." : "");
    }

    public void describe(HelpFactory help, PrintWriter out) {
        help.describeArgument(this, out);
    }

    // Command line completion
    private String[] values = NO_VALUES;

    private boolean satisfied = false;

    /**
     * Perform argument completion on the supplied (partial) argument value.  The
     * results of the completion should be added to the supplied CompletionInfo.
     * <p>
     * The default behavior is to return the argument value as a partial completion.  
     * Subtypes of Argument should override this method if they are capable of doing
     * non-trivial completion.  Completions should be registered by calling one
     * of the 'addCompletion' methods on the CompletionInfo.
     * 
     * @param completion the CompletionInfo object for registering any completions.
     * @param partial the argument string to be completed.
     */
    public void complete(CompletionInfo completion, String partial) {
        completion.addCompletion(partial, true);
    }

    protected final void setValue(String value) {
        if (isMulti()) {
            String[] values = new String[ this.values.length + 1];
            System.arraycopy(this.values, 0, values, 0, this.values.length);
            values[ this.values.length] = value;
            this.values = values;
        } else {
            this.values = new String[] {value};
        }
        setSatisfied(!isMulti());
    }

    /**
     * Override this method to check if a given value "fits" this argument.
     * 
     * @param value
     * @return true if value, false otherwise.
     */
    protected boolean isValidValue(String value) {
        return true;
    }

    public final String getValue(ParsedArguments args) {
        String[] result = getValues(args);
        if ((result == null) || (result.length == 0)) return null;
        return result[ 0];
    }

    public final String[] getValues(ParsedArguments args) {
        return args.getValues(this);
    }

    final String[] getValues() {
        return values;
    }

    protected final void clear() {
        this.values = new String[ 0];
        setSatisfied(false);
    }

    protected final void setSatisfied(boolean satisfied) {
        this.satisfied = satisfied;
    }

    public final boolean isSatisfied() {
        return satisfied;
    }
}
