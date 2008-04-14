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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Echo the command's arguments to its output. 
 * 
 * @author epr
 * @author crawley@jnode.org
 */
public class EchoCommand extends AbstractCommand {
    
    private final StringArgument ARG_WORDS = 
        new StringArgument("text", Argument.MULTIPLE, "the text to be printed");
    
    public EchoCommand() {
        super("Print the argument text to standard output");
        registerArguments(ARG_WORDS);
    }
    
	public static void main(String[] args) throws Exception {
		new EchoCommand().execute(args);
	}

	/**
	 * Execute the command
	 */
	@SuppressWarnings("deprecation")
	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err)
	throws Exception {
	    String[] words = ARG_WORDS.getValues();
	    for (int i = 0; i < words.length; i++) {
	        if (i > 0) {
	            out.print(' ');
	        }
	        out.print(words[i]);
	    }
	    out.println();
	}
}
