/*
 * $Id$
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
 
package org.jnode.vm.compiler.ir;

import java.util.List;

import org.jnode.util.BootableArrayList;

/**
 * @author Madhu Siddalingaiah
 */
public class SSAStack<T> {
    private final List<Variable<T>> stack;
    private int count;
    private Variable variable;

    /**
     *
     */
    public SSAStack(Variable<T> variable) {
        this.variable = variable;
        count = 0;
        stack = new BootableArrayList<Variable<T>>();
    }

    public Variable<T> peek() {
        int n = stack.size();
        // This deals with cases where there are excessive phis (unpruned SSA)
        if (n <= 0) {
            return null;
        }
        Variable<T> var = stack.get(n - 1);
        return var;
    }

    public Variable<T> getNewVariable() {
        count += 1;
        Variable<T> var = (Variable<T>) variable.clone();
        var.setSSAValue(count);
        stack.add(var);
        return var;
    }

    public Variable<T> pop() {
        Variable<T> var = stack.remove(stack.size() - 1);
        return var;
    }
}
