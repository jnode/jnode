/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.io.PrintWriter;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.RepeatSyntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This class implements the 'readonly' built-in.
 * 
 * @author crawley@jnode.org
 */
final class ReadonlyBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = 
        new SyntaxBundle("readonly", new RepeatSyntax(new ArgumentSyntax("readonly")));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("readonly", SYNTAX, new ReadonlyBuiltin(context), context);
        }
    };
    
    private final AssignmentArgument argVariables; 
    
    
    ReadonlyBuiltin(BjorneContext context) {
        super("Mark shell variables as readonly");
        argVariables = new AssignmentArgument(
                "readonly", context, Argument.MANDATORY, "variables to be marked as readonly");
        registerArguments(argVariables);
    }

    public void execute() throws Exception {
        int errorCount = 0;
        if (!argVariables.isSet()) {
            // FIXME - implement this?
        } else {
            BjorneContext pc = getParentContext();
            PrintWriter err = getError().getPrintWriter();
            for (String var : argVariables.getValues()) {
                int pos = var.indexOf('=');
                if (pos == -1) {
                    pc.setVariableReadonly(var, true);
                } else if (pos == 0) { 
                    err.println("readonly: " + var + ": not a valid identifier");
                    errorCount++;
                } else {
                    String name = var.substring(0, pos);
                    String value = var.substring(pos + 1);
                    if (!BjorneToken.isName(name)) {
                        err.println("readonly: " + name + ": not a valid identifier");
                        errorCount++;
                    }
                    pc.setVariable(name, value);
                    pc.setVariableReadonly(name, true);
                }
            }
        }
        if (errorCount > 0) {
            exit(1);
        }
    }
}
