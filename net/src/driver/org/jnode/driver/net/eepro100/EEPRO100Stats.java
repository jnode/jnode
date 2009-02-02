/*
 * $Id$
 *
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
 
package org.jnode.driver.net.eepro100;

import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.vmmagic.unboxed.Address;

/**
 * @author flesire
 */
public class EEPRO100Stats implements EEPRO100Constants {
    /**
     * statistic block size (60,72,78) + dword for status information
     */
    public static final int STATS_BLOCK_SIZE = 60 + 8;

    private EEPRO100Registers regs;

    private byte[] stats;
    private MemoryResource mem;
    private Address memAddr;

    /**
     * Statistical counters
     */
    private int tx_aborted_errors;
    private int tx_window_errors;
    private int tx_fifo_errors;
    private int collisions;
    private int rx_crc_errors;
    private int rx_frame_errors;
    private int rx_over_errors;
    private int rx_fifo_errors;
    private int rx_length_errors;

    public EEPRO100Stats(ResourceManager rm, EEPRO100Registers regs) {
        this.regs = regs;
        this.stats = new byte[STATS_BLOCK_SIZE];
        this.mem = rm.asMemoryResource(this.stats);
        this.memAddr = this.mem.getAddress();
    }

    public void loadBlock() {
        regs.setReg32(SCBPointer, memAddr.toInt());
        mem.setInt(64, 0);
        regs.setReg8(SCBCmd, CUStatsAddr);
    }

    /*
     * The Speedo-3 has an especially awkward and unusable method of getting
     * statistics out of the chip. It takes an unpredictable length of time for
     * the dump-stats command to complete. To avoid a busy-wait loop we update
     * the stats with the previous dump results, and then trigger a new dump.
     * These problems are mitigated by the current /proc implementation, which
     * calls this routine first to judge the output length, and then to emit the
     * output. Oh, and incoming frames are dropped while executing dump-stats!
     */
    /**
     * Gets the stats attribute of the EEPro100 object
     */
    final void getStats() {

        /*
         * Update only if the previous dump finished.
         */
        if (mem.getInt(64) == 0xA007) {
            tx_aborted_errors += mem.getInt(4);
            tx_window_errors += mem.getInt(8);
            tx_fifo_errors += mem.getInt(12);
            tx_fifo_errors += mem.getInt(16);
            /*
             * stats.tx_deferred += le32_to_cpu(lstats.tx_deferred);
             */
            collisions += mem.getInt(32);
            rx_crc_errors += mem.getInt(40);
            rx_frame_errors += mem.getInt(44);
            rx_over_errors += mem.getInt(48);
            rx_fifo_errors += mem.getInt(52);
            rx_length_errors += mem.getInt(60);
            mem.setInt(64, 0);
            EEPRO100Utils.waitForCmdDone(regs);
            regs.setReg8(SCBCmd, CUDumpStats);
        }
    }
}
