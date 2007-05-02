/*
 * $Id$
 */
package org.jnode.driver.net.via_rhine;

import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.driver.DriverException;
import org.jnode.driver.Device;
import org.jnode.driver.bus.pci.PCIHeaderType0;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.util.TimeoutException;
import org.jnode.util.NumberUtils;
import org.jnode.system.*;
import org.jnode.naming.InitialNaming;
import javax.naming.NameNotFoundException;

import static org.jnode.net.ethernet.EthernetConstants.*;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.*;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.ParsedArguments;
import java.io.*;
import java.net.URL;

/**
 * @author Levente Sántha
 */
public class ViaRhineCore extends AbstractDeviceCore implements IRQHandler {
    private final int ioBase;
    private final IOResource io;
	private final IRQResource irq;
	private EthernetAddress hwAddress;
    private ViaRhineDriver driver;


    /*
    char devname[8];		// Used only for kernel debugging.
    const char *product_name;
    struct rhine_rx_desc *rx_ring;
    struct rhine_tx_desc *tx_ring;
    char *rx_buffs[RX_RING_SIZE];
    char *tx_buffs[TX_RING_SIZE];

    // temporary Rx buffers.

    int chip_id;
    int chip_revision;
    unsigned short ioaddr;
    unsigned int cur_rx, cur_tx;	// The next free and used entries
    unsigned int dirty_rx, dirty_tx;
    // The saved address of a sent-in-place packet/buffer, for skfree().
    struct sk_buff *tx_skbuff[TX_RING_SIZE];
    unsigned char mc_filter[8];	// Current multicast filter.
    char phys[4];		// MII device addresses.
    unsigned int tx_full:1;	// The Tx queue is full.
    unsigned int full_duplex:1;	// Full-duplex operation requested.
    unsigned int default_port:4;	// Last dev->if_port value.
    unsigned int media2:4;	// Secondary monitored media port.
    unsigned int medialock:1;	// Don't sense media type.
    unsigned int mediasense:1;	// Media sensing in progress.

     */

    ViaRhineRxDescriptor[] rx_ring = new ViaRhineRxDescriptor[RX_RING_SIZE];
    ViaRhineTxDescriptor[] tx_ring = new ViaRhineTxDescriptor[TX_RING_SIZE];
    byte[] rx_buffs = new byte[RX_RING_SIZE];
    byte[] tx_buffs = new byte[TX_RING_SIZE];

    int chip_id;
    int chip_revision;
    short ioaddr;
    int cur_rx, cur_tx;	// The next free and used entries
    int dirty_rx, dirty_tx;
    // The saved address of a sent-in-place packet/buffer, for skfree().
    SocketBuffer[] tx_skbuff = new SocketBuffer[TX_RING_SIZE];
    char[] mc_filter = new char[8];	// Current multicast filter.
    char[] phys = new char[4];		// MII device addresses.
    int tx_full =1;	// The Tx queue is full.
    int full_duplex = 1;	// Full-duplex operation requested.
    int default_port = 4;	// Last dev->if_port value.
    int media2 = 4;	// Secondary monitored media port.
    int medialock = 1;	// Don't sense media type.
    int mediasense = 1;	// Media sensing in progress.

    RxRing rxRing;
    TxRing txRing;

    public ViaRhineCore(ViaRhineDriver driver, Device device, ResourceOwner owner, Flags flags)
            throws DriverException, ResourceNotFreeException{
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
				+ ", MAC Address = "+ hwAddress);
    }

    protected PCIBaseAddress getIOBaseAddress(Device device, Flags flags)
	throws DriverException {
		final PCIHeaderType0 config = ((PCIDevice)device).getConfig().asHeaderType0();
		final PCIBaseAddress[] addrs = config.getBaseAddresses();
		if (addrs.length < 1) {
			throw new DriverException("Cannot find iobase: not base addresses");
		}
		if (!addrs[0].isIOSpace()) {
			throw new DriverException("Cannot find iobase: first address is not I/O");
		}
		return addrs[0];
	}

    /**
	 * Gets the IRQ used by the given device
	 * @param device
	 * @param flags
	 */
	protected int getIRQ(Device device, Flags flags) throws DriverException {
        final PCIHeaderType0 config = ((PCIDevice)device).getConfig().asHeaderType0();
		return config.getInterruptLine();
	}

    public void handleInterrupt(int irq) {
        log.debug("handleInterrupt()");

        printIntrStatus();
        setIRQEnabled(false);

        int intr_status = getIntrStatus();
        if((intr_status & (IntrRxDone | IntrRxNoBuf | IntrRxOverflow |
                IntrRxDropped | IntrRxEarly | IntrRxEmpty | IntrRxErr | IntrRxWakeUp)) != 0){
            /* Acknowledge all of the current interrupt sources ASAP. */
            //outw(DEFAULT_INTR & ~IntrRxDone, nic->ioaddr + IntrStatus);
            //IOSYNC;
            try {

                Thread.sleep(50);
                if(!rxRing.currentDesc().isOwnBit()){
                    SocketBuffer packet = rxRing.getPacket();
                    driver.onReceive(packet);
                    log.debug("New packet");
                    log.debug(packet.getLinkLayerHeader().getSourceAddress());
                    log.debug(packet.getLinkLayerHeader().getDestinationAddress());
                    log.debug("\n" + hexDump(packet.toByteArray()) + "\n");
                    rxRing.currentDesc().setOwnBit();
                    rxRing.next();
                }

            } catch(Exception e ){
                log.error("error in irq handler", e);
            }
            //setReg16(IntrStatus, DEFAULT_INTR & ~IntrRxDone);
            setReg16(IntrStatus, DEFAULT_INTR);
        }

        if((intr_status & (IntrTxDone | IntrTxAborted | IntrTxDescRace |
                IntrTxError | IntrTxErrSummary | IntrTxUnderrun)) != 0) {
            try {

                if((intr_status & IntrTxError) != 0){
                    reset();
                    return;
                }

                Thread.sleep(50);
            } catch(Exception e ){
                log.error("error in irq handler", e);
            }

            setReg16(IntrStatus, DEFAULT_INTR  | my_INTR);

        }


        setIRQEnabled(true);
    }

    int my_INTR = IntrTxDone | IntrTxError | IntrTxUnderrun;

    private void printIntrStatus(){
        /*
        int IntrRxDone=0x0001, IntrRxErr=0x0004, IntrRxEmpty=0x0020,
                    IntrTxDone=0x0002, IntrTxError=0x0008, IntrTxUnderrun=0x0210,
                    IntrPCIErr=0x0040,
                    IntrStatsMax=0x0080, IntrRxEarly=0x0100,
                    IntrRxOverflow=0x0400, IntrRxDropped=0x0800, IntrRxNoBuf=0x1000,
                    IntrTxAborted=0x2000, IntrLinkChange=0x4000,
                    IntrRxWakeUp=0x8000,
                    IntrNormalSummary=0x0003, IntrAbnormalSummary=0xC260,
                    IntrTxDescRace=0x080000,        // mapped from IntrStatus2
                    IntrTxErrSummary=0x082218;
        */

        int intr_status = getIntrStatus();

        log.debug("Interrupt status word: 0x" + NumberUtils.hex(intr_status));

        if((intr_status & IntrRxDone) != 0)
            log.debug("Interrupt status: " + "IntrRxDone");

        if((intr_status & IntrRxErr) != 0)
            log.debug("Interrupt status: " + "IntrRxErr");

        if((intr_status & IntrRxEmpty) != 0)
                    log.debug("Interrupt status: " + "IntrRxEmpty");

        if((intr_status & IntrTxDone) != 0)
                    log.debug("Interrupt status: " + "IntrTxDone");

        if((intr_status & IntrTxError) != 0)
                    log.debug("Interrupt status: " + "IntrTxError");

        if((intr_status & IntrTxUnderrun) != 0)
                    log.debug("Interrupt status: " + "IntrTxUnderrun");

        if((intr_status & IntrPCIErr) != 0)
                    log.debug("Interrupt status: " + "IntrPCIErr");

        if((intr_status & IntrStatsMax) != 0)
                    log.debug("Interrupt status: " + "IntrStatsMax");

        if((intr_status & IntrRxEarly) != 0)
                    log.debug("Interrupt status: " + "IntrRxEarly");

        if((intr_status & IntrRxOverflow) != 0)
                    log.debug("Interrupt status: " + "IntrRxOverflow");

        if((intr_status & IntrRxDropped) != 0)
                    log.debug("Interrupt status: " + "IntrRxDropped");

        if((intr_status & IntrRxNoBuf) != 0)
                    log.debug("Interrupt status: " + "IntrRxNoBuf");

        if((intr_status & IntrTxAborted) != 0)
                    log.debug("Interrupt status: " + "IntrTxAborted");

        if((intr_status & IntrLinkChange) != 0)
                    log.debug("Interrupt status: " + "IntrLinkChange");

        if((intr_status & IntrRxWakeUp) != 0)
                    log.debug("Interrupt status: " + "IntrRxWakeUp");

        if((intr_status & IntrTxDescRace) != 0)
                    log.debug("Interrupt status: " + "IntrTxDescRace");

    }

    private void setIRQEnabled(boolean enable){
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
        log.debug("getHwAddress");
        return hwAddress;
    }

    public void initialize() throws DriverException {
        log.debug("initialize()");
        probe();
        reset();
    }

    public void disable() {
        log.debug("disable()");
        /* merge reset and disable */
        //rhine_reset(nic);
        reset();

        /* Switch to loopback mode to avoid hardware races. */
        //writeb(0x60 | 0x01, byTCR);
        setReg8(byTCR, 0x60 | 0x01);

        /* Stop the chip's Tx and Rx processes. */
        //writew(CR_STOP, byCR0);
        setReg16(byCR0, CR_STOP);
    }

    private void reset(){
        /* software reset */
        setReg8(byCR1, CR1_SFRST);
        MIIDelay();

        //init ring
        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            rxRing = new RxRing(rm);
            log.debug("Rx ring initialised");
            txRing = new TxRing(rm);
            log.debug("Tx ring initialised");
        } catch (NameNotFoundException ex) {
            throw new RuntimeException("Cannot find ResourceManager");
        }
        
        /*write TD RD Descriptor to MAC */
        setReg32(dwCurrentRxDescAddr, rxRing.ringAddr);
        setReg32(dwCurrentTxDescAddr, txRing.ringAddr);

        /* close IMR */
        setReg16 (byIMR0, 0x0000);

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
        int FDXFlag = queryAuto ();
        if (FDXFlag == 1) {
            setReg8(byCFGD, CFGD_CFDX);
            setReg16(byCR0, CR_FDX);
        }

        /* KICK NIC to WORK */
        //CRbak = inw (byCR0);
        //CRbak = CRbak & 0xFFFB;	/* not CR_STOP */
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

    private void setRxMode(){
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

    private void reloadEEPROM()
    {
        //int i;
        //outb(0x20, byEECSR);
        setReg8(byEECSR, 0x20);

        /* Typically 2 cycles to reload. */
        //for (i = 0; i < 150; i++)
        //    if (! (inb(byEECSR) & 0x20))
        //        break;

        for (int i = 0; i < 150; i++)
            if ( (getReg8(byEECSR) & 0x20) == 0)
                break;
    }


    void initRing () {
        int i;
        tx_full = 0;
        cur_rx = cur_tx = 0;
        dirty_rx = dirty_tx = 0;

        for (i = 0; i < RX_RING_SIZE; i++) {

            rx_ring[i].rxStatus_bits.own_bit = 1;
            rx_ring[i].rxControl_bits.rx_buf_size = 1536;

            //--rx_ring[i].buf_addr_1 = virt_to_bus (tp->rx_buffs[i]);
            //--rx_ring[i].buf_addr_2 = virt_to_bus (&tp->rx_ring[i + 1]);

            /* printf("[%d]buf1=%hX,buf2=%hX",i,tp->rx_ring[i].buf_addr_1,tp->rx_ring[i].buf_addr_2); */
        }
        /* Mark the last entry as wrapping the ring. */
        /* tp->rx_ring[i-1].rx_ctrl.bits.rx_buf_size =1518; */

        //--rx_ring[i - 1].buf_addr_2 = virt_to_bus (&tp->rx_ring[0]);

        /*printf("[%d]buf1=%hX,buf2=%hX",i-1,tp->rx_ring[i-1].buf_addr_1,tp->rx_ring[i-1].buf_addr_2); */

        /* The Tx buffer descriptor is filled in as needed, but we
           do need to clear the ownership bit. */

        for (i = 0; i < TX_RING_SIZE; i++) {

        tx_ring[i].txStatus_lw = 0;
        tx_ring[i].txControl_lw = 0x00e08000;
        //--tx_ring[i].buf_addr_1 = virt_to_bus (tp->tx_buffs[i]);
        //--tx_ring[i].buf_addr_2 = virt_to_bus (&tp->tx_ring[i + 1]);
        /* printf("[%d]buf1=%hX,buf2=%hX",i,tp->tx_ring[i].buf_addr_1,tp->tx_ring[i].buf_addr_2); */
        }

        //--tx_ring[i - 1].buf_addr_2 = virt_to_bus (&tp->tx_ring[0]);
        /* printf("[%d]buf1=%hX,buf2=%hX",i,tp->tx_ring[i-1].buf_addr_1,tp->tx_ring[i-1].buf_addr_2); */
    }

    private int queryAuto () {
        int byMIIIndex;
        int MIIReturn;

        int advertising,mii_reg5;
        int negociated;

        byMIIIndex = 0x04;
        MIIReturn = ReadMII (byMIIIndex);
        advertising=MIIReturn;

        byMIIIndex = 0x05;
        MIIReturn = ReadMII (byMIIIndex);
        mii_reg5=MIIReturn;

        negociated=mii_reg5 & advertising;

        if ( (negociated & 0x100) != 0 || (negociated & 0x1C0) == 0x40 )
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
        setReg8 (byMIICR, byMIICRbak & 0x7f);
        MIIDelay ();

        setReg8(byMIIAD, byMIIIndex);
        MIIDelay ();

        setReg8(byMIICR, getReg8(byMIICR) | 0x40);

        byMIItemp = getReg8(byMIICR);
        byMIItemp = byMIItemp & 0x40;

        while (byMIItemp != 0) {
            byMIItemp = getReg8(byMIICR);
            byMIItemp = byMIItemp & 0x40;
        }
        MIIDelay ();

        ReturnMII = getReg16(wMIIDATA);

        setReg8(byMIIAD, byMIIAdrbak);
        setReg8(byMIICR, byMIICRbak);
        MIIDelay ();

        return (ReturnMII);
    }

    void WriteMII (int byMIISetByte, int byMIISetBit, int byMIIOP) {
        int ReadMIItmp;
        int MIIMask;
        int byMIIAdrbak;
        int byMIICRbak;
        int byMIItemp;


        byMIIAdrbak = getReg8(byMIIAD);

        byMIICRbak = getReg8(byMIICR);
        setReg8(byMIICR, byMIICRbak & 0x7f);
        MIIDelay ();
        setReg8(byMIIAD, byMIISetByte);
        MIIDelay ();

        setReg8(byMIICR, getReg8(byMIICR) | 0x40);

        byMIItemp = getReg8(byMIICR);
        byMIItemp = byMIItemp & 0x40;

        while (byMIItemp != 0) {
            byMIItemp = getReg8(byMIICR);
            byMIItemp = byMIItemp & 0x40;
        }
        MIIDelay ();

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
        MIIDelay ();

        setReg8(byMIICR, getReg8(byMIICR) | 0x20);
        byMIItemp = getReg8(byMIICR);
        byMIItemp = byMIItemp & 0x20;

        while (byMIItemp != 0) {
            byMIItemp = getReg8(byMIICR);
            byMIItemp = byMIItemp & 0x20;
        }

        MIIDelay ();

        setReg8(byMIIAD, byMIIAdrbak & 0x7f);
        setReg8(byMIICR, byMIICRbak);
        MIIDelay ();

    }

    private void MIIDelay (){
        for (int i = 0; i < 0x7fff; i++) {
            getReg8(0x61);
            getReg8(0x61);
            getReg8(0x61);
            getReg8(0x61);
        }
    }

    void probe() {
        int options = -1;
        int did_version = 0;	/* Already printed version info. */
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
        WriteMII (0, 9, 1);
        log.info ("Analyzing Media type,this will take several seconds........");
        for (i = 0; i < 5; i++) {

            /* need to wait 1 millisecond - we will round it up to 50-100ms */
            try {
                Thread.sleep(70);
            } catch(InterruptedException x){
                //ignore
            }

            if ((ReadMII(1) & 0x0020) != 0)
                break;
        }
        log.info("OK\n");

        /*
    #if	0
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
        if (LineSpeed != 0){						//JJM
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
        WriteMII (17, 1, 1);

        /* turn on MII link change */
        MIICRbak = getReg8(byMIICR);
        setReg8(byMIICR, MIICRbak & 0x7F);
        MIIDelay ();
        setReg8(byMIIAD, 0x41);
        MIIDelay ();

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

    public void transmit(SocketBuffer buf, HardwareAddress destination, long timeout) throws InterruptedException, TimeoutException {
        log.debug("transmit(): to " + destination);
//        destination.writeTo(buf, 0);
//        hwAddress.writeTo(buf, 6);        
        txRing.currentDesc().setOwnBit();
        txRing.currentDesc().setFrameLength(buf.getSize());
        txRing.currentDesc().setPacket(buf);
        log.debug("\n" + hexDump(buf.toByteArray()) + "\n");

        int CR1bak = getReg8(byCR1);

        CR1bak = CR1bak | CR1_TDMD1;
        setReg8(byCR1, CR1bak);

//        do {

            int i = 0;
            while(txRing.currentDesc().isOwnBit()) {
                try{
                    Thread.sleep(10);
                } catch(InterruptedException x){
                    //
                }
                if(i++ > 5) break;
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



    String hexDump(byte[] data){
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
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
