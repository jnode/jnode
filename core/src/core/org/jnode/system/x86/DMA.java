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
 
package org.jnode.system.x86;

import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.system.DMAException;
import org.jnode.system.IOResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;

/*
 * NOTES about DMA transfers:
 *
 *  controller 1: channels 0-3, byte operations, ports 00-1F
 *  controller 2: channels 4-7, word operations, ports C0-DF
 *
 *  - ALL registers are 8 bits only, regardless of transfer size
 *  - channel 4 is not used - cascades 1 into 2.
 *  - channels 0-3 are byte - addresses/counts are for physical bytes
 *  - channels 5-7 are word - addresses/counts are for physical words
 *  - transfers must not cross physical 64K (0-3) or 128K (5-7) boundaries
 *  - transfer count loaded to registers is 1 less than actual count
 *  - controller 2 offsets are all even (2x offsets for controller 1)
 *  - page registers for 5-7 don't use data bit 0, represent 128K pages
 *  - page registers for 0-3 use bit 0, represent 64K pages
 *
 * DMA transfers are limited to the lower 16MB of _physical_ memory.  
 * Note that addresses loaded into registers must be _physical_ addresses,
 * not logical addresses (which may differ if paging is active).
 *
 *  Address mapping for channels 0-3:
 *
 *   A23 ... A16 A15 ... A8  A7 ... A0    (Physical addresses)
 *    |  ...  |   |  ... |   |  ... |
 *    |  ...  |   |  ... |   |  ... |
 *    |  ...  |   |  ... |   |  ... |
 *   P7  ...  P0  A7 ... A0  A7 ... A0   
 * |    Page    | Addr MSB | Addr LSB |   (DMA registers)
 *
 *  Address mapping for channels 5-7:
 *
 *   A23 ... A17 A16 A15 ... A9 A8 A7 ... A1 A0    (Physical addresses)
 *    |  ...  |   \   \   ... \  \  \  ... \  \
 *    |  ...  |    \   \   ... \  \  \  ... \  (not used)
 *    |  ...  |     \   \   ... \  \  \  ... \
 *   P7  ...  P1 (0) A7 A6  ... A0 A7 A6 ... A0   
 * |      Page      |  Addr MSB   |  Addr LSB  |   (DMA registers)
 *
 * Again, channels 5-7 transfer _physical_ words (16 bits), so addresses
 * and counts _must_ be word-aligned (the lowest address bit is _ignored_ at
 * the hardware level, so odd-byte transfers aren't possible).
 *
 * Transfer count (_not # bytes_) is limited to 64K, represented as actual
 * count - 1 : 64K => 0xFFFF, 1 => 0x0000.  Thus, count is always 1 or more,
 * and up to 128K bytes may be transferred on channels 5-7 in one operation. 
 *
 * TAKEN FROM Linux kernel.
 */
/**
 * @author epr
 */
@MagicPermission
final class DMA implements DMAConstants {

    /**
     * Number of channels
     */
    public static final int MAX = 8;

    /**
     * Page I/O ports
     */
    private final IOResource pageIO;
    private final IOResource dma1IO;
    private final IOResource dma2IO;

    /**
     * Create a new instance
     *
     * @throws DMAException
     */
    public DMA() throws DMAException {
        final ResourceManager rm;
        try {
            rm = InitialNaming.lookup(ResourceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new DMAException("Cannot find ResourceManager", ex);
        }
        IOResource pageIO = null;
        IOResource dma1IO = null;
        IOResource dma2IO = null;

        try {
            final ResourceOwner owner = new SimpleResourceOwner("DMA-X86");
            pageIO = claimPorts(rm, owner, 0x81, 0x8f - 0x81 + 1);
            dma1IO = claimPorts(rm, owner, 0x00, 16);
            dma2IO = claimPorts(rm, owner, 0xc0, 32);

            this.pageIO = pageIO;
            this.dma1IO = dma1IO;
            this.dma2IO = dma2IO;

            for (int dmanr = 0; dmanr < MAX; dmanr++) {
                clearFF(dmanr);
            }

        } catch (ResourceNotFreeException ex) {
            if (pageIO != null) {
                pageIO.release();
            }
            if (dma1IO != null) {
                dma1IO.release();
            }
            if (dma2IO != null) {
                dma2IO.release();
            }
            throw new DMAException("Cannot claim DMA I/O ports", ex);
        }
    }

    /**
     * Release all resources
     */
    protected final void release() {
        pageIO.release();
        dma1IO.release();
        dma2IO.release();
    }

    /**
     * Program the page register for a given channel
     *
     * @param dmanr
     * @param page
     */
    private final void setPage(int dmanr, int page) {
        switch (dmanr) {
            case 0:
                pageIO.outPortByte(DMA_PAGE_0, page);
                break;
            case 1:
                pageIO.outPortByte(DMA_PAGE_1, page);
                break;
            case 2:
                pageIO.outPortByte(DMA_PAGE_2, page);
                break;
            case 3:
                pageIO.outPortByte(DMA_PAGE_3, page);
                break;
            case 5:
                pageIO.outPortByte(DMA_PAGE_5, page & 0xfe);
                break;
            case 6:
                pageIO.outPortByte(DMA_PAGE_6, page & 0xfe);
                break;
            case 7:
                pageIO.outPortByte(DMA_PAGE_7, page & 0xfe);
                break;
            default:
                throw new IllegalArgumentException("Invalid dmanr " + dmanr);
        }
    }

    /**
     * Program the address register for a given channel
     *
     * @param dmanr
     * @param address
     * @throws DMAException
     */
    public void setAddress(int dmanr, Address address) throws DMAException {
        final int a32 = address.toInt();
        final int page = (a32 >> 16);

        setPage(dmanr, page);
        if (dmanr <= 3) {
            final int port = DMA_ADDR_0 + ((dmanr & 3) << 1);
            dma1IO.outPortByte(port, a32 & 0xFF);
            dma1IO.outPortByte(port, (a32 >> 8) & 0xFF);
        } else {
            final int port = DMA_ADDR_4 + ((dmanr & 3) << 2);
            dma1IO.outPortByte(port, (a32 >> 1) & 0xFF);
            dma1IO.outPortByte(port, (a32 >> 9) & 0xFF);
        }
    }

    /**
     * Program the address register for a given channel
     *
     * @param dmanr
     * @param length
     * @throws DMAException
     */
    public void setLength(int dmanr, int length) throws DMAException {
        length--;
        if (dmanr <= 3) {
            final int port = DMA_CNT_0 + ((dmanr & 3) << 1);
            dma1IO.outPortByte(port, length & 0xFF);
            dma1IO.outPortByte(port, (length >> 8) & 0xFF);
        } else {
            final int port = DMA_CNT_4 + ((dmanr & 3) << 2);
            dma1IO.outPortByte(port, (length >> 1) & 0xFF);
            dma1IO.outPortByte(port, (length >> 9) & 0xFF);
        }
    }

    public int getLength(int dmanr) {
        final int port;
        int count;
        if (dmanr <= 3) {
            port = DMA_CNT_0 + ((dmanr & 3) << 1);
        } else {
            port = DMA_CNT_4 + ((dmanr & 3) << 2);
        }
        count = (dma1IO.inPortByte(port) & 0xFF) + 1;
        count += ((dma1IO.inPortByte(port) & 0xFF) << 8);
        if (dmanr <= 3) {
            return count;
        } else {
            return count << 1;
        }
    }

    /**
     * Program the mode register for a given channel
     *
     * @param dmanr
     * @param mode
     * @throws DMAException
     */
    public void setMode(int dmanr, int mode) throws DMAException {
        mode |= (dmanr & 3);
        if (dmanr <= 3) {
            dma1IO.outPortByte(DMA1_MODE_REG, mode);
        } else {
            dma2IO.outPortByte(DMA2_MODE_REG, mode);
        }
    }

    /**
     * Enable the given channel
     *
     * @param dmanr
     */
    public void enable(int dmanr) {
        if (dmanr <= 3) {
            dma1IO.outPortByte(DMA1_MASK_REG, dmanr);
        } else {
            dma2IO.outPortByte(DMA2_MASK_REG, dmanr & 3);
        }
    }

    /**
     * Disable the given channel
     *
     * @param dmanr
     */
    public void disable(int dmanr) {
        if (dmanr <= 3) {
            dma1IO.outPortByte(DMA1_MASK_REG, dmanr | 4);
        } else {
            dma2IO.outPortByte(DMA2_MASK_REG, (dmanr & 3) | 4);
        }
    }

    /**
     * Clear the 'DMA Pointer Flip Flop'.
     * Write 0 for LSB/MSB, 1 for MSB/LSB access.
     * Use this once to initialize the FF to a known state.
     * After that, keep track of it. :-)
     *
     * @param dmanr
     */
    protected final void clearFF(int dmanr) {
        if (dmanr <= 3) {
            dma1IO.outPortByte(DMA1_CLEAR_FF_REG, 0);
        } else {
            dma2IO.outPortByte(DMA2_CLEAR_FF_REG, 0);
        }
    }

    /**
     * Test the combination of address and length
     *
     * @param dmanr
     * @param address
     * @param length
     * @throws IllegalArgumentException
     */
    protected final void test(int dmanr, Address address, int length)
        throws IllegalArgumentException {
        final int maxLength;
        final int pageMask;
        if (dmanr <= 3) {
            maxLength = 64 * 1024;
            pageMask = 0xff;
        } else {
            maxLength = 128 * 1024;
            if ((length & 2) != 0) {
                throw new IllegalArgumentException("Invalid length-alignment: " + length);
            }
            pageMask = 0xfe;
        }
        if ((length <= 0) || (length > maxLength)) {
            throw new IllegalArgumentException("Invalid length: " + length);
        }

        final int a32 = address.toInt();
        final int pageStart = (a32 >> 16) & pageMask;
        final int pageEnd = ((a32 + length - 1) >> 16) & pageMask;
        if (pageStart != pageEnd) {
            throw new IllegalArgumentException("Invalid address alignment. DMA block cannot cross pages");
        }
    }

    private IOResource claimPorts(final ResourceManager rm, final ResourceOwner owner, final int low, final int length)
        throws ResourceNotFreeException, DMAException {
        try {
            return (IOResource) AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ResourceNotFreeException {
                    return rm.claimIOResource(owner, low, length);
                }
            });
        } catch (ResourceNotFreeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DMAException("Unknown exception", ex);
        }

    }
}
