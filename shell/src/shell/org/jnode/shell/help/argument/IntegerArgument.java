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

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 * TODO should be factorized with LongArgument
 */
public class IntegerArgument extends Argument {
    private int min;
    private int max;

    public IntegerArgument(String name, String description) {
        this(name, description, SINGLE);
    }

    public IntegerArgument(String name, String description, boolean multi) {
        this(name, description, multi, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntegerArgument(String name, String description, boolean multi, int min, int max) {
        super(name, description, multi);
        if (min > max) {
            throw new IllegalArgumentException("min(value:" + min + ") > max(value:" + max + ")");
        }

        this.min = min;
        this.max = max;
    }

    @Override
    protected boolean isValidValue(String value) {
        int val = -1;
        try {
            val = getIntValue(value);
        } catch (NumberFormatException e) {
            return false;
        }

        if (val < min) {
            return false;
        }

        if (val > max) {
            return false;
        }

        return true;
    }

    public int getInteger(ParsedArguments args) {
        return getIntValue(this.getValue(args));
    }

    protected int getIntValue(String value) {
        return Integer.parseInt(value);
    }
}
