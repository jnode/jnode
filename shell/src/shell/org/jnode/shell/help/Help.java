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
 
package org.jnode.shell.help;

import java.io.PrintWriter;

/**
 * This is the interface for an object that outputs command help.  Different
 * implementations support different command syntax mechanisms, and (in the
 * future) will provide help in different output formats; e.g. plain text, 
 * HTML and so on.
 * 
 * @author crawley@jnode.org
 */
public interface Help {
    
    /**
     * Output complete help for the command.
     * 
     * @param pw the help information is written here
     */
    public void help(PrintWriter pw);
    
    /**
     * Output the usage message(s) for the command.
     * 
     * @param pw the help information is written here
     */
    public void usage(PrintWriter pw);
}
