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
 * This class provides a way of representing an empty syntax or syntax alternative; e.g. when
 * a command or command variant takes no options or arguments.  An EmptySyntax allows you to
 * attach a description or a syntax label, something that cannot be done when the empty alternative
 * is represented by a <code>null</code>.
 * 
 * @author crawley@jnode.org
 */
public class EmptySyntax extends Syntax {
    
    public EmptySyntax(String label, String description) {
        super(label, description);
    }
    
    @Override
    public String format(ArgumentBundle bundle) {
        return "";
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        return null;
    }

    @Override
    public XMLElement toXML() {
        return basicElement("empty");
    }

}
