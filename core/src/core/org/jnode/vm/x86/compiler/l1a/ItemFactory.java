/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import java.util.ArrayList;

import org.jnode.assembler.x86.Register;
import org.jnode.util.Counter;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.classmgr.VmConstString;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class ItemFactory {

    private static ThreadLocal itemFactory = new ThreadLocal();

    private final ArrayList intItems = new ArrayList();

    private final ArrayList longItems = new ArrayList();

    private final ArrayList floatItems = new ArrayList();

    private final ArrayList doubleItems = new ArrayList();

    private final ArrayList refItems = new ArrayList();

    private int createCount = 0;

    /**
     * Create a constant item
     * 
     * @param val
     */
    final IntItem createIConst(int val) {
        final IntItem item = (IntItem) getOrCreate(JvmType.INT);
        item.initialize(Item.Kind.CONSTANT, 0, null, val);
        return item;
    }

    /**
     * Create a constant item
     * 
     * @param val
     */
    final FloatItem createFConst(float val) {
        final FloatItem item = (FloatItem) getOrCreate(JvmType.FLOAT);
        item.initialize(Item.Kind.CONSTANT, 0, null, val);
        return item;
    }

    /**
     * Create a constant item
     * 
     * @param val
     */
    final RefItem createAConst(VmConstString val) {
        final RefItem item = (RefItem) getOrCreate(JvmType.REFERENCE);
        item.initialize(Item.Kind.CONSTANT, 0, null, val);
        return item;
    }

    /**
     * Create a constant item
     * 
     * @param val
     */
    final LongItem createLConst(long val) {
        final LongItem item = (LongItem) getOrCreate(JvmType.LONG);
        item.initialize(Item.Kind.CONSTANT, 0, null, null, val);
        return item;
    }

    /**
     * Create a constant item
     * 
     * @param val
     */
    final DoubleItem createDConst(double val) {
        final DoubleItem item = (DoubleItem) getOrCreate(JvmType.DOUBLE);
        item.initialize(Item.Kind.CONSTANT, 0, null, null, val);
        return item;
    }

    /**
     * Create a stack item.
     * 
     * @param jvmType
     */
    public Item createStack(int jvmType) {
        final Item item = getOrCreate(jvmType);
        item.initialize(Item.Kind.STACK, 0);
        return item;
    }

    /**
     * Create an FPU stack item.
     * 
     * @param jvmType
     */
    public Item createFPUStack(int jvmType) {
        final Item item = getOrCreate(jvmType);
        item.initialize(Item.Kind.FPUSTACK, 0);
        return item;
    }

    /**
     * Create an LOCAL item.
     * 
     * @param jvmType
     */
    public Item createLocal(int jvmType, int ebpOffset) {
        final Item item = getOrCreate(jvmType);
        item.initialize(Item.Kind.LOCAL, ebpOffset);
        return item;
    }

    /**
     * Create a word register item.
     * 
     * @param jvmType
     * @param reg
     */
    public WordItem createReg(int jvmType, Register reg) {
        final WordItem item = (WordItem) getOrCreate(jvmType);
        item.initialize(Item.Kind.REGISTER, reg, 0);
        return item;
    }

    /**
     * Create a doubleword register item.
     * 
     * @param jvmType
     * @param lsb
     * @param msb
     */
    public DoubleWordItem createReg(int jvmType, Register lsb, Register msb) {
        final DoubleWordItem item = (DoubleWordItem) getOrCreate(jvmType);
        item.initialize(Item.Kind.REGISTER, 0, lsb, msb);
        return item;
    }

    /**
     * Add the given item to the free list of this factory.
     * 
     * @param item
     */
    final void release(Item item) {
        if (Vm.VerifyAssertions)
            Vm._assert(item.kind == 0, "Item is not yet released");
        final ArrayList list = getList(item.getType());
        if (Vm.VerifyAssertions)
            Vm._assert(!list.contains(item), "Item already released");
        list.add(item);

        final String name = getClass().getName();
        final Counter cnt = Vm.getVm().getCounter(name);
        cnt.inc();

    }

    /**
     * Get an item out of the cache or if not present, create a new one.
     * 
     * @param jvmType
     */
    private final Item getOrCreate(int jvmType) {
        final ArrayList list = getList(jvmType);
        final Item item;
        if (list.isEmpty()) {
            item = createNew(jvmType);
        } else {
            item = (Item) list.remove(list.size() - 1);
            if (Vm.VerifyAssertions)
                Vm._assert(item.kind == 0, "kind == 0, but " + item.kind);
        }
        return item;
    }

    /**
     * Gets the cache array for a given type.
     * 
     * @param jvmType
     */
    private final ArrayList getList(int jvmType) {
        switch (jvmType) {
        case JvmType.INT:
            return intItems;
        case JvmType.LONG:
            return longItems;
        case JvmType.FLOAT:
            return floatItems;
        case JvmType.DOUBLE:
            return doubleItems;
        case JvmType.REFERENCE:
            return refItems;
        default:
            throw new IllegalArgumentException("Invalid jvmType " + jvmType);
        }
    }

    /**
     * Create a new item of a given type.
     * 
     * @param jvmType
     */
    private final Item createNew(int jvmType) {
        createCount++;
        switch (jvmType) {
        case JvmType.INT:
            return new IntItem(this);
        case JvmType.LONG:
            return new LongItem(this);
        case JvmType.FLOAT:
            return new FloatItem(this);
        case JvmType.DOUBLE:
            return new DoubleItem(this);
        case JvmType.REFERENCE:
            return new RefItem(this);
        default:
            throw new IllegalArgumentException("Invalid jvmType " + jvmType);
        }
    }

    /**
     * Hidden constructor.
     */
    private ItemFactory() {
    }

    /**
     * Gets the item factory. This item factory is singleton per thread.
     */
    static final ItemFactory getFactory() {
        ItemFactory fac = (ItemFactory) itemFactory.get();
        if (fac == null) {
            fac = new ItemFactory();
            itemFactory.set(fac);
        }
        return fac;
    }
}