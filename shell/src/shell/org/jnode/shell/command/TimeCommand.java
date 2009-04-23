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
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Measures how much execution of time command takes
 * <p/>
 *
 * @author petriai@gmail.com
 */
public class TimeCommand extends AbstractCommand {

    /* for i18n */
    private final static String help_alias = "command to be run";
    private static final String help_args  = "command parameters";
    private final static String fmt_start  = "%nstart: %d%n";
    private final static String fmt_end    = "end  : %d%n";
    private final static String fmt_diff   = "       %d%n";

    private final AliasArgument Alias;
    private final StringArgument Args;
        
    private static final boolean DEBUG = false;
    
    public TimeCommand() {
        super("Measures execution time of command");
        Alias = new AliasArgument("alias", Argument.MANDATORY, help_alias);
        Args = new StringArgument("args", Argument.MULTIPLE, help_args);
        registerArguments(Alias, Args);
    }

    public static void main(String[] args) throws Exception {
        new TimeCommand().execute(args);
    }

    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();

        StringBuilder sb = new StringBuilder(Alias.getValue());
        for (String arg : Args.getValues()) {
            sb.append(" ");
            sb.append(arg);
        }
        
        CommandShell shell = null;
        int ret = 1;
        try {
            shell = (CommandShell) ShellUtils.getShellManager().getCurrentShell();
            //CommandInfo cmdInfo =  shell.getCommandInfo(alias);
            long start = System.currentTimeMillis();
            ret = shell.runCommand(sb.toString());
            long end = System.currentTimeMillis();
            out.format(fmt_start, start);
            out.format(fmt_end, end);
            out.format(fmt_diff, end - start);
        } catch (ShellException ex) {
            if (DEBUG) {
                err.println(ex.getMessage());
            }
            throw ex;
        } finally {
            exit(ret);
        }
    }
}
