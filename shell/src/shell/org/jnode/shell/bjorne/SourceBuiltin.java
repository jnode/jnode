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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * The 'source' built-in executes commands read from a file in the
 * current shell context.  This differs from running a script in
 * the normal way in that 1) the current interpreter is used, and
 * 2) variables etc are set in the current shell context.
 * 
 * @author crawley@jnode.org
 */
final class SourceBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = 
        new SyntaxBundle("source", new ArgumentSyntax("script"));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("source", SYNTAX, new SourceBuiltin(), context);
        }
    };
    
    private final FileArgument argScript = new FileArgument(
            "script", Argument.MANDATORY, "the script to be executed");
    
    
    private SourceBuiltin() {
        super("run commands from a script file in the current shell context");
        registerArguments(argScript);
    }
    
    @Override
    public void execute() throws Exception {
        File file = argScript.getValue();
        FileReader reader = null;
        try {
            reader = new FileReader(file);

            // TODO ... implement args.
            BjorneContext pc = getParentContext();
            int rc = pc.getInterpreter().interpret(pc.getShell(), reader, true, "", new String[0]);
            if (rc != 0) {
                exit(rc);
            }
        } catch (IOException ex) {
            getError().getPrintWriter().println("source: " + file + ": " + ex.getMessage());
            exit(1);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    /* blah */
                }
            }
        }
    }
}
