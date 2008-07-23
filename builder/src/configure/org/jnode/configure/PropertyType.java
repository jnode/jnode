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

import org.jnode.configure.PropertySet.Value;

/**
 * A property type constrains the values of a property.
 * 
 * @author crawley@jnode.org
 */
public abstract class PropertyType {
    private final String typeName;

    public PropertyType(String typeName) {
        super();
        this.typeName = typeName;
    }

    public abstract Value fromInput(String token);

    public abstract Value fromValue(String value);

    public String getTypeName() {
        return typeName;
    }

    public abstract String describe(Value defaultValue);

}
