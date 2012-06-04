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

import java.util.HashSet;
import java.util.Set;

import org.jnode.nanoxml.XMLElement;

/**
 * This is the base class for Syntax classes that compose other Syntaxes.  The
 * interesting behavior (e.g. the {@link Syntax#prepare(ArgumentBundle)} method) is implemented
 * in child classes.
 * 
 * @author crawley@jnode.org
 */
public abstract class GroupSyntax extends Syntax {
    private static Syntax MY_EMPTY = new EmptySyntax(null, null);

    final Syntax[] syntaxes;
    private final HashSet<String> childLabels;

    public GroupSyntax(String label, String description, Syntax...syntaxes) {
        super(label, description);
        this.syntaxes = syntaxes;
        this.childLabels = new HashSet<String>();
        for (Syntax syntax : syntaxes) {
            childLabels.addAll(syntax.childLabels());
            syntax.setParent(this);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("label='").append(label).append("', children=[");
        for (int i = 0; i < syntaxes.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(syntaxes[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public Set<String> childLabels() {
        return childLabels;
    }

    @Override
    public Syntax[] getChildren() {
        return this.syntaxes;
    }

    @Override
    public XMLElement basicElement(String name) {
        XMLElement element = super.basicElement(name);
        for (Syntax child : this.syntaxes) {
            element.addChild((child == null ? MY_EMPTY : child).toXML());
        }
        return element;
    }

}
