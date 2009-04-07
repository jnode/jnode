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

public class BasenameCommand extends AbstractCommand {

    private static final String help_name = "Strip this filename of its directory and optionally suffix components.";
    private static final String help_suffix = "Strip this suffix from the filename";
    
    private final StringArgument Name = new StringArgument("name", Argument.MANDATORY, help_name);
    private final StringArgument Suffix = new StringArgument("suffix", Argument.OPTIONAL, help_suffix);
    public BasenameCommand() {
        super("Strip directory and suffix from filenames");
        registerArguments(Name, Suffix);
    }
    
    public void execute() {
        String name = Name.getValue();
        
        int start = name.lastIndexOf('/') + 1;
        int end = name.length();
        
        if (Suffix.isSet()) {
            String suffix = Suffix.getValue();
            if (name.endsWith(suffix)) {
                end -= suffix.length();
                if ((start + 1) == end) {
                    end = name.length();
                }
            }
        }
        
        getOutput().getPrintWriter().println(name.substring(start,end));
    }
}
