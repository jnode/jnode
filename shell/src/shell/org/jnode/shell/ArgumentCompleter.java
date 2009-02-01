/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
        argument.complete(completion, token == null ? "" : token.text);
        if (token != null) {
            completion.setCompletionStart(token.start);
        }
    }

}
