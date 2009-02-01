/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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

public class YesNo extends Component {
    public YesNo(Context context) {
        super(context);
    }

    public boolean show(String question) throws IOException {
        return show(question, null);
    }

    public boolean show(String question, Boolean defaultValue) throws IOException {
        checkNonNull("question", question);

        println();
        print(question);

        if (defaultValue != null) {
            String defaultValueStr = getValueStr(defaultValue);
            print("[" + defaultValueStr + "]");
        }

        Boolean value;
        do {
            value = readBoolean(defaultValue);

            if (value == null) {
                reportError("invalid value");
            }
        } while (value == null);

        return value;
    }
}
