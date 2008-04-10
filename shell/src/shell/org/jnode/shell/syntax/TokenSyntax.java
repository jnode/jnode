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

import nanoxml.XMLElement;

public class TokenSyntax extends Syntax {
    
    private final String symbol;
    
    public TokenSyntax(String label, String symbol, String description) {
        super(label, description);
        this.symbol = symbol;
    }

    public TokenSyntax(String label, String symbol) {
        this(label, symbol, null);
    }

    public TokenSyntax(String symbol) {
        this(null, symbol, null);
    }

    @Override
    public String format(ArgumentBundle bundle) {
        return symbol;
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        return new MuSymbol(label, symbol);
    }

    @Override
    public XMLElement toXML() {
        XMLElement element = basicElement("symbol");
        element.setAttribute("symbol", symbol);
        return element;
    }

}
