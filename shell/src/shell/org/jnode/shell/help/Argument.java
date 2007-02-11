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

import java.util.Collection;

import javax.naming.NameNotFoundException;

import org.jnode.shell.ShellUtils;

/**
 * @author qades
 */
public class Argument extends CommandLineElement {

    public static final boolean SINGLE = false;

    public static final boolean MULTI = true;

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

    public void describe(Help help) {
        help.describeArgument(this);
    }

    // Command line completion
    private String[] values = new String[ 0];

    private boolean satisfied = false;

    public String complete(String partial) {
        // No completion per default
        return partial;
    }

    protected String complete(String partial, Collection<String> list) {
        if (list.size() == 0) // none found
                return partial;

        if (list.size() == 1) return (String) list.iterator().next() + " ";

        // list matching
        String[] result = list.toArray(new String[list.size()]);
        list(result);

        // return the common part, i.e. complete as much as possible
        return common(result);
    }

    protected final void setValue(String value) {
        if (isMulti()) {
            String[] values = new String[ this.values.length + 1];
            System.arraycopy(this.values, 0, values, 0, this.values.length);
            values[ this.values.length] = value;
            this.values = values;
        } else {
            this.values = new String[] { value};
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

    protected String common(String... items) {
        if (items.length == 0)
        	return "";
        
        String result = items[ 0];
        for (String item : items) {
            while (!item.startsWith(result)) {
                // shorten the result until it matches
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }

    public void list(String... items) {
        try {
            ShellUtils.getShellManager().getCurrentShell().list(items);
        } catch (NameNotFoundException ex) {
            // should not happen!
            System.err.println("No list available");
        }
    }

}
