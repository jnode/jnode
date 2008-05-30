/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.system.pnp;

import java.util.HashMap;
import java.util.Map;

/**
 * PnP.
 * <p/>
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * The list is taken from Linux sources
 * <p>
 * Licence: GNU LGPL
 * </p>
 * <p>
 * </p>
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */

public class PnP {

    private static Map<String, String> pnpDB = null;

    static char[] AcpiGbl_HexToAscii = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static char AcpiUtHexToAsciiChar(int value, int position) {

        return (AcpiGbl_HexToAscii[(value >> position) & 0xF]);
    }

    private static int AcpiUtDwordByteSwap(int id) {
        int eisaId = (id & 0xFF) << 24;
        eisaId |= (id & 0xFF00) << 8;
        eisaId |= (id & 0xFF0000) >> 8;
        eisaId |= (id & 0xFF000000) >> 24;
        return eisaId;
    }

    public static String eisaIdToString(int id) {
        String result = "";
        int eisaId = AcpiUtDwordByteSwap(id);
        result += (char) ('@' + ((eisaId >> 26) & 0x1f));
        result += (char) ('@' + ((eisaId >> 21) & 0x1f));
        result += (char) ('@' + ((eisaId >> 16) & 0x1f));
        result += AcpiUtHexToAsciiChar(eisaId, 12);
        result += AcpiUtHexToAsciiChar(eisaId, 8);
        result += AcpiUtHexToAsciiChar(eisaId, 4);
        result += AcpiUtHexToAsciiChar(eisaId, 0);
        return result;
    }

    public static String getDescription(String pnpid) {
        if (pnpDB == null) {
            pnpDB = new HashMap<String, String>();
            initDB();
        }
        String result = (String) pnpDB.get(pnpid.toUpperCase());
        if (result == null)
            result = pnpid;
        return result;
    }

    private static void put(String id, String description) {
        pnpDB.put(id.toUpperCase(), description);
    }

    private static void initDB() {

        put("CSC0000", "Crystal Semiconductor CS423x sound -- SB/WSS/OPL3 emulation");
        put("CSC0010", "Crystal Semiconductor CS423x sound -- control");
        put("CSC0001", "Crystal Semiconductor CS423x sound -- joystick");
        put("CSC0003", "Crystal Semiconductor CS423x sound -- MPU401");

        put("IBM3780", "IBM pointing device");
        put("IBM0071", "IBM infrared communications device");
        put("IBM3760", "IBM DSP");

        put("NSC6001", "National Semiconductor Serial Port with Fast IR");

        put("PNP0000", "AT Interrupt Controller");
        put("PNP0001", "EISA Interrupt Controller");
        put("PNP0002", "MCA Interrupt Controller");
        put("PNP0003", "APIC");
        put("PNP0004", "Cyrix SLiC MP Interrupt Controller");
        put("PNP0100", "AT Timer");
        put("PNP0101", "EISA Timer");
        put("PNP0102", "MCA Timer");
        put("PNP0200", "AT DMA Controller");
        put("PNP0201", "EISA DMA Controller");
        put("PNP0202", "MCA DMA Controller");
        put("PNP0300", "IBM PC/XT keyboard controller (83-key)");
        put("PNP0301", "IBM PC/AT keyboard controller (86-key)");
        put("PNP0302", "IBM PC/XT keyboard controller (84-key)");
        put("PNP0303", "IBM Enhanced (101/102-key, PS/2 mouse support)");
        put("PNP0304", "Olivetti Keyboard (83-key)");
        put("PNP0305", "Olivetti Keyboard (102-key)");
        put("PNP0306", "Olivetti Keyboard (86-key)");
        put("PNP0307", "Microsoft Windows(R) Keyboard");
        put("PNP0308", "General Input Device Emulation Interface (GIDEI) legacy");
        put("PNP0309", "Olivetti Keyboard (A101/102 key)");
        put("PNP030a", "AT&T 302 Keyboard");
        put("PNP0320", "Japanese 106-key keyboard A01");
        put("PNP0321", "Japanese 101-key keyboard");
        put("PNP0322", "Japanese AX keyboard");
        put("PNP0323", "Japanese 106-key keyboard 002/003");
        put("PNP0324", "Japanese 106-key keyboard 001");
        put("PNP0325", "Japanese Toshiba Desktop keyboard");
        put("PNP0326", "Japanese Toshiba Laptop keyboard");
        put("PNP0327", "Japanese Toshiba Notebook keyboard");
        put("PNP0340", "Korean 84-key keyboard");
        put("PNP0341", "Korean 86-key keyboard");
        put("PNP0342", "Korean Enhanced keyboard");
        put("PNP0343", "Korean Enhanced keyboard 101b");
        put("PNP0343", "Korean Enhanced keyboard 101c");
        put("PNP0344", "Korean Enhanced keyboard 103");
        put("PNP0400", "Standard LPT printer port");
        put("PNP0401", "ECP printer port");
        put("PNP0500", "Standard PC COM port");
        put("PNP0501", "16550A-compatible COM port");
        put("PNP0502", "Multiport serial device (non-intelligent 16550)");
        put("PNP0510", "Generic IRDA-compatible device");
        put("PNP0511", "Generic IRDA-compatible device");
        put("PNP0600", "Generic ESDI/IDE/ATA compatible hard disk controller");
        put("PNP0601", "Plus Hardcard II");
        put("PNP0602", "Plus Hardcard IIXL/EZ");
        put("PNP0603", "Generic IDE supporting Microsoft Device Bay Specification");
        put("PNP0700", "PC standard floppy disk controller");
        put("PNP0701", "Standard floppy controller supporting MS Device Bay Spec");
        put("PNP0802", "Microsoft Sound System or Compatible Device (obsolete)");
        put("PNP0900", "VGA Compatible");
        put("PNP0901", "Video Seven VRAM/VRAM II/1024i");
        put("PNP0902", "8514/A Compatible");
        put("PNP0903", "Trident VGA");
        put("PNP0904", "Cirrus Logic Laptop VGA");
        put("PNP0905", "Cirrus Logic VGA");
        put("PNP0906", "Tseng ET4000");
        put("PNP0907", "Western Digital VGA");
        put("PNP0908", "Western Digital Laptop VGA");
        put("PNP0909", "S3 Inc. 911/924");
        put("PNP090a", "ATI Ultra Pro/Plus (Mach 32)");
        put("PNP090b", "ATI Ultra (Mach 8)");
        put("PNP090c", "XGA Compatible");
        put("PNP090d", "ATI VGA Wonder");
        put("PNP090e", "Weitek P9000 Graphics Adapter");
        put("PNP090f", "Oak Technology VGA");
        put("PNP0910", "Compaq QVision");
        put("PNP0911", "XGA/2");
        put("PNP0912", "Tseng Labs W32/W32i/W32p");
        put("PNP0913", "S3 Inc. 801/928/964");
        put("PNP0914", "Cirrus Logic 5429/5434 (memory mapped)");
        put("PNP0915", "Compaq Advanced VGA (AVGA)");
        put("PNP0916", "ATI Ultra Pro Turbo (Mach64)");
        put("PNP0917", "Reserved by Microsoft");
        put("PNP0918", "Matrox MGA");
        put("PNP0919", "Compaq QVision 2000");
        put("PNP091a", "Tseng W128");
        put("PNP0930", "Chips & Technologies Super VGA");
        put("PNP0931", "Chips & Technologies Accelerator");
        put("PNP0940", "NCR 77c22e Super VGA");
        put("PNP0941", "NCR 77c32blt");
        put("PNP09ff", "Plug and Play Monitors (VESA DDC)");
        put("PNP0A00", "ISA Bus");
        put("PNP0A01", "EISA Bus");
        put("PNP0A02", "MCA Bus");
        put("PNP0A03", "PCI Bus");
        put("PNP0A04", "VESA/VL Bus");
        put("PNP0A05", "Generic ACPI Bus");
        put("PNP0A06", "Generic ACPI Extended-IO Bus (EIO bus)");
        put("PNP0800", "AT-style speaker sound");
        put("PNP0A00", "AT Real-Time Clock");
        put("PNP0B00", "Real-Time Clock");
        put("PNP0C00", "Plug and Play BIOS (only created by the root enumerator)");
        put("PNP0C01", "System Board");
        put("PNP0C02", "Reserved Motherboard Resources");
        put("PNP0C03", "Plug and Play BIOS Event Notification Interrupt");
        put("PNP0C04", "Math Coprocessor");
        put("PNP0C05", "APM BIOS (Version independent)");
        put("PNP0C06", "Reserved for identification of early Plug and Play BIOS implementation.");
        put("PNP0C07", "Reserved for identification of early Plug and Play BIOS implementation.");
        put("PNP0C08", "ACPI system board hardware");
        put("PNP0C09", "ACPI Embedded Controller");
        put("PNP0C0A", "ACPI Control Method Battery");
        put("PNP0C0B", "ACPI Fan");
        put("PNP0C0C", "ACPI power button device");
        put("PNP0C0D", "ACPI lid device");
        put("PNP0C0E", "ACPI sleep button device");
        put("PNP0C0F", "PCI interrupt link device");
        put("PNP0C10", "ACPI system indicator device");
        put("PNP0C11", "ACPI thermal zone");
        put("PNP0C12", "Device Bay Controller");
        put("PNP0C13", "Plug and Play BIOS (used when ACPI mode cannot be used)");
        put("PNP0C15", "Docking Station");
        put("PNP0E00", "Intel 82365-Compatible PCMCIA Controller");
        put("PNP0E01", "Cirrus Logic CL-PD6720 PCMCIA Controller");
        put("PNP0E02", "VLSI VL82C146 PCMCIA Controller");
        put("PNP0E03", "Intel 82365-compatible CardBus controller");
        put("PNP0F00", "Microsoft Bus Mouse");
        put("PNP0F01", "Microsoft Serial Mouse");
        put("PNP0F02", "Microsoft InPort Mouse");
        put("PNP0F03", "Microsoft PS/2-style Mouse");
        put("PNP0F04", "Mouse Systems Mouse");
        put("PNP0F05", "Mouse Systems 3-Button Mouse (COM2)");
        put("PNP0F06", "Genius Mouse (COM1)");
        put("PNP0F07", "Genius Mouse (COM2)");
        put("PNP0F08", "Logitech Serial Mouse");
        put("PNP0F09", "Microsoft BallPoint Serial Mouse");
        put("PNP0F0a", "Microsoft Plug and Play Mouse");
        put("PNP0f0b", "Microsoft Plug and Play BallPoint Mouse");
        put("PNP0f0c", "Microsoft-compatible Serial Mouse");
        put("PNP0f0d", "Microsoft-compatible InPort-compatible Mouse");
        put("PNP0f0e", "Microsoft-compatible PS/2-style Mouse");
        put("PNP0f0f", "Microsoft-compatible Serial BallPoint-compatible Mouse");
        put("PNP0f10", "Texas Instruments QuickPort Mouse");
        put("PNP0f11", "Microsoft-compatible Bus Mouse");
        put("PNP0f12", "Logitech PS/2-style Mouse");
        put("PNP0f13", "PS/2 Port for PS/2-style Mice");
        put("PNP0f14", "Microsoft Kids Mouse");
        put("PNP0f15", "Logitech bus mouse");
        put("PNP0f16", "Logitech SWIFT device");
        put("PNP0f17", "Logitech-compatible serial mouse");
        put("PNP0f18", "Logitech-compatible bus mouse");
        put("PNP0f19", "Logitech-compatible PS/2-style Mouse");
        put("PNP0f1a", "Logitech-compatible SWIFT Device");
        put("PNP0f1b", "HP Omnibook Mouse");
        put("PNP0f1c", "Compaq LTE Trackball PS/2-style Mouse");
        put("PNP0f1d", "Compaq LTE Trackball Serial Mouse");
        put("PNP0f1e", "Microsoft Kids Trackball Mouse");
        put("PNP8001", "Novell/Anthem NE3200");
        put("PNP8004", "Compaq NE3200");
        put("PNP8006", "Intel EtherExpress/32");
        put("PNP8008", "HP EtherTwist EISA LAN Adapter/32 (HP27248A)");
        put("PNP8065", "Ungermann-Bass NIUps or NIUps/EOTP");
        put("PNP8072", "DEC (DE211) EtherWorks MC/TP");
        put("PNP8073", "DEC (DE212) EtherWorks MC/TP_BNC");
        put("PNP8078", "DCA 10 Mb MCA");
        put("PNP8074", "HP MC LAN Adapter/16 TP (PC27246)");
        put("PNP80c9", "IBM Token Ring");
        put("PNP80ca", "IBM Token Ring II");
        put("PNP80cb", "IBM Token Ring II/Short");
        put("PNP80cc", "IBM Token Ring 4/16Mbs");
        put("PNP80d3", "Novell/Anthem NE1000");
        put("PNP80d4", "Novell/Anthem NE2000");
        put("PNP80d5", "NE1000 Compatible");
        put("PNP80d6", "NE2000 Compatible");
        put("PNP80d7", "Novell/Anthem NE1500T");
        put("PNP80d8", "Novell/Anthem NE2100");
        put("PNP80dd", "SMC ARCNETPC");
        put("PNP80de", "SMC ARCNET PC100, PC200");
        put("PNP80df", "SMC ARCNET PC110, PC210, PC250");
        put("PNP80e0", "SMC ARCNET PC130/E");
        put("PNP80e1", "SMC ARCNET PC120, PC220, PC260");
        put("PNP80e2", "SMC ARCNET PC270/E");
        put("PNP80e5", "SMC ARCNET PC600W, PC650W");
        put("PNP80e7", "DEC DEPCA");
        put("PNP80e8", "DEC (DE100) EtherWorks LC");
        put("PNP80e9", "DEC (DE200) EtherWorks Turbo");
        put("PNP80ea", "DEC (DE101) EtherWorks LC/TP");
        put("PNP80eb", "DEC (DE201) EtherWorks Turbo/TP");
        put("PNP80ec", "DEC (DE202) EtherWorks Turbo/TP_BNC");
        put("PNP80ed", "DEC (DE102) EtherWorks LC/TP_BNC");
        put("PNP80ee", "DEC EE101 (Built-In)");
        put("PNP80ef", "DECpc 433 WS (Built-In)");
        put("PNP80f1", "3Com EtherLink Plus");
        put("PNP80f3", "3Com EtherLink II or IITP (8 or 16-bit)");
        put("PNP80f4", "3Com TokenLink");
        put("PNP80f6", "3Com EtherLink 16");
        put("PNP80f7", "3Com EtherLink III");
        put("PNP80f8", "3Com Generic Etherlink Plug and Play Device");
        put("PNP80fb", "Thomas Conrad TC6045");
        put("PNP80fc", "Thomas Conrad TC6042");
        put("PNP80fd", "Thomas Conrad TC6142");
        put("PNP80fe", "Thomas Conrad TC6145");
        put("PNP80ff", "Thomas Conrad TC6242");
        put("PNP8100", "Thomas Conrad TC6245");
        put("PNP8105", "DCA 10 MB");
        put("PNP8106", "DCA 10 MB Fiber Optic");
        put("PNP8107", "DCA 10 MB Twisted Pair");
        put("PNP8113", "Racal NI6510");
        put("PNP811c", "Ungermann-Bass NIUpc");
        put("PNP8120", "Ungermann-Bass NIUpc/EOTP");
        put("PNP8123", "SMC StarCard PLUS (WD/8003S)");
        put("PNP8124", "SMC StarCard PLUS With On Board Hub (WD/8003SH)");
        put("PNP8125", "SMC EtherCard PLUS (WD/8003E)");
        put("PNP8126", "SMC EtherCard PLUS With Boot ROM Socket (WD/8003EBT)");
        put("PNP8127", "SMC EtherCard PLUS With Boot ROM Socket (WD/8003EB)");
        put("PNP8128", "SMC EtherCard PLUS TP (WD/8003WT)");
        put("PNP812a", "SMC EtherCard PLUS 16 With Boot ROM Socket (WD/8013EBT)");
        put("PNP812d", "Intel EtherExpress 16 or 16TP");
        put("PNP812f", "Intel TokenExpress 16/4");
        put("PNP8130", "Intel TokenExpress MCA 16/4");
        put("PNP8132", "Intel EtherExpress 16 (MCA)");
        put("PNP8137", "Artisoft AE-1");
        put("PNP8138", "Artisoft AE-2 or AE-3");
        put("PNP8141", "Amplicard AC 210/XT");
        put("PNP8142", "Amplicard AC 210/AT");
        put("PNP814b", "Everex SpeedLink /PC16 (EV2027)");
        put("PNP8155", "HP PC LAN Adapter/8 TP (HP27245)");
        put("PNP8156", "HP PC LAN Adapter/16 TP (HP27247A)");
        put("PNP8157", "HP PC LAN Adapter/8 TL (HP27250)");
        put("PNP8158", "HP PC LAN Adapter/16 TP Plus (HP27247B)");
        put("PNP8159", "HP PC LAN Adapter/16 TL Plus (HP27252)");
        put("PNP815f", "National Semiconductor Ethernode *16AT");
        put("PNP8160", "National Semiconductor AT/LANTIC EtherNODE 16-AT3");
        put("PNP816a", "NCR Token-Ring 4 Mbs ISA");
        put("PNP816d", "NCR Token-Ring 16/4 Mbs ISA");
        put("PNP8191", "Olicom 16/4 Token-Ring Adapter");
        put("PNP81c3", "SMC EtherCard PLUS Elite (WD/8003EP)");
        put("PNP81c4", "SMC EtherCard PLUS 10T (WD/8003W)");
        put("PNP81c5", "SMC EtherCard PLUS Elite 16 (WD/8013EP)");
        put("PNP81c6", "SMC EtherCard PLUS Elite 16T (WD/8013W)");
        put("PNP81c7", "SMC EtherCard PLUS Elite 16 Combo (WD/8013EW or 8013EWC)");
        put("PNP81c8", "SMC EtherElite Ultra 16");
        put("PNP81e4", "Pure Data PDI9025-32 (Token Ring)");
        put("PNP81e6", "Pure Data PDI508+ (ArcNet)");
        put("PNP81e7", "Pure Data PDI516+ (ArcNet)");
        put("PNP81eb", "Proteon Token Ring (P1390)");
        put("PNP81ec", "Proteon Token Ring (P1392)");
        put("PNP81ed", "Proteon ISA Token Ring (1340)");
        put("PNP81ee", "Proteon ISA Token Ring (1342)");
        put("PNP81ef", "Proteon ISA Token Ring (1346)");
        put("PNP81f0", "Proteon ISA Token Ring (1347)");
        put("PNP81ff", "Cabletron E2000 Series DNI");
        put("PNP8200", "Cabletron E2100 Series DNI");
        put("PNP8209", "Zenith Data Systems Z-Note");
        put("PNP820a", "Zenith Data Systems NE2000-Compatible");
        put("PNP8213", "Xircom Pocket Ethernet II");
        put("PNP8214", "Xircom Pocket Ethernet I");
        put("PNP821d", "RadiSys EXM-10");
        put("PNP8227", "SMC 3000 Series");
        put("PNP8228", "SMC 91C2 controller");
        put("PNP8231", "Advanced Micro Devices AM2100/AM1500T");
        put("PNP8263", "Tulip NCC-16");
        put("PNP8277", "Exos 105");
        put("PNP828a", "Intel '595 based Ethernet");
        put("PNP828b", "TI2000-style Token Ring");
        put("PNP828c", "AMD PCNet Family cards");
        put("PNP828d", "AMD PCNet32 (VL version)");
        put("PNP8294", "IrDA Infrared NDIS driver (Microsoft-supplied)");
        put("PNP82bd", "IBM PCMCIA-NIC");
        put("PNP82c2", "Xircom CE10");
        put("PNP82c3", "Xircom CEM2");
        put("PNP8321", "DEC Ethernet (All Types)");
        put("PNP8323", "SMC EtherCard (All Types except 8013/A)");
        put("PNP8324", "ARCNET Compatible");
        put("PNP8326", "Thomas Conrad (All Arcnet Types)");
        put("PNP8327", "IBM Token Ring (All Types)");
        put("PNP8385", "Remote Network Access Driver");
        put("PNP8387", "RNA Point-to-point Protocol Driver");
        put("PNP8388", "Reserved for Microsoft Networking components");
        put("PNP8389", "Peer IrLAN infrared driver (Microsoft-supplied)");
        put("PNP8390", "Generic network adapter");
        put("PNPa002", "Future Domain 16-700 compatible controller");
        put("PNPa003", "Panasonic proprietary CD-ROM adapter (SBPro/SB16)");
        put("PNPa01b", "Trantor 128 SCSI Controller");
        put("PNPa01d", "Trantor T160 SCSI Controller");
        put("PNPa01e", "Trantor T338 Parallel SCSI controller");
        put("PNPa01f", "Trantor T348 Parallel SCSI controller");
        put("PNPa020", "Trantor Media Vision SCSI controller");
        put("PNPa022", "Always IN-2000 SCSI controller");
        put("PNPa02b", "Sony proprietary CD-ROM controller");
        put("PNPa02d", "Trantor T13b 8-bit SCSI controller");
        put("PNPa02f", "Trantor T358 Parallel SCSI controller");
        put("PNPa030", "Mitsumi LU-005 Single Speed CD-ROM controller + drive");
        put("PNPa031", "Mitsumi FX-001 Single Speed CD-ROM controller + drive");
        put("PNPa032", "Mitsumi FX-001 Double Speed CD-ROM controller + drive");
        put("PNPb000", "Sound Blaster 1.5 sound device");
        put("PNPb001", "Sound Blaster 2.0 sound device");
        put("PNPb002", "Sound Blaster Pro sound device");
        put("PNPb003", "Sound Blaster 16 sound device");
        put("PNPb004", "Thunderboard-compatible sound device");
        put("PNPb005", "Adlib-compatible FM synthesizer device");
        put("PNPb006", "MPU401 compatible");
        put("PNPb007", "Microsoft Windows Sound System-compatible sound device");
        put("PNPb008", "Compaq Business Audio");
        put("PNPb009", "Plug and Play Microsoft Windows Sound System Device");
        put("PNPb00a", "MediaVision Pro Audio Spectrum (Trantor SCSI enabled, Thunder Chip Disabled)");
        put("PNPb00b", "MediaVision Pro Audio 3D");
        put("PNPb00c", "MusicQuest MQX-32M");
        put("PNPb00d", "MediaVision Pro Audio Spectrum Basic (No Trantor SCSI, Thunder Chip Enabled)");
        put("PNPb00e", "MediaVision Pro Audio Spectrum (Trantor SCSI enabled, Thunder Chip Enabled)");
        put("PNPb00f", "MediaVision Jazz-16 chipset (OEM Versions)");
        put("PNPb010", "Auravision VxP500 chipset - Orchid Videola");
        put("PNPb018", "MediaVision Pro Audio Spectrum 8-bit");
        put("PNPb019", "MediaVision Pro Audio Spectrum Basic (no Trantor SCSI, Thunder chip Disabled)");
        put("PNPb020", "Yamaha OPL3-compatible FM synthesizer device");
        put("PNPb02f", "Joystick/Game port");
        put("PNPb000", "Compaq 14400 Modem (TBD)");
        put("PNPc001", "Compaq 2400/9600 Modem (TBD)");
        put("PNP0XXX", "Unknown System Device");
        put("PNP8XXX", "Unknown Network Adapter");
        put("PNPaXXX", "Unknown SCSI, Proprietary CD Adapter");
        put("PNPbXXX", "Unknown Multimedia Device");
        put("PNPcXXX", "Unknown Modem");

        put("ACPI0001", "SMBus 1.0 Host Controler");
        put("ACPI0002", "Smart Battery Subsystem");
        put("ACPI0003", "AC Device");
        put("ACPI0004", "Module Device");
        put("ACPI0005", "SMBus 2.0 Host Controler");
        put("ACPI0006", "GPE Block Device");

        put("SMCF010", "IrDA Controler");
    }

    public PnP() {
    }

}
