/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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

/**
 * This class represents the micro-syntax for a number of alternative elements.
 * 
 * @author crawley@jnode.org
 */
public class MuAlternation extends MuSyntax {
    
    private final MuSyntax[] alternatives;
    
    public MuAlternation(MuSyntax ... alternatives) {
        this(null, alternatives);
    }
        
    public MuAlternation(String label, MuSyntax ... alternatives) {
        super(label);
        if (alternatives.length < 1) {
            throw new IllegalArgumentException("too few alternatives");
        }
        this.alternatives = alternatives;
    }

    public String format(FormatState state) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("( ");
        for (MuSyntax syntax : alternatives) {
            if (first) {
                first = false;
            } else {
                sb.append(" | ");
            }
            if (syntax != null) {
                sb.append(syntax.formatInline(state));
            }
        }
        sb.append(" )");
        return sb.toString();
    }

    public MuSyntax[] getAlternatives() {
        return alternatives;
    }

    @Override
    public int getKind() {
        return ALTERNATION;
    }
    
    @Override
    MuSyntax resolveBackReferences(ResolveState state) throws SyntaxFailureException {
        if (state.seen.add(this)) {
            if (label != null) {
                state.refMap.put(label, this);
            }
            for (int i = 0; i < alternatives.length; i++) {
                if (alternatives[i] != null) {
                    alternatives[i] = alternatives[i].resolveBackReferences(state);
                }
            }
        }
        return this;
    }
}
