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
 
package org.jnode.shell.bjorne;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.OptionalSyntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This class implements the 'set' built-in.  It works by updating the state
 * of the shell's context object. 
 * 
 * @author crawley@jnode.org
 */
final class ShiftBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = 
        new SyntaxBundle("shift", new OptionalSyntax(new ArgumentSyntax("count")));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo createInstance(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("shift", SYNTAX, new ShiftBuiltin(), context);
        }
    };

    private final IntegerArgument argCount = new IntegerArgument(
            "count", Argument.OPTIONAL, 0, Integer.MAX_VALUE, "the shift count");
    
    private ShiftBuiltin() {
        super("shift the current arguments");
        registerArguments(argCount);
    }
    
    public void execute() throws Exception {
        BjorneContext pc = getParentContext();
        int nos = argCount.isSet() ? argCount.getValue() : 1;
        if (nos > 0) {
            int nosOldArgs = pc.nosArgs();
            if (nos >= nosOldArgs) {
                pc.setArgs(new String[0]);
                if (nos != nosOldArgs) {
                    exit(1);
                }
            } else {
                String[] oldArgs = pc.getArgs();
                String[] newArgs = new String[oldArgs.length - nos];
                System.arraycopy(oldArgs, nos, newArgs, 0, newArgs.length);
                pc.setArgs(newArgs);
            }
        }
    }
}
