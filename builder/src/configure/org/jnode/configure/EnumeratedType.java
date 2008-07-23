/*
 * $Id $
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
package org.jnode.configure;

import java.util.List;

import org.jnode.configure.PropertySet.Value;

/**
 * This class represents a property type with a enumerated set of values.
 * 
 * @author crawley@jnode.org
 */
public class EnumeratedType extends PropertyType {

    public static class Alternate {
        public final String token;
        public final String value;

        public Alternate(String token, String value) {
            super();
            this.token = token;
            this.value = value;
        }
    }

    private final List<Alternate> alternates;

    public EnumeratedType(String typeName, List<Alternate> alternates) {
        super(typeName);
        this.alternates = alternates;
    }

    @Override
    public Value fromInput(String token) {
        for (Alternate alternate : alternates) {
            if (alternate.token.equals(token)) {
                return new Value(token, alternate.value);
            }
        }
        return null;
    }

    @Override
    public Value fromValue(String value) {
        for (Alternate alternate : alternates) {
            if (alternate.value.equals(value)) {
                return new Value(alternate.token, value);
            }
        }
        return null;
    }

    @Override
    public String describe(Value defaultValue) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        boolean first = true;
        for (Alternate alternate : alternates) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            boolean isDefault = alternate.value.equals(defaultValue.getText());
            if (isDefault) {
                sb.append("[");
            }
            sb.append(alternate.token);
            if (isDefault) {
                sb.append("]");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
