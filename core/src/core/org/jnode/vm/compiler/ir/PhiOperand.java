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
 
package org.jnode.vm.compiler.ir;

import java.util.List;

import org.jnode.util.BootableArrayList;

/**
 * @author Madhu Siddalingaiah
 */
public class PhiOperand<T> extends Operand<T> {
    private final List<Operand<T>> sources;
    private int varIndex;

    public PhiOperand() {
        this(UNKNOWN);
    }

    /**
     * @param type
     */
    public PhiOperand(int type) {
        super(type);
        sources = new BootableArrayList<Operand<T>>();
    }

    public void addSource(Variable<T> source) {
        sources.add(source);
        int type = getType();
        if (type == UNKNOWN) {
            setType(source.getType());
            Variable<T> v = source;
            varIndex = v.getIndex();
        } else if (type != source.getType()) {
            throw new AssertionError("phi operand source types don't match");
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("phi(");
        int n = sources.size();
        for (int i = 0; i < n; i += 1) {
            sb.append(sources.get(i).toString());
            if (i < n - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * @return the sources
     */
    public List<Operand<T>> getSources() {
        return sources;
    }

    public int getIndex() {
        return varIndex;
    }

    /**
     * @see org.jnode.vm.compiler.ir.Operand#getAddressingMode()
     */
    public AddressingMode getAddressingMode() {
        Variable<T> first = (Variable<T>) sources.get(0);
        return first.getAddressingMode();
    }

    /**
     * @see org.jnode.vm.compiler.ir.Operand#simplify()
     */
    public Operand<T> simplify() {
        int n = sources.size();
        for (int i = 0; i < n; i += 1) {
            Variable<T> src = (Variable<T>) sources.get(i);
            Operand<T> op = src.simplify();
            if (op instanceof StackVariable || op instanceof LocalVariable) {
                sources.set(i, op);
            } else {
                src.getAssignQuad().setDeadCode(false);
            }
        }
        return this;
    }
}
