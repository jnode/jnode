/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.shell.bjorne;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.OptionalSyntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This class implements the 'break' built-in.  This is done by throwing a 
 * BjorneControlException with code 'BRANCH_BREAK'.
 * 
 * @author crawley@jnode.org
 */
final class BreakBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX =
        new SyntaxBundle("break", new OptionalSyntax(new ArgumentSyntax("count")));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("break", SYNTAX, new BreakBuiltin(), context);
        }
    };

    private final IntegerArgument argCount = new IntegerArgument(
            "count", Argument.OPTIONAL, 1, Integer.MAX_VALUE, "the enclosing block count");
    
    private BreakBuiltin() {
        super("Break out of one or more enclosing blocks");
        registerArguments(argCount);
    }

    @Override
    public void execute() throws Exception {
        int count = argCount.isSet() ? argCount.getValue() : 1;
        throw new BjorneControlException(BjorneInterpreter.BRANCH_BREAK, count);
    }
}
