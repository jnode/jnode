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
 * A SequenceSyntax specifies that a group of child syntaxes need to be 
 * satisfied in strict sequence. 
 * 
 * @author crawley@jnode
 */
public class SequenceSyntax extends GroupSyntax {
    
    public SequenceSyntax(String label, String description, Syntax...syntaxes) {
        super(label, description, syntaxes);
    }

    public SequenceSyntax(String label, Syntax...syntaxes) {
        this(label, null, syntaxes);
    }

    public SequenceSyntax(Syntax...syntaxes) {
        this(null, null, syntaxes);
    }

    public String format(ArgumentBundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (Syntax child : getChildren()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(child.format(bundle));
        }
        return sb.toString();
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        Syntax[] childSyntaxes = getChildren();
        MuSyntax[] muSyntaxes = new MuSyntax[childSyntaxes.length];
        for (int i = 0; i < childSyntaxes.length; i++) {
            muSyntaxes[i] = childSyntaxes[i].prepare(bundle);
        }
        return new MuSequence(label, muSyntaxes);
    }

    @Override
    public XMLElement toXML() {
        return basicElement("sequence");
    }
}
