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

package org.jawk;

import java.io.IOException;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

public class JawkCommand extends AbstractCommand {

    private static final String help_vars = "Variable assignt before execution";
    private static final String help_fs = "Field sepatator";
    private static final String help_script = "Use the given file as the program";
    private static final String help_interm_out = "Write intermediate file. The file can be given to -f";
    private static final String help_interm_file = "Output file for intermediate data";
    private static final String help_compile = "Compile for JVM instead of interpreting";
    private static final String help_compile_exec = "Compile for JVM and execute";
    private static final String help_compile_dir = "Compile results to the destination directory";
    private static final String help_dump_interm = "Dump the intermediate code";
    private static final String help_dump_ast = "Dump the syntax tree";
    private static final String help_xfuncs = "Enables the _sleep, _dump and exec functions";
    private static final String help_xtypes = "Enables _INTEGER, _DOUBLE and _STRING type casting keywords";
    private static final String help_sort_arrays = "Maintain array keys in sorted order";
    private static final String help_no_fmt_trap = "Do not trap formatting errors from printf/sprintf";
    private static final String help_program = "Run this as the program, instead of from a file";
    private static final String help_input_files = "Process the files as input";
    
    private StringArgument Vars;
    private StringArgument FS;
    private StringArgument Program;
    private FileArgument Script;
    private FileArgument IntermFile;
    private FileArgument CompileDir;
    private FileArgument InputFiles;
    private FlagArgument IntermOut;
    private FlagArgument Compile;
    private FlagArgument CompileExec;
    private FlagArgument DumpInterm;
    private FlagArgument DumpAST;
    private FlagArgument XFuncs;
    private FlagArgument XTypes;
    private FlagArgument SortArrays;
    private FlagArgument NoFmtTrap;
    
    public JawkCommand() {
        super("awk text processing language");
        Vars = new StringArgument("vars", Argument.OPTIONAL | Argument.MULTIPLE, help_vars);
        FS = new StringArgument("field-sep", Argument.OPTIONAL, help_fs);
        Program = new StringArgument("program", Argument.OPTIONAL, help_program);
        Script = new FileArgument("script", Argument.OPTIONAL | Argument.EXISTING, help_script);
        IntermFile = new FileArgument("interm-file", Argument.OPTIONAL, help_interm_file);
        CompileDir = new FileArgument("compile-dir", Argument.OPTIONAL | Argument.EXISTING, help_compile_dir);
        InputFiles 
            = new FileArgument("files", Argument.OPTIONAL | Argument.MULTIPLE | Argument.EXISTING, help_input_files);
        IntermOut = new FlagArgument("interm-out", Argument.OPTIONAL, help_interm_out);
        Compile = new FlagArgument("compile", Argument.OPTIONAL, help_compile);
        CompileExec = new FlagArgument("compile-exec", Argument.OPTIONAL, help_compile_exec);
        DumpInterm = new FlagArgument("dump-interm", Argument.OPTIONAL, help_dump_interm);
        DumpAST = new FlagArgument("dump-ast", Argument.OPTIONAL, help_dump_ast);
        XFuncs = new FlagArgument("xfuncs", Argument.OPTIONAL, help_xfuncs);
        XTypes = new FlagArgument("xtypes", Argument.OPTIONAL, help_xtypes);
        SortArrays = new FlagArgument("sort-arrays", Argument.OPTIONAL, help_sort_arrays);
        NoFmtTrap = new FlagArgument("no-fmt-trap", Argument.OPTIONAL, help_no_fmt_trap);
        registerArguments(Vars, FS, Program, Script, IntermFile, CompileDir, IntermOut, Compile, CompileExec);
        registerArguments(DumpInterm, DumpAST, XFuncs, XTypes, SortArrays, NoFmtTrap, InputFiles);
    }
    
    public void execute() {
        int rc = 1;
        try {
            rc = Awk.invoke(getCommandLine().getArguments());
        } catch (IOException e) {
            rc = 1;
            getError().getPrintWriter().println(e.getLocalizedMessage());
        } catch (ClassNotFoundException e2) {
            rc = 1;
            getError().getPrintWriter().println(e2.getLocalizedMessage());
        } finally {
            exit(rc);
        }
    }
}
