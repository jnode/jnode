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
 
package org.jnode.vm.compiler.ir;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jnode.util.ObjectArrayIterator;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmInterpretedExceptionHandler;
import org.jnode.vm.compiler.ir.quad.AssignQuad;
import org.jnode.vm.compiler.ir.quad.CallAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewMultiArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewObjectArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewPrimitiveArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.PhiAssignQuad;
import org.jnode.vm.compiler.ir.quad.Quad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;
import org.jnode.vm.objects.BootableArrayList;

/**
 * @author Madhu Siddalingaiah
 */
//TODO simpify to use existing CFG from l1

public class IRControlFlowGraph<T> implements Iterable<IRBasicBlock<T>> {

    private SSAStack<T>[] renumberArray;
    private final IRBasicBlock<T>[] bblocks;
    private List<IRBasicBlock<T>> postOrderList;
    private IRBasicBlock<T> startBlock;

    /**
     * Create a new instance
     *
     * @param bytecode
     */
    public IRControlFlowGraph(VmByteCode bytecode) {
        // First determine the basic blocks
        final IRBasicBlockFinder<T> bbf = new IRBasicBlockFinder<T>();
        BytecodeParser.parse(bytecode, bbf);
        this.bblocks = bbf.createBasicBlocks();
        startBlock = bblocks[0];
        computeDominance(bytecode);
    }

    //todo use set
    public List<Variable<?>> computeLiveVariables() {
        List<Variable<?>> liveVariables = new BootableArrayList<Variable<?>>();
        for (IRBasicBlock<T> b : this) {
//            System.out.println();
//            System.out.println(b + ", stackOffset = " + b.getStackOffset());
            for (Quad<T> q : b.getQuads()) {
                if (!q.isDeadCode()) {
                    q.computeLiveness(liveVariables);
//                    System.out.println(q);
                }
            }
        }
        return liveVariables;
    }

    public void removeUnusedVars() {
        Map<Variable, Integer> varUses = getVariableUsage();
        boolean loop;
        do {
            loop = false;
            for (Map.Entry<Variable, Integer> u : varUses.entrySet()) {
                if (u.getValue() == 0 && !u.getKey().getAssignQuad().isDeadCode()) {
                    AssignQuad dq = u.getKey().getAssignQuad();
                    if (dq instanceof CallAssignQuad ||
                        dq instanceof NewAssignQuad ||
                        dq instanceof NewObjectArrayAssignQuad ||
                        dq instanceof NewPrimitiveArrayAssignQuad ||
                        dq instanceof NewMultiArrayAssignQuad) {
                        //todo optimize it, could be transformed to CallQuad
                        continue;
                    }
                    dq.setDeadCode(true);
                    Operand<T>[] refs = dq.getReferencedOps();
                    if (refs != null) {
                        for (Operand<T> ref : refs) {
                            if (ref instanceof Variable) {
                                Variable<T> r = (Variable<T>) ref;
                                Integer c = varUses.get(r);
                                if (c > 0) {
                                    c--;
                                }
                                varUses.put(r, c);
                            }
                        }
                    }
                    loop = true;
                    break;
                }
            }
        } while (loop);
    }

    public void removeDefUseChains() {
        Map<Variable, Integer> varUses = getVariableUsage();

        for (Map.Entry<Variable, Integer> u : varUses.entrySet()) {
            Variable var = u.getKey();
            if (u.getValue() == 1 && !(var instanceof MethodArgument) && !var.getAssignQuad().isDeadCode()) {
                for (IRBasicBlock<T> b : this) {
                    for (Quad<T> q : b.getQuads()) {
                        if (!q.isDeadCode()) {
                            if (q instanceof VariableRefAssignQuad) {
                                VariableRefAssignQuad vq = (VariableRefAssignQuad) q;
                                if (vq.getRHS().equals(var) &&
                                    vq.getBasicBlock().equals(var.getAssignQuad().getBasicBlock())) {
                                    vq.setDeadCode(true);
                                    var.getAssignQuad().setLHS(vq.getLHS());

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<Variable, Integer> getVariableUsage() {
        Map<Variable, Integer> varUses = new HashMap<Variable, Integer>();
        for (IRBasicBlock<T> b : this) {
            for (Quad<T> q : b.getQuads()) {
                if (!q.isDeadCode()) {
                    if (q instanceof AssignQuad) {
                        AssignQuad aq = (AssignQuad) q;
                        Variable v = aq.getLHS();
                        if (!varUses.containsKey(v)) {
                            varUses.put(v, 0);
                        }
                    }
                    Operand<T>[] refs = q.getReferencedOps();
                    if (refs != null) {
                        for (Operand<T> ref : refs) {
                            if (ref instanceof Variable) {
                                Variable<T> v = (Variable<T>) ref;
                                Integer c = varUses.get(v);
                                if (c == null) {
                                    c = 0;
                                }
                                c++;
                                varUses.put(v, c);
                            }
                        }
                    }
                }
            }
        }
        return varUses;
    }

    /**
     * Create an iterator to iterate over all basic blocks.
     *
     * @return An iterator that will return instances of IRBasicBlock.
     */
    public Iterator<IRBasicBlock<T>> iterator() {
        return new ObjectArrayIterator<IRBasicBlock<T>>(bblocks);
    }

    /**
     * Gets the number of basic blocks in this graph
     *
     * @return count of basic blocks
     */
    public int getBasicBlockCount() {
        return bblocks.length;
    }

    /**
     * Gets the basic block that contains the given address.
     *
     * @param pc
     * @return the basic block or {@code null}.
     */
    public IRBasicBlock getBasicBlock(int pc) {
        final int max = bblocks.length;
        for (int i = 0; i < max; i++) {
            final IRBasicBlock bb = bblocks[i];
            if (bb.contains(pc)) {
                return bb;
            }
        }
        return null;
    }

    public void computeDominance(VmByteCode bytecode) {
        postOrderList = new BootableArrayList<IRBasicBlock<T>>();
        startBlock.computePostOrder(postOrderList);
        doComputeDominance(bytecode);
        computeDominanceFrontier();
        computeDominatedBlocks();
    }

    /*
         for all nodes, b // initialize the dominators array
             doms[b] = Undefined
         doms[start_node] = start_node
         Changed = true
         while (Changed)
             Changed = false
             for all nodes, b, in reverse postorder (except start node)
                 new_idom = first (processed) predecessor of b // (pick one)
                 for all other predecessors, p, of b
                     if doms[p] != Undefined // i.e., if doms[p] already calculated
                         new_idom = intersect(p, new_idom)
             if doms[b] != new_idom
                 doms[b] = new_idom
                 Changed = true
     */
    private void doComputeDominance(VmByteCode bytecode) {
        // This is critical, must be done in reverse postorder
        startBlock.setIDominator(startBlock);
        boolean changed = true;
        while (changed) {
            changed = false;
            int i = postOrderList.size() - 1; // skip startBlock
            while (i >= 0) {
                IRBasicBlock<T> b = postOrderList.get(i--);
                if (b == startBlock) {
                    continue;
                }
                Iterator<IRBasicBlock<T>> ip = b.getPredecessors().iterator();
                if (!ip.hasNext()) {
                    throw new AssertionError(b + " has no predecessors!");
                }
                IRBasicBlock<T> newIdom = ip.next();
                while (newIdom.getIDominator() == null && ip.hasNext()) {
                    newIdom = ip.next();
                }
                if (newIdom.getIDominator() == null) {
                    throw new AssertionError(newIdom + " has no dominator!");
                }
                while (ip.hasNext()) {
                    IRBasicBlock<T> p = ip.next();
                    if (p.getIDominator() != null) {
                        newIdom = intersect(p, newIdom);
                    }
                }
                if (b.getIDominator() != newIdom) {
                    b.setIDominator(newIdom);
                    changed = true;
                }
            }
        }
        startBlock.setIDominator(null);
        for (VmInterpretedExceptionHandler eh : bytecode.getExceptionHandlers()) {
            IRBasicBlock block = getBasicBlock(eh.getHandlerPC());
            if (block != null && block.getIDominator() == null) {
                IRBasicBlock pBlock = getBasicBlock(eh.getStartPC());
                if (pBlock != null) {
                    block.setIDominator(pBlock.getIDominator());
                }
            }

        }
    }

    /**
     * @param p
     * @param newIdom
     * @return
     */
    /*
         function intersect(b1, b2) returns node
             finger1 = b1
             finger2 = b2
             while (finger1 != finger2)
                 while (finger1 < finger2)
                     finger1 = doms[finger1]
                 while (finger2 < finger1)
                     finger2 = doms[finger2]
             return finger1
     */
    private IRBasicBlock<T> intersect(IRBasicBlock<T> b1, IRBasicBlock<T> b2) {
        while (b1 != b2) {
            while (b1.getPostOrderNumber() < b2.getPostOrderNumber()) {
                b1 = b1.getIDominator();
            }
            while (b2.getPostOrderNumber() < b1.getPostOrderNumber()) {
                b2 = b2.getIDominator();
            }
        }
        return b1;
    }

    /*
         for all nodes, b
             if the number of predecessors of b >= 2
                 for all predecessors, p, of b
                     runner = p
                     while runner != doms[b]
                         add b to runner\u2019s dominance frontier set
                         runner = doms[runner]
     */
    private void computeDominanceFrontier() {
        for (IRBasicBlock<T> b : postOrderList) {
            List<IRBasicBlock<T>> predList = b.getPredecessors();
            if (predList.size() >= 2) {
                for (IRBasicBlock<T> runner : predList) {
                    while (runner != b.getIDominator()) {
                        runner.addDominanceFrontier(b);
                        runner = runner.getIDominator();
                    }
                }
            }
        }
    }

    /**
     *
     */
    private void computeDominatedBlocks() {
        for (IRBasicBlock<T> b : postOrderList) {
            IRBasicBlock<T> idom = b.getIDominator();
            if (idom != null) {
                idom.addDominatedBlock(b);
                idom = idom.getIDominator();
            }
        }
    }

    /**
     *
     */
    public void constructSSA() {
        Variable<T>[] vars = startBlock.getVariables();
        int nvars = vars.length;
        renumberArray = new SSAStack[nvars];
        // Push method arguments on the stack since they are not assigned
        for (int i = 0; i < nvars; i += 1) {
            Variable<T> vi = vars[i];
            SSAStack<T> st = getStack(vi);
            if (vi instanceof MethodArgument) {
                st.getNewVariable();
            }
        }
        placePhiFunctions();
        renameVariables(startBlock);
    }

    /**
     *
     */
    public void optimize() {
        for (IRBasicBlock<T> b : bblocks) {
            for (Quad<T> q : b.getQuads()) {
                q.doPass2();
            }
        }
    }

    public void optimize(Collection<Variable<T>> values) {
        for (IRBasicBlock<T> b : bblocks) {
            for (Quad<T> q : b.getQuads()) {
                if (!q.isDeadCode()) {
                    q.doPass3(values);
                }
            }
        }
    }

    public void deconstrucSSA() {
        final List<PhiAssignQuad<T>> phiQuads = new BootableArrayList<PhiAssignQuad<T>>();
        for (IRBasicBlock<T> b : bblocks) {
            for (Quad<T> q : b.getQuads()) {
                if (q instanceof PhiAssignQuad && !q.isDeadCode()) {
                    phiQuads.add((PhiAssignQuad<T>) q);
                } else {
                    break;
                }
            }
        }

        Collections.sort(phiQuads, new Comparator<PhiAssignQuad<T>>() {
            @Override
            public int compare(PhiAssignQuad<T> o1, PhiAssignQuad<T> o2) {
                int i = o2.getBasicBlock().getEndPC() - o1.getBasicBlock().getEndPC();
                if (i == 0) {
                    i = o1.getLHS().getIndex() - o2.getLHS().getIndex();
                }
                return i;
            }
        });
        for (PhiAssignQuad<T> paq : phiQuads) {
            Variable<T> lhs = paq.getLHS();
            IRBasicBlock<T> firstBlock = null;
            AssignQuad<T> firstPhiMove = null;
            for (Operand<T> o : paq.getPhiOperand().getSources()) {
                Variable<T> rhs = (Variable<T>) o;
                AssignQuad<T> assignQuad = rhs.getAssignQuad();
                IRBasicBlock<T> ab;
                if (assignQuad == null && rhs instanceof MethodArgument) {
                    ab = startBlock;
                } else {
                    ab = assignQuad.getBasicBlock();
                }
                AssignQuad<T> phiMove;
                phiMove = new VariableRefAssignQuad<T>(0, ab, lhs, rhs);
                phiMove.doPass2();
                ab.add(phiMove);
                if (firstBlock == null || ab.getStartPC() < firstBlock.getStartPC()) {
                    firstBlock = ab;
                    firstPhiMove = phiMove;
                }
            }
            lhs.setAssignQuad(firstPhiMove);
            paq.setDeadCode(true);
        }
    }

    public void deconstrucSSA(Collection<Variable<T>> liveVariables) {
        final List<PhiAssignQuad<T>> phiQuads = new BootableArrayList<PhiAssignQuad<T>>();
        for (IRBasicBlock<T> b : bblocks) {
            for (Quad<T> q : b.getQuads()) {
                if (q instanceof PhiAssignQuad) {
                    PhiAssignQuad<T> q1 = (PhiAssignQuad<T>) q;
                    if (liveVariables.contains(q1.getLHS())) {
                        phiQuads.add(q1);
                    } else {
                        q1.setDeadCode(true);
                    }
                }
//                else {
//                    break;
//                }
            }
        }
        for (PhiAssignQuad<T> paq : phiQuads) {
            Variable<T> lhs = paq.getLHS();
            IRBasicBlock<T> firstBlock = null;
            VariableRefAssignQuad<T> firstPhiMove = null;
            for (Operand<T> o : paq.getPhiOperand().getSources()) {
                Variable<T> rhs = (Variable<T>) o;
                IRBasicBlock<T> ab = rhs.getAssignQuad().getBasicBlock();
                VariableRefAssignQuad<T> phiMove;
                phiMove = new VariableRefAssignQuad<T>(0, ab, lhs, rhs);
                ab.add(phiMove);
//                fixupAddresses();  //todo possible optimisation to remove assignment chains
                phiMove.doPass2();
                if (firstBlock == null || ab.getStartPC() < firstBlock.getStartPC()) {
                    firstBlock = ab;
                    firstPhiMove = phiMove;
                }
            }
            lhs.setAssignQuad(firstPhiMove);
            paq.setDeadCode(true);
        }
    }

    public void fixupAddresses() {
        int address = 0;
        for (IRBasicBlock<T> b : bblocks) {
            b.setStartPC(address);
            for (Quad<T> q : b.getQuads()) {
                q.setAddress(address);
                if (!q.isDeadCode()) {
                    address += 1;
                }
            }
            b.setEndPC(address);
        }
    }

    private void placePhiFunctions() {
        for (IRBasicBlock<T> b : bblocks) {
            for (Operand<T> def : b.getDefList()) {
                for (IRBasicBlock<T> dfb : b.getDominanceFrontier()) {
                    dfb.add(new PhiAssignQuad<T>(dfb, ((Variable<T>) def).getIndex()));
                }
            }
        }
    }

    /**
     * @param block
     */
    private void renameVariables(IRBasicBlock<T> block) {
        doRenameVariables(block);
        for (IRBasicBlock<T> b : block.getSuccessors()) {
            rewritePhiParams(b);
        }

        for (IRBasicBlock<T> b : block.getDominatedBlocks()) {
            if (b != block) {
                renameVariables(b);
            }
        }
        popVariables(block);
    }

    /**
     * @param block
     */
    private void doRenameVariables(IRBasicBlock<T> block) {
        for (Quad<T> q : block.getQuads()) {
            Operand<T>[] refs = q.getReferencedOps();
            if (refs != null) {
                int n = refs.length;
                for (int i = 0; i < n; i += 1) {
                    SSAStack<T> st = getStack(refs[i]);
                    if (st != null) {
                        Variable[] vars = block.getVariables();
                        Variable<T> peek = st.peek();
                        vars[((Variable) refs[i]).getIndex()] = peek;
                        refs[i] = peek;
                    }
                }
            }
            if (q instanceof AssignQuad) {
                AssignQuad<T> aq = (AssignQuad<T>) q;
                SSAStack<T> st = getStack(aq.getLHS());
                Variable var = aq.getLHS();
                Variable[] vars = block.getVariables();
                Variable<T> nvar = st.getNewVariable();
                vars[var.getIndex()] = nvar;
                aq.setLHS(nvar);
            }
        }
    }

    /**
     * @param block
     */
    private void rewritePhiParams(IRBasicBlock<T> block) {
        if (block == null) {
            return;
        }
        for (Quad<T> q : block.getQuads()) {
            if (q instanceof PhiAssignQuad) {
                PhiAssignQuad<T> aq = (PhiAssignQuad<T>) q;
                if (!aq.isDeadCode()) {
                    SSAStack<T> st = getStack(aq.getLHS());
                    Variable<T> var = st.peek();
                    // If there was no incoming branch to this phi, I think it's dead...
                    if (var != null) {
                        PhiOperand<T> phi = aq.getPhiOperand();
                        phi.addSource(var);
                    } else {
                        aq.setDeadCode(true);
                    }
                }
            }
        }
    }

    /**
     * @param block
     */
    private void popVariables(IRBasicBlock<T> block) {
        for (Quad<T> q : block.getQuads()) {
            if (q instanceof AssignQuad) {
                AssignQuad<T> aq = (AssignQuad<T>) q;
                SSAStack<T> st = getStack(aq.getLHS());
                st.pop();
            }
        }
    }

    /**
     * @param operand
     * @return
     */
    private SSAStack<T> getStack(Operand<T> operand) {
        if (operand instanceof Variable) {
            return getStack((Variable<T>) operand);
        }
        return null;
    }

    private SSAStack<T> getStack(Variable<T> var) {
        int index = var.getIndex();
        SSAStack<T> st = renumberArray[index];
        if (st == null) {
            st = new SSAStack<T>(var);
            renumberArray[index] = st;
        }
        return st;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IRBasicBlock<T> bb : this) {
            sb.append(bb.toString());
            sb.append(":\n  predecessors:");
            final List<IRBasicBlock<T>> pred = bb.getPredecessors();
            for (IRBasicBlock<T> aPred : pred) {
                sb.append("\n    ");
                sb.append(aPred.toString());
            }
            sb.append("\n  successors:");
            for (IRBasicBlock<T> succ : bb.getSuccessors()) {
                sb.append("\n    ");
                sb.append(succ);
            }
            sb.append("\n  idom: ");
            sb.append(bb.getIDominator());
            sb.append("\n  DF:");
            for (IRBasicBlock<T> dfb : bb.getDominanceFrontier()) {
                sb.append(' ');
                sb.append(dfb);
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }
}
