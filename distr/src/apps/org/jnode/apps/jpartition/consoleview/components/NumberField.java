/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.apps.jpartition.consoleview.components;

import java.io.IOException;

import org.jnode.apps.jpartition.Context;

public class NumberField extends Component {
    public NumberField(Context context) {
        super(context);
    }

    public long show(String question) throws IOException {
        return show(question, Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public long show(String question, Long defaultValue) throws IOException {
        return show(question, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public Long show(String question, Long defaultValue, long min, long max) throws IOException {
        checkNonNull("question", question);
        checkInBounds("min", min, "max", max, "defaultValue", defaultValue);

        print(question);
        if (defaultValue != null) {
            print(" [" + defaultValue + "]");
        }

        Long value = readInt(defaultValue, min, max);
        while ((value == null) || (value < min) || (value > max)) {
            value = readInt(defaultValue);
        }

        return value;
    }
}
