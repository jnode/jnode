/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.jnasm.assembler;

import java.util.List;
import java.util.Map;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public abstract class AssemblerModule {
    
    protected final Map<String, Label> labels;

    protected final Map<String, Integer> constants;

    protected AssemblerModule(Map<String, Label> labels,
            Map<String, Integer> constants) {
        this.labels = labels;
        this.constants = constants;
    }

    public abstract boolean emit(String mnemonic, List<Object> operands,
            int operandSize);

    public abstract void setNativeStream(NativeStream stream);
}
