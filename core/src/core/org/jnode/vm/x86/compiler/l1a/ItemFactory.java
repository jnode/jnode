/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.vm.x86.compiler.l1a;

import java.util.ArrayList;
import org.jnode.assembler.x86.X86Register;
import org.jnode.vm.JvmType;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.compiler.IllegalModeException;
import org.jnode.vm.facade.VmUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class ItemFactory {
    public static final boolean CHECK_BALANCED_ITEM_FACTORY = true;

    private static ThreadLocal itemFactory = new ThreadLocal();

    private final ArrayList<IntItem> intItems = new ArrayList<IntItem>();

    private final ArrayList<LongItem> longItems = new ArrayList<LongItem>();

    private final ArrayList<FloatItem> floatItems = new ArrayList<FloatItem>();

    private final ArrayList<DoubleItem> doubleItems = new ArrayList<DoubleItem>();

    private final ArrayList<RefItem> refItems = new ArrayList<RefItem>();

    private int createCount = 0;

    private int getOrCreateCount = 0;

    private int releaseCount = 0;

    /**
     * Create a constant item
     *
     * @param ec
     * @param val
     * @return
     */
    final IntItem createIConst(EmitterContext ec, int val) {
        final IntItem item = (IntItem) getOrCreate(JvmType.INT);
        item.initialize(ec, Item.Kind.CONSTANT, (short) 0, null, val);
        return item;
    }

    /**
     * Create a constant item
     *
     * @param ec
     * @param val
     * @return
     */
    final FloatItem createFConst(EmitterContext ec, float val) {
        final FloatItem item = (FloatItem) getOrCreate(JvmType.FLOAT);
        item.initialize(ec, Item.Kind.CONSTANT, (short) 0, null, val);
        return item;
    }

    /**
     * Create a constant item
     *
     * @param ec
     * @param val
     * @return
     */
    final RefItem createAConst(EmitterContext ec, VmConstString val) {
        final RefItem item = (RefItem) getOrCreate(JvmType.REFERENCE);
        item.initialize(ec, Item.Kind.CONSTANT, (short) 0, null, val);
        return item;
    }

    /**
     * Create a constant item
     *
     * @param ec
     * @param val
     * @return
     */
    final LongItem createLConst(EmitterContext ec, long val) {
        final LongItem item = (LongItem) getOrCreate(JvmType.LONG);
        item.initialize(ec, Item.Kind.CONSTANT, (short) 0, null, null, null, null, val);
        return item;
    }

    /**
     * Create a constant item
     *
     * @param ec
     * @param val
     * @return
     */
    final DoubleItem createDConst(EmitterContext ec, double val) {
        final DoubleItem item = (DoubleItem) getOrCreate(JvmType.DOUBLE);
        item.initialize(ec, Item.Kind.CONSTANT, (short) 0, null, null, null, null, val);
        return item;
    }

    /**
     * Create a stack item.
     *
     * @param jvmType
     * @return
     */
    public Item createStack(int jvmType) {
        final Item item = getOrCreate(jvmType);
        item.initialize(Item.Kind.STACK, (short) 0, null);
        return item;
    }

    /**
     * Create an FPU stack item.
     *
     * @param jvmType
     * @return
     */
    public Item createFPUStack(int jvmType) {
        final Item item = getOrCreate(jvmType);
        item.initialize(Item.Kind.FPUSTACK, (short) 0, null);
        return item;
    }

    /**
     * Create an LOCAL item.
     *
     * @param jvmType
     * @param ebpOffset
     * @return
     */
    public Item createLocal(int jvmType, short ebpOffset) {
        final Item item = getOrCreate(jvmType);
        item.initialize(Item.Kind.LOCAL, ebpOffset, null);
        return item;
    }

    /**
     * Create an XMM item.
     *
     * @param jvmType
     * @param xmm
     * @return
     */
    public Item createLocal(int jvmType, X86Register.XMM xmm) {
        final Item item = getOrCreate(jvmType);
        item.initialize(Item.Kind.XMM, (short) 0, xmm);
        return item;
    }

    /**
     * Create a word register item.
     *
     * @param ec
     * @param jvmType
     * @param reg
     * @return
     */
    public WordItem createReg(EmitterContext ec, int jvmType, X86Register reg) {
        final WordItem item = (WordItem) getOrCreate(jvmType);
        item.initialize(ec, Item.Kind.GPR, reg, (short) 0);
        return item;
    }

    /**
     * Create a doubleword register item.
     *
     * @param ec
     * @param jvmType
     * @param lsb
     * @param msb
     * @return
     */
    public DoubleWordItem createReg(EmitterContext ec, int jvmType, X86Register.GPR lsb, X86Register.GPR msb) {
        if (!ec.getStream().isCode32()) {
            throw new IllegalModeException("Only supported in 32-bit mode");
        }
        final DoubleWordItem item = (DoubleWordItem) getOrCreate(jvmType);
        item.initialize(ec, Item.Kind.GPR, (short) 0, lsb, msb, null, null);
        return item;
    }

    /**
     * Create a doubleword register item.
     *
     * @param ec
     * @param jvmType
     * @param reg
     * @return
     */
    public DoubleWordItem createReg(EmitterContext ec, int jvmType, X86Register.GPR64 reg) {
        final DoubleWordItem item = (DoubleWordItem) getOrCreate(jvmType);
        item.initialize(ec, Item.Kind.GPR, (short) 0, null, null, reg, null);
        return item;
    }

    /**
     * Add the given item to the free list of this factory.
     *
     * @param item
     */
    @SuppressWarnings("unchecked")
    final <T extends Item> void release(T item) {
        if (CHECK_BALANCED_ITEM_FACTORY) {
            releaseCount++;
        }
        if (VmUtils.verifyAssertions()) {
            VmUtils._assert(item.getKind() == 0, "Item is not yet released");
        }
        final ArrayList<T> list = (ArrayList<T>) getList(item.getType());
        if (VmUtils.verifyAssertions()) {
            VmUtils._assert(!list.contains(item), "Item already released");
        }
        list.add(item);

        if (false) {
            final String name = item.getClass().getName();
            VmUtils.getVm().getCounterGroup(name).getCounter("release").inc();
        }
    }

    /**
     * Get an item out of the cache or if not present, create a new one.
     *
     * @param jvmType
     * @return
     */
    private Item getOrCreate(int jvmType) {
        if (CHECK_BALANCED_ITEM_FACTORY) {
            getOrCreateCount++;
        }
        final ArrayList<? extends Item> list = getList(jvmType);
        final Item item;
        if (list.isEmpty()) {
            item = createNew(jvmType);
        } else {
            item = (Item) list.remove(list.size() - 1);
            if (VmUtils.verifyAssertions())
                VmUtils._assert(item.getKind() == 0, "kind == 0, but " + item.getKind());
        }
        return item;
    }

    /**
     * Gets the cache array for a given type.
     *
     * @param jvmType
     * @return
     */
    private ArrayList<? extends Item> getList(int jvmType) {
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
     * @return
     */
    private Item createNew(int jvmType) {
        if (CHECK_BALANCED_ITEM_FACTORY) {
            createCount++;
        }
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
     *
     * @return
     */
    static ItemFactory getFactory() {
        ItemFactory fac = (ItemFactory) itemFactory.get();
        if (fac == null) {
            fac = new ItemFactory();
            itemFactory.set(fac);
        }
        return fac;
    }

    boolean isBalanced() {
        return getOrCreateCount == releaseCount;
    }

    void balance() {
        getOrCreateCount = releaseCount = 0;
    }
}
