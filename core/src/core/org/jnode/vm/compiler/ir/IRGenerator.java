/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir;

import java.util.Iterator;

import org.jnode.util.BootableArrayList;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.BytecodeVisitor;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.compiler.ir.quad.BinaryQuad;
import org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.Quad;
import org.jnode.vm.compiler.ir.quad.UnaryQuad;
import org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.VarReturnQuad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.VoidReturnQuad;

/**
 * Intermediate Representation Generator.
 * Visits bytecodes of a given method and translates them into a
 * list of Quads.
 */
public class IRGenerator extends BytecodeVisitor {
	private final static Constant NULL_CONSTANT = Constant.getInstance(null);
	private int nArgs;
	private int nLocals;
	private int maxStack;
	private int stackOffset;
	private Variable[] variables;
	private int address;
	private BootableArrayList quadList;
	private Iterator basicBlockIterator;
	private IRBasicBlock currentBlock;

	/**
	 *
	 */
	public IRGenerator(IRControlFlowGraph cfg) {
		basicBlockIterator = cfg.basicBlockIterator();
		currentBlock = (IRBasicBlock) basicBlockIterator.next();
	}

	public BootableArrayList getQuadList() {
		return quadList;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#setParser(org.jnode.vm.bytecode.BytecodeParser)
	 */
	public void setParser(BytecodeParser parser) {
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
	 */
	public void startMethod(VmMethod method) {
		VmByteCode code = method.getBytecode();
		nArgs = method.getArgSlotCount();
		nLocals = code.getNoLocals();
		maxStack = code.getMaxStack();
		stackOffset = nLocals;
		variables = new Variable[nLocals + maxStack];
		int index = 0;
		for (int i=0; i<nArgs; i+=1) {
			variables[index] = new MethodArgument(Operand.UNKNOWN, index);
			index += 1;
		}
		for (int i=nArgs; i<nLocals; i+=1) {
			variables[index] = new LocalVariable(Operand.UNKNOWN, index);
			index += 1;
		}
		for (int i=0; i<maxStack; i+=1) {
			variables[index] = new StackVariable(Operand.UNKNOWN, index);
			index += 1;
		}
		quadList = new BootableArrayList(code.getLength() >> 1);
		currentBlock.setVariables(variables);
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#endMethod()
	 */
	public void endMethod() {
		// patch last block
		currentBlock.resolvePhiReferences();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#startInstruction(int)
	 */
	public void startInstruction(int address) {
		this.address = address;
		if (address >= currentBlock.getEndPC()) {
			currentBlock.resolvePhiReferences();
			currentBlock = (IRBasicBlock) basicBlockIterator.next();
			Iterator pi = currentBlock.getPredecessors().iterator();
			if (!pi.hasNext()) {
				// this must be the first block in the method
				// We probably never get here, but just in case...
				stackOffset = nLocals;
				return;
			}
			stackOffset = currentBlock.getStackOffset();
			while (pi.hasNext()) {
				IRBasicBlock irb = (IRBasicBlock) pi.next();
				if (irb.getEndPC() <= address) {
					Variable[] prevVars = irb.getVariables();
					int n = prevVars.length;
					variables = new Variable[n];
					for (int i=0; i<n; i+=1) {
						variables[i] = prevVars[i];
					}
					currentBlock.setVariables(variables);
					return;
				}
			}
			throw new AssertionError("can't find a preceding basic block");
		}
		if (address < currentBlock.getStartPC() || address >= currentBlock.getEndPC()) {
			throw new AssertionError("instruction not in basic block!");
		}
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#endInstruction()
	 */
	public void endInstruction() {
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_nop()
	 */
	public void visit_nop() {
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aconst_null()
	 */
	public void visit_aconst_null() {
		quadList.add(new ConstantRefAssignQuad(address, currentBlock, stackOffset,
			NULL_CONSTANT));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iconst(int)
	 */
	public void visit_iconst(int value) {
		Quad quad = new ConstantRefAssignQuad(address, currentBlock, stackOffset,
					Constant.getInstance(value));
		quadList.add(quad);
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lconst(long)
	 */
	public void visit_lconst(long value) {
		quadList.add(new ConstantRefAssignQuad(address, currentBlock, stackOffset,
			Constant.getInstance(value)));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fconst(float)
	 */
	public void visit_fconst(float value) {
		quadList.add(new ConstantRefAssignQuad(address, currentBlock, stackOffset,
			Constant.getInstance(value)));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dconst(double)
	 */
	public void visit_dconst(double value) {
		quadList.add(new ConstantRefAssignQuad(address, currentBlock, stackOffset,
			Constant.getInstance(value)));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstString)
	 */
	public void visit_ldc(VmConstString value) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstClass)
     */
    public final void visit_ldc(VmConstClass value) {
        throw new Error("Not implemented yet");
    }

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iload(int)
	 */
	public void visit_iload(int index) {
		variables[index].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		VariableRefAssignQuad assignQuad = new VariableRefAssignQuad(address, currentBlock,
			stackOffset, index);
		quadList.add(assignQuad);
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lload(int)
	 */
	public void visit_lload(int index) {
		variables[index].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		quadList.add(new VariableRefAssignQuad(address, currentBlock,
			stackOffset, index));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fload(int)
	 */
	public void visit_fload(int index) {
		variables[index].setType(Operand.FLOAT);
		variables[stackOffset].setType(Operand.FLOAT);
		quadList.add(new VariableRefAssignQuad(address, currentBlock,
			stackOffset, index));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dload(int)
	 */
	public void visit_dload(int index) {
		variables[index].setType(Operand.DOUBLE);
		variables[stackOffset].setType(Operand.DOUBLE);
		quadList.add(new VariableRefAssignQuad(address, currentBlock,
			stackOffset, index));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aload(int)
	 */
	public void visit_aload(int index) {
		variables[index].setType(Operand.REFERENCE);
		variables[stackOffset].setType(Operand.REFERENCE);
		quadList.add(new VariableRefAssignQuad(address, currentBlock, stackOffset, index));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iaload()
	 */
	public void visit_iaload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_laload()
	 */
	public void visit_laload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_faload()
	 */
	public void visit_faload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_daload()
	 */
	public void visit_daload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aaload()
	 */
	public void visit_aaload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_baload()
	 */
	public void visit_baload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_caload()
	 */
	public void visit_caload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_saload()
	 */
	public void visit_saload() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_istore(int)
	 */
	public void visit_istore(int index) {
		stackOffset -= 1;
		variables[index].setType(Operand.INT);
		variables[stackOffset].setType(Operand.INT);
		VariableRefAssignQuad assignQuad = new VariableRefAssignQuad(address, currentBlock, index, stackOffset);
		quadList.add(assignQuad);
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lstore(int)
	 */
	public void visit_lstore(int index) {
		stackOffset -= 2;
		variables[index].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.LONG);
		quadList.add(new VariableRefAssignQuad(address, currentBlock, index, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fstore(int)
	 */
	public void visit_fstore(int index) {
		stackOffset -= 1;
		variables[index].setType(Operand.FLOAT);
		variables[stackOffset].setType(Operand.FLOAT);
		quadList.add(new VariableRefAssignQuad(address, currentBlock, index, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dstore(int)
	 */
	public void visit_dstore(int index) {
		stackOffset -= 2;
		variables[index].setType(Operand.DOUBLE);
		variables[stackOffset].setType(Operand.DOUBLE);
		quadList.add(new VariableRefAssignQuad(address, currentBlock, index, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_astore(int)
	 */
	public void visit_astore(int index) {
		stackOffset -= 1;
		variables[index].setType(Operand.REFERENCE);
		variables[stackOffset].setType(Operand.REFERENCE);
		quadList.add(new VariableRefAssignQuad(address, currentBlock, index, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iastore()
	 */
	public void visit_iastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lastore()
	 */
	public void visit_lastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fastore()
	 */
	public void visit_fastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dastore()
	 */
	public void visit_dastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aastore()
	 */
	public void visit_aastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_bastore()
	 */
	public void visit_bastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_castore()
	 */
	public void visit_castore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_sastore()
	 */
	public void visit_sastore() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop()
	 */
	public void visit_pop() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop2()
	 */
	public void visit_pop2() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup()
	 */
	public void visit_dup() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x1()
	 */
	public void visit_dup_x1() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x2()
	 */
	public void visit_dup_x2() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2()
	 */
	public void visit_dup2() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x1()
	 */
	public void visit_dup2_x1() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x2()
	 */
	public void visit_dup2_x2() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_swap()
	 */
	public void visit_swap() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iadd()
	 */
	public void visit_iadd() {
		quadList.add(doBinaryQuad(BinaryQuad.IADD, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ladd()
	 */
	public void visit_ladd() {
		quadList.add(doBinaryQuad(BinaryQuad.LADD, Operand.LONG));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fadd()
	 */
	public void visit_fadd() {
		quadList.add(doBinaryQuad(BinaryQuad.FADD, Operand.FLOAT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dadd()
	 */
	public void visit_dadd() {
		quadList.add(doBinaryQuad(BinaryQuad.DADD, Operand.DOUBLE));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_isub()
	 */
	public void visit_isub() {
		quadList.add(doBinaryQuad(BinaryQuad.ISUB, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lsub()
	 */
	public void visit_lsub() {
		quadList.add(doBinaryQuad(BinaryQuad.LSUB, Operand.LONG));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fsub()
	 */
	public void visit_fsub() {
		quadList.add(doBinaryQuad(BinaryQuad.FSUB, Operand.FLOAT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dsub()
	 */
	public void visit_dsub() {
		quadList.add(doBinaryQuad(BinaryQuad.DSUB, Operand.DOUBLE));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_imul()
	 */
	public void visit_imul() {
		quadList.add(doBinaryQuad(BinaryQuad.IMUL, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lmul()
	 */
	public void visit_lmul() {
		quadList.add(doBinaryQuad(BinaryQuad.LMUL, Operand.LONG));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fmul()
	 */
	public void visit_fmul() {
		quadList.add(doBinaryQuad(BinaryQuad.FMUL, Operand.FLOAT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dmul()
	 */
	public void visit_dmul() {
		quadList.add(doBinaryQuad(BinaryQuad.DMUL, Operand.DOUBLE));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_idiv()
	 */
	public void visit_idiv() {
		quadList.add(doBinaryQuad(BinaryQuad.IDIV, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldiv()
	 */
	public void visit_ldiv() {
		quadList.add(doBinaryQuad(BinaryQuad.LDIV, Operand.LONG));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fdiv()
	 */
	public void visit_fdiv() {
		quadList.add(doBinaryQuad(BinaryQuad.FDIV, Operand.FLOAT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ddiv()
	 */
	public void visit_ddiv() {
		quadList.add(doBinaryQuad(BinaryQuad.DDIV, Operand.DOUBLE));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_irem()
	 */
	public void visit_irem() {
		quadList.add(doBinaryQuad(BinaryQuad.IREM, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lrem()
	 */
	public void visit_lrem() {
		quadList.add(doBinaryQuad(BinaryQuad.LREM, Operand.LONG));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_frem()
	 */
	public void visit_frem() {
		quadList.add(doBinaryQuad(BinaryQuad.FREM, Operand.FLOAT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_drem()
	 */
	public void visit_drem() {
		quadList.add(doBinaryQuad(BinaryQuad.DREM, Operand.DOUBLE));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ineg()
	 */
	public void visit_ineg() {
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		quadList.add(new UnaryQuad(address, currentBlock, s1, UnaryQuad.INEG, s1));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lneg()
	 */
	public void visit_lneg() {
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		quadList.add(new UnaryQuad(address, currentBlock, s1, UnaryQuad.LNEG, s1));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fneg()
	 */
	public void visit_fneg() {
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.FLOAT);
		quadList.add(new UnaryQuad(address, currentBlock, s1, UnaryQuad.FNEG, s1));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dneg()
	 */
	public void visit_dneg() {
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.DOUBLE);
		quadList.add(new UnaryQuad(address, currentBlock, s1, UnaryQuad.DNEG, s1));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishl()
	 */
	public void visit_ishl() {
		quadList.add(doBinaryQuad(BinaryQuad.ISHL, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshl()
	 */
	public void visit_lshl() {
		stackOffset -= 1;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.INT);
		quadList.add(new BinaryQuad(address, currentBlock, s1, s1, BinaryQuad.LSHL, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishr()
	 */
	public void visit_ishr() {
		quadList.add(doBinaryQuad(BinaryQuad.ISHR, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshr()
	 */
	public void visit_lshr() {
		stackOffset -= 1;
		int s1 = stackOffset - 2;
		variables[s1].setType(Operand.LONG);
		variables[stackOffset].setType(Operand.INT);
		quadList.add(new BinaryQuad(address, currentBlock, s1, s1, BinaryQuad.LSHR, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iushr()
	 */
	public void visit_iushr() {
		quadList.add(doBinaryQuad(BinaryQuad.IUSHR, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lushr()
	 */
	public void visit_lushr() {
		stackOffset -= 2;
		int s1 = stackOffset - 1;
		variables[s1].setType(Operand.INT);
		variables[stackOffset].setType(Operand.LONG);
		quadList.add(new BinaryQuad(address, currentBlock, s1, s1, BinaryQuad.LUSHR, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iand()
	 */
	public void visit_iand() {
		quadList.add(doBinaryQuad(BinaryQuad.IAND, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_land()
	 */
	public void visit_land() {
		quadList.add(doBinaryQuad(BinaryQuad.LAND, Operand.LONG));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ior()
	 */
	public void visit_ior() {
		quadList.add(doBinaryQuad(BinaryQuad.IOR, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lor()
	 */
	public void visit_lor() {
		quadList.add(doBinaryQuad(BinaryQuad.LOR, Operand.LONG));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ixor()
	 */
	public void visit_ixor() {
		quadList.add(doBinaryQuad(BinaryQuad.IXOR, Operand.INT));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lxor()
	 */
	public void visit_lxor() {
		quadList.add(doBinaryQuad(BinaryQuad.LXOR, Operand.LONG));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iinc(int, int)
	 */
	public void visit_iinc(int index, int incValue) {
		variables[index].setType(Operand.INT);
		BinaryQuad binaryQuad = new BinaryQuad(address, currentBlock, index, index, BinaryQuad.IADD, new IntConstant(incValue));
		quadList.add(binaryQuad.foldConstants());
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2l()
	 */
	public void visit_i2l() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.LONG);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.I2L, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2f()
	 */
	public void visit_i2f() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.FLOAT);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.I2F, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2d()
	 */
	public void visit_i2d() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.DOUBLE);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.I2D, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2i()
	 */
	public void visit_l2i() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.INT);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.L2I, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2f()
	 */
	public void visit_l2f() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.FLOAT);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.L2F, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2d()
	 */
	public void visit_l2d() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.DOUBLE);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.L2D, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2i()
	 */
	public void visit_f2i() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.INT);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.F2I, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2l()
	 */
	public void visit_f2l() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.LONG);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.F2L, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2d()
	 */
	public void visit_f2d() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.DOUBLE);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.F2D, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2i()
	 */
	public void visit_d2i() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.INT);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.D2I, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2l()
	 */
	public void visit_d2l() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.LONG);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.D2L, stackOffset));
		stackOffset += 2;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2f()
	 */
	public void visit_d2f() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2b()
	 */
	public void visit_i2b() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.BYTE);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.I2B, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2c()
	 */
	public void visit_i2c() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.CHAR);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.I2C, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2s()
	 */
	public void visit_i2s() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.SHORT);
		quadList.add(new UnaryQuad(address, currentBlock, stackOffset, UnaryQuad.I2S, stackOffset));
		stackOffset += 1;
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lcmp()
	 */
	public void visit_lcmp() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpl()
	 */
	public void visit_fcmpl() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpg()
	 */
	public void visit_fcmpg() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpl()
	 */
	public void visit_dcmpl() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpg()
	 */
	public void visit_dcmpg() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifeq(int)
	 */
	public void visit_ifeq(int address) {
		int s1 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IFEQ, address));
		stackOffset -= 1;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifne(int)
	 */
	public void visit_ifne(int address) {
		int s1 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IFNE, address));
		stackOffset -= 1;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iflt(int)
	 */
	public void visit_iflt(int address) {
		int s1 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IFLT, address));
		stackOffset -= 1;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifge(int)
	 */
	public void visit_ifge(int address) {
		int s1 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IFGE, address));
		stackOffset -= 1;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifgt(int)
	 */
	public void visit_ifgt(int address) {
		int s1 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IFGT, address));
		stackOffset -= 1;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifle(int)
	 */
	public void visit_ifle(int address) {
		int s1 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IFLE, address));
		stackOffset -= 1;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpeq(int)
	 */
	public void visit_if_icmpeq(int address) {
		int s1 = stackOffset - 2;
		int s2 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		variables[s2].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IF_ICMPEQ, s2, address));
		stackOffset -= 2;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpne(int)
	 */
	public void visit_if_icmpne(int address) {
		int s1 = stackOffset - 2;
		int s2 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		variables[s2].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IF_ICMPNE, s2, address));
		stackOffset -= 2;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmplt(int)
	 */
	public void visit_if_icmplt(int address) {
		int s1 = stackOffset - 2;
		int s2 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		variables[s2].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IF_ICMPLT, s2, address));
		stackOffset -= 2;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpge(int)
	 */
	public void visit_if_icmpge(int address) {
		int s1 = stackOffset - 2;
		int s2 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		variables[s2].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IF_ICMPGE, s2, address));
		stackOffset -= 2;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpgt(int)
	 */
	public void visit_if_icmpgt(int address) {
		int s1 = stackOffset - 2;
		int s2 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		variables[s2].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IF_ICMPGT, s2, address));
		stackOffset -= 2;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmple(int)
	 */
	public void visit_if_icmple(int address) {
		int s1 = stackOffset - 2;
		int s2 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.INT);
		variables[s2].setType(Operand.INT);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IF_ICMPLE, s2, address));
		stackOffset -= 2;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpeq(int)
	 */
	public void visit_if_acmpeq(int address) {
		int s1 = stackOffset - 2;
		int s2 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.REFERENCE);
		variables[s2].setType(Operand.REFERENCE);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IF_ACMPEQ, s2, address));
		stackOffset -= 2;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpne(int)
	 */
	public void visit_if_acmpne(int address) {
		int s1 = stackOffset - 2;
		int s2 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.REFERENCE);
		variables[s2].setType(Operand.REFERENCE);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IF_ACMPNE, s2, address));
		stackOffset -= 2;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_goto(int)
	 */
	public void visit_goto(int address) {
		quadList.add(new UnconditionalBranchQuad(this.address, currentBlock, address));
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_jsr(int)
	 */
	public void visit_jsr(int address) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
	 */
	public void visit_ret(int index) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_tableswitch(int, int, int, int[])
	 */
	public void visit_tableswitch(int defValue, int lowValue, int highValue, int[] addresses) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lookupswitch(int, int[], int[])
	 */
	public void visit_lookupswitch(int defValue, int[] matchValues, int[] addresses) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
	 */
	public void visit_ireturn() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.INT);
		quadList.add(new VarReturnQuad(address, currentBlock, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
	 */
	public void visit_lreturn() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.LONG);
		quadList.add(new VarReturnQuad(address, currentBlock, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
	 */
	public void visit_freturn() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.FLOAT);
		quadList.add(new VarReturnQuad(address, currentBlock, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
	 */
	public void visit_dreturn() {
		stackOffset -= 2;
		variables[stackOffset].setType(Operand.DOUBLE);
		quadList.add(new VarReturnQuad(address, currentBlock, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
	 */
	public void visit_areturn() {
		stackOffset -= 1;
		variables[stackOffset].setType(Operand.REFERENCE);
		quadList.add(new VarReturnQuad(address, currentBlock, stackOffset));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
	 */
	public void visit_return() {
		quadList.add(new VoidReturnQuad(address, currentBlock));
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getstatic(org.jnode.vm.classmgr.VmConstFieldRef)
	 */
	public void visit_getstatic(VmConstFieldRef fieldRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putstatic(org.jnode.vm.classmgr.VmConstFieldRef)
	 */
	public void visit_putstatic(VmConstFieldRef fieldRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getfield(org.jnode.vm.classmgr.VmConstFieldRef)
	 */
	public void visit_getfield(VmConstFieldRef fieldRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putfield(org.jnode.vm.classmgr.VmConstFieldRef)
	 */
	public void visit_putfield(VmConstFieldRef fieldRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
	 */
	public void visit_invokevirtual(VmConstMethodRef methodRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
	 */
	public void visit_invokespecial(VmConstMethodRef methodRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokestatic(org.jnode.vm.classmgr.VmConstMethodRef)
	 */
	public void visit_invokestatic(VmConstMethodRef methodRef) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokeinterface(org.jnode.vm.classmgr.VmConstIMethodRef, int)
	 */
	public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_new(org.jnode.vm.classmgr.VmConstClass)
	 */
	public void visit_new(VmConstClass clazz) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_newarray(int)
	 */
	public void visit_newarray(int type) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_anewarray(org.jnode.vm.classmgr.VmConstClass)
	 */
	public void visit_anewarray(VmConstClass clazz) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_arraylength()
	 */
	public void visit_arraylength() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_athrow()
	 */
	public void visit_athrow() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_checkcast(org.jnode.vm.classmgr.VmConstClass)
	 */
	public void visit_checkcast(VmConstClass clazz) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_instanceof(org.jnode.vm.classmgr.VmConstClass)
	 */
	public void visit_instanceof(VmConstClass clazz) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorenter()
	 */
	public void visit_monitorenter() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorexit()
	 */
	public void visit_monitorexit() {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_multianewarray(org.jnode.vm.classmgr.VmConstClass, int)
	 */
	public void visit_multianewarray(VmConstClass clazz, int dimensions) {
		throw new IllegalArgumentException("byte code not yet supported");
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnull(int)
	 */
	public void visit_ifnull(int address) {
		int s1 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.REFERENCE);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IFNULL, address));
		stackOffset -= 1;
		setSuccessorStackOffset();
	}

	/**
	 * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnonnull(int)
	 */
	public void visit_ifnonnull(int address) {
		int s1 = stackOffset - 1;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(Operand.REFERENCE);
		quadList.add(new ConditionalBranchQuad(this.address, currentBlock,
			s1, ConditionalBranchQuad.IFNONNULL, address));
		stackOffset -= 1;
		setSuccessorStackOffset();
	}

	// TODO
	public Quad doBinaryQuad(int op, int type) {
		int sCount = 1;
		if (type == Operand.DOUBLE || type == Operand.LONG) {
			sCount = 2;
		}
		stackOffset -= sCount;
		int s1 = stackOffset - sCount;
		Variable[] variables = currentBlock.getVariables();
		variables[s1].setType(type);
		variables[stackOffset].setType(type);
		BinaryQuad bop = new BinaryQuad(address, currentBlock, s1, s1, op, stackOffset);
		return bop.foldConstants();
	}

	/**
	 * @return
	 */
	public Variable[] getVariables() {
		return variables;
	}

	/**
	 * @return
	 */
	public int getNoArgs() {
		return this.nArgs;
	}

	private void setSuccessorStackOffset() {
		Iterator it = currentBlock.getSuccessors().iterator();
		while (it.hasNext()) {
			IRBasicBlock sBlock = (IRBasicBlock) it.next();
			sBlock.setStackOffset(stackOffset);
		}
	}
}
