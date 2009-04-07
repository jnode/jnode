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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;

public class DirnameCommand extends AbstractCommand {

    private static final String help_name = "Strip the non-directory suffix from this file name";
    
    private final StringArgument Name = new StringArgument("name", Argument.MANDATORY, help_name);
    
    public DirnameCommand() {
        super("Strip non-directory suffix from the file name");
        registerArguments(Name);
    }
    
    public void execute() {
        String name = Name.getValue();
        
        /* The dirname on linux will not recognize a name that ends with a / as a directory, unless the
         * only character is /. So foo/bar/ gets output as foo/, and bar/ gets output as .
         */
        if (name.length() == 1) {
            if (!name.equals("/")) {
                name = ".";
            }
        } else {
            int end = name.lastIndexOf("/", name.length() - 2);
            if (end == -1) {
                name = ".";
            } else {
                name = name.substring(0,end);
            }
        }
        
        getOutput().getPrintWriter().println(name);
    }
}
