/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.shell.help;

import java.util.List;
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

    protected String complete(String partial, List<String> list) {
        if (list.size() == 0) // none found
                return partial;

        if (list.size() == 1) return (String) list.get(0) + " ";

        // list matching
        String[] result = (String[]) list
                .toArray(new String[ list.size()/*
                                                 * ToDo: remove this ugly
                                                 * workaround
                                                 */]);
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

    protected String common(String[] items) {
        final int len = items.length;
        if (len == 0) return "";
        String result = items[ 0];
        for (int i = 1; i < len; i++) {
            while (!items[ i].startsWith(result)) {
                // shorten the result until it matches
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }

    public void list(String[] items) {
        try {
            ShellUtils.getShellManager().getCurrentShell().list(items);
        } catch (NameNotFoundException ex) {
            // should not happen!
            System.err.println("No list available");
        }
    }

}
