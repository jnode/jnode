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
 * A MuBackReference is a placeholder for another MuSyntax node.  Its intended use is for
 * representing cycles in a syntax graph; e.g. in " &lt;a-seq&gt; ::= 'a' | 'a' &lt;a-seq&gt; ",
 * the &lt;a-seq&gt; on the RHS would initially be represented by a MuBackReference.
 * <p>
 * Back references must be resolved by calling resolveBackReferences() on the syntax tree root
 * before the parse(...) method is called.
 * 
 * @author crawley@jnode.org
 */
public class MuBackReference extends MuSyntax {
    
    public MuBackReference(String label) {
        super(label);
        if (label.length() == 0) {
            throw new IllegalArgumentException("empty label");
        }
    }

    @Override
    String format(FormatState state) {
        return "<" + label + ">";
    }

    @Override
    public int getKind() {
        return BACK_REFERENCE;
    }

    @Override
    MuSyntax resolveBackReferences(ResolveState state)
            throws SyntaxFailureException {
        MuSyntax target = state.refMap.get(label);
        if (target == null) {
            throw new SyntaxFailureException("Cannot resolve '" + label + "'");
        }
        return target;
    }
}
