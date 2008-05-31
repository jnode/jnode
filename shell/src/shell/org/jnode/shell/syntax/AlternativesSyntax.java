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
 * An AlternativesSyntax specifies that one of its child syntaxes needs to be 
 * satisfied.  The alternatives will be tried one at a time in the order that 
 * they were supplied to the constructor.
 * 
 * @author crawley@jnode.org
 */
public class AlternativesSyntax extends GroupSyntax {
    
    public AlternativesSyntax(String label, String description, Syntax...syntaxes) {
        super(label, description, syntaxes);
    }

    public AlternativesSyntax(String label, Syntax...syntaxes) {
        super(label, null, syntaxes);
    }

    public AlternativesSyntax(Syntax...syntaxes) {
        super(null, null, syntaxes);
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        Syntax[] childSyntaxes = getChildSyntaxes();
        MuSyntax[] muSyntaxes = new MuSyntax[childSyntaxes.length];
        for (int i = 0; i < childSyntaxes.length; i++) {
            muSyntaxes[i] = childSyntaxes[i].prepare(bundle);
        }
        return new MuAlternation(muSyntaxes);
    }

    @Override
    public String format(ArgumentBundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (Syntax childSyntax : getChildSyntaxes()) {
            String formatted = childSyntax.format(bundle);
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            if (needsBracketting(formatted)) {
                sb.append("( ").append(formatted).append(" )");
            }
            else {
                sb.append(formatted);
            }
        }
        return sb.toString();
    }

    @Override
    public XMLElement toXML() {
        return basicElement("alternatives");
    }
    
}
