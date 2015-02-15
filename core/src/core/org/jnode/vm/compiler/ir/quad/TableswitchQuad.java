/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.vm.compiler.ir.quad;

import java.util.Arrays;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;

/**
 * @author Levente S\u00e1ntha
 */
public class TableswitchQuad<T> extends Quad<T> {
    private IRBasicBlock<T>[] targetBlocks;
    private Operand<T> refs[];
    private int defaultValue;
    private int lowValue;
    private int highValue;

    /**
     * @param address
     * @param block
     * @param defValue
     * @param lowValue
     * @param highValue
     * @param targetAddresses
     * @param indIndex
     */
    public TableswitchQuad(int address, IRBasicBlock<T> block, int defValue, int lowValue, int highValue,
                           int[] targetAddresses, int indIndex) {
        super(address, block);
        targetBlocks = new IRBasicBlock[targetAddresses.length + 1];
        for (int i = 0; i < targetAddresses.length; i++) {
            for (IRBasicBlock<T> succ : block.getSuccessors()) {
                if (succ.getStartPC() == targetAddresses[i]) {
                    targetBlocks[i] = succ;
                    break;
                }
            }
            if (targetBlocks[i] == null) {
                throw new AssertionError("unable to find target block!");
            }
        }

        for (IRBasicBlock<T> succ : block.getSuccessors()) {
            if (succ.getStartPC() == defValue) {
                targetBlocks[targetAddresses.length] = succ;
                break;
            }
        }
        if (targetBlocks[targetAddresses.length] == null) {
            throw new AssertionError("unable to find target block!");
        }

        this.defaultValue = defValue;
        this.lowValue = lowValue;
        this.highValue = highValue;
        refs = new Operand[]{getOperand(indIndex)};
    }

    public int getDefaultAddress() {
        return defaultValue;
    }

    public int getLowValue() {
        return lowValue;
    }

    public int getHighValue() {
        return highValue;
    }

    public Operand getValue() {
        return refs[0];
    }

    //    /**
//     * @return the start address of the target block
//     */
//    public int getTargetAddress() {
//        return targetBlock.getStartPC();
//    }

    /**
     * @return the target block
     */
    public IRBasicBlock<T>[] getTargetBlocks() {
        return targetBlocks;
    }

    @Override
    public Operand<T> getDefinedOp() {
        return null;
    }

    @Override
    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    @Override
    public void doPass2() {
        refs[0] = refs[0].simplify();
    }

    @Override
    public void generateCode(CodeGenerator<T> cg) {
        cg.generateCodeFor(this);
    }

    @Override
    public String toString() {
        return getAddress() + ": tableswitch(" + refs[0] + ") lo=" + lowValue + ", hi=" + highValue +
            ", def=" + defaultValue + " targets=" + Arrays.toString(targetBlocks);
    }
}
