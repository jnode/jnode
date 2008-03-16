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


/**
 * A Powerset syntax allows a group of child Syntaxes to be provided in any order.
 * The syntax allows a child syntax to appear more than once, subject to Argument
 * multiplicity constraints.  As with an Alternatives, the child syntaxes are tried
 * one at a time in the same order as the they were provided to the constructor.
 * 
 * @author crawley@jnode.org
 */
public class PowersetSyntax extends GroupSyntax {
    
    public PowersetSyntax(String label, Syntax...syntaxes) {
        super(label, syntaxes);
    }

    public PowersetSyntax(Syntax...syntaxes) {
        this(null, syntaxes);
    }

    @Override
    public String toString() {
        return "PowersetSyntax{" + super.toString()  + "}";
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        Syntax[] children = getChildSyntaxes();
        MuSyntax[] childMuSyntaxes = new MuSyntax[children.length];
        for (int i = 0; i < children.length; i++) {
            childMuSyntaxes[i] = children[i].prepare(bundle);
        }
        String label = this.label == null ? MuSyntax.genLabel() : this.label;
        MuSyntax res = new MuAlternation(label, null, 
                new MuSequence(new MuAlternation((String) null, childMuSyntaxes),
                        new MuBackReference(label)));
        res.resolveBackReferences();
        return res;
    }

    @Override
    public String format(ArgumentBundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (Syntax childSyntax : getChildSyntaxes()) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            else {
                sb.append("[ ");
            }
            String formatted = childSyntax.format(bundle);
            if (needsBracketting(formatted)) {
                sb.append("( ").append(formatted).append(" )");
            }
            else {
                sb.append(formatted);
            }
        }
        if (sb.length() > 0) {
            sb.append(" ] ...");
        }
        return sb.toString();
    }
}