/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Command to get the filename part of a pathname.
 * 
 * @see <a href="http://www.opengroup.org/onlinepubs/000095399/utilities/basename.html">POSIX "basename" command</a>
 * @author chris boertien
 */
public class BasenameCommand extends AbstractCommand {

    private static final String help_name = "Strip this file name of its directory and optionally suffix components.";
    private static final String help_suffix = "Strip this suffix from the file name";
    private static final String help_super = "Strip directory and suffix from files names";
    
    private final StringArgument argName;
    private final StringArgument argSuffix;
    
    public BasenameCommand() {
        super(help_super);
        argName   = new StringArgument("name", Argument.MANDATORY, help_name);
        argSuffix = new StringArgument("suffix", Argument.OPTIONAL, help_suffix);
        registerArguments(argName, argSuffix);
    }
    
    public void execute() {
        String name = argName.getValue();
        
        int start = 0;
        int end = name.length();
        
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
            if (name.endsWith("/")) {
                end--;
            }
            start = name.lastIndexOf('/', end - 1) + 1;
            name = name.substring(start, end);
            
            if (argSuffix.isSet()) {
                String suffix = argSuffix.getValue();
                if (!name.equals(suffix) && name.endsWith(suffix)) {
                    name = name.substring(0, name.length() - suffix.length());
                }
            }
        }
        getOutput().getPrintWriter().println(name);
    }
}
