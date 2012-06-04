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

import java.util.Iterator;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.OptionalSyntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This class implements the 'exit' built-in.  This is done by throwing a 
 * BjorneControlException with code 'BRANCH_EXIT'.
 * 
 * @author crawley@jnode.org
 */
final class ExitBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = 
        new SyntaxBundle("exit", new OptionalSyntax(new ArgumentSyntax("status")));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("exit", SYNTAX, new ExitBuiltin(), context);
        }
    };
    
    private final IntegerArgument argStatus = new IntegerArgument("status", Argument.OPTIONAL, 
            "The exit status");
    
    private ExitBuiltin() {
        super("Exit the current context (shell, shell function or script)");
        registerArguments(argStatus);
    }
    
    @Override
    public void execute() throws Exception {
        if (argStatus.isSet()) {
            throw new BjorneControlException(BjorneInterpreter.BRANCH_EXIT, argStatus.getValue());
        } else {
            throw new BjorneControlException(BjorneInterpreter.BRANCH_EXIT,
                    getParentContext().getLastReturnCode());
        }
    }

    @SuppressWarnings("deprecation")
    public int invoke(CommandLine command, BjorneInterpreter interpreter,
            BjorneContext context) throws ShellException {
        Iterator<String> args = command.iterator();
        if (!args.hasNext()) {
            throw new BjorneControlException(BjorneInterpreter.BRANCH_EXIT,
                    context.getParent().getLastReturnCode());
        } else {
            String arg = args.next();
            try {
                throw new BjorneControlException(BjorneInterpreter.BRANCH_EXIT,
                        Integer.parseInt(arg));
            } catch (NumberFormatException ex) {
                error("exit: " + arg + ": numeric argument required", context);
            }
        }
        return 1;
    }
}
