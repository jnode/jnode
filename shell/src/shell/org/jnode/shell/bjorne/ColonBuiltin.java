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
 
package org.jnode.shell.bjorne;

import org.jnode.shell.syntax.SequenceSyntax;
import org.jnode.shell.syntax.SyntaxBundle;



/**
 * This builtin does nothing.  It is intended for use in places where
 * the syntax requires a command; e.g. in an 'if' statement.
 * 
 * @author crawley@jnode.org
 */
final class ColonBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = new SyntaxBundle(":", new SequenceSyntax());
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context) {
            return new BjorneBuiltinCommandInfo(":", SYNTAX, new ColonBuiltin(), context);
        }
    };
    
    private ColonBuiltin() {
        super("The no-op command");
    }
    
    @Override
    public void execute() throws Exception {
        // no-op
    }
}
