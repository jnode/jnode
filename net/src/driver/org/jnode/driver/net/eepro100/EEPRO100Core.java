/*
 * Created on 13-Apr-2004
 *  
 */
package org.jnode.driver.net.eepro100;

import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.jnode.driver.DriverException;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.driver.pci.PCIBaseAddress;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCIDeviceConfig;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.system.IOResource;
import org.jnode.system.IRQHandler;
import org.jnode.system.IRQResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author flesire
 *  
 */
public class EEPRO100Core extends AbstractDeviceCore implements IRQHandler, EEPRO100Constants, EthernetConstants {

    /** Device Driver */
    private final EEPRO100Driver driver;

    /** Start of IO address space */
    private final int iobase;

    /** IO address space resource */
    private final IOResource io;

    /** IRQ resource */
    private final IRQResource irq;

    /** My ethernet address */
    private EthernetAddress hwAddress;

    /** Flags for the specific device found */
    private final EEPRO100Flags flags;

    /** */
    private int eeReadCmd;

    /** */
    private int eeSize;

    /** */
    private int eeAddress;

    /** Enable congestion control in the DP83840. */
    final static boolean congenb = false;

    /**
     * Create a new instance and allocate all resources
     * 
     * @throws ResourceNotFreeException
     */
public EEPRO100Core(EEPRO100Driver driver, ResourceOwner owner, PCIDevice device, Flags flags) throws ResourceNotFreeException, DriverException {
        this.driver = driver;
        this.flags = (EEPRO100Flags) flags;

        final PCIDeviceConfig config = device.getConfig();
        final int irq = config.getInterruptLine();

        final PCIBaseAddress[] addrs = config.getBaseAddresses();
        if (addrs.length < 1) { throw new DriverException("Cannot find iobase: not base addresses"); }
        if (!addrs[1].isIOSpace()) { throw new DriverException("Cannot find iobase: first address is not I/O"); }

        // Get the start of the IO address space
        iobase = addrs[1].getIOBase();
        final int iolength = addrs[1].getSize();
        log.debug("Found Lance IOBase: 0x" + NumberUtils.hex(iobase) + ", length: " + iolength);
        ResourceManager rm;
        try {
            rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find ResourceManager");
        }
        this.irq = rm.claimIRQ(owner, irq, this, true);
        try {
            io = claimPorts(rm, owner, iobase, iolength);
        } catch (ResourceNotFreeException ex) {
            this.irq.release();
            throw ex;
        }

        int i, option = 0;
        int[] eeprom = new int[0x100];

        short sum = 0;
        int j;
        int read_cmd, ee_size;

        sizeEeprom();

        final byte[] hwAddrArr = new byte[ETH_ALEN];

        for (j = 0, i = 0; i < eeSize; i++) {
            int value = doEepromCmd(i) & 0xffff;
            eeprom[i] = value;
            sum += value;
            if (i < 3) {
                hwAddrArr[j++] = (byte) value;
                hwAddrArr[j++] = (byte) (value >> 8);
            }
        }

        if (sum != 0xBABA) {
            log.debug(this.flags.getName() + ": Invalid EEPROM checksum " + Integer.toHexString(sum) + ", check settings before activating this device!");
        }

        this.hwAddress = new EthernetAddress(hwAddrArr, 0);

        /*
         * Reset the chip: stop Tx and Rx processes and clear counters. This
         * takes less than 10usec and will easily finish before the next action.
         */
        setReg32(SCBPort, PortReset);
        eepromDelay();

        log.debug("Found " + flags.getName() + " IRQ=" + irq + ", IOBase=0x" + NumberUtils.hex(iobase) + ", MAC Address=" + hwAddress);

        // We have a cyclone PMC52 card, IQ80310 board
        /*
         * if(((Integer)deviceIdTable[deviceId+2]).intValue() == 0x360113c ||
         * ((Integer)deviceIdTable[deviceId+2]).intValue() == 0x700113c) { //
         * set the phy address; for 82559 this defaults to 1; phy[0] = 1; //
         * read and print out the id registers sb.append("phy id:
         * ").append(Integer.toHexString(mdioRead(phy[0], 2))); sb.append('
         * ').append(Integer.toHexString(mdioRead(phy[0], 3)));
         * System.out.println(sb.toString()); sb.setLength(0); // assuming we
         * are i82555 } else {
         */
        /*
         * OK, this is pure kernel bloat. I don't like it when other drivers
         * waste non-pageable kernel space to emit similar messages, but I need
         * them for bug reports.
         */
        String connectors[] = { " RJ45", " BNC", " AUI", " MII"};
        if ((eeprom[3] & 0x03) != 0) log.info("Receiver lock-up bug exists -- enabling work-around.");
        log.debug("Board assembly " + Integer.toHexString(eeprom[8]) + " " + Integer.toHexString(eeprom[9] >> 8) + "  " + (eeprom[9] & 0xff) + " connectors present: ");
        for (i = 0; i < 4; i++) {
            if ((eeprom[5] & (1 << i)) != 0) log.debug(connectors[i]);
            log.debug("Primary interface chip " + (phys[(eeprom[6] >> 8) & 15]));
            log.debug(" PHY #" + (eeprom[6] & 0x1f));
            if ((eeprom[7] & 0x0700) != 0) {
                log.debug("Secondary interface chip " + (phys[(eeprom[7] >> 8) & 7]));
            }

            if (((eeprom[6] >> 8) & 0x3f) == DP83840 || ((eeprom[6] >> 8) & 0x3f) == DP83840A) {
                int mdi_reg23 = mdioRead(eeprom[6] & 0x1f, 23) | 0x0422;
                if (congenb) mdi_reg23 |= 0x0100;
                log.debug("DP83840 specific setup, setting register 23 to " + Integer.toHexString(mdi_reg23));
                mdioWrite(eeprom[6] & 0x1f, 23, mdi_reg23);
            }
            if ((option >= 0) && (option & 0x330) != 0) {
                log.debug("  Forcing " + ((option & 0x300) != 0 ? 100 : 10) + "Mbs " + ((option & 0x220) != 0 ? "full" : "half") + "-duplex operation.");
                mdioWrite(eeprom[6] & 0x1f, 0, ((option & 0x300) != 0 ? 0x2000 : 0) | /* 100mbps? */
                ((option & 0x220) != 0 ? 0x0100 : 0)); /* Full duplex? */
            } else {
                int mii_bmcrctrl = mdioRead(eeprom[6] & 0x1f, 0);
                /* Reset out of a transceiver left in 10baseT-fixed mode. */
                if ((mii_bmcrctrl & 0x3100) == 0) mdioWrite(eeprom[6] & 0x1f, 0, 0x8000);
            }
        }

        byte[] data = new byte[32];
        MemoryResource selfTest = rm.asMemoryResource(data);
        
        /* Perform a system self-test. */
       log.debug("self test: " + Integer.toHexString(selfTest.getAddress().as32bit(selfTest.getAddress())));

        setReg32(SCBPort, selfTest.getAddress().as32bit(selfTest.getAddress()) | PortSelfTest);
        selfTest.setInt(2, 0);  // rom signature
        selfTest.setInt(0, -1); //status
        int boguscnt = 16000; // Timeout for set-test.
        do {
            systemDelay(10);
            int i0=100;
            while(i0-->0) ; 
        }while(selfTest.getInt(0) ==-1 && --boguscnt >=0);
        
        StringBuffer sb = new StringBuffer();
        
        if (boguscnt < 0) {
            /* Test optimized out. */
            log.debug("Self test failed, status"+ Long.toHexString(selfTest.getLong(4))+ "Failure to initialize the i82557.");
            log.debug("Verify that the card is a bus-master capable slot.");
        } else { 
            int results = selfTest.getInt(0);
            log.debug("General self-test:"+ ((results &0x1000) == 0 ? "failed" : "passed"));
            log.debug("Serial sub-system self-test: "+((results &0x0020) == 0 ? "failed" : "passed"));
            log.debug("Internal registers self-test:"+((results &0x0008) == 0 ? "failed" : "passed"));
            log.debug(" ROM checksum self-test:"+((results & 0x0004) == 0 ? "failed" : "passed") + "(" +Integer.toHexString(selfTest.getInt(2)) + ")");
            log.debug(sb.toString());
            sb.setLength(0); 
        }                                                              
        /* reset adapter to default state*/
        setReg32(SCBPort, PortReset);
        systemDelay(100);
        // 	pci_dev = pdev;
        // 	chip_id = chip_idx;
        // 	drv_flags = pci_id_tbl[chip_idx].drv_flags;
        // 	acpi_pwr = acpi_idle_state;

        /*
         * full_duplex = option >= 0 && (option & 0x220) ? 1 : 0; if (card_idx >=
         * 0) { if (full_duplex[card_idx] >= 0) full_duplex =
         * full_duplex[card_idx]; } if (full_duplex) medialock = 1;
         */

        // 		phy[0] = eeprom[6];
        // 		phy[1] = eeprom[7];
        // 		rxBug = (eeprom[3] & 0x03) == 3;
        // 		if (rxBug)
        // 			System.out.println("Receiver lock-up workaround activated.");
    }    /*
          * (non-Javadoc)
          * 
          * @see org.jnode.driver.net.AbstractDeviceCore#getHwAddress()
          */
    public HardwareAddress getHwAddress() {
        return hwAddress;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.driver.net.AbstractDeviceCore#initialize()
     */
    public void initialize() {
        log.debug(this.flags.getName() + ": Done open(), status ");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.driver.net.AbstractDeviceCore#disable()
     */
    public void disable() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.driver.net.AbstractDeviceCore#release()
     */
    public void release() {
        io.release();
        irq.release();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.driver.net.AbstractDeviceCore#transmit(org.jnode.net.SocketBuffer,
     *      long)
     */
    public void transmit(SocketBuffer buf, long timeout) throws InterruptedException, TimeoutException {
        // TODO Auto-generated method stub
        // Set the source address
        hwAddress.writeTo(buf, 6);

    }

    public void handleInterrupt(int irq) {
        // TODO Not yet implemented
    }

    //--- PRIVATE METHODS ---

    private IOResource claimPorts(final ResourceManager rm, final ResourceOwner owner, final int low, final int length) throws ResourceNotFreeException, DriverException {
        try {
            return (IOResource) AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction() {

                public Object run() throws ResourceNotFreeException {
                    return rm.claimIOResource(owner, low, length);
                }
            });
        } catch (ResourceNotFreeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DriverException("Unknown exception", ex);
        }

    }

    //--- EEPROM METHODS ---

    /**
     * Delay between EEPROM clock transitions. The code works with no delay on
     * 33Mhz PCI.
     */
    final void eepromDelay() {
        //SystemResource.getTimer().udelay(4);
        int i = 100;
        while (i-- > 0)
            ;
    }

    final void sizeEeprom() {

        setReg16(SCBeeprom, EE_CS);
        int cmd = EE_READ_CMD << 8;
        int addressBits = 0;
        for (int i = 10; i >= 0; i--, addressBits++) {
            int data = (cmd & 1 << i) == 0 ? EE_WRITE_0 : EE_WRITE_1;
            setReg16(SCBeeprom, data);
            setReg16(SCBeeprom, data | EE_SHIFT_CLK);
            eepromDelay();
            setReg16(SCBeeprom, data);
            eepromDelay();

            int ee = getReg16(SCBeeprom);
            if ((ee & EE_DATA_READ) == 0) {
                if (addressBits == 8) {
                    // 64 registers
                    eeSize = 0x40;
                    eeReadCmd = EE_READ_CMD << 6;
                    eeAddress = 8;
                } else {
                    // 256 registers
                    eeSize = 0x100;
                    eeReadCmd = EE_READ_CMD << 8;
                    eeAddress = 10;
                }
                break;
            }
        }
        // read but discard
        for (int i = 0; i < 16; i++) {
            setReg16(SCBeeprom, EE_CS);
            setReg16(SCBeeprom, EE_CS | EE_SHIFT_CLK);
            eepromDelay();
            setReg16(SCBeeprom, EE_CS);
            eepromDelay();
        }
        // disable the eeprom
        setReg16(SCBeeprom, 0);
    }

    final int doEepromCmd(int cmd) {
        int data = 0;

        setReg16(SCBeeprom, EE_CS);
        cmd |= eeReadCmd;

        for (int i = eeAddress; true; i--) {
            data = (cmd & 1 << i) == 0 ? EE_WRITE_0 : EE_WRITE_1;
            setReg16(SCBeeprom, data);
            setReg16(SCBeeprom, data | EE_SHIFT_CLK);
            eepromDelay();
            setReg16(SCBeeprom, data);
            eepromDelay();
            if ((getReg16(SCBeeprom) & EE_DATA_READ) == 0) break;
        }
        data = 0;
        // read value
        for (int i = 0; i < 16; i++) {
            setReg16(SCBeeprom, EE_CS);
            setReg16(SCBeeprom, EE_CS | EE_SHIFT_CLK);
            eepromDelay();
            data <<= 1;
            if ((getReg16(SCBeeprom) & EE_DATA_READ) != 0) data |= 1;

            setReg16(SCBeeprom, EE_CS);
            eepromDelay();
        }
        // disable the eeprom
        setReg16(SCBeeprom, 0);
        return data;
    }

    //--- REGISTER METHODS

    /**
     * Reads a 8-bit NIC register
     * 
     * @param reg
     */
    protected final int getReg8(int reg) {
        return io.inPortByte(iobase + reg);
    }

    /**
     * Reads a 16-bit NIC register
     * 
     * @param reg
     */
    protected final int getReg16(int reg) {
        return io.inPortWord(iobase + reg);
    }

    /**
     * Reads a 32-bit NIC register
     * 
     * @param reg
     */

    protected final int getReg32(int reg) {
        return io.inPortDword(iobase + reg);
    }

    /**
     * Writes a 8-bit NIC register
     * 
     * @param reg
     * @param value
     */

    protected final void setReg8(int reg, int value) {
        io.outPortByte(iobase + reg, value);
    }

    /**
     * Writes a 16-bit NIC register
     * 
     * @param reg
     * @param value
     */

    protected final void setReg16(int reg, int value) {
        io.outPortWord(iobase + reg, value);
    }

    /**
     * Writes a 32-bit NIC register
     * 
     * @param reg
     * @param value
     */

    protected final void setReg32(int reg, int value) {
        io.outPortDword(iobase + reg, value);
    }

    //--- OTHER METHODS
    final void systemDelay(int delay) {
        //SystemResource.getTimer().udelay(4);
        int i = 100;
        while (i-- > 0)
            ;
    }

    //--- MD IO METHODS

    final int mdioRead(int phy_id, int location) {
        int val, boguscnt = 7; /* <64 usec. to complete, typ 27 ticks */
        setReg32(SCBCtrlMDI, 0x08000000 | (location << 16) | (phy_id << 21));
        do {
            systemDelay(10);
            val = getReg32(SCBCtrlMDI);
            if (--boguscnt < 0) {
                log.debug(this.flags.getName() + ": mdioRead() timed out with val = " + Integer.toHexString(val));
                break;
            }
        } while ((val & 0x10000000) == 0);
        return val & 0xffff;
    }

    final int mdioWrite(int phy_id, int location, int value) {
        int val, boguscnt = 7; /* <64 usec. to complete, typ 27 ticks */
        setReg32(SCBCtrlMDI, 0x04000000 | (location << 16) | (phy_id << 21) | value);
        do {
            systemDelay(10);
            val = getReg32(SCBCtrlMDI);
            if (--boguscnt < 0) {
                StringBuffer sb = new StringBuffer();
                log.debug("eepro100: mdioWrite() timed out with val =" + Integer.toHexString(val));
                break;
            }
        } while ((val & 0x10000000) == 0);
        return val & 0xffff;
    }
}