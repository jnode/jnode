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
public class LookupswitchQuad<T> extends Quad<T> {
    private IRBasicBlock<T>[] targetBlocks;
    private Operand<T> refs[];
    private int defaultAddress;
    private int[] matchValues;

    /**
     * @param address
     * @param block
     * @param defAddress
     * @param matchValues
     * @param targetAddresses
     * @param key
     *
     */
    public LookupswitchQuad(int address, IRBasicBlock<T> block, int defAddress, int[] matchValues,
                            int[] targetAddresses,
                            int key) {

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
            if (succ.getStartPC() == defAddress) {
                targetBlocks[targetAddresses.length] = succ;
                break;
            }
        }
        if (targetBlocks[targetAddresses.length] == null) {
            throw new AssertionError("unable to find target block!");
        }

        this.defaultAddress = defAddress;
        this.matchValues = matchValues;
        refs = new Operand[]{getOperand(key)};
    }

    public int[] getMatchValues() {
        return matchValues;
    }

    public int getDefaultAddress() {
        return defaultAddress;
    }

    public Operand getKey() {
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
        return getAddress() + ": lookupswitch(" + refs[0] + ") , def=" + defaultAddress +
            ", matches=" + Arrays.toString(matchValues) + ", targets=" + Arrays.toString(targetBlocks);
    }
}
