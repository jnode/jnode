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

import java.io.PrintWriter;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.OptionSyntax;
import org.jnode.shell.syntax.RepeatSyntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This class implements the 'unalias' built-in.
 * 
 * @author crawley@jnode.org
 */
final class UnaliasBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = new SyntaxBundle("unalias", 
            new OptionSyntax("all", 'a'),
            new RepeatSyntax(new ArgumentSyntax("alias"), 1, Integer.MAX_VALUE));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("unalias", SYNTAX, new UnaliasBuiltin(context), context);
        }
    };
    
    private final FlagArgument flagAll = new FlagArgument(
            "all", Argument.OPTIONAL, "if set, undefine all aliases");
    
    private final BjorneAliasNameArgument argAlias; 
    
    private UnaliasBuiltin(BjorneContext context) {
        super("undefined Bjorne shell aliases");
        argAlias = new BjorneAliasNameArgument(
                "alias", context, Argument.OPTIONAL + Argument.MULTIPLE, "aliases to be undefined");
        registerArguments(flagAll, argAlias);
    }
    
    @Override
    public void execute() throws Exception {
        BjorneContext pc = getParentContext();
        PrintWriter err = getError().getPrintWriter();
        int rc = 0;
        if (flagAll.isSet()) {
            pc.getAliases().clear();
        } else {
            for (String arg : argAlias.getValues()) {
                String alias = pc.getAlias(arg);
                if (alias == null) {
                    err.println("alias: " + arg + " not found");
                    rc = 1;
                } else {
                    pc.undefineAlias(arg);
                }
            }
        }
        if (rc != 0) {
            exit(rc);
        }
    }
}
