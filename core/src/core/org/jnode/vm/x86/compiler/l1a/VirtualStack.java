/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;

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
    Item[] stack;

    // top of stack; stack[tos] is not part of the stack!
    int tos;

    // the real stack (only if checkStackOrder == true)
    private Item[] operandStack;

    private int operandTos;

    private AbstractX86Stream os;

    /**
     * 
     * Constructor; create and initialize stack with default size
     */
    VirtualStack(AbstractX86Stream os) {
        this.os = os;
        reset();
    }

    void reset() {
        stack = new Item[ 8];
        tos = 0;
        if (checkOperandStack) {
            operandStack = new Item[ 8];
            operandTos = 0;
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
        if (tos == 0) {
            // the item requested in not on the virtual stack
            // but already on the operand stack (it was pushed
            // outside the current basic block)
            // thus create a new stack item
            Item it = createStack(type);
            if (checkOperandStack) {
                // insert at the begin of stack
                // even if the vstack is empty, there
                // may still be items popped from vstack
                // that are not popped from operand stack
                prependToOperandStack(it);
            }
            return it;
            // pushStack(type);
        }
        tos--;
        Item i = stack[ tos];
        stack[ tos] = null;
        if (i.getType() != type)
                throw new VerifyError("Expected:" + Integer.toString(type)
                        + " Actual:" + Integer.toString(i.getType()));
        return i;
    }

    IntItem popInt() {
        // testing in pop and casting here: test is just redundant
        return (IntItem) pop(Item.JvmType.INT);
    }

    RefItem popRef() {
        // testing in pop and casting here: test is just redundant
        return (RefItem) pop(Item.JvmType.REFERENCE);
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
            pushOnOperandStack(item);
        }
    }

    //TODO: deprecated
    Item createStack(int type) {
        Item res = null;
        switch (type) {
        case Item.JvmType.INT:
            res = IntItem.createStack();
            break;
        case Item.JvmType.REFERENCE:
            res = RefItem.createStack();
            break;
        case Item.JvmType.LONG:
            res = LongItem.createStack();
            break;
        case Item.JvmType.FLOAT:
            res = FloatItem.createStack();
            break;
        case Item.JvmType.DOUBLE:
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

    // operations on the operand stack
    // used to check sanity
    private final void growOperandStack() {
        final Item[] tmp = new Item[ operandStack.length * 2];
        System.arraycopy(operandStack, 0, tmp, 0, operandStack.length);
        operandStack = tmp;
    }

    private void prependToOperandStack(Item item) {
        os.log("prepend");
        Item.myAssert(item.getKind() == Item.Kind.STACK);

        if (operandTos == operandStack.length) growOperandStack();

        for (int i = operandTos; i > 0; i--)
            operandStack[ i] = operandStack[ i - 1];

        operandTos++;
        operandStack[ 0] = item;
    }

    void pushOnOperandStack(Item item) {
        Item.myAssert(item.getKind() == Item.Kind.STACK);

        if (operandTos == operandStack.length) growOperandStack();

        os.log("push " + Integer.toString(item.getType()));
        operandStack[ operandTos++] = item;
    }

    void popFromOperandStack(Item item) {
        if (operandTos <= 0) { throw new Error("OperandStack is empty"); }
        if (operandStack[ --operandTos] != item) {
            int i = operandTos - 1;
            while ((i >= 0) && (operandStack[ i] != item))
                i--;

            throw new Error("OperandStack[" + operandTos
                    + "] is not the expected element (found at " + i + ")");
        }
        os.log("pop");
        operandStack[ operandTos] = null;
    }
    
    boolean uses(Register reg) {
        for (int i = 0; i < tos; i++) {
            if (stack[i].uses(reg)) {
                return true;
            }
        }
        return false;
    }
}