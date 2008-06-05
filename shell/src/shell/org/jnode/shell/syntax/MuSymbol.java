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

public class MuSymbol extends MuSyntax {
    
    private final String symbol;
    
    public MuSymbol(String symbol) {
        this(null, symbol);
    }
    
    public MuSymbol(String label, String symbol) {
        super(label);
        this.symbol = symbol;
        if (symbol.length() == 0) {
            throw new IllegalArgumentException("empty symbol string");
        }
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    String format(FormatState state) {
        return '\'' + symbol + '\'';
    }

    @Override
    public int getKind() {
        return SYMBOL;
    }

    @Override
    MuSyntax resolveBackReferences(ResolveState state) throws SyntaxFailureException {
        if (label != null) {
            state.refMap.put(label, this);
        }
        return this;
    }
}
