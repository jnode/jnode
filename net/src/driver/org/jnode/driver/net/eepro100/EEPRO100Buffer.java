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

import org.apache.log4j.Logger;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.system.ResourceManager;
import org.jnode.util.NumberUtils;

/**
 * @author flesire
 */
public class EEPRO100Buffer implements EEPRO100Constants {
    //--- Constants
    private static final int FRAME_SIZE = EthernetConstants.ETH_FRAME_LEN;
    private static final int PKT_BUF_SZ = 1536;
    private static final int DATA_BUFFER_SIZE = 1536;
    private static final int PacketReceived = 0xc000;

    //---
    protected final Logger log = Logger.getLogger(getClass());
    private ResourceManager rm;
    private EEPRO100Registers regs;
    private EEPRO100RxFD rxPacket;
    private EEPRO100TxFD txFD;

    // --- Rx Variables
    private int rxMode;
    private int curRx;
    private int dirtyRx;
    public EEPRO100RxFD[] rxRing = new EEPRO100RxFD[RX_RING_SIZE];
    private EEPRO100RxFD[] rxPackets = new EEPRO100RxFD[128];
    private int rx_packets;
    private int rxErrors;
    private EEPRO100RxFD last_rxf;
    private int rxPacketIndex;
    private int lastRxTime;

    // --- Tx Variables
    private int txThreshold = 0x01200000;
    private int curTx;
    private int dirtyTx;
    public EEPRO100TxFD[] txRing = new EEPRO100TxFD[TX_RING_SIZE];
    private EEPRO100TxFD lastCmd;
    private int lastCmdTime;

    // --- Others variables
    private int jiffies;

    public EEPRO100Buffer(EEPRO100Registers regs, ResourceManager rm) {
        this.regs = regs;
        this.rm = rm;
        curTx = 0;
        dirtyTx = 0;
    }

    /**
     *
     *
     */
    public final void initSingleRxRing() {
        /* Base = 0 */
        regs.setReg32(SCBPointer, 0);
        regs.setReg16(SCBCmd, SCBMaskAll | RxAddrLoad);
        EEPRO100Utils.waitForCmdDone(regs);
        log.debug("Set RX base addr.");
        rxPacket = new EEPRO100RxFD(rm);
        rxPacket.setStatus(0x0001);
        rxPacket.setCommand(0x0000);
        rxPacket.setLink(rxPacket.getStatus());
        // TODO Set correct value
        rxPacket.setRxBufferAddress(0);
        // ---------------------------------
        rxPacket.setCount(0);
        rxPacket.setSize(1528);
        rxPacket.initialize();
        regs.setReg32(SCBPointer, rxPacket.getBufferAddress());
        regs.setReg16(SCBCmd, SCBMaskAll | RxStart);
        EEPRO100Utils.waitForCmdDone(regs);
        log.debug("Started rx process.");
        // Start the receiver.
        rxPacket.setStatus(0);
        rxPacket.setCommand(0xC000);

        regs.setReg32(SCBPointer, rxPacket.getBufferAddress());
        regs.setReg16(SCBCmd, SCBMaskAll | RxStart);
    }

    /**
     *
     *
     */
    public final void initSingleTxRing() {
        EEPRO100Utils.waitForCmdDone(regs);
        log.debug("Set TX base addr.");
        txFD = new EEPRO100TxFD(rm);
        txFD.setCommand(CmdIASetup);
        txFD.setStatus(0x0000);
        txFD.setCount(0x02208000);
        txFD.setLink(txFD.getFirstDPDAddress().toInt());
        txFD.setDescriptorAddress(txFD.getBufferAddress());
    }

    /**
     * @param buf
     */
    public void transmit(SocketBuffer buf) {
        int status;
        int s1;
        int s2;

        status = regs.getReg16(SCBStatus);
        /* Acknowledge all of the current interrupt sources ASAP. */
        regs.setReg16(SCBStatus, status & IntrAllNormal);

        log.debug("transmitting status = " + NumberUtils.hex(status) + ", cmd=" +
                NumberUtils.hex(regs.getReg16(SCBStatus)) + "\n");

        txFD.setStatus(0);
        txFD.setCommand(CmdSuspend | CmdTx | CmdTxFlex);
        txFD.setLink(txFD.getFirstDPDAddress().toInt());
        txFD.setCount(0x02208000);
        log.debug("Tx FD :\n" + txFD.toString());
        txFD.initialize(buf);
        log.debug("Tx FD 2:\n" + txFD.toString());

        regs.setReg16(SCBPointer, txFD.getBufferAddress());
        regs.setReg16(SCBCmd, SCBMaskAll | CUStart);
        EEPRO100Utils.waitForCmdDone(regs);

        s1 = regs.getReg16(SCBStatus);
        // TODO wait 10 ms for transmiting;
        long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() <= start + 10) && (txFD.getStatus() != 0)) {
            // FIXME ... busy wait!!!!
        }
        s2 = regs.getReg16(SCBStatus);
        log.debug("Tx FD :");
        log.debug(txFD.toString());
        log.debug("s1=" + NumberUtils.hex(s1) + " s2=" + NumberUtils.hex(s2));

    }

    /**
     * @param driver
     */
    public void poll(EEPRO100Driver driver) throws NetworkException {
        log.debug("*** RX packet status : " + rxPacket.getStatus());
        if (rxPacket.getStatus() != 0) {
            // Got a packet, restart the receiver
            rxPacket.setStatus(0);
            rxPacket.setCommand(0xc000);

            regs.setReg16(SCBPointer, rxPacket.getStatus());
            regs.setReg16(SCBCmd, SCBMaskAll | RxStart);
            EEPRO100Utils.waitForCmdDone(regs);

            log.debug("Got a packet: Len=" + NumberUtils.hex(rxPacket.getCount()));

            final SocketBuffer skbuf = rxPacket.getPacket();
            driver.onReceive(skbuf);
        }
    }

    /**
     *
     *
     */
    public void txProcess() {
        // Caution: the write order is important here, set the base address with
        // the "ownership" bits last.

        // Prevent interrupts from changing the Tx ring from underneath us.
        int flags;
        /* Calculate the Tx descriptor entry. */
        int txEntry = getCurTx() & TX_RING_SIZE - 1;
        txRing[txEntry].setStatus(0);
        txRing[txEntry].setCommand(CmdSuspend | CmdTx | CmdTxFlex);

        getNextTx();
        txRing[txEntry].setLink(txRing[getCurTx() & TX_RING_SIZE - 1].getBufferAddress());
        // We may nominally release the lock here.
        txRing[txEntry].setDescriptorAddress(txRing[txEntry].getBufferAddress() + 16);
        // The data region is always in one buffer descriptor.
        txRing[txEntry].setCount(getTxThreshold());
        EEPRO100TxFD lastCmd0 = lastCmd;
        lastCmd = txRing[txEntry];
        EEPRO100Utils.waitForCmdDone(regs);
        regs.setReg8(SCBCmd, CUResume);
    }

    // --- Accessors ---

    /**
     * @return Returns the curRx.
     */
    public int getCurRx() {
        return curRx;
    }

    /**
     * @param curRx The curRx to set.
     */
    public void setCurRx(int curRx) {
        this.curRx = curRx;
    }

    /**
     * @return Returns the curTx.
     */
    public int getCurTx() {
        return curTx;
    }

    /**
     * @return Returns the next Tx.
     */
    public int getNextTx() {
        return curTx++;
    }

    /**
     * @param curTx The curTx to set.
     */
    public void setCurTx(int curTx) {
        this.curTx = curTx;
    }

    /**
     * @return Returns the dirtyRx.
     */
    public int getDirtyRx() {
        return dirtyRx;
    }

    /**
     * @param dirtyRx The dirtyRx to set.
     */
    public void setDirtyRx(int dirtyRx) {
        this.dirtyRx = dirtyRx;
    }

    /**
     * @return Returns the dirtyTx.
     */
    public int getDirtyTx() {
        return dirtyTx;
    }

    /**
     * @param dirtyTx The dirtyTx to set.
     */
    public void setDirtyTx(int dirtyTx) {
        this.dirtyTx = dirtyTx;
    }

    /**
     * @return Returns the txThreshold.
     */
    public int getTxThreshold() {
        return txThreshold;
    }

    /**
     * @param txThreshold The txThreshold to set.
     */
    public void setTxThreshold(int txThreshold) {
        this.txThreshold = txThreshold;
    }

    /**
     * @return Returns the rxMode.
     */
    public int getRxMode() {
        return rxMode;
    }

    /**
     * @param rxMode The rxMode to set.
     */
    public void setRxMode(int rxMode) {
        this.rxMode = rxMode;
    }
}
