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
 
package org.jnode.shell.command;
import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.driver.console.InputHistory;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.StringArgument;

/** 
 * List or execute a command from the shell's command history.
 * 
 * @author Matt Paine
 * @author crawley@jnode.org
 */
public class HistoryCommand extends AbstractCommand {

    private final IntegerArgument ARG_INDEX = 
        new IntegerArgument("index", Argument.OPTIONAL, 0, Integer.MAX_VALUE, "A history list index");
    private final StringArgument ARG_PREFIX = 
        new StringArgument("prefix", Argument.OPTIONAL, "A history list prefix (>= 0)");
    private final FlagArgument FLAG_TEST =
        new FlagArgument("test", Argument.OPTIONAL, "If set, don't try to execute the history command");

    private Shell shell;
    private PrintStream out;
    private PrintStream err;
    private InputHistory history;


    public HistoryCommand() {
        super("Command history list or execute");
        registerArguments(ARG_INDEX, ARG_PREFIX, FLAG_TEST);
    }

    @Override
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        shell = ShellUtils.getShellManager().getCurrentShell();
        history = shell.getCommandHistory();
        this.out = out;
        this.err = err;

        int index = ARG_INDEX.isSet() ? ARG_INDEX.getValue() : -1;
        String prefix = ARG_PREFIX.isSet() ? ARG_PREFIX.getValue() : null;
        boolean test = FLAG_TEST.isSet();

        if (index == -1 && prefix == null && !test) {
            listCommands();
        } else {
            runCommand(index, prefix, test);
        }
    }

    /** 
     * List every command in the history with its history index. 
     */
    public void listCommands() {
        for (int i = 0; i < history.size(); i++) {
            out.println("" + i + ": " + history.getLineAt(i));
        }
    }

    /**
     * Select and run (or print) a command from the history.
     * 
     * @param index a history index or <code>-1</code>
     * @param prefix a command prefix or <code>null</code>
     * @param test if <code>true</code> this is just a test ... don't execute the command.
     */
    public void runCommand(int index, String prefix, boolean test) {
        String line = (index >= 0) ? history.getLineAt(index) : history.getLineWithPrefix(prefix);

        if (line == null) {
            if (index >= 0) {
                err.println("History command #" + index + " not found");
            } else {
                err.println("History command starting with '" + prefix + "' not found");
            }
            exit(1);
        } else if (test) {
            out.println(line);
        } else {
            err.println("History command running is not implemented yet: try '-t' to test");
            exit(2);
        }
    }

    public static void main(String[] args) throws Exception {
        new HistoryCommand().execute(args);
    }
}
