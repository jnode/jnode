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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jnode.util.BootableArrayList;

/**
 * @author Madhu Siddalingaiah
 */
public class LinearScanAllocator<T> {
    private LiveRange<T>[] liveRanges;
    private List<LiveRange<T>> active;
    private RegisterPool<T> registerPool;
    private EndPointComparator<T> endPointComparator;
    private List<Variable<T>> spilledVariableList;
    private Variable<T>[] spilledVariables;

    public LinearScanAllocator(LiveRange<T>[] liveRanges) {
        this.liveRanges = liveRanges;
        final CodeGenerator<T> cg = CodeGenerator.getInstance();
        this.registerPool = cg.getRegisterPool();
        this.active = new BootableArrayList<LiveRange<T>>();
        this.endPointComparator = new EndPointComparator<T>();
        this.spilledVariableList = new BootableArrayList<Variable<T>>();
    }

    public void allocate() {
        int n = liveRanges.length;
        for (int i = 0; i < n; i += 1) {
            LiveRange<T> lr = liveRanges[i];
            Variable<T> var = lr.getVariable();
            if (!(var instanceof MethodArgument)) {
                // don't allocate method arguments to registers
                expireOldRange(lr);
                T reg = registerPool.request(var.getType());
                if (reg == null) {
                    spillRange(lr);
                } else {
                    lr.setLocation(new RegisterLocation<T>(reg));
                    active.add(lr);
                    Collections.sort(active, endPointComparator);
                }
            }
        }
        // This sort is probably not necessary...
        Collections.sort(spilledVariableList, new StorageSizeComparator<T>());
        n = spilledVariableList.size();
        spilledVariables = new Variable[n];
        for (int i = 0; i < n; i += 1) {
            spilledVariables[i] = spilledVariableList.get(i);
        }
    }

    public Variable<T>[] getSpilledVariables() {
        return spilledVariables;
    }

    /**
     * @param lr
     */
    private void expireOldRange(LiveRange<T> lr) {
        for (LiveRange<T> l : new ArrayList<LiveRange>(active)) {
            if (l.getLastUseAddress() >= lr.getAssignAddress()) {
                return;
            }
            active.remove(l);
            RegisterLocation<T> regLoc = (RegisterLocation<T>) l.getLocation();
            registerPool.release(regLoc.getRegister());
        }
    }

    /**
     * @param lr
     */
    private void spillRange(LiveRange<T> lr) {
        LiveRange<T> spill = active.get(active.size() - 1);
        if (spill.getLastUseAddress() > lr.getLastUseAddress()) {
            lr.setLocation(spill.getLocation());
            spill.setLocation(new StackLocation<T>());
            this.spilledVariableList.add(spill.getVariable());
            active.remove(spill);
            active.add(lr);
            Collections.sort(active);
        } else {
            lr.setLocation(new StackLocation<T>());
            this.spilledVariableList.add(lr.getVariable());
        }
    }
}

class EndPointComparator<T> implements Comparator<LiveRange<T>> {
    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(LiveRange<T> lr1, LiveRange<T> lr2) {
        return lr1.getLastUseAddress() - lr2.getLastUseAddress();
    }
}

class StorageSizeComparator<T> implements Comparator<Variable<T>> {
    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Variable<T> lr1, Variable<T> lr2) {
        int size1 = 0;
        int size2 = 0;
        // These are defined in the order on the stack
        switch (lr1.getType()) {
            case Operand.BYTE:
                size1 = 1;
                break;
            case Operand.SHORT:
                size1 = 2;
                break;
            case Operand.CHAR:
                size1 = 3;
                break;
            case Operand.INT:
                size1 = 4;
                break;
            case Operand.FLOAT:
                size1 = 5;
                break;
                // this could be 32 or 64 bits, in between FLOAT and LONG is best
            case Operand.REFERENCE:
                size1 = 6;
                break;
            case Operand.LONG:
                size1 = 7;
                break;
            case Operand.DOUBLE:
                size1 = 8;
                break;
        }
        switch (lr2.getType()) {
            case Operand.BYTE:
                size2 = 1;
                break;
            case Operand.SHORT:
                size2 = 2;
                break;
            case Operand.CHAR:
                size2 = 3;
                break;
            case Operand.INT:
                size2 = 4;
                break;
            case Operand.FLOAT:
                size2 = 5;
                break;
            case Operand.REFERENCE:
                size2 = 6;
                break;
            case Operand.LONG:
                size2 = 7;
                break;
            case Operand.DOUBLE:
                size2 = 8;
                break;
        }
        return size1 - size2;
    }
}
