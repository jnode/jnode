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
package org.jnode.shell.command;

import java.io.PrintWriter;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.SimpleCommandInvoker;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.CommandInfo;
import org.jnode.shell.ShellException;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.vm.VmSystem;

/**
 * Measures how much execution of time command takes
 * <p/>
 * Date: Apr 16, 2009
 * Time: 9:18:45 PM
 *
 * @author petriai@gmail.com
 */
public class TimeCommand extends AbstractCommand {

    /* for i18n */
    private final static String TIME_COMMAND_START = "\nstart: %d\n";
    private final static String TIME_COMMAND_END   = "end  : %d\n";
    private final static String TIME_COMMAND_DIFF  = "       %d\n";

    private final AliasArgument ARG_CMD =
        new AliasArgument("alias", Argument.MANDATORY, "command to be run");

    private final StringArgument ARG_PARAMS =
        new StringArgument("args", Argument.MULTIPLE, "command parameters");

    public TimeCommand() {
        super("Measures execution time of command");
        registerArguments(ARG_CMD, ARG_PARAMS);
    }

    public static void main(String[] args) throws Exception {
        new TimeCommand().execute(args);
    }

    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();

        String alias = ARG_CMD.getValue();
        String[] args = ARG_PARAMS.getValues();
        
        CommandIO[] ios = new CommandIO[4];
        ios[0] = getIO(0);
        ios[1] = getIO(1);
        ios[2] = getIO(2);
        ios[3] = getIO(3);

        CommandLine commandLine = new CommandLine(alias, args, ios);

        CommandShell shell = null;
        try {
            shell = (CommandShell) ShellUtils.getShellManager().getCurrentShell();

            CommandInfo cmdInfo =  shell.getCommandInfo(alias);
            SimpleCommandInvoker invoker = shell.getDefaultCommandInvoker();
            long start = VmSystem.currentKernelMillis();
            int ret = invoker.invoke(commandLine, cmdInfo);
            long end = VmSystem.currentKernelMillis();
            out.format(TIME_COMMAND_START, start);
            out.format(TIME_COMMAND_END, end);
            out.format(TIME_COMMAND_DIFF, end - start);

        } catch (ShellException ex) {
            err.println(ex.getMessage());
            throw ex;
        }
    }

}
