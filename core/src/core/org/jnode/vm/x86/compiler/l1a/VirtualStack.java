/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.x86.compiler.AbstractX86StackManager;
import org.jnode.vm.x86.compiler.JvmType;

/**
 * @author Patrik Reali
 */

// TODO: work with Items to keep track of each item's stack level until popped
// and ensure consistency and correctness
final class VirtualStack {

    // explicitely check that elements on the operant stack
    // are popped in the appropriate order
    //
    static final boolean checkOperandStack = true;

    // the virtual stack
    private Item[] stack;

    // top of stack; stack[tos] is not part of the stack!
    private int tos;

    final ItemStack operandStack;
    
    final ItemStack fpuStack = new ItemStack(Item.Kind.FPUSTACK);

    /**
     * 
     * Constructor; create and initialize stack with default size
     */
    VirtualStack(AbstractX86Stream os) {
        this.operandStack = checkOperandStack ? new ItemStack(Item.Kind.STACK)
                : null;
        reset();
    }

    void reset() {
        stack = new Item[ 8];
        tos = 0;
        if (checkOperandStack) {
            operandStack.reset();
        }
    }

    int TOS() {
        return tos;
    }

    boolean isEmpty() {
        return (tos == 0);
    }

    /**
     * Increase stack size
     */
    private void growStack() {
        Item[] tmp = new Item[ stack.length * 2];
        System.arraycopy(stack, 0, tmp, 0, stack.length);
        stack = tmp;
    }

    /**
     * Pop top item from stack. If no item on the stack, return UNKNOWN item
     * (avoiding this requires knowing the stack contents across basic blocks)
     * 
     * Use pop as far as possible, but the brain-dead implementation of all dup
     * opcodes (from the 2nd edition of the spec) allows popping elements
     * without knowing their type.
     * 
     * @return top item
     */
    Item pop() {
        // do not autocreate item: if no type available, just fail; avoid this
        // case in the bytecode visitor
        //		if (tos == 0)
        //			//TODO: support items across basic blocks
        //			pushStack(Item.UNKNOWN);
        tos--;
        Item i = stack[ tos];
        stack[ tos] = null;
        return i;
    }

    /**
     * Pop top item from stack, check its type also. If none is present, create
     * a new stack item with the given type
     * 
     * @param type
     * @return pop the top of stack item
     * @exception VerifyError
     *                if the type does not correspond
     */
    Item pop(int type) {
        //        if (tos == 0) {
        //            // the item requested in not on the virtual stack
        //            // but already on the operand stack (it was pushed
        //            // outside the current basic block)
        //            // thus create a new stack item
        //            Item it = createStack(type);
        //            if (checkOperandStack) {
        //                // insert at the begin of stack
        //                // even if the vstack is empty, there
        //                // may still be items popped from vstack
        //                // that are not popped from operand stack
        //                prependToOperandStack(it);
        //            }
        //            return it;
        //            // pushStack(type);
        //        }
        tos--;
        Item i = stack[ tos];
        stack[ tos] = null;
        if (i.getType() != type)
                throw new VerifyError("Expected:" + Integer.toString(type)
                        + " Actual:" + Integer.toString(i.getType()));
        return i;
    }

    /**
     * Pop an item of the stack. If the type is different from INT, an exception
     * is thrown.
     * 
     * @return
     */
    final IntItem popInt() {
        // testing in pop and casting here: test is just redundant
        return (IntItem) pop(JvmType.INT);
    }

    /**
     * Pop an item of the stack. If the type is different from LONG, an
     * exception is thrown.
     * 
     * @return
     */
    final LongItem popLong() {
        // testing in pop and casting here: test is just redundant
        return (LongItem) pop(JvmType.LONG);
    }

    /**
     * Pop an item of the stack. If the type is different from REFERENCE, an
     * exception is thrown.
     * 
     * @return
     */
    final RefItem popRef() {
        // testing in pop and casting here: test is just redundant
        return (RefItem) pop(JvmType.REFERENCE);
    }

    /**
     * Pop an item of the stack. If the type is different from FLOAT, an
     * exception is thrown.
     * 
     * @return
     */
    final FloatItem popFloat() {
        // testing in pop and casting here: test is just redundant
        return (FloatItem) pop(JvmType.FLOAT);
    }

    /**
     * Pop an item of the stack. If the type is different from REFERENCE, an
     * exception is thrown.
     * 
     * @return
     */
    final DoubleItem popDouble() {
        // testing in pop and casting here: test is just redundant
        return (DoubleItem) pop(JvmType.DOUBLE);
    }

    /**
     * Push item on stack.
     */
    void push(Item item) {
        if ((item.getKind() == Item.Kind.STACK) && (tos > 0))
                Item.myAssert(stack[ tos - 1].getKind() == Item.Kind.STACK);

        if (tos == stack.length) growStack();

        stack[ tos++] = item;
    }

    /**
     * Push on vstack and operand stack (special case for old-style code, to be
     * eventually removed)
     */
    void push1(Item item) {
        push(item);
        if (checkOperandStack && (item.getKind() == Item.Kind.STACK)) {
            operandStack.push(item);
        }
    }

    //TODO: deprecated
    Item createStack(int type) {
        Item res = null;
        switch (type) {
        case JvmType.INT:
            res = IntItem.createStack();
            break;
        case JvmType.REFERENCE:
            res = RefItem.createStack();
            break;
        case JvmType.LONG:
            res = LongItem.createStack();
            break;
        case JvmType.FLOAT:
            res = FloatItem.createStack();
            break;
        case JvmType.DOUBLE:
            res = DoubleItem.createStack();
            break;
        default:
            throw new VerifyError("No type " + Integer.toString(type));
        }
        return res;
    }

    /**
     * load every instance of local with given index into a register (used to
     * avoid aliasing problems)
     * 
     * @param offsetToFP
     */
    void loadLocal(EmitterContext ec, int offsetToFP) {
        for (int i = 0; i < tos; i++) {
            final Item item = stack[ i];
            if ((item.getKind() == Item.Kind.LOCAL)
                    && (item.getOffsetToFP() == offsetToFP)) item.load(ec);
        }

    }

    /**
     * Push all items on the virtual stack to the actual stack.
     * 
     * @param ec
     */
    final int push(EmitterContext ec) {
        int i = 0;
        while ((i < tos) && (stack[ i].getKind() == Item.Kind.STACK)) {
            i++;
        }
        int cnt = 0;
        while (i < tos) {
            final Item item = stack[ i];
            Item.myAssert(item.getKind() != Item.Kind.STACK);
            item.push(ec);
            i++;
            cnt++;
        }
        return cnt;
    }

    //    private void prependToOperandStack(Item item) {
    //        os.log("prepend");
    //        Item.myAssert(item.getKind() == Item.Kind.STACK);
    //
    //        if (operandTos == operandStack.length) growOperandStack();
    //
    //        for (int i = operandTos; i > 0; i--)
    //            operandStack[ i] = operandStack[ i - 1];
    //
    //        operandTos++;
    //        operandStack[ 0] = item;
    //    }
    //

    boolean uses(Register reg) {
        for (int i = 0; i < tos; i++) {
            if (stack[ i].uses(reg)) { return true; }
        }
        return false;
    }

    final AbstractX86StackManager createStackMgr() {
        return new StackManagerImpl();
    }

    final class StackManagerImpl implements AbstractX86StackManager {

        /**
         * @see org.jnode.vm.x86.compiler.AbstractX86StackManager#writePUSH(int,
         *      org.jnode.assembler.x86.Register)
         */
        public void writePUSH(int jvmType, Register reg) {
            final Item item;
            switch (jvmType) {
            case JvmType.INT:
                item = IntItem.createReg(reg);
                break;
            case JvmType.REFERENCE:
                item = RefItem.createRegister(reg);
                break;
            default:
                throw new IllegalArgumentException("Unknown JvmType " + jvmType);
            }
            push(item);
        }

        /**
         * @see org.jnode.vm.x86.compiler.AbstractX86StackManager#writePUSH64(int,
         *      org.jnode.assembler.x86.Register,
         *      org.jnode.assembler.x86.Register)
         */
        public void writePUSH64(int jvmType, Register lsbReg, Register msbReg) {
            final Item item;
            switch (jvmType) {
            case JvmType.LONG:
                item = LongItem.createReg(lsbReg, msbReg);
                break;
            case JvmType.DOUBLE:
                item = DoubleItem.createReg(lsbReg, msbReg);
                break;
            default:
                throw new IllegalArgumentException("Unknown JvmType " + jvmType);
            }
            push(item);
        }
    }
}