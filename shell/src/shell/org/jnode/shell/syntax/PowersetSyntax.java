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
 * A Powerset syntax allows a group of child Syntaxes to be provided in any order.
 * The syntax allows a child syntax to appear more than once, subject to Argument
 * multiplicity constraints.  As with an Alternatives, the child syntaxes are tried
 * one at a time in the same order as the they were provided to the constructor.
 * The 'eager' parameter specifies whether the syntax is 'eager' (i.e. matching as 
 * many instances as possible) or 'lazy' (i.e. matching as few instances as possible).
 * 
 * @author crawley@jnode.org
 */
public class PowersetSyntax extends GroupSyntax {
    
    private final boolean eager;
    
    public PowersetSyntax(String label, boolean eager, String description, Syntax...syntaxes) {
        super(label, description, syntaxes);
        this.eager = eager;
    }

    public PowersetSyntax(String label, Syntax...syntaxes) {
        this(label, false, null, syntaxes);
    }

    public PowersetSyntax(Syntax...syntaxes) {
        this(null, false, null, syntaxes);
    }

    @Override
    public String toString() {
        return "PowersetSyntax{" + super.toString()  + "}";
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        Syntax[] children = getChildren();
        MuSyntax[] childMuSyntaxes = new MuSyntax[children.length];
        for (int i = 0; i < children.length; i++) {
            childMuSyntaxes[i] = children[i].prepare(bundle);
        }
        String label = this.label == null ? MuSyntax.genLabel() : this.label;
        MuSyntax res;
        if (eager) {
            res = new MuAlternation(label, 
                    new MuSequence(new MuAlternation((String) null, childMuSyntaxes),
                            new MuBackReference(label)),
                    null);
        } else {
            res = new MuAlternation(label, null, 
                    new MuSequence(new MuAlternation((String) null, childMuSyntaxes),
                            new MuBackReference(label)));
        } 
        res.resolveBackReferences();
        return res;
    }

    @Override
    public String format(ArgumentBundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (Syntax childSyntax : getChildren()) {
            if (sb.length() > 0) {
                sb.append(" | ");
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
            sb.append(" ] ...");
        }
        return sb.toString();
    }

    @Override
    public XMLElement toXML() {
        XMLElement element = basicElement("powerSet");
        if (eager) {
            element.setAttribute("eager", "true");
        }
        return element;
    }
}
