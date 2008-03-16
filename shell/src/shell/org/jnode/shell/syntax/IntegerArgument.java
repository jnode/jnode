/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell.syntax;

import org.jnode.shell.Completable;
import org.jnode.shell.CommandLine.Token;

public class IntegerArgument extends Argument<Integer> {
    
    private final int min, max;

    public IntegerArgument(String label, int flags, String description) {
        this(label, flags, Integer.MIN_VALUE, Integer.MAX_VALUE, description);
    }

    public IntegerArgument(String label, int flags) {
        this(label, flags, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }

    public IntegerArgument(String label, int flags, int min, int max, String description) {
        super(label, flags, new Integer[0], description);
        this.min = min;
        this.max = max;
    }

    @Override
    protected void doAccept(Token token) throws CommandSyntaxException {
        try {
            int tmp = Integer.parseInt(token.token);
            if (tmp < min || tmp > max) {
                throw new CommandSyntaxException("number '" + token.token + "' is out of range");
            }
            addValue(new Integer(token.token));
        }
        catch (NumberFormatException ex) {
            throw new CommandSyntaxException("invalid number '" + token.token + "'");
        }
    }
  
    public boolean doIsAcceptable(Token token) {
        try {
            int tmp = Integer.parseInt(token.token);
            if (min <= tmp && tmp <= max) {
                return true;
            }
        }
        catch (NumberFormatException ex) {
            //
        }
        return false;
    }

    /**
     * It is not possible to complete an integer argument without some extra
     * context about what it means.  Subclasses for which completion is 
     * feasible should override this method.
     */
	public Completable createCompleter(String partial, int start, int end) {
		return null;
	}

    @Override
    public String toString() {
        return "IntegerArgument{" + super.toString() + "}";
    }
    
    @Override
    protected String argumentKind() {
        return "integer";
    }
}
