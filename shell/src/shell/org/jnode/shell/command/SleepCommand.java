/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.IntegerArgument;


/**
 * Sleep for a given number of seconds.
 * 
 * @author crawley@jnode.org
 */

public class SleepCommand extends AbstractCommand {
    private IntegerArgument SECONDS_ARG = 
        new IntegerArgument("seconds", Argument.MANDATORY, "the number of seconds to sleep");
    
    public SleepCommand() {
        super("Sleep for a given number of seconds");
        registerArguments(SECONDS_ARG);
    }

    public static void main(String[] args) throws Exception {
        new SleepCommand().execute(args);
    }

    public void execute() throws Exception {
        Integer seconds = SECONDS_ARG.getValue();
        if (seconds > 0) {
            Thread.sleep(seconds * 1000);
        }
    }
}
