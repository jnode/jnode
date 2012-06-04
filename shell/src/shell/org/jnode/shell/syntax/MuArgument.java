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


/**
 * This class represents the micro-syntax for an instance of an Argument.
 * 
 * @author crawley@jnode.org
 */
public class MuArgument extends MuSyntax {
    
    private final String argName;
    private final int flags;
    
    public MuArgument(String argName) {
        this (null, argName, 0);
    }
    
    public MuArgument(String label, String argName, int flags) {
        super(label);
        if (argName.length() == 0) {
            throw new IllegalArgumentException("empty argName");
        }
        this.argName = argName;
        this.flags = flags;
    }

    @Override
    String format(FormatState state) {
        return "<<" + argName + ">>";
    }

    @Override
    public MuSyntaxKind getKind() {
        return MuSyntaxKind.ARGUMENT;
    }

    public String getArgName() {
        return argName;
    }

    @Override
    MuSyntax resolveBackReferences(ResolveState state) throws SyntaxFailureException {
        if (label != null) {
            state.refMap.put(label, this);
        }
        return this;
    }

    public int getFlags() {
        return flags;
    }
}
