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

import java.io.PrintWriter;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.RepeatSyntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This class implements the 'export' built-in.
 * 
 * @author crawley@jnode.org
 */
final class ExportBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = 
        new SyntaxBundle("export", new RepeatSyntax(new ArgumentSyntax("export")));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("export", SYNTAX, new ExportBuiltin(context), context);
        }
    };
    
    private final AssignmentArgument argExport; 
    
    
    ExportBuiltin(BjorneContext context) {
        super("Export shell variables to the environment");
        argExport = new AssignmentArgument(
                "export", context, Argument.MANDATORY, "variables to be exported");
        registerArguments(argExport);
    }

    public void execute() throws Exception {
        int errorCount = 0;
        if (!argExport.isSet()) {
            // FIXME - implement this?
        } else {
            BjorneContext pc = getParentContext();
            PrintWriter err = getError().getPrintWriter();
            for (String arg : argExport.getValues()) {
                int pos = arg.indexOf('=');
                if (pos == -1) {
                    pc.setExported(arg, true);
                } else if (pos == 0) { 
                    err.println("export: " + arg + ": not a valid identifier");
                    errorCount++;
                } else {
                    String name = arg.substring(0, pos);
                    String value = arg.substring(pos + 1);
                    if (!BjorneToken.isName(name)) {
                        err.println("export: " + name + ": not a valid identifier");
                        errorCount++;
                    }
                    pc.setVariable(name, value);
                    pc.setExported(name, true);
                }
            }
        }
        if (errorCount > 0) {
            exit(1);
        }
    }
}
