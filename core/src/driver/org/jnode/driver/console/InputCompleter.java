/*
 * $Id: ConsoleManager.java 4564 2008-09-18 22:01:10Z fduminy $
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
package org.jnode.driver.console;

import java.io.PrintWriter;

/**
 * This interface is implemented by objects registered with a Console as
 * input completers.
 * 
 * @author crawley@jnode.org
 */
public interface InputCompleter {
    /**
     * Perform completion for the supplied partial input line.  Completion is
     * performed at the end line.
     * 
     * @param partial the partial input line.
     * @return a CompletionInfo that contains the possible completions.
     */
    public CompletionInfo complete(String partial);
    
    /**
     * Show incremental syntax help for the supplied partial input line.
     * 
     * @param partial the partial input line.
     * @param out the PrintWriter that help information should be written to.
     * @return <code>true</code> if any help information was written.
     */
    public boolean help(String partial, PrintWriter out); 

    /**
     * Gets the completer's current InputHistory object.  If the completer is modal,
     * different histories may be returned at different times.
     */
    public InputHistory getInputHistory();


}
