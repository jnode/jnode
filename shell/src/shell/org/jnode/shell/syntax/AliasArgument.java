/*
 * $Id: AliasArgument.java 2945 2006-12-20 08:51:17Z qades $
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

import java.util.TreeSet;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandShell;
import org.jnode.shell.Completable;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.CommandLine.Token;

/**
 * @author qades
 * @author crawley@jnode.org
 */
public class AliasArgument extends Argument<String> {

    public AliasArgument(String label, int flags, String description) {
        super(label, flags, new String[0], description);
    }

    public AliasArgument(String label, int flags) {
        this(label, flags, null);
    }

    public AliasArgument(String label) {
        this(label, 0);
    }
    
    public Completable createCompleter(String partial, int start, int end) {
    	return AliasArgument.completer(partial, start, end);
    }
    
    public static Completable completer(final String partial, final int start, final int end) {
    	return null;
    }
    
    public static Completable completer(Token token) {
    	return completer(token.token, token.start, token.end);
    }

	@Override
	public void doAccept(Token value) throws CommandSyntaxException {
	    throw new UnsupportedOperationException("not implemented");
	}

	public boolean doIsAcceptable(Token value) {
        throw new UnsupportedOperationException("not implemented");
	}
	
    @Override
    public String toString() {
        return "AliasArgument{" + super.toString() + "}";
    }
    
    @Override
    protected String argumentKind() {
        return "alias";
    }
}
