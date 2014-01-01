/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.command.common;

import java.io.PrintWriter;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Measures the execution time of a simple command.
 * <p/>
 *
 * @author petriai@gmail.com
 * @author chris boertien
 */
public class TimeCommand extends AbstractCommand {

    /* for i18n */
    private static final String help_alias = "command to be run";
    private static final String help_args  = "command parameters";
    private static final String help_super = "Measures execution time of commands";
    private static final String fmt_diff   = "time: %s%n";

    private final AliasArgument Alias;
    private final StringArgument Args;
    
    public TimeCommand() {
        super(help_super);
        Alias = new AliasArgument("alias", Argument.MANDATORY, help_alias);
        Args  = new StringArgument("args", Argument.MULTIPLE, help_args);
        registerArguments(Alias, Args);
    }

    public static void main(String[] args) throws Exception {
        new TimeCommand().execute(args);
    }

    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();

        StringBuilder sb = new StringBuilder(Alias.getValue());
        for (String arg : Args.getValues()) {
            sb.append(' ');
            sb.append(arg);
        }
        
        CommandShell shell = null;
        int ret = 1;
        try {
            shell = (CommandShell) ShellUtils.getShellManager().getCurrentShell();
            long start = System.currentTimeMillis();
            ret = shell.runCommand(sb.toString());
            long end = System.currentTimeMillis();
            out.format(fmt_diff, getRuntime((int) (end - start)));
        } catch (ShellException ex) {
            throw ex;
        } finally {
            exit(ret);
        }
    }
    
    public String getRuntime(int time) {
        int hours = time / (60 * 60 * 1000);
        int minutes = (time / (60 * 1000)) % 60;
        int seconds = (time / 1000) % 60;
        int millis  = time % 1000;
        if (hours > 0) {
            return String.format("%2d:%2d:%2d.%3ds", hours, minutes, seconds, millis);
        }
        if (minutes > 0) {
            return String.format("%2d:%2d.%3ds", minutes, seconds, millis);
        }
        if (seconds > 0) {
            return String.format("%2d.%3ds", seconds, millis);
        }
        return String.format("%3dms", millis);
    }
}
