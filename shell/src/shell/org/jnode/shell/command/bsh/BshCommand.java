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

import java.io.*;

import bsh.Interpreter;
import org.jnode.shell.help.*;
import org.jnode.shell.help.argument.StringArgument;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.shell.help.argument.OptionArgument;
import org.jnode.shell.CommandLine;

/**
 * @author Levente S\u00e1ntha
 */
public class BshCommand {

    private static final StringArgument ARG_CODE = new StringArgument("code", "a BeanShell code snippet to execute");
    static final OptionArgument ARG_C = new OptionArgument("-c", "execute code",
            new OptionArgument.Option("-c", "execute code"));
    private static final Parameter PARAM_C = new Parameter(ARG_C, Parameter.MANDATORY);
    private static final Parameter PARAM_CODE = new Parameter(ARG_CODE, Parameter.MANDATORY);
    private static final FileArgument ARG_FILE = new FileArgument("file", "a BeanShell script file to execute");
    static final OptionArgument ARG_F = new OptionArgument("-f", "execute file",
            new OptionArgument.Option("-f", "execute file"));
    private static final Parameter PARAM_F = new Parameter(ARG_F, Parameter.MANDATORY);
    private static final Parameter PARAM_FILE = new Parameter(ARG_FILE, Parameter.MANDATORY);
    static final OptionArgument ARG_I = new OptionArgument("-i", "go interactive",
            new OptionArgument.Option("-i", "go interactive"));
    private static final Parameter PARAM_INTERACTIVE = new Parameter(ARG_I, Parameter.MANDATORY);
    public static Help.Info HELP_INFO = new Help.Info(
            "bsh",
            new Syntax("start the interactive BeanShell interpreter"),
            new Syntax("invoke the BeanShell interpreter", PARAM_C, PARAM_CODE),
            new Syntax("invoke the BeanShell interpreter", PARAM_F, PARAM_FILE),
            new Syntax("invoke the BeanShell interpreter", PARAM_C, PARAM_CODE, PARAM_F, PARAM_FILE),
            new Syntax("invoke the BeanShell interpreter", PARAM_INTERACTIVE, PARAM_C, PARAM_CODE),
            new Syntax("invoke the BeanShell interpreter", PARAM_INTERACTIVE, PARAM_F, PARAM_FILE),
            new Syntax("invoke the BeanShell interpreter", PARAM_INTERACTIVE, PARAM_C, PARAM_CODE, PARAM_F, PARAM_FILE)
    );

    public static void main(String[] args) throws Exception {
        new BshCommand().execute(new CommandLine(args), System.in, System.out, System.err);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
        ParsedArguments parsedArguments = HELP_INFO.parse(commandLine.toStringArray());
        Interpreter bsh = null;
        Object ret;
        boolean interactive = false;

        if(PARAM_INTERACTIVE.isSet(parsedArguments)){
            bsh = createInterpreter(in, out, err, true);
            interactive = true;
        }


        if (PARAM_C.isSet(parsedArguments)){
            if(bsh == null)
                bsh = createInterpreter(in, out, err, false);

            String code = ARG_CODE.getValue(parsedArguments);
            ret = bsh.eval(code);

            if(ret != null)
                out.println(ret);
        }


        if(PARAM_F.isSet(parsedArguments)){
            if(bsh == null)
                bsh = createInterpreter(in, out, err, false);

            String file = ARG_FILE.getValue(parsedArguments);
            ret = bsh.source(file);

            if(ret != null)
                out.println(ret);
        }

        if(bsh == null){
            bsh = createInterpreter(in, out, err, true);
            interactive = true;
        }
        
        if(interactive)
            bsh.run();
    }

    private static Interpreter createInterpreter(InputStream in, OutputStream out, OutputStream err, boolean interactive)
        throws Exception{
        Interpreter interpreter = new Interpreter(
                new BufferedReader(new InputStreamReader(in)),
                new PrintStream(out),
                new PrintStream(err), interactive);
        if(interactive){
            interpreter.eval("show();");
        }
        return interpreter;
    }
}
