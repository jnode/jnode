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

import org.jnode.nanoxml.XMLElement;


/**
 * An Optional syntax specifies that one or more child syntaxes are an optional
 * sequence.
 * 
 * @author crawley@jnode.org
 */
public class OptionalSyntax extends GroupSyntax {
    
    public OptionalSyntax(String label, String description, Syntax...syntaxes) {
        super(label, description, syntaxes);
    }

    public OptionalSyntax(String label, Syntax...syntaxes) {
        this(label, null, syntaxes);
    }

    public OptionalSyntax(Syntax...syntaxes) {
        this(null, null, syntaxes);
    }

    @Override
    public String toString() {
        return "OptionalSyntax{" + super.toString()  + "}";
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        Syntax[] children = getChildSyntaxes();
        MuSyntax tmp;
        if (children.length == 0) {
            return null;
        } else if (children.length == 1) {
            tmp = children[0].prepare(bundle);
        } else {
            MuSyntax[] childMuSyntaxes = new MuSyntax[children.length];
            for (int i = 0; i < children.length; i++) {
                childMuSyntaxes[i] = children[i].prepare(bundle);
            }
            tmp = new MuSequence((String) null, childMuSyntaxes);
        }
        return new MuAlternation(label, null, tmp);
    }

    @Override
    public String format(ArgumentBundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (Syntax childSyntax : getChildSyntaxes()) {
            if (sb.length() > 0) {
                sb.append(" ");
            } else {
                sb.append("[ ");
            }
            String formatted = childSyntax.format(bundle);
            if (needsBracketting(formatted)) {
                sb.append("( ").append(formatted).append(" )");
            } else {
                sb.append(formatted);
            }
        }
        if (sb.length() > 0) {
            sb.append(" ]");
        }
        return sb.toString();
    }

    @Override
    public XMLElement toXML() {
        return basicElement("optional");
    }
}
