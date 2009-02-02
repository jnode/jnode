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
 
package org.jnode.shell.syntax;

import org.jnode.nanoxml.XMLElement;

/**
 * A VerbSyntax matches a given string setting an argument to the string
 * "true" if it succeeds.  The behavior is similar to an OptionSyntax
 * with a 'longName', except that we don't prepend "--" to the symbol. 
 * 
 * @author crawley@jnode.org
 */
public class VerbSyntax extends ArgumentSyntax {
    
    private final String symbol;
    private final String argName;
    
    public VerbSyntax(String label, String symbol, String argName, String description) {
        super(label, argName, description);
        this.symbol = symbol;
        if (symbol.length() == 0) {
            throw new IllegalArgumentException("empty symbol");
        }
        this.argName = argName;
        if (argName.length() == 0) {
            throw new IllegalArgumentException("empty argName");
        }
    }

    @Override
    public String format(ArgumentBundle bundle) {
        // Hmmmm
        return symbol;
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        Argument<?> arg = bundle.getArgument(this);
        return new MuSequence(
                new MuSymbol(symbol), 
                new MuPreset(arg.getLabel(), "true"));
    }

    @Override
    public XMLElement toXML() {
        XMLElement element = basicElement("verb");
        element.setAttribute("symbol", symbol);
        element.setAttribute("argName", argName);
        return element;
    }

    @Override
    public String toString() {
        return "VerbSyntax{" + super.toString() + ",symbol=" + symbol + 
                ",argName=" + argName + "}";
    }

}
