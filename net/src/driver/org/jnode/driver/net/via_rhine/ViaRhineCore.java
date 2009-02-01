/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.driver.net.via_rhine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.naming.NameNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIHeaderType0;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.CFGD_CFDX;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.CR1_SFRST;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.CR1_TDMD1;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.CR_DPOLL;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.CR_FDX;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.CR_RXON;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.CR_STOP;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.CR_STRT;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.CR_TXON;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.DEFAULT_INTR;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IMRShadow;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrEnable;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrLinkChange;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrPCIErr;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrRxDone;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrRxDropped;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrRxEarly;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrRxEmpty;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrRxErr;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrRxNoBuf;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrRxOverflow;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrRxWakeUp;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrStatsMax;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrStatus;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrStatus2;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrTxAborted;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrTxDescRace;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrTxDone;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrTxErrSummary;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrTxError;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.IntrTxUnderrun;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.MIISR_SPEED;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.RX_RING_SIZE;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.TX_RING_SIZE;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byBCR0;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byBCR1;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byCFGD;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byCR0;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byCR1;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byEECSR;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byIMR0;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byMAR0;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byMAR4;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byMIIAD;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byMIICR;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byPAR0;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byRCR;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.byTCR;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.dwCurrentRxDescAddr;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.dwCurrentTxDescAddr;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.wMIIDATA;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import static org.jnode.net.ethernet.EthernetConstants.ETH_ALEN;
import org.jnode.system.IOResource;
import org.jnode.system.IRQHandler;
import org.jnode.system.IRQResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author Levente S\u00e1ntha
 */
class ViaRhineCore extends AbstractDeviceCore implements IRQHandler {
    private final int ioBase;
    private final IOResource io;
    private final IRQResource irq;
    private EthernetAddress hwAddress;
    private ViaRhineDriver driver;
    private ViaRhineRxRing rxRing;
    private ViaRhineTxRing txRing;

    /*
   // temporary Rx buffers.

   int chip_id;
   int chip_revision;

   unsigned int dirty_rx, dirty_tx;
   // The saved address of a sent-in-place packet/buffer, for skfree().
   struct sk_buff *tx_skbuff[TX_RING_SIZE];
   unsigned char mc_filter[8];  // Current multicast filter.
   char phys[4];        // MII device addresses.

    */

    //ViaRhineRxDescriptor[] rx_ring = new ViaRhineRxDescriptor[RX_RING_SIZE];
    //ViaRhineTxDescriptor[] tx_ring = new ViaRhineTxDescriptor[TX_RING_SIZE];
    byte[] rx_buffs = new byte[RX_RING_SIZE];
    byte[] tx_buffs = new byte[TX_RING_SIZE];

    int chip_id;
    int chip_revision;
    short ioaddr;
    int cur_rx, cur_tx;    // The next free and used entries
    int dirty_rx, dirty_tx;
    // The saved address of a sent-in-place packet/buffer, for skfree().
    SocketBuffer[] tx_skbuff = new SocketBuffer[TX_RING_SIZE];
    char[] mc_filter = new char[8];    // Current multicast filter.
    char[] phys = new char[4];        // MII device addresses.
    int tx_full = 1;    // The Tx queue is full.
    int full_duplex = 1;    // Full-duplex operation requested.
    int default_port = 4;    // Last dev->if_port value.
    int media2 = 4;    // Secondary monitored media port.
    int medialock = 1;    // Don't sense media type.
    int mediasense = 1;    // Media sensing in progress.

    public ViaRhineCore(ViaRhineDriver driver, Device device, ResourceOwner owner, Flags flags)
        throws DriverException, ResourceNotFreeException {
        this.driver = driver;
        final int irq_nr = getIRQ(device, flags);
        PCIBaseAddress addr = getIOBaseAddress(device, flags);
        this.ioBase = addr.getIOBase();
        int io_length = addr.getSize();
        final ResourceManager rm;

        try {
            rm = InitialNaming.lookup(ResourceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find ResourceManager");
        }

        this.irq = rm.claimIRQ(owner, irq_nr, this, true);

        try {
            io = rm.claimIOResource(owner, ioBase, io_length);
        } catch (ResourceNotFreeException ex) {
            this.irq.release();
            throw ex;
        }

        final byte[] hwAddrArr = new byte[ETH_ALEN];
        for (int i = 0; i < ETH_ALEN; i++)
            hwAddrArr[i] = (byte) getReg8(byPAR0 + i);

        this.hwAddress = new EthernetAddress(hwAddrArr, 0);

        log.debug("Found " + flags.getName() + " IRQ = " + irq.getIRQ()
            + ", IO Base = 0x" + NumberUtils.hex(ioBase)
            + ", IO Length = " + io_length
            + ", MAC Address = " + hwAddress);
    }

    protected PCIBaseAddress getIOBaseAddress(Device device, Flags flags)
        throws DriverException {
        final PCIHeaderType0 config = ((PCIDevice) device).getConfig().asHeaderType0();
        final PCIBaseAddress[] addrs = config.getBaseAddresses();
        if (addrs.length < 1) {
            throw new DriverException("Cannot find iobase: no base address");
        }
        if (!addrs[0].isIOSpace()) {
            throw new DriverException("Cannot find iobase: first address is not I/O");
        }
        return addrs[0];
    }

    /**
     * Gets the IRQ used by the given device
     *
     * @param device
     * @param flags
     */
    protected int getIRQ(Device device, Flags flags) throws DriverException {
        final PCIHeaderType0 config = ((PCIDevice) device).getConfig().asHeaderType0();
        return config.getInterruptLine();
    }

    public void handleInterrupt(int irq) {
        log.debug("handleInterrupt()");

        printIntrStatus();
        setIRQEnabled(false);

        int intr_status = getIntrStatus();
        if ((intr_status & (IntrRxDone | IntrRxNoBuf | IntrRxOverflow |
            IntrRxDropped | IntrRxEarly | IntrRxEmpty | IntrRxErr | IntrRxWakeUp)) != 0) {
            /* Acknowledge all of the current interrupt sources ASAP. */
            //outw(DEFAULT_INTR & ~IntrRxDone, nic->ioaddr + IntrStatus);
            //IOSYNC;
            try {

                Thread.sleep(50);
                if (!rxRing.currentDesc().isOwnBit()) {
                    SocketBuffer packet = rxRing.currentDesc().getPacket();
                    driver.onReceive(packet);
                    log.debug("New packet");
                    log.debug(packet.getLinkLayerHeader().getSourceAddress());
                    log.debug(packet.getLinkLayerHeader().getDestinationAddress());
                    log.debug("\n" + hexDump(packet.toByteArray()) + "\n");
                    rxRing.currentDesc().setOwnBit();
                    rxRing.next();
                }

            } catch (Exception e) {
                log.error("error in irq handler", e);
            }
            //setReg16(IntrStatus, DEFAULT_INTR & ~IntrRxDone);
            setReg16(IntrStatus, DEFAULT_INTR);
        }

        if ((intr_status & (IntrTxDone | IntrTxAborted | IntrTxDescRace |
            IntrTxError | IntrTxErrSummary | IntrTxUnderrun)) != 0) {
            try {

                if ((intr_status & IntrTxError) != 0) {
                    reset();
                    return;
                }

                Thread.sleep(50);
            } catch (Exception e) {
                log.error("error in irq handler", e);
            }

            setReg16(IntrStatus, DEFAULT_INTR | my_INTR);

        }


        setIRQEnabled(true);
    }

    private static final int my_INTR = IntrTxDone | IntrTxError | IntrTxUnderrun;

    private void printIntrStatus() {
        int intr_status = getIntrStatus();

        log.debug("Interrupt status word: 0x" + NumberUtils.hex(intr_status));

        if ((intr_status & IntrRxDone) != 0)
            log.debug("Interrupt status: " + "IntrRxDone");

        if ((intr_status & IntrRxErr) != 0)
            log.debug("Interrupt status: " + "IntrRxErr");

        if ((intr_status & IntrRxEmpty) != 0)
            log.debug("Interrupt status: " + "IntrRxEmpty");

        if ((intr_status & IntrTxDone) != 0)
            log.debug("Interrupt status: " + "IntrTxDone");

        if ((intr_status & IntrTxError) != 0)
            log.debug("Interrupt status: " + "IntrTxError");

        if ((intr_status & IntrTxUnderrun) != 0)
            log.debug("Interrupt status: " + "IntrTxUnderrun");

        if ((intr_status & IntrPCIErr) != 0)
            log.debug("Interrupt status: " + "IntrPCIErr");

        if ((intr_status & IntrStatsMax) != 0)
            log.debug("Interrupt status: " + "IntrStatsMax");

        if ((intr_status & IntrRxEarly) != 0)
            log.debug("Interrupt status: " + "IntrRxEarly");

        if ((intr_status & IntrRxOverflow) != 0)
            log.debug("Interrupt status: " + "IntrRxOverflow");

        if ((intr_status & IntrRxDropped) != 0)
            log.debug("Interrupt status: " + "IntrRxDropped");

        if ((intr_status & IntrRxNoBuf) != 0)
            log.debug("Interrupt status: " + "IntrRxNoBuf");

        if ((intr_status & IntrTxAborted) != 0)
            log.debug("Interrupt status: " + "IntrTxAborted");

        if ((intr_status & IntrLinkChange) != 0)
            log.debug("Interrupt status: " + "IntrLinkChange");

        if ((intr_status & IntrRxWakeUp) != 0)
            log.debug("Interrupt status: " + "IntrRxWakeUp");

        if ((intr_status & IntrTxDescRace) != 0)
            log.debug("Interrupt status: " + "IntrTxDescRace");

    }

    private void setIRQEnabled(boolean enable) {
        int intr_status = getIntrStatus();

        if (enable)
            intr_status = intr_status | DEFAULT_INTR | my_INTR;
        else
            intr_status = (intr_status & ~(DEFAULT_INTR | my_INTR));

        setReg16(IntrEnable, intr_status);
    }

    private int getIntrStatus() {
        int intr_status = getReg16(IntrStatus);
        /* On Rhine-II, Bit 3 indicates Tx descriptor write-back race. */

        /* added comment by guard */
        /* For supporting VT6107, please use revision id to recognize different chips in driver */
        // if (tp->chip_id == 0x3065)

        //if( tp->chip_revision < 0x80 && tp->chip_revision >=0x40 )
        intr_status |= getReg8(IntrStatus2) << 16;

        return intr_status;
    }

    public HardwareAddress getHwAddress() {
        return hwAddress;
    }

    public void initialize() throws DriverException {
        log.debug("initialize()");
        probe();
        reset();
    }

    public void disable() {
        log.debug("disable()");
        // merge reset and disable
        reset();

        // Switch to loopback mode to avoid hardware races.
        setReg8(byTCR, 0x60 | 0x01);

        // Stop the chip's Tx and Rx processes.
        setReg16(byCR0, CR_STOP);
    }

    private void reset() {
        /* software reset */
        setReg8(byCR1, CR1_SFRST);
        MIIDelay();

        //init ring
        initRing();

        /*write TD RD Descriptor to MAC */
        setReg32(dwCurrentRxDescAddr, rxRing.ringAddr);
        setReg32(dwCurrentTxDescAddr, txRing.ringAddr);

        /* close IMR */
        setReg16(byIMR0, 0x0000);

        /* Setup Multicast */
        //set_rx_mode(nic);
        setRxMode();

        /* set TCR RCR threshold to store and forward*/
        //outb (0x3e, byBCR0);
        //outb (0x38, byBCR1);
        //outb (0x2c, byRCR);
        //outb (0x60, byTCR);
        setReg8(byBCR0, 0x3e);
        setReg8(byBCR1, 0x38);
        setReg8(byRCR, 0x2c);
        setReg8(byTCR, 0x60);

        /* Set Fulldupex */
        int FDXFlag = queryAuto();
        if (FDXFlag == 1) {
            setReg8(byCFGD, CFGD_CFDX);
            setReg16(byCR0, CR_FDX);
        }

        /* KICK NIC to WORK */
        //CRbak = inw (byCR0);
        //CRbak = CRbak & 0xFFFB;   /* not CR_STOP */
        //outw ((CRbak | CR_STRT | CR_TXON | CR_RXON | CR_DPOLL), byCR0);
        int cr = getReg8(byCR0);
        cr = cr & 0xFFFB;
        setReg16(byCR0, cr | CR_STRT | CR_TXON | CR_RXON | CR_DPOLL);

        /* disable all known interrupt */
        //outw (0, byIMR0);
        //setReg16(byIMR0, 0);

        //--------------------
        //outw (IMRShadow, byIMR0);
        setReg16(byIMR0, IMRShadow);
    }

    private void setRxMode() {
        /* ! IFF_PROMISC */
        //outl(0xffffffff, byMAR0);
        //outl(0xffffffff, byMAR4);
        //rx_mode = 0x0C;
        //outb(0x60 /* thresh */ | rx_mode, byRCR );
        setReg32(byMAR0, 0xffffffff);
        setReg32(byMAR4, 0xffffffff);
        int rx_mode = 0x0C;
        setReg8(byRCR, 0x60 | rx_mode);
    }

    private void reloadEEPROM() {
        setReg8(byEECSR, 0x20);
        /* Typically 2 cycles to reload. */
        for (int i = 0; i < 150; i++)
            if ((getReg8(byEECSR) & 0x20) == 0)
                break;
    }

    void initRing() {
        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            rxRing = new ViaRhineRxRing(rm);
            log.debug("Rx ring initialised");
            txRing = new ViaRhineTxRing(rm);
            log.debug("Tx ring initialised");
        } catch (NameNotFoundException ex) {
            throw new RuntimeException("Cannot find ResourceManager");
        }
    }

    private int queryAuto() {
        int byMIIIndex;
        int MIIReturn;

        int advertising, mii_reg5;
        int negociated;

        byMIIIndex = 0x04;
        MIIReturn = ReadMII(byMIIIndex);
        advertising = MIIReturn;

        byMIIIndex = 0x05;
        MIIReturn = ReadMII(byMIIIndex);
        mii_reg5 = MIIReturn;

        negociated = mii_reg5 & advertising;

        if ((negociated & 0x100) != 0 || (negociated & 0x1C0) == 0x40)
            return 1;
        else
            return 0;

    }

    private int ReadMII(int byMIIIndex) {
        int ReturnMII;
        int byMIIAdrbak;
        int byMIICRbak;
        int byMIItemp;

        byMIIAdrbak = getReg8(byMIIAD);
        byMIICRbak = getReg8(byMIICR);
        setReg8(byMIICR, byMIICRbak & 0x7f);
        MIIDelay();

        setReg8(byMIIAD, byMIIIndex);
        MIIDelay();

        setReg8(byMIICR, getReg8(byMIICR) | 0x40);

        byMIItemp = getReg8(byMIICR);
        byMIItemp = byMIItemp & 0x40;

        while (byMIItemp != 0) {
            byMIItemp = getReg8(byMIICR);
            byMIItemp = byMIItemp & 0x40;
        }
        MIIDelay();

        ReturnMII = getReg16(wMIIDATA);

        setReg8(byMIIAD, byMIIAdrbak);
        setReg8(byMIICR, byMIICRbak);
        MIIDelay();

        return (ReturnMII);
    }

    void WriteMII(int byMIISetByte, int byMIISetBit, int byMIIOP) {
        int ReadMIItmp;
        int MIIMask;
        int byMIIAdrbak;
        int byMIICRbak;
        int byMIItemp;


        byMIIAdrbak = getReg8(byMIIAD);

        byMIICRbak = getReg8(byMIICR);
        setReg8(byMIICR, byMIICRbak & 0x7f);
        MIIDelay();
        setReg8(byMIIAD, byMIISetByte);
        MIIDelay();

        setReg8(byMIICR, getReg8(byMIICR) | 0x40);

        byMIItemp = getReg8(byMIICR);
        byMIItemp = byMIItemp & 0x40;

        while (byMIItemp != 0) {
            byMIItemp = getReg8(byMIICR);
            byMIItemp = byMIItemp & 0x40;
        }
        MIIDelay();

        ReadMIItmp = getReg16(wMIIDATA);
        MIIMask = 0x0001;
        MIIMask = MIIMask << byMIISetBit;


        if (byMIIOP == 0) {
            MIIMask = ~MIIMask;
            ReadMIItmp = ReadMIItmp & MIIMask;
        } else {
            ReadMIItmp = ReadMIItmp | MIIMask;
        }

        setReg16(wMIIDATA, ReadMIItmp);
        MIIDelay();

        setReg8(byMIICR, getReg8(byMIICR) | 0x20);
        byMIItemp = getReg8(byMIICR);
        byMIItemp = byMIItemp & 0x20;

        while (byMIItemp != 0) {
            byMIItemp = getReg8(byMIICR);
            byMIItemp = byMIItemp & 0x20;
        }

        MIIDelay();

        setReg8(byMIIAD, byMIIAdrbak & 0x7f);
        setReg8(byMIICR, byMIICRbak);
        MIIDelay();

    }

    private void MIIDelay() {
        for (int i = 0; i < 0x7fff; i++) {
            getReg8(0x61);
            getReg8(0x61);
            getReg8(0x61);
            getReg8(0x61);
        }
    }

    void probe() {
        int options = -1;
        int did_version = 0;    /* Already printed version info. */
        int i;
        int timeout;
        int FDXFlag;
        int byMIIvalue, LineSpeed, MIICRbak;

        //if (rhine_debug > 0 && did_version++ == 0)
        //    printf (version);
        reloadEEPROM();
        /* Perhaps this should be read from the EEPROM? */
        //--for (i = 0; i < ETH_ALEN; i++)
        //--nic->node_addr[i] = inb (byPAR0 + i);
        //--printf ("IO address %hX Ethernet Address: %!\n", ioaddr, nic->node_addr);

        /* restart MII auto-negotiation */
        WriteMII(0, 9, 1);
        log.info("Analyzing Media type,this will take several seconds........");
        for (i = 0; i < 5; i++) {

            /* need to wait 1 millisecond - we will round it up to 50-100ms */
            try {
                Thread.sleep(70);
            } catch (InterruptedException x) {
                //ignore
            }

            if ((ReadMII(1) & 0x0020) != 0)
                break;
        }
        log.info("OK\n");

        /*
    #if 0
        //* JJM : for Debug
        printf("MII : Address %hhX ",inb(ioaddr+0x6c));
        {
         unsigned char st1,st2,adv1,adv2,l1,l2;

         st1=ReadMII(1,ioaddr)>>8;
         st2=ReadMII(1,ioaddr)&0xFF;
         adv1=ReadMII(4,ioaddr)>>8;
         adv2=ReadMII(4,ioaddr)&0xFF;
         l1=ReadMII(5,ioaddr)>>8;
         l2=ReadMII(5,ioaddr)&0xFF;
         printf(" status 0x%hhX%hhX, advertising 0x%hhX%hhX, link 0x%hhX%hhX\n", st1,st2,adv1,adv2,l1,l2);
        }
    #endif
        */
        /* query MII to know LineSpeed,duplex mode */
        byMIIvalue = getReg8(0x6d);
        LineSpeed = byMIIvalue & MIISR_SPEED;
        if (LineSpeed != 0) {                        //JJM
            log.info("Linespeed=10Mbs");
        } else {
            log.info("Linespeed=100Mbs");
        }

        FDXFlag = queryAuto();
        if (FDXFlag == 1) {
            log.info(" Fullduplex\n");
            setReg16(byCR0, CR_FDX);
        } else {
            log.info(" Halfduplex\n");
        }

        /* set MII 10 FULL ON */
        WriteMII(17, 1, 1);

        /* turn on MII link change */
        MIICRbak = getReg8(byMIICR);
        setReg8(byMIICR, MIICRbak & 0x7F);
        MIIDelay();
        setReg8(byMIIAD, 0x41);
        MIIDelay();

        /* while((inb(byMIIAD)&0x20)==0) ; */
        setReg8(byMIICR, MIICRbak | 0x80);

        /* The lower four bits are the media type. */
        if (options > 0) {
            full_duplex = (options & 16) != 0 ? 1 : 0;
            default_port = options & 15;
            if (default_port != 0)
                medialock = 1;
        }
    }

    public void release() {
        log.debug("release()");
        io.release();
        log.debug("irq.release");
        irq.release();
        log.debug("end of release");
    }

    public void transmit(SocketBuffer buf, HardwareAddress destination, long timeout)
        throws InterruptedException, TimeoutException {
        log.debug("transmit(): to " + destination);
//        destination.writeTo(buf, 0);
//        hwAddress.writeTo(buf, 6);        
        txRing.currentDesc().setOwnBit();
        txRing.currentDesc().setPacket(buf);
        log.debug("\n" + hexDump(buf.toByteArray()) + "\n");

        int CR1bak = getReg8(byCR1);

        CR1bak = CR1bak | CR1_TDMD1;
        setReg8(byCR1, CR1bak);

//        do {

        int i = 0;
        while (txRing.currentDesc().isOwnBit()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException x) {
                //
            }
            if (i++ > 5) break;
        }

//            if(tp->tx_ring[entry].tx_status.bits.terr == 0)
//                break;

//            if(tp->tx_ring[entry].tx_status.bits.abt == 1)
//            {
//                // turn on TX
//                int CR0bak = getReg8(byCR0);
//                CR0bak = CR0bak | CR_TXON;
//                setReg8(byCR0, CR0bak);
//            }
        //      } while(true);
        txRing.next();
    }

    private int getReg8(int reg) {
        return io.inPortByte(ioBase + reg);
    }

    private int getReg16(int reg) {
        return io.inPortWord(ioBase + reg);
    }

    private int getReg32(int reg) {
        return io.inPortDword(ioBase + reg);
    }

    private void setReg8(int reg, int value) {
        io.outPortByte(ioBase + reg, value);
    }

    private void setReg16(int reg, int value) {
        io.outPortWord(ioBase + reg, value);
    }

    private void setReg32(int reg, int value) {
        io.outPortDword(ioBase + reg, value);
    }


    String hexDump(byte[] data) {
        try {
            InputStream is = new ByteArrayInputStream(data);
            StringWriter swriter = new StringWriter();
            PrintWriter out = new PrintWriter(swriter);

            final int rowlen = 16;
            int prt = 0;
            int len;

            final byte[] buf = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while ((len = is.read(buf)) > 0) {
                int left = len;
                int ofs = 0;
                //
                while (left > 0) {
                    sb.setLength(0);
                    int sz = Math.min(rowlen, left);

                    sb.append(NumberUtils.hex(prt, 8)).append("  ");

                    for (int i = 0; i < rowlen; i++) {
                        if (ofs + i < len)
                            sb.append(NumberUtils.hex(buf[ofs + i], 2));
                        else
                            sb.append("  ");
                        if ((i + 1) < rowlen)
                            sb.append(" ");
                        if ((i + 1) == rowlen / 2)
                            sb.append(" ");
                    }

                    sb.append("  |");

                    for (int i = 0; i < rowlen; i++) {
                        if (ofs + i < len) {
                            char c = (char) buf[ofs + i];
                            if ((c >= ' ') && (c < (char) 0x7f))
                                sb.append(c);
                            else
                                sb.append(".");
                        } else
                            sb.append(" ");
                    }

                    sb.append("|");

                    left -= sz;
                    ofs += sz;
                    prt += sz;

                    out.println(sb.toString());
                    out.flush();
                }
            }

            out.flush();
            out.close();
            is.close();


            return swriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
