/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
import org.jnode.shell.help.CompletionException;

/**
 * A Completable is an object that is capable of performing completion on
 * a command shell input line.
 * 
 * @author crawley@jnode.org
 */
public interface Completable {

    /**
     * Add valid completions to the supplied 'completions' object.  Each
     * completion should be a complete (acceptable) token or a prefix for 
     * one or more acceptable tokens.  The method should not add too many
     * completions.
     * 
     * @param completions
     * @param shell
     * @throws CompletionException
     */
    void complete(CompletionInfo completions, CommandShell shell)
        throws CompletionException;

}
