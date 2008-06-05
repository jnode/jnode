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


public class MuSequence extends MuSyntax {
    
    private final MuSyntax[] elements;
    
    public MuSequence(MuSyntax ... elements) {
        this (null, elements);
    }
    
    public MuSequence(String label, MuSyntax ... elements) {
        super(label);
        for (MuSyntax element : elements) {
            @SuppressWarnings("unused")
            String dummy = element.label; // Check non-null.
        }
        this.elements = elements;
    }

    @Override
    public String format(FormatState state) {
        StringBuilder sb = new StringBuilder();
        for (MuSyntax syntax : elements) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(syntax.formatInline(state));
        }
        return sb.toString();
    }

    public MuSyntax[] getElements() {
        return elements;
    }

    @Override
    public int getKind() {
        return SEQUENCE;
    }

    @Override
    MuSyntax resolveBackReferences(ResolveState state) throws SyntaxFailureException {
        if (state.seen.add(this)) {
            if (label != null) {
                state.refMap.put(label, this);
            }
            for (int i = 0; i < elements.length; i++) {
                elements[i] = elements[i].resolveBackReferences(state);
            }
        }
        return this;
    }

}
