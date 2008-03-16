/*
 * $Id$
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

package org.jnode.shell.syntax;

import java.io.File;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandShell;
import org.jnode.shell.Completable;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.CommandLine.Token;

public class FileArgument extends Argument<File> {

    public FileArgument(String label, int flags, String description) {
        super(label, flags, new File[0], description);
    }

    public FileArgument(String label, int flags) {
        this(label, flags, null);
    }

    @Override
    protected void doAccept(Token token) throws CommandSyntaxException {
        // FIXME ... do proper filename checks ...
        if (token.token.length() > 0) {
            addValue(new File(token.token));
        }
        else {
            throw new CommandSyntaxException("invalid file name '" + token.token + "'");
        }
    }
  
//    public boolean doIsAcceptable(Token token) {
//        return token.value.length() > 0;
//    }

	public Completable createCompleter(String partial, int start, int end) {
		return FileArgument.completer(partial, start, end);
	}

	public static Completable completer(Token token) {
		return FileArgument.completer(token.token, token.start, token.end);
	}
	
	public static Completable completer(String partial, int start, int end) {
		return null;
	}

	@Override
	public String toString() {
	    return "FileArgument{" + super.toString() + "}";
	}
	
	@Override
    protected String argumentKind() {
        return "file";
    }
}
