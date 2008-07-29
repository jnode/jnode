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

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;
/**
 * This Argument class accepts integer values.  If instantiated with 'min' and 'max' values 
 * close together, it can perform completion.
 * 
 * @author crawley@jnode.org
 */
public class IntegerArgument extends Argument<Integer> {
    /**
     * Only do completion if <code>(max - min)</code> is less that this value.  This stops
     * us from generating an unmanageably large number of completions.
     */
    private static final int COMPLETION_THRESHOLD = 100;
    
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
        if (max < min) {
            throw new IllegalArgumentException("max < min");
        }
    }

    @Override
    protected Integer doAccept(Token token) throws CommandSyntaxException {
        try {
            int tmp = Integer.parseInt(token.token);
            if (tmp < min || tmp > max) {
                throw new CommandSyntaxException("number is out of range");
            }
            return new Integer(token.token);
        } catch (NumberFormatException ex) {
            throw new CommandSyntaxException("invalid number");
        }
    }
  
    @Override
    public void complete(CompletionInfo completion, String partial) {
        // FIXME ... maybe someone could figure out how to partial
        // completion efficiently when max - min is large?
        if (max - min >= 0 && max - min < COMPLETION_THRESHOLD) {
            for (int i = min; i <= max; i++) {
                String candidate = Integer.toString(i);
                if (candidate.startsWith(partial)) {
                    completion.addCompletion(candidate);
                }
            }
        }
    }

    @Override
    protected String state() {
        return super.state() + ",min=" + min + ",max=" + max;
    }
    
    @Override
    protected String argumentKind() {
        return "integer";
    }
}
