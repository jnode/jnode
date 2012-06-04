/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.util.Date;

import org.jnode.shell.AbstractCommand;

/** 
 * A shell command to access the display the system date.
 * @author Matt Paine
 * @author crawley@jnode.org
 */
public class DateCommand extends AbstractCommand {

    private static final String help_super = "Print the current date";
    
    public DateCommand() {
        super(help_super);
    }

    @Override
    public void execute() throws Exception {
        getOutput().getPrintWriter().println(new Date());
    }

    /** 
     * Displays the system date
     * @param args No arguments.
     **/
    public static void main(String[] args) throws Exception {
        new DateCommand().execute(args);
    }

}
