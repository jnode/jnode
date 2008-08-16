/*
 * $Id: OptionArgument.java 3093 2007-01-27 13:40:17Z lsantha $
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
import java.lang.reflect.Array;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author qades
 * @deprecated use the org.jnode.shell.syntax.* classes instead.
 */
public class EnumOptionArgument<T extends Enum<?>> extends Argument {

    private final EnumOption<T>[] options;

    public EnumOptionArgument(String name, String description, boolean multi, EnumOption<T>... options) {
        super(name, description, multi);
        this.options = options;
    }

    public EnumOptionArgument(String name, String description, EnumOption<T>... options) {
        this(name, description, SINGLE, options);
    }

    public String format() {
        StringBuilder result = new StringBuilder();
        for (EnumOption<T> option : options) {
            result.append('|').append(option.getName());
        }
        return result.substring(1);
    }

    public void describe(Help help, PrintStream out) {
        for (EnumOption<T> option : options)
            option.describe(help, out);
    }

    public void complete(CompletionInfo completion, String partial) {
        for (EnumOption<T> option : options) {
            final String name = option.getName();
            if (name.startsWith(partial)) {
                completion.addCompletion(name);
            }
        }
    }

    public final T getEnum(ParsedArguments args, Class<T> type) {
        T[] result = getEnums(args, type);
        if ((result == null) || (result.length == 0)) {
            return null;
        }
        return result[0];
    }

    @SuppressWarnings("unchecked")
    public final T[] getEnums(ParsedArguments args, Class<T> type) {
        String[] values = getValues(args);
        T[] enums = (T[]) Array.newInstance(type, values.length);
        int i = 0;
        for (String value : values) {
            T enumValue = null;
            for (EnumOption<T> e : options) {
                if (value.equals(e.getName())) {
                    enumValue = e.getValue();
                    break;
                }
            }
            enums[i++] = enumValue;
        }
        return enums;
    }

    /**
     * Override this method to check if a given value "fits" this argument.
     * 
     * @param value
     * @return true if value, false otherwise.
     */
    protected boolean isValidValue(String value) {
        for (EnumOption<T> option : options) {
            if (option.getName().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Options: ");
        for (EnumOption<?> option : options) {
            sb.append(", ");
            sb.append(option.getName());
        }
        return sb.toString().substring(2);
    }

    public static class EnumOption<T extends Enum<?>> extends Parameter {
        private final T value;

        public EnumOption(String name, String description, T value) {
            super(name, description, MANDATORY);
            this.value = value;
        }

        public final T getValue() {
            return value;
        }
    }
}
