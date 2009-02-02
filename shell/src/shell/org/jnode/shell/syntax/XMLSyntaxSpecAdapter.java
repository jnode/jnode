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
 * This class adapts nanoxml.XMLElement instances for Syntax loaders / readers.
 * 
 * @author crawley@jnode.org
 */
public class XMLSyntaxSpecAdapter implements SyntaxSpecAdapter {
    private final XMLElement element;
    
    public XMLSyntaxSpecAdapter(XMLElement element) {
        this.element = element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getAttribute(String name) {
        String tmp = element.getStringAttribute(name);
        return (tmp != null && tmp.length() == 0) ? null : tmp;
    }

    @Override
    public SyntaxSpecAdapter getChild(int childNo) {
        return new XMLSyntaxSpecAdapter(element.getChildren().get(childNo));
    }

    @Override
    public int getNosChildren() {
        return element.getChildren().size();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof XMLSyntaxSpecAdapter)) {
            return false;
        }
        return element == ((XMLSyntaxSpecAdapter) obj).element;
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }
}
