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

package org.jnode.driver.bus.pci;

/**
 * @author epr
 */
public interface PCI_IDs {

    // PCI identifiers
    public static final int PCI_VENDOR_ID_AMD = 0x1022;
    public static final int PCI_DEVICE_ID_AMD_LANCE = 0x2000;

    public static final int PCI_VENDOR_ID_ADMTEK = 0x1317;
    public static final int PCI_DEVICE_ID_ADMTEK_0985 = 0x0985;
    public static final int PCI_VENDOR_ID_WINBOND2 = 0x1050;
    public static final int PCI_DEVICE_ID_WINBOND2_89C940 = 0x0940;
    public static final int PCI_DEVICE_ID_WINBOND2_89C840 = 0x0840;
    public static final int PCI_VENDOR_ID_COMPEX = 0x11f6;
    public static final int PCI_DEVICE_ID_COMPEX_RL2000 = 0x1401;
    public static final int PCI_DEVICE_ID_COMPEX_RL100ATX = 0x2011;
    public static final int PCI_VENDOR_ID_KTI = 0x8e2e;
    public static final int PCI_DEVICE_ID_KTI_ET32P2 = 0x3000;
    public static final int PCI_VENDOR_ID_NETVIN = 0x4a14;
    public static final int PCI_DEVICE_ID_NETVIN_NV5000SC = 0x5000;
    public static final int PCI_VENDOR_ID_HOLTEK = 0x12c3;
    public static final int PCI_DEVICE_ID_HOLTEK_HT80232 = 0x0058;
    public static final int PCI_VENDOR_ID_3COM = 0x10b7;
    public static final int PCI_DEVICE_ID_3COM_3C590 = 0x5900;
    public static final int PCI_DEVICE_ID_3COM_3C595 = 0x5950;
    public static final int PCI_DEVICE_ID_3COM_3C595_1 = 0x5951;
    public static final int PCI_DEVICE_ID_3COM_3C595_2 = 0x5952;
    public static final int PCI_DEVICE_ID_3COM_3C900TPO = 0x9000;
    public static final int PCI_DEVICE_ID_3COM_3C900COMBO = 0x9001;
    public static final int PCI_DEVICE_ID_3COM_3C905TX = 0x9050;
    public static final int PCI_DEVICE_ID_3COM_3C905T4 = 0x9051;
    public static final int PCI_DEVICE_ID_3COM_3C905B_TX = 0x9055;
    public static final int PCI_DEVICE_ID_3COM_3C905C_TXM = 0x9200;
    public static final int PCI_VENDOR_ID_INTEL = 0x8086;
    public static final int PCI_DEVICE_ID_INTEL_82557 = 0x1229;
    public static final int PCI_DEVICE_ID_INTEL_82559ER = 0x1209;
    public static final int PCI_DEVICE_ID_INTEL_ID1029 = 0x1029;
    public static final int PCI_DEVICE_ID_INTEL_ID1030 = 0x1030;
    public static final int PCI_DEVICE_ID_INTEL_82562 = 0x2449;
    public static final int PCI_VENDOR_ID_AMD_HOMEPNA = 0x1022;
    public static final int PCI_DEVICE_ID_AMD_HOMEPNA = 0x2001;
    public static final int PCI_VENDOR_ID_DEC = 0x1011;
    public static final int PCI_DEVICE_ID_DEC_TULIP = 0x0002;
    public static final int PCI_DEVICE_ID_DEC_TULIP_FAST = 0x0009;
    public static final int PCI_DEVICE_ID_DEC_TULIP_PLUS = 0x0014;
    public static final int PCI_DEVICE_ID_DEC_21142 = 0x0019;
    public static final int PCI_VENDOR_ID_SMC = 0x10B8;
    public static final int PCI_DEVICE_ID_SMC_EPIC100 = 0x0005;
    public static final int PCI_VENDOR_ID_MACRONIX = 0x10d9;
    public static final int PCI_DEVICE_ID_MX987x5 = 0x0531;
    public static final int PCI_VENDOR_ID_LINKSYS = 0x11AD;
    public static final int PCI_DEVICE_ID_LC82C115 = 0xC115;
    public static final int PCI_VENDOR_ID_VIATEC = 0x1106;
    public static final int PCI_DEVICE_ID_VIA_RHINE_I = 0x3043;
    public static final int PCI_DEVICE_ID_VIA_VT6102 = 0x3065;
    public static final int PCI_DEVICE_ID_VIA_86C100A = 0x6100;
    public static final int PCI_VENDOR_ID_DAVICOM = 0x1282;
    public static final int PCI_DEVICE_ID_DM9009 = 0x9009;
    public static final int PCI_DEVICE_ID_DM9102 = 0x9102;
    public static final int PCI_VENDOR_ID_SIS = 0x1039;
    public static final int PCI_DEVICE_ID_SIS900 = 0x0900;
    public static final int PCI_DEVICE_ID_SIS7016 = 0x7016;
    public static final int PCI_VENDOR_ID_DLINK = 0x1186;
    public static final int PCI_DEVICE_ID_DFE530TXP = 0x1300;
    public static final int PCI_VENDOR_ID_NS = 0x100B;
    public static final int PCI_DEVICE_ID_DP83815 = 0x0020;
    public static final int PCI_VENDOR_ID_OLICOM = 0x108d;
    public static final int PCI_DEVICE_ID_OLICOM_OC3136 = 0x0001;
    public static final int PCI_DEVICE_ID_OLICOM_OC2315 = 0x0011;
    public static final int PCI_DEVICE_ID_OLICOM_OC2325 = 0x0012;
    public static final int PCI_DEVICE_ID_OLICOM_OC2183 = 0x0013;
    public static final int PCI_DEVICE_ID_OLICOM_OC2326 = 0x0014;
    public static final int PCI_DEVICE_ID_OLICOM_OC6151 = 0x0021;

    public static final int PCI_VENDOR_ID_VMWARE = 0x15ad;
    public static final int PCI_DEVICE_ID_VMWARE_SVGA = 0x0710;
    public static final int PCI_DEVICE_ID_VMWARE_SVGA1 = 0x0405;

    public static final int PCI_VENDOR_ID_NVIDIA = 0x10de;
    public static final int PCI_DEVICE_ID_NVIDIA_VANTA = 0x002c;
    public static final int PCI_DEVICE_ID_NVIDIA_GE3TI500 = 0x0202;

    public static final int PCI_VENDOR_ID_REALTEK = 0x10ec;
    public static final int PCI_DEVICE_ID_REALTEK_8029 = 0x8029;
    public static final int PCI_DEVICE_ID_REALTEK_8139 = 0x8139;
    public static final int PCI_VENDOR_ID_SMC_1211 = 0x1113; // pciids.sf.net
    // http://www.etherboot.org/db/nics.php?show=tech_data&chip_manufacturer=Realtek
    public static final int PCI_DEVICE_ID_SMC_1211 = 0x1211;

    public static final int PCI_VENDOR_ID_CIRRUS = 0x1013;
    public static final int PCI_DEVICE_ID_5446 = 0x00D8; // emulate by qemu

}
