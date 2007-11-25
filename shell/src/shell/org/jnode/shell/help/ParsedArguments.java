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

import java.util.Map;

/**
 * @author qades
 */
public class ParsedArguments {

    private final Map<CommandLineElement, String[]> args;

    ParsedArguments(Map<CommandLineElement, String[]> args) {
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
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<CommandLineElement, String[]> entry : args.entrySet()) {
            if (sb.charAt(sb.length() - 1) != '{') {
                sb.append(',');
            }
            sb.append(entry.getKey().format()).append("->");
            if (entry.getValue() == null) {
                sb.append("null");
            }
            else {
                sb.append('[');
                for (String value: entry.getValue()) {
                    if (sb.charAt(sb.length() - 1) != '[') {
                        sb.append(',');
                    }
                    if (value == null) {
                        sb.append("null");
                    }
                    else {
                        sb.append('"').append(value).append('"');
                    }
                }
                sb.append(']');
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
