/*
 * $Id: Command.java 3772 2008-02-10 15:02:53Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.syntax.Argument;

/**
 * This class wraps an Argument as a Completable for use in a default syntax.
 * 
 * @author crawley@jnode.org
 */
public class ArgumentCompleter implements Completable {
    
    private final Argument<?> argument;
    private final CommandLine.Token token;
    
    /**
     * Create the wrapper.
     * 
     * @param argument the Argument to be used for completion.
     * @param token a CommandLine.Token supplying a partial value,
     *     or <code>null</code>.
     */
    public ArgumentCompleter(Argument<?> argument, CommandLine.Token token) {
        this.argument = argument;
        this.token = token;
    }

    public void complete(CompletionInfo completion, CommandShell shell) {
        argument.complete(completion, token == null ? "" : token.token);
        if (token != null) {
            completion.setCompletionStart(token.start);
        }
    }

}
