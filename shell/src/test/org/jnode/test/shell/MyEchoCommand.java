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
 
package org.jnode.test.shell;

import java.io.PrintWriter;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Echo the command's arguments to its output. 
 * 
 * @author epr
 * @author crawley@jnode.org
 */
public class MyEchoCommand extends AbstractCommand {

    private static final String help_words = "the text to be printed";
    private static final String help_super = "Print text to standard output";
    
    private final StringArgument argWords;

    public MyEchoCommand() {
        super(help_super);
        argWords = new StringArgument("text", Argument.MULTIPLE, help_words);
        registerArguments(argWords);
    }

    public static void main(String[] args) throws Exception {
        new MyEchoCommand().execute(args);
    }

    /**
     * Execute the command
     */
    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        String[] words = argWords.getValues();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                out.print(' ');
            }
            out.print(words[i]);
        }
        out.println();
    }
}
