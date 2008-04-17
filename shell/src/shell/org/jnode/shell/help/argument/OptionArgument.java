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
 
package org.jnode.shell.help.argument;

import java.io.PrintStream;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;

/**
 * @author qades
 */
public class OptionArgument extends Argument {

    private final Option[] options;

    public OptionArgument(String name, String description, boolean multi,
            Option... options) {
        super(name, description, multi);
        this.options = options;
    }

    public OptionArgument(String name, String description, Option... options) {
        this(name, description, SINGLE, options);
    }

    @Override
    public String format() {
        StringBuilder result = new StringBuilder();
        for (Option option : options)
            result.append('|').append(option.getName());
        return result.substring(1);
    }

    @Override
    public void describe(Help help, PrintStream out) {
        for (Option option : options)
            option.describe(help, out);
    }

    @Override
    public void complete(CompletionInfo completion, String partial) {
        for (Option option : options) {
            final String name = option.getName();
            if (name.startsWith(partial)) {
                completion.addCompletion(name);
            }
        }
    }

    /**
     * Override this method to check if a given value "fits" this argument.
     * 
     * @param value
     * @return true if value, false otherwise.
     */
    @Override
    protected boolean isValidValue(String value) {
        for (Option option : options) {
            if (option.getName().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Options: ");
        for (Option option : options) {
            sb.append(", ");
            sb.append(option.getName());
        }
        return sb.toString().substring(2);
    }

    public static class Option extends Parameter {

        public Option(String name, String description) {
            super(name, description, MANDATORY);
        }
    }
}
