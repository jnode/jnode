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
 
package org.jnode.shell.command.posix;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Unix `dirname` command
 * @see http://www.opengroup.org/onlinepubs/000095399/utilities/dirname.html
 * @author chris boertien
 */
public class DirnameCommand extends AbstractCommand {

    private static final String help_name = "Strip the non-directory suffix from this file name";
    private static final String help_super = "Strip non-directory suffix from the file name";
    
    private final StringArgument argName = new StringArgument("name", Argument.MANDATORY, help_name);
    
    public DirnameCommand() {
        super(help_super);
        registerArguments(argName);
    }
    
    public void execute() {
        String name = argName.getValue();
        
        if (name.equals("")) {
            name = ".";
        }
        
        boolean allSlashes = true;
        for (char c : name.toCharArray()) {
            if (c != '/') {
                allSlashes = false;
                break;
            }
        }
        
        if (allSlashes) {
            name = "/";
        } else {
            int i = name.length() - 1;
            if (name.endsWith("/")) {
                i--;
            }
            i = name.lastIndexOf('/', i);
            if (i == -1) {
                name = ".";
            } else if (i == 0) {
                name = "/";
            } else {
                name = name.substring(0, i);
            }
        }
        
        getOutput().getPrintWriter().println(name);
    }
}
