/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.shell.command.bsh;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

import bsh.Interpreter;

/**
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class BshCommand extends AbstractCommand {

    private final StringArgument ARG_CODE = 
        new StringArgument("code", Argument.OPTIONAL, "a BeanShell code snippet to execute");
    private final FileArgument ARG_FILE = 
        new FileArgument("file", Argument.OPTIONAL, "a BeanShell script file to execute");
    private final FlagArgument FLAG_INTERACTIVE = 
        new FlagArgument("interactive", Argument.OPTIONAL, "go interactive");
    
    public BshCommand() {
        super("run the BeanShell interpreter");
        registerArguments(ARG_CODE, ARG_FILE, FLAG_INTERACTIVE);
    }

    public static void main(String[] args) throws Exception {
        new BshCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
        throws Exception {
        Interpreter bsh = null;
        Object ret;
        boolean interactive = false;

        if (FLAG_INTERACTIVE.isSet()) {
            bsh = createInterpreter(in, out, err, true);
            interactive = true;
        }

        if (ARG_CODE.isSet()) {
            if (bsh == null) {
                bsh = createInterpreter(in, out, err, false);
            }
            String code = ARG_CODE.getValue();
            ret = bsh.eval(code);

            if (ret != null) {
                out.println(ret);
            }
        }

        if (ARG_FILE.isSet()) {
            if (bsh == null) {
                bsh = createInterpreter(in, out, err, false);
            }

            String file = ARG_FILE.getValue().toString();
            ret = bsh.source(file);

            if (ret != null) {
                out.println(ret);
            }
        }

        if (bsh == null) {
            // If no arguments were given, default to interactive mode.
            bsh = createInterpreter(in, out, err, true);
            interactive = true;
        }

        if (interactive) {
            bsh.run();
        }
    }

    private Interpreter createInterpreter(
            InputStream in, OutputStream out, OutputStream err, boolean interactive)
        throws Exception {
        Interpreter interpreter = new Interpreter(
                new BufferedReader(new InputStreamReader(in)),
                new PrintStream(out),
                new PrintStream(err), interactive);
        if (interactive) {
            interpreter.eval("show();");
        }
        return interpreter;
    }
}
