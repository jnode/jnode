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

import org.jnode.shell.Completable;
import org.jnode.shell.CommandLine.Token;

public class StringArgument extends Argument<String> {

    public StringArgument(String label, int flags, String description) {
        super(label, flags, new String[0], description);
    }

    public StringArgument(String label, int flags) {
        this(label, flags, null);
    }

    public StringArgument(String label) {
        this(label, 0, null);
    }

    @Override
    protected void doAccept(Token token) throws CommandSyntaxException {
        addValue(token.token);
    }
    
   /**
     * It is not possible to complete a simple argument without some extra
     * context about what it means.  Subclasses for which completion is 
     * feasible should override this method.
     */
	public Completable createCompleter(String partial, int start, int end) {
		return null;
	}
	
	@Override
    protected String argumentKind() {
        return "string";
    }
}
