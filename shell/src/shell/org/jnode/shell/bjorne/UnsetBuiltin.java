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
import org.jnode.shell.syntax.RepeatSyntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This class implements the 'unset' built-in.
 * 
 * @author crawley@jnode.org
 */
final class UnsetBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = 
        new SyntaxBundle("unset", new RepeatSyntax(new ArgumentSyntax("unset")));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("unset", SYNTAX, new UnsetBuiltin(context), context);
        }
    };
    
    private final VariableNameArgument argVariables; 
    
    
    UnsetBuiltin(BjorneContext context) {
        super("Export shell variables to the environment");
        argVariables = new VariableNameArgument(
                "unset", context, Argument.MANDATORY, "variables to be unset");
        registerArguments(argVariables);
    }

    public void execute() throws Exception {
        int errorCount = 0;
        if (!argVariables.isSet()) {
            // FIXME - implement this?
        } else {
            BjorneContext pc = getParentContext();
            for (String var : argVariables.getValues()) {
                if (pc.isVariableReadonly(var)) {
                    errorCount++;
                } else {
                    pc.unsetVariable(var);
                }
            }
        }
        if (errorCount > 0) {
            exit(1);
        }
    }
}
