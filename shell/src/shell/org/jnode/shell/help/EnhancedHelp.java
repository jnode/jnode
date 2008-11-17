/*
 * $Id: CommandLineElement.java 4556 2008-09-13 08:02:20Z crawley $
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
package org.jnode.shell.help;

import java.io.PrintWriter;

/**
 * This enhanced help interface provides extra fine-grained help 
 * methods.
 * 
 * @author crawley@jnode.org
 */
public interface EnhancedHelp extends Help {
    /**
     * Output the description (summary) text for the command.
     * 
     * @param pw the help information is written here
     */
    void description(PrintWriter pw);
    
    /**
     * Output the argument descriptions for the command.
     * 
     * @param pw the help information is written here
     */
    public void arguments(PrintWriter pw);
    
    /**
     * Output the option descriptions for the command.
     * 
     * @param pw the help information is written here
     */
    public void options(PrintWriter pw);
    
    /**
     * Output the detailed help text for the command.
     * 
     * @param pw the help information is written here
     */
    public void details(PrintWriter pw);
}
