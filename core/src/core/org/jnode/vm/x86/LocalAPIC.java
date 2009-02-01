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
 
package org.jnode.vm.x86;

import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeUtils;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Word;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
final class LocalAPIC {

    static final int ICR_DELIVERY_MODE_FIXED = 0x00 << 8;
    static final int ICR_DELIVERY_MODE_LOW_PRIO = 0x01 << 8;
    static final int ICR_DELIVERY_MODE_SMI = 0x02 << 8;
    static final int ICR_DELIVERY_MODE_NMI = 0x04 << 8;
    static final int ICR_DELIVERY_MODE_INIT = 0x05 << 8;
    static final int ICR_DELIVERY_MODE_STARTUP = 0x06 << 8;

    static final int ICR_DESTINATION_MODE_PHYSICAL = 0x00 << 11;
    static final int ICR_DESTINATION_MODE_LOGICAL = 0x01 << 11;

    static final int ICR_DELIVERY_STATUS_IDLE = 0x00 << 12;
    static final int ICR_DELIVERY_STATUS_PENDING = 0x01 << 12;

    static final int ICR_LEVEL_DEASSERT = 0x00 << 14;
    static final int ICR_LEVEL_ASSERT = 0x01 << 14;

    static final int ICR_TRIGGER_MODE_EDGE = 0x00 << 15;
    static final int ICR_TRIGGER_MODE_LEVEL = 0x01 << 15;

    static final int ICR_DESTINATION_SHORTHAND_NONE = 0x00 << 18;
    static final int ICR_DESTINATION_SHORTHAND_SELF = 0x01 << 18;
    static final int ICR_DESTINATION_SHORTHAND_ALL = 0x02 << 18;
    static final int ICR_DESTINATION_SHORTHAND_ALL_EX_SELF = 0x03 << 18;

    static final int SVR_APIC_DISABLED = 0x00 << 8;
    static final int SVR_APIC_ENABLED = 0x01 << 8;

    /**
     * Local APIC ID register
     */
    static final int REG_APIC_ID = 0x0020;
    /**
     * Local APIC Version register (readonly)
     */
    static final int REG_APIC_VERSION = 0x0030;
    /**
     * Task priority register
     */
    static final int REG_TPR = 0x0080;
    /**
     * Arbitration priority register (readonly)
     */
    static final int REG_APR = 0x0090;
    /**
     * Processor priority register (readonly)
     */
    static final int REG_PPR = 0x00A0;
    /**
     * End of Interrupt register (writeonly)
     */
    static final int REG_EOI = 0x00B0;
    /**
     * Spurious Interrupt vector register
     */
    static final int REG_SVR = 0x00F0;
    /**
     * Error status register
     */
    static final int REG_ESR = 0x0280;
    /**
     * Interrupt command register (high part)
     */
    static final int REG_ICR_HIGH = 0x0310;
    /**
     * Interrupt command register (low part)
     */
    static final int REG_ICR_LOW = 0x0300;

    /**
     * Memory region for local APIC
     */
    private final MemoryResource mem;

    /**
     * Initialize this instance.
     *
     * @param rm
     * @param owner
     * @param ptr
     */
    public LocalAPIC(ResourceManager rm, ResourceOwner owner, Address ptr) throws ResourceNotFreeException {
        mem = rm.claimMemoryResource(owner, ptr, Extent.fromIntZeroExtend(4096), ResourceManager.MEMMODE_NORMAL);
    }

    /**
     * Gets the Local APIC ID
     */
    public final int getId() {
        return (mem.getInt(REG_APIC_ID) >> 24) & 0xFF;
    }

    /**
     * Is the local APIC enabled?
     */
    public final boolean isEnabled() {
        return ((mem.getInt(REG_SVR) & SVR_APIC_ENABLED) != 0);
    }

    /**
     * Enable/disable the local APIC.
     */
    public final void setEnabled(boolean enabled) {
        int v = mem.getInt(REG_SVR);
        if (enabled) {
            v |= SVR_APIC_ENABLED;
        } else {
            v &= ~SVR_APIC_ENABLED;
        }
        mem.setInt(REG_SVR, v);
    }

    /**
     * Send an INIT IPI to the processor with the given ID.
     *
     * @param dstId
     */
    public final void sendInitIPI(int dstId, boolean levelAssert) {
        final int high = (dstId & 0xFF) << 24;
        int low = ICR_DELIVERY_MODE_INIT | ICR_TRIGGER_MODE_LEVEL;
        if (levelAssert) {
            low |= ICR_LEVEL_ASSERT;
        }
        mem.setInt(REG_ICR_HIGH, high);
        mem.setInt(REG_ICR_LOW, low);
    }

    /**
     * Send a STARTUP IPI to the processor with the given ID.
     * The processor will start at the given address.
     *
     * @param dstId
     * @param vector Address must be a 4K aligned address below 1Mb.
     */
    public final void sendStartupIPI(int dstId, Address vector) {
        final int high = (dstId & 0xFF) << 24;
        int low = ICR_DELIVERY_MODE_STARTUP | ICR_DESTINATION_MODE_PHYSICAL | ICR_DESTINATION_SHORTHAND_NONE;
        int v = vector.toInt();
        if ((v & 0xFFF00FFF) != 0) {
            throw new IllegalArgumentException(
                "Invalid vector 0x" + NumberUtils.hex(vector.toInt()) + " must be like 0x000vv000");
        }
        low |= (v >> 12) & 0xFF;
        mem.setInt(REG_ICR_HIGH, high);
        mem.setInt(REG_ICR_LOW, low);
    }

    /**
     * Send a FIXED interrupt on the inter processor bus.
     *
     * @param dstId
     * @param dstShorthand
     * @param vector       Interrupt vector
     */
    @KernelSpace
    @Uninterruptible
    @Inline
    final void sendFixedIPI(int dstId, int dstShorthand, int vector) {
        // Wait until not busy
        while (isIPIPending()) {
            // Wait
        }

        final int high = (dstId & 0xFF) << 24;
        int low = ICR_DELIVERY_MODE_FIXED | ICR_DESTINATION_MODE_PHYSICAL | dstShorthand;
        low |= (vector & 0xFF);
        mem.setInt(REG_ICR_HIGH, high);
        mem.setInt(REG_ICR_LOW, low);
    }

    /**
     * Wait until the IPI is finished.
     */
    public final void loopUntilNotBusy() {
        while (isIPIPending()) {
            TimeUtils.loop(1);
        }
    }

    /**
     * Clear any error.
     */
    public final void clearErrors() {
        mem.getInt(REG_SVR);
        mem.setInt(REG_ESR, 0);
        mem.getInt(REG_ESR);
    }

    /**
     * Get the current error flags.
     */
    public final int getErrors() {
        return mem.getInt(REG_ESR);
    }

    /**
     * Gets the address if the EOI register.
     *
     * @return
     */
    final Address getEOIAddress() {
        return mem.getAddress().add(Word.fromIntZeroExtend(REG_EOI));
    }

    /**
     * Is an IPI pending?
     */
    @KernelSpace
    @Uninterruptible
    @Inline
    public final boolean isIPIPending() {
        return ((mem.getInt(REG_ICR_LOW) & ICR_DELIVERY_STATUS_PENDING) != 0);
    }

    /**
     * Release all resources.
     */
    final void release() {
        mem.release();
    }
}
