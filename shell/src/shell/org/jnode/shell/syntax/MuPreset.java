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


/**
 * This class represents the micro-syntax for setting an argument without matching any tokens.
 * 
 * @author crawley@jnode.org
 */
public class MuPreset extends MuSyntax {
    
    private final String argName;
    private final String preset;
    
    public MuPreset(String argName, String preset) {
        this (null, argName, preset);
    }
    
    public MuPreset(String label, String argName, String preset) {
        super(label);
        if (argName.length() == 0) {
            throw new IllegalArgumentException("empty argName");
        }
        this.argName = argName;
        this.preset = preset;
    }

    @Override
    String format(FormatState state) {
        return "<<" + argName + "=" + preset + ">>";
    }

    @Override
    public int getKind() {
        return PRESET;
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

    public String getPreset() {
        return preset;
    }
}
