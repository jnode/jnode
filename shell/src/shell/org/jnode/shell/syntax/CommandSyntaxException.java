/*
 * $Id: CompletionException.java 3561 2007-10-20 10:29:35Z lsantha $
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
 
package org.jnode.shell.syntax;

import java.util.List;

import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellException;
import org.jnode.shell.CommandLine.Token;

/**
 * This exception is thrown when the supplied command line arguments are not
 * compatible with the chosen syntax.
 * 
 * @author crawley@jnode.org
 */
public class CommandSyntaxException extends ShellException {
    public static class Context {
        public final CommandLine.Token token;
        public final MuSyntax syntax;
        public final int sourcePos;
        public final CommandSyntaxException exception;
        
        public Context(Token token, MuSyntax syntax, int sourcePos, 
                CommandSyntaxException exception) {
            super();
            this.token = token;
            this.syntax = syntax;
            this.sourcePos = sourcePos;
            this.exception = exception;
        }
    }
    
    private static final long serialVersionUID = 1L;

    private List<Context> argErrors;
    
    public CommandSyntaxException(String message) {
        super(message);
    }

    public CommandSyntaxException(String message, List<Context> argErrors) {
        super(message);
        this.argErrors = argErrors;
    }

    public List<Context> getArgErrors() {
        return argErrors;
    }
}
