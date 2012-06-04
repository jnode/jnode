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
 
package org.jnode.shell.syntax;


/**
 * This class represents the micro-syntax for a fixed symbol.
 * 
 * @author crawley@jnode.org
 */
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
    public MuSyntaxKind getKind() {
        return MuSyntaxKind.SYMBOL;
    }

    @Override
    MuSyntax resolveBackReferences(ResolveState state) throws SyntaxFailureException {
        if (label != null) {
            state.refMap.put(label, this);
        }
        return this;
    }
}
