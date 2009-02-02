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
 
package org.jnode.driver.bus.pci;

/**
 * @author epr
 */
public interface PCIConstants {
    /**
     * Maximum number of units a PCI bus can have
     */
    public static final int MAX_UNITS = 32;
    /**
     * Maximum number of functions a PCI card can have
     */
    public static final int MAX_FUNCTIONS = 8;

    /**
     * PCI Config address port
     */
    public static final int PW32_CONFIG_ADDRESS = 0xcf8;

    /**
     * PCI Config data port
     */
    public static final int PRW32_CONFIG_DATA = 0xcfc;

    public static final int PCI_FIRST_PORT = PW32_CONFIG_ADDRESS;
    public static final int PCI_LAST_PORT = PRW32_CONFIG_DATA + 3;

    // --------------------------------
    // Header type constants

    /**
     * Normal header type
     */
    public static final int HEADER_TYPE_NORMAL = 0;

    /**
     * PCI-PCI bridge header type
     */
    public static final int HEADER_TYPE_BRIDGE = 1;

    /**
     * Cardbus header type
     */
    public static final int HEADER_TYPE_CARDBUS = 2;

    // --------------------------------
    // Class constants

    public static final int CLASS_MASS_STORAGE_CONTROLLER = 0x01;
    public static final int SUBCLASS_MSC_SCSI = 0x00;
    public static final int SUBCLASS_MSC_IDE = 0x01;
    public static final int SUBCLASS_MSC_FLOPPY = 0x02;
    public static final int SUBCLASS_MSC_IPI = 0x03;
    public static final int SUBCLASS_MSC_RAID = 0x04;
    public static final int SUBCLASS_MSC_OTHER = 0x80;

    public static final int CLASS_NETWORK_CONTROLLER = 0x02;
    public static final int SUBCLASS_NC_ETHERNET = 0x00;
    public static final int SUBCLASS_NC_TOKENRING = 0x01;
    public static final int SUBCLASS_NC_FDDI = 0x02;
    public static final int SUBCLASS_NC_OTHER = 0x80;

    public static final int CLASS_VIDEO_CONTROLLER = 0x03;
    public static final int SUBCLASS_VC_VGA = 0x00;
    public static final int SUBCLASS_VC_XGA = 0x01;
    public static final int SUBCLASS_VC_OTHER = 0x80;

    public static final int CLASS_MULTIMEDIA_CONTROLLER = 0x04;
    public static final int SUBCLASS_MMC_VIDEO = 0x00;
    public static final int SUBCLASS_MMC_AUDIO = 0x01;
    public static final int SUBCLASS_MMC_OTHER = 0x80;

    public static final int CLASS_MEMORY_CONTROLLER = 0x05;
    public static final int SUBCLASS_MEMC_RAM = 0x00;
    public static final int SUBCLASS_MEMC_FLASH = 0x01;
    public static final int SUBCLASS_MEMC_OTHER = 0x80;

    public static final int CLASS_BRIDGE = 0x06;
    public static final int SUBCLASS_BR_HOST = 0x00;
    public static final int SUBCLASS_BR_ISA = 0x01;
    public static final int SUBCLASS_BR_EISA = 0x02;
    public static final int SUBCLASS_BR_MCI = 0x03;
    public static final int SUBCLASS_BR_PCI = 0x04;
    public static final int SUBCLASS_BR_PCMCIA = 0x05;
    public static final int SUBCLASS_BR_OTHER = 0x80;

    public static final int CLASS_COMMUNICATION = 0x07;
    public static final int SUBCLASS_COMM_SERIAL = 0x00;
    public static final int SUBCLASS_COMM_PARALLEL = 0x01;
    public static final int SUBCLASS_COMM_MULTISERIAL = 0x02;
    public static final int SUBCLASS_COMM_MODEM = 0x03;
    public static final int SUBCLASS_COMM_OTHER = 0x80;

    public static final int CLASS_SYSTEM = 0x08;
    public static final int SUBCLASS_SYSTEM_PIC = 0x00;
    public static final int SUBCLASS_SYSTEM_DMA = 0x01;
    public static final int SUBCLASS_SYSTEM_TIMER = 0x02;
    public static final int SUBCLASS_SYSTEM_RTC = 0x03;
    public static final int SUBCLASS_SYSTEM_PCI_HOTPLUG = 0x04;
    public static final int SUBCLASS_SYSTEM_OTHER = 0x80;

    public static final int CLASS_INPUT = 0x09;
    public static final int SUBCLASS_INPUT_KEYBOARD = 0x00;
    public static final int SUBCLASS_INPUT_PEN = 0x01;
    public static final int SUBCLASS_INPUT_MOUSE = 0x02;
    public static final int SUBCLASS_INPUT_SCANNER = 0x03;
    public static final int SUBCLASS_INPUT_GAMEPORT = 0x04;
    public static final int SUBCLASS_INPUT_OTHER = 0x80;

    public static final int CLASS_DOCKING = 0x0a;
    public static final int SUBCLASS_DOCKING_GENERIC = 0x00;
    public static final int SUBCLASS_DOCKING_OTHER = 0x80;

    public static final int CLASS_PROCESSOR = 0x0b;
    public static final int SUBCLASS_PROCESSOR_386 = 0x00;
    public static final int SUBCLASS_PROCESSOR_486 = 0x01;
    public static final int SUBCLASS_PROCESSOR_PENTIUM = 0x02;
    public static final int SUBCLASS_PROCESSOR_ALPHA = 0x10;
    public static final int SUBCLASS_PROCESSOR_POWERPC = 0x20;
    public static final int SUBCLASS_PROCESSOR_MIPS = 0x30;
    public static final int SUBCLASS_PROCESSOR_CO = 0x40;

    public static final int CLASS_SERIAL = 0x0c;
    public static final int SUBCLASS_SERIAL_FIREWIRE = 0x00;
    public static final int SUBCLASS_SERIAL_ACCESS = 0x01;
    public static final int SUBCLASS_SERIAL_SSA = 0x02;
    public static final int SUBCLASS_SERIAL_USB = 0x03;
    public static final int SUBCLASS_SERIAL_FIBER = 0x04;
    public static final int SUBCLASS_SERIAL_SMBUS = 0x05;

    public static final int CLASS_INTELLIGENT = 0x0e;
    public static final int SUBCLASS_INTELLIGENT_I2O = 0x00;

    public static final int CLASS_SATELLITE = 0x0f;
    public static final int SUBCLASS_SATELLITE_TV = 0x00;
    public static final int SUBCLASS_SATELLITE_AUDIO = 0x01;
    public static final int SUBCLASS_SATELLITE_VOICE = 0x03;
    public static final int SUBCLASS_SATELLITE_DATA = 0x04;

    public static final int CLASS_CRYPT = 0x10;
    public static final int SUBCLASS_CRYPT_NETWORK = 0x00;
    public static final int SUBCLASS_CRYPT_ENTERTAINMENT = 0x01;
    public static final int SUBCLASS_CRYPT_OTHER = 0x80;

    public static final int CLASS_SIGNAL_PROCESSING = 0x11;
    public static final int SUBCLASS_SP_DPIO = 0x00;
    public static final int SUBCLASS_SP_OTHER = 0x80;

    /*
     * Under PCI, each device has 256 bytes of configuration address space, of which the first 64
     * bytes are standardized as follows:
     */
    public static final int PCI_COMMAND_IO = 0x1; /* Enable response in I/O space */
    public static final int PCI_COMMAND_MEMORY = 0x2; /* Enable response in Memory space */
    public static final int PCI_COMMAND_MASTER = 0x4; /* Enable bus mastering */
    public static final int PCI_COMMAND_SPECIAL = 0x8; /* Enable response to special cycles */
    public static final int PCI_COMMAND_INVALIDATE = 0x10; /* Use memory write and invalidate */
    public static final int PCI_COMMAND_VGA_PALETTE = 0x20; /* Enable palette snooping */
    public static final int PCI_COMMAND_PARITY = 0x40; /* Enable parity checking */
    public static final int PCI_COMMAND_WAIT = 0x80; /* Enable address/data stepping */
    public static final int PCI_COMMAND_SERR = 0x100; /* Enable SERR */
    public static final int PCI_COMMAND_FAST_BACK = 0x200; /* Enable back-to-back writes */

    public static final int PCI_STATUS_CAP_LIST = 0x10; /* Support Capability List */
    public static final int PCI_STATUS_66MHZ = 0x20; /* Support 66 Mhz PCI 2.1 bus */
    public static final int PCI_STATUS_UDF = 0x40; /* Support User Definable Features [obsolete] */
    public static final int PCI_STATUS_FAST_BACK = 0x80; /* Accept fast-back to back */
    public static final int PCI_STATUS_PARITY = 0x100; /* Detected parity error */
    public static final int PCI_STATUS_DEVSEL_MASK = 0x600; /* DEVSEL timing */
    public static final int PCI_STATUS_DEVSEL_FAST = 0x000;
    public static final int PCI_STATUS_DEVSEL_MEDIUM = 0x200;
    public static final int PCI_STATUS_DEVSEL_SLOW = 0x400;
    public static final int PCI_STATUS_SIG_TARGET_ABORT = 0x800; /* Set on target abort */
    public static final int PCI_STATUS_REC_TARGET_ABORT = 0x1000; /* Master ack of " */
    public static final int PCI_STATUS_REC_MASTER_ABORT = 0x2000; /* Set on master abort */
    public static final int PCI_STATUS_SIG_SYSTEM_ERROR = 0x4000; /* Set when we drive SERR */
    public static final int PCI_STATUS_DETECTED_PARITY = 0x8000; /* Set on parity error */

    public static final int PCI_BIST_CODE_MASK = 0x0f; /* Return result */
    public static final int PCI_BIST_START = 0x40; /* 1 to start BIST, 2 secs or less */
    public static final int PCI_BIST_CAPABLE = 0x80; /* 1 if BIST capable */

    /* Header type 0 (normal devices) */

    /* = 0x35-= 0x3b are reserved */

    /* Capability lists */

    /* Power Management Registers */

    public static final int PCI_PM_PMC = 2; /* PM Capabilities Register */
    public static final int PCI_PM_CAP_VER_MASK = 0x0007; /* Version */
    public static final int PCI_PM_CAP_PME_CLOCK = 0x0008; /* PME clock required */
    public static final int PCI_PM_CAP_RESERVED = 0x0010; /* Reserved field */
    public static final int PCI_PM_CAP_DSI = 0x0020; /* Device specific initialization */
    public static final int PCI_PM_CAP_AUX_POWER = 0x01C0; /* Auxilliary power support mask */
    public static final int PCI_PM_CAP_D1 = 0x0200; /* D1 power state support */
    public static final int PCI_PM_CAP_D2 = 0x0400; /* D2 power state support */
    public static final int PCI_PM_CAP_PME = 0x0800; /* PME pin supported */
    public static final int PCI_PM_CAP_PME_MASK = 0xF800; /* PME Mask of all supported states */
    public static final int PCI_PM_CAP_PME_D0 = 0x0800; /* PME# from D0 */
    public static final int PCI_PM_CAP_PME_D1 = 0x1000; /* PME# from D1 */
    public static final int PCI_PM_CAP_PME_D2 = 0x2000; /* PME# from D2 */
    public static final int PCI_PM_CAP_PME_D3 = 0x4000; /* PME# from D3 (hot) */
    public static final int PCI_PM_CAP_PME_D3cold = 0x8000; /* PME# from D3 (cold) */
    public static final int PCI_PM_CTRL = 4; /* PM control and status register */
    public static final int PCI_PM_CTRL_STATE_MASK = 0x0003; /* Current power state (D0 to D3) */
    public static final int PCI_PM_CTRL_PME_ENABLE = 0x0100; /* PME pin enable */
    public static final int PCI_PM_CTRL_DATA_SEL_MASK = 0x1e00; /* Data select (??) */
    public static final int PCI_PM_CTRL_DATA_SCALE_MASK = 0x6000; /* Data scale (??) */
    public static final int PCI_PM_CTRL_PME_STATUS = 0x8000; /* PME pin status */
    public static final int PCI_PM_PPB_EXTENSIONS = 6; /* PPB support extensions (??) */
    public static final int PCI_PM_PPB_B2_B3 = 0x40; /* Stop clock when in D3hot (??) */
    public static final int PCI_PM_BPCC_ENABLE = 0x80; /* Bus power/clock control enable (??) */
    public static final int PCI_PM_DATA_REGISTER = 7; /* (??) */
    public static final int PCI_PM_SIZEOF = 8;

    /* AGP registers */

    public static final int PCI_AGP_VERSION = 2; /* BCD version number */
    public static final int PCI_AGP_RFU = 3; /* Rest of capability flags */
    public static final int PCI_AGP_STATUS = 4; /* Status register */
    public static final int PCI_AGP_STATUS_RQ_MASK = 0xff000000; /* Maximum number of requests - 1 */
    public static final int PCI_AGP_STATUS_SBA = 0x0200; /* Sideband addressing supported */
    public static final int PCI_AGP_STATUS_64BIT = 0x0020; /* 64-bit addressing supported */
    public static final int PCI_AGP_STATUS_FW = 0x0010; /* FW transfers supported */
    public static final int PCI_AGP_STATUS_RATE4 = 0x0004; /* 4x transfer rate supported */
    public static final int PCI_AGP_STATUS_RATE2 = 0x0002; /* 2x transfer rate supported */
    public static final int PCI_AGP_STATUS_RATE1 = 0x0001; /* 1x transfer rate supported */
    public static final int PCI_AGP_COMMAND = 8; /* Control register */
    public static final int PCI_AGP_COMMAND_RQ_MASK = 0xff000000; /*
                                                                    * Master: Maximum number of
                                                                    * requests
                                                                    */
    public static final int PCI_AGP_COMMAND_SBA = 0x0200; /* Sideband addressing enabled */
    public static final int PCI_AGP_COMMAND_AGP = 0x0100; /* Allow processing of AGP transactions */
    public static final int PCI_AGP_COMMAND_64BIT = 0x0020; /* Allow processing of 64-bit addresses */
    public static final int PCI_AGP_COMMAND_FW = 0x0010; /* Force FW transfers */
    public static final int PCI_AGP_COMMAND_RATE4 = 0x0004; /* Use 4x rate */
    public static final int PCI_AGP_COMMAND_RATE2 = 0x0002; /* Use 2x rate */
    public static final int PCI_AGP_COMMAND_RATE1 = 0x0001; /* Use 1x rate */
    public static final int PCI_AGP_SIZEOF = 12;

    /* Slot Identification */

    public static final int PCI_SID_ESR = 2; /* Expansion Slot Register */
    public static final int PCI_SID_ESR_NSLOTS = 0x1f; /* Number of expansion slots available */
    public static final int PCI_SID_ESR_FIC = 0x20; /* First In Chassis Flag */
    public static final int PCI_SID_CHASSIS_NR = 3; /* Chassis Number */

    /* Message Signalled Interrupts registers */

    public static final int PCI_MSI_FLAGS = 2; /* Various flags */
    public static final int PCI_MSI_FLAGS_64BIT = 0x80; /* 64-bit addresses allowed */
    public static final int PCI_MSI_FLAGS_QSIZE = 0x70; /* Message queue size configured */
    public static final int PCI_MSI_FLAGS_QMASK = 0x0e; /* Maximum queue size available */
    public static final int PCI_MSI_FLAGS_ENABLE = 0x01; /* MSI feature enabled */
    public static final int PCI_MSI_RFU = 3; /* Rest of capability flags */
    public static final int PCI_MSI_ADDRESS_LO = 4; /* Lower 32 bits */
    public static final int PCI_MSI_ADDRESS_HI = 8; /* Upper 32 bits (if PCI_MSI_FLAGS_64BIT set) */
    public static final int PCI_MSI_DATA_32 = 8; /* 16 bits of data for 32-bit devices */
    public static final int PCI_MSI_DATA_64 = 12; /* 16 bits of data for 64-bit devices */
}
