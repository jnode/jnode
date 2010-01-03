/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.command.system;

import java.io.PrintWriter;
import java.util.Set;

import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;

/**
 * @author epr
 */
public class NamespaceCommand extends AbstractCommand {

    private static final String help_super = "Print the contents of the system namespace";
    
    public NamespaceCommand() {
        super(help_super);
    }

    public static void main(String[] args) throws Exception {
        new NamespaceCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
        PrintWriter out = getOutput().getPrintWriter();
        Set<Class< ? >> names = InitialNaming.nameSet();
        for (Class< ? > name : names) {
            out.println(name);
        }
    }

}
