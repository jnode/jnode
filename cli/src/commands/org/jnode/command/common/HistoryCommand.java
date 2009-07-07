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
 
package org.jnode.command.common;

import java.io.PrintWriter;

import org.jnode.driver.console.InputHistory;
import org.jnode.shell.AbstractCommand;
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

    private static final String help_index = "A history list index";
    private static final String help_prefix = "A history list prefix (>= 0)";
    private static final String help_test = "If set, don't try to execute the history command";
    private static final String help_super = "Command history list or execute";
    private static final String err_not_found_1 = "History command #%d not found%n";
    private static final String err_not_found_2 = "History command starting with '%s' not found%n";
    private static final String err_test = "History command running is not implemented yet: try '-t' to test";
    
    private final IntegerArgument argIndex;
    private final StringArgument argPrefix;
    private final FlagArgument argTest;

    private Shell shell;
    private PrintWriter out;
    private PrintWriter err;
    private InputHistory history;


    public HistoryCommand() {
        super(help_super);
        argIndex  = new IntegerArgument("index", Argument.OPTIONAL, help_index);
        argPrefix = new StringArgument("prefix", Argument.OPTIONAL, help_prefix);
        argTest   = new FlagArgument("test", Argument.OPTIONAL, help_test);
        registerArguments(argIndex, argPrefix, argTest);
    }

    @Override
    public void execute() throws Exception {
        shell = ShellUtils.getShellManager().getCurrentShell();
        history = shell.getCommandHistory();
        this.out = getOutput().getPrintWriter();
        this.err = getError().getPrintWriter();

        int index = argIndex.isSet() ? argIndex.getValue() : -1;
        String prefix = argPrefix.isSet() ? argPrefix.getValue() : null;
        boolean test = argTest.isSet();

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
                err.format(err_not_found_1, index);
            } else {
                err.format(err_not_found_2, prefix);
            }
            exit(1);
        } else if (test) {
            out.println(line);
        } else {
            err.println(err_test);
            exit(2);
        }
    }

    public static void main(String[] args) throws Exception {
        new HistoryCommand().execute(args);
    }
}
