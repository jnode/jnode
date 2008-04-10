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


/**
 * A SimpleSyntax instance allows an Argument to appear exactly once.
 * 
 * @author crawley@jnode.org
 *
 */
public class ArgumentSyntax extends Syntax {

    private final String argName;
    
    public ArgumentSyntax(String label, String argName, String description) {
        super(label, description);
        this.argName = argName;
        if (argName.length() == 0) {
            throw new IllegalArgumentException("empty argName");
        }
    }

    public ArgumentSyntax(String label, String argName) {
        this(label, argName, null);
    }

    public ArgumentSyntax(String argName) {
        this(null, argName, null);
    }

    @Override
    public String format(ArgumentBundle bundle) {
        try {
            Argument<?> arg = bundle.getArgument(this);
            return "<" + arg.format() + ">";
        }
        catch (SyntaxFailureException ex) {
            return "<" + label + "> (Unmatched syntax label!)";
        }
    }

    @Override
    public String toString() {
        return "SimpleSyntax{" + super.toString() + ",argName=" + argName + "}";
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        return new MuArgument(label, argName);
    }

    public String getArgName() {
        return argName;
    }

    @Override
    public XMLElement toXML() {
        XMLElement element = basicElement("argument");
        element.setAttribute("argLabel", argName);
        return element;
    }
}
