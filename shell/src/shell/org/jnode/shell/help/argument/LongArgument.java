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
 */
/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 * TODO should be factorized with IntegerArgument
 */
public class LongArgument extends Argument {
    private long min;
    private long max;

    public LongArgument(String name, String description) {
        this(name, description, SINGLE);
    }

    public LongArgument(String name, String description, boolean multi) {
        this(name, description, multi, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public LongArgument(String name, String description, boolean multi, long min, long max) {
        super(name, description, multi);
        if (min > max) {
            throw new IllegalArgumentException("min(value:" + min + ") > max(value:" + max + ")");
        }

        this.min = min;
        this.max = max;
    }

    @Override
    protected boolean isValidValue(String value) {
        long val = -1;
        try {
            val = getLongValue(value);
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

    public long getLong(ParsedArguments args) {
        return getLongValue(this.getValue(args));
    }

    protected long getLongValue(String value) {
        return Long.parseLong(value);
    }
}
