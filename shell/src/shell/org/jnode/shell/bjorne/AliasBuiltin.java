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
import java.util.Map;
import java.util.TreeMap;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.RepeatSyntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This class implements the 'alias' built-in.
 * 
 * @author crawley@jnode.org
 */
final class AliasBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = 
        new SyntaxBundle("alias", new RepeatSyntax(new ArgumentSyntax("alias")));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("alias", SYNTAX, new AliasBuiltin(context), context);
        }
    };
    
    private final BjorneAliasDefinitionArgument argAlias; 
    
    private AliasBuiltin(BjorneContext context) {
        super("define or list aliases");
        argAlias = new BjorneAliasDefinitionArgument(
                "alias", context, Argument.OPTIONAL + Argument.MULTIPLE, "an alias to be defined or printed");
        registerArguments(argAlias);
    }

    @Override
    public void execute() throws Exception {
        BjorneContext pc = getParentContext();
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        int rc = 0;
        if (!argAlias.isSet()) {
            printAliases(out, pc.getAliases());
        } else {
            for (String arg : argAlias.getValues()) {
                int pos = arg.indexOf('=');
                String aliasName;
                String alias;
                if (pos <= 0) {
                    aliasName = arg;
                    alias = null;
                } else {
                    aliasName = arg.substring(0, pos);
                    alias = arg.substring(pos + 1);
                }
                if (alias == null) {
                    alias = pc.getAlias(aliasName);
                    if (alias == null) {
                        err.println("alias: " + aliasName + " not found");
                        rc = 1;
                    } else {
                        printAlias(out, aliasName, alias);
                    }
                } else {
                    if (!BjorneToken.isName(aliasName)) {
                        err.println("alias: " + aliasName + ": not a valid alias name");
                    }
                    pc.defineAlias(aliasName, alias);
                }
            }
        }
        if (rc != 0) {
            exit(rc);
        }
    }

    private void printAliases(PrintWriter pw, TreeMap<String, String> aliases) {
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            printAlias(pw, entry.getKey(), entry.getValue());
        }
    }

    private void printAlias(PrintWriter pw, String aliasName, String alias) {
        pw.println(aliasName + "='" + alias + "'");
    }
   
    
}
