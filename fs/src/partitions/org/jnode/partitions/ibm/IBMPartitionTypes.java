/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

package org.jnode.partitions.ibm;

/**
 * @author epr
 */
public enum IBMPartitionTypes {
    PARTTYPE_EMPTY(0x00, "Empty"),
    PARTTYPE_UNKNOWN(-1, "Unknown"),
    PARTTYPE_DOS_FAT12(0x01, "DOS FAT12"),
    PARTTYPE_XENIX_ROOT(0x02, "XENIX root file system"),
    PARTTYPE_XENIX_USR(0x03, "XENIX /usr file system (obsolete)"),
    PARTTYPE_DOS_FAT16_LT32M(0x04, "DOS FAT16 (up to 32M)"),
    PARTTYPE_DOS_EXTENDED(0x05, "DOS 3.3+ extended partition"),
    PARTTYPE_DOS_FAT16_GT32M(0x06, "DOS 3.31+ Large File System (FAT16, over 32M)"),
    PARTTYPE_NTFS(0x07, "NTFS, OS/2 HPFS, Advanced Unix"),
    PARTTYPE_AIX_BOOTABLE(0x08, "AIX bootable partition, SplitDrive"),
    PARTTYPE_AIX_DATA(0x09, "AIX data partition, Coherent filesystem"),
    PARTTYPE_OS2_BOOT_MANAGER(0x0A, "OS/2 Boot Manager, OPUS, Coherent swap partition"),
    PARTTYPE_WIN95_FAT32(0x0B, "Windows 95 FAT Partition"),
    PARTTYPE_WIN95_FAT32_LBA(0x0C, "Windows 95 FAT32 Partition (LBA)"),
    PARTTYPE_WIN95_FAT16_LBA(0x0E, "Windows 95 FAT16 Partition (LBA)"),
    PARTTYPE_WIN95_FAT32_EXTENDED(0x0F, "Windows 95 Extended"),
    PARTTYPE_OPUS(0x10, "OPUS"),
    PARTTYPE_OS2_BOOT_HIDDEN_12(0x11, "OS/2 Boot Manager hidden FAT12 partition"),
    PARTTYPE_COMPAQ_DIAG(0x12, "Compaq Diagnostics partition"),
    PARTTYPE_OS2_BOOT_HIDDEN_16_S32(0x14,
        "(resulted from using Novell DOS 7.0 FDISK to delete Linux Native part), " +
            "OS/2 Boot Manager hidden FAT16 (up to 32M) partition"),
    PARTTYPE_OS2_BOOT_HIDDEN_16_O32(0x16, "OS/2 Boot Manager hidden FAT16 (over 32M) partition"),
    PARTTYPE_OS2_BOOT_HIDDEN_HPFS(0x17, "OS/2 Boot Manager hidden HPFS partition"),
    PARTTYPE_WINDOWS_SWAP(0x18, "AST special Windows swap file"),
    PARTTYPE_WILLOWTECH_PHOTON_COS(0x19, "Willowtech Photon coS"),
    PARTTYPE_WIN95_FAT32_HIDDEN(0x1B, "Hidden Windows 95 FAT Partition"),
    PARTTYPE_WIN95_FAT32_LBA_HIDDEN(0x1C, "Hidden Windows 95 FAT32 Partition (LBA)"),
    PARTTYPE_WIN95_FAT16_LBA_HIDDEN(0x1E, "Hidden Windows 95 FAT16 Partition (LBA)"),
    PARTTYPE_OS2_MANAGER_HIDDEN_CONTAINER(0x1F, "OS/2 Boot Manager Hidden Container"),
    PARTTYPE_WINDOWS_MOBILE_UPDATE(0x20, "Windows Mobile update XIP"),
    PARTTYPE_HP_VOLUME_EXPANSION(0x21, "HP Volume Expansion, SpeedStor variant"),
    PARTTYPE_OXYGEN_EXTENDED_PARTITION_TABLE(0x22, "Oxygen Extended Partition Table"),
    PARTTYPE_WINDOWS_MOBILE_BOOT(0x23, "Windows Mobile boot XIP"),
    PARTTYPE_NEC_MSDOS(0x24, "NEC MS-DOS 3.x"),
    PARTTYPE_WINDOWS_MOBILE_IMGFS(0x25, "Windows Mobile IMGFS"),
    PARTTYPE_WINDOWS_RE_HIDDEN(0x27, "Windows Recovery Environment (RE) partition"),
    PARTTYPE_ATHFS(0x2a, "AtheOS File System (AthFS)"),
    PARTTYPE_SYLLABLESECURE(0x2b, "SyllableSecure (SylStor)"),
    PARTTYPE_NOS(0x32, "NOS"),
    PARTTYPE_OS2_JFS(0x35, "OS/2 JFS"),
    PARTTYPE_THEOS_3_2(0x38, "THEOS 3.2"),
    PARTTYPE_PLAN_9(0x39, "Plan 9"),
    PARTTYPE_THEOS_4(0x3a, "THEOS 4"),
    PARTTYPE_THEOS_4_EXT(0x3b, "THEOS 4 extended partition"),
    PARTTYPE_POWERQUEST_RECOVERY(0x3C, "PowerQuest PartitionMagic recovery partition"),
    PARTTYPE_HIDDEN_NETWARE(0x3d, "Hidden NetWare"),
    PARTTYPE_VENIX80286(0x40, "VENIX 80286"),
    PARTTYPE_PPC_BOOT(0x41, "PPC_BOOT"),
    PARTTYPE_SFS_OR_EXTENDED_PARTITION(0x42, "Secure File System, Windows 2000/XP Dynamic extended partition"),
    PARTTYPE_LINUX_DRDOS(0x43, "Linux native shared with DR DOS 6.0"),
    PARTTYPE_GOBACK(0x44, "GoBack partition"),
    PARTTYPE_PRIAM(0x45, "Priam partition"),
    PARTTYPE_EUMEL_ELAN_x46(0x46, "EUMEL/ELAN partition"),
    PARTTYPE_EUMEL_ELAN_x47(0x47, "EUMEL/ELAN partition"),
    PARTTYPE_EUMEL_ELAN_x48(0x48, "EUMEL/ELAN partition"),
    PARTTYPE_ADAOS(0x4a, "AdaOS Aquila"),
    PARTTYPE_OBERON(0x4c, "Oberon partition"),
    PARTTYPE_QNX(0x4d, "QNX"),
    PARTTYPE_QNX_SECOND(0x4e, "QNX second Part"),
    PARTTYPE_QNX_THIRD(0x4f, "QNX third Part"),
    PARTTYPE_DISK_MANAGER_RO(0x50, "Disk Manager, read-only partition"),
    PARTTYPE_DISK_MANAGER_RW(0x51, "Disk Manager, read/write partition,  Novell???"),
    PARTTYPE_CPM(0x52, "CP/M,  Microport System V/386"),
    PARTTYPE_ONTRACK_AUX(0x53, "Ontrack ?"),
    PARTTYPE_ONTRACK(0x54, "Ontrack ?"),
    PARTTYPE_EZ_DRIVE(0x55, "EZ_DRIVE"),
    PARTTYPE_VFEATURE(0x56, "GoldenBow VFeature"),
    PARTTYPE_DRIVEPRO(0x57, "StorageSoft DrivePro"),
    PARTTYPE_PRIAM_EDISK(0x5c, "Priam Edisk"),
    PARTTYPE_APTI_ALT(0x5d, "APTI alternate partition"),
    PARTTYPE_APTI_ALT_EXT_x5E(0x5e, "APTI alternate extended partition"),
    PARTTYPE_APTI_ALT_EXT_x5F(0x5f, "APTI alternate extended partition"),
    PARTTYPE_SPEEDSTOR(0x61, "SpeedStor"),
    PARTTYPE_UNIX_SYS_V(0x63, "Unix SysV/386, 386/ix; ach, MtXinu BSD 4.3 on Mach; GNU HURD"),
    PARTTYPE_NOVELL(0x64, "Novell NetWare"),
    PARTTYPE_NOVELL_31(0x65, "Novell NetWare (3.11)"),
    PARTTYPE_NOVELL_SMS(0x66, "Novell NetWare Storage Management Services (SMS)"),
    PARTTYPE_NOVELL_WOLF_MOUNTAIN(0x67, "Novell Wolf Mountain"),
    PARTTYPE_NOVELL_ALT(0x68, "Novell NetWare"),
    PARTTYPE_NOVELL_5(0x69, "Novell NetWare 5"),
    PARTTYPE_DISK_SECURE(0x70, "DiskSecure Multi-Boot"),
    PARTTYPE_APTI_ALT_FAT12(0x72, "APTI alternate FAT12 partition"),
    PARTTYPE_SCRAMDISK(0x74, "Scramdisk"),
    PARTTYPE_PC_IX(0x75, "PC/IX"),
    PARTTYPE_M2FS(0x77, "M2FS/M2CS partition"),
    PARTTYPE_XOSL(0x78, "XOSL bootloader"),
    PARTTYPE_APTI_ALT_FAT16(0x79, "APTI alternate FAT16 partition"),
    PARTTYPE_APTI_ALT_FAT16X(0x7A, "APTI alternate FAT6X partition"),
    PARTTYPE_APTI_ALT_FAT16B(0x7B, "APTI alternate FAT16B partition"),
    PARTTYPE_APTI_ALT_FAT32X(0x7C, "APTI alternate FAT32X partition"),
    PARTTYPE_APTI_ALT_FAT32(0x7D, "APTI alternate FAT32 partition"),
    PARTTYPE_FIX(0x7E, "FIX"),
    PARTTYPE_ALT_OS(0x7F, "Alternative OS Development Partition Standard"),
    PARTTYPE_MINIX(0x80, "Minix v1.1 - 1.4a"),
    PARTTYPE_LINUX(0x81, "Linux; Mitac Advanced Disk Manager"),
    PARTTYPE_LINUX_SWAP(0x82, "Linux Swap partition"),
    PARTTYPE_LINUXNATIVE(0x83, "Linux native file system (ext2fs/xiafs)"),
    PARTTYPE_OS2_HIDING_DOS(0x84, "OS/2-renumbered type 04h partition (related to hiding DOS C: drive);"),
    PARTTYPE_LINUX_EXTENDED(0x85, "Linux extendet partition"),
    PARTTYPE_WINNT_FAT16B(0x86, "Windows NT 4.0 fault tolerant FAT16"),
    PARTTYPE_WINNT_HPFS_NTFS(0x87, "Windows NT 4.0 fault tolerant HPFS/NTFS"),
    PARTTYPE_LINUX_PLAINTEXT_PARTITION_TABLE(0x88, "Linux plaintext partition table"),
    PARTTYPE_LINUX_AIRBOOT(0x8A, "Linux AiR-BOOT"),
    PARTTYPE_WINNT_FAT32(0x8B, "Windows NT 4.0 fault tolerant FAT32"),
    PARTTYPE_WINNT_FAT32X(0x8C, "Windows NT 4.0 fault tolerant FAT32X"),
    PARTTYPE_FREEDOS_HIDDEN_FAT12(0x8D, "FreeDOS hidden FAT12"),
    PARTTYPE_LINUX_LVM(0x8E, "Linux LVM"),
    PARTTYPE_FREEDOS_HIDDEN_FAT16(0x90, "FreeDOS hidden FAT16"),
    PARTTYPE_FREEDOS_HIDDEN_PART_CHS(0x91, "FreeDOS hidden extended partition (CHS addressing)"),
    PARTTYPE_FREEDOS_HIDDEN_FAT16B(0x92, "FreeDOS hidden FAT16B"),
    PARTTYPE_AMOEBA(0x93, "Amoeba file system"),
    PARTTYPE_AMOEBA_BAD_BLOCK(0x94, "Amoeba bad block table"),
    PARTTYPE_EXOPC(0x95, "EXOPC"),
    PARTTYPE_CHRP(0x96, "CHRP ISO-9660"),
    PARTTYPE_FREEDOS_HIDDEN_FAT32(0x97, "FreeDOS hidden FAT32"),
    PARTTYPE_FREEDOS_HIDDEN_FAT32X(0x98, "FreeDOS hidden FAT32X"),
    PARTTYPE_FREEDOS_HIDDEN_FAT16X(0x9a, "FreeDOS hidden FAT16X"),
    PARTTYPE_FREEDOS_HIDDEN_EXTENDED_PARTITION(0x9b, "FreeDOS hidden extended partition"),
    PARTTYPE_FORTHOS(0x9E, "ForthOS"),
    PARTTYPE_BSD(0x9F, "BSD"),
    PARTTYPE_THINK_PAD_HIDDEN(0xA0, "IBM Thinkpad hidden partition"),
    PARTTYPE_HP_VOLUME_EXPANSION_A1(0xA1, "HP Volume Expansion (SpeedStor)"),
    PARTTYPE_HP_VOLUME_EXPANSION_A3(0xA3, "HP Volume Expansion (SpeedStor)"),
    PARTTYPE_HP_VOLUME_EXPANSION_A4(0xA4, "HP Volume Expansion (SpeedStor)"),
    PARTTYPE_FREE_BSD(0xA5, "FreeBSD"),
    PARTTYPE_OPEN_BSD(0xA6, "OpenBSD"),
    PARTTYPE_NEXT_STEP(0xA7, "NextStep"),
    PARTTYPE_APPLE_UFS(0xA8, "Apple UFS"),
    PARTTYPE_NETBSD(0xA9, "NetBSD"),
    PARTTYPE_OLIVETTI(0xAA, "Olivetti FAT12"),
    PARTTYPE_APPLE_BOOT(0xAB, "Apple OSX Boot"),
    PARTTYPE_ADFS(0xAD, "AFDS"),
    PARTTYPE_SHAGOS(0xAE, "ShagOS"),
    PARTTYPE_APPLE_HFS_HFSPLUS(0xAF, "Apple HFS/HFS+"),
    PARTTYPE_HP_VOLUME_EXPANSION_xB1(0xB1, "HP Volume Expansion (SpeedStor)"),
    PARTTYPE_QNX_NEUTRINO(0xB2, "QNX Neutrino"),
    PARTTYPE_HP_VOLUME_EXPANSION_xB3(0xB3, "HP Volume Expansion (SpeedStor)"),
    PARTTYPE_HP_VOLUME_EXPANSION_xB4(0xB4, "HP Volume Expansion (SpeedStor)"),
    PARTTYPE_HP_VOLUME_EXPANSION_xB6(0xB6, "HP Volume Expansion (SpeedStor)"),
    PARTTYPE_BSDI(0xB7, "BSDI file system (secondarily swap)"),
    PARTTYPE_BSDI_SWAP(0xB8, "BSDI swap partition (secondarily file system)"),
    PARTTYPE_WINNT_FAT32_MIRROR(0xBB, "Windows NT 4.0 fault tolerant FAT32 mirror"),
    PARTTYPE_WINNT_FAT32X_MIRROR(0xBC, "Windows NT 4.0 fault tolerant FAT32X mirror"),
    PARTTYPE_SOLARIS_8_BOOT(0xBE, "Solarsis 8 boot"),
    PARTTYPE_SOLARIS_X86(0xBF, "Solarsis x86"),
    PARTTYPE_DR_DOS_SECURED_FAT(0xC0, "DR-DOS secured FAT"),
    PARTTYPE_DR_DOS_12(0xC1, "DR-DOS 6.0 LOGIN.EXE-secured 12-bit FAT partition;"),
    PARTTYPE_DR_DOS_16(0xC4, "DR-DOS 6.0 LOGIN.EXE-secured 16-bit FAT partition"),
    PARTTYPE_DR_DOS_SECURED_PARTITION_CHS(0xC5, "DR-DOS secured extended partition CHS"),
    PARTTYPE_DR_DOS_HUGE(0xC6, "DR-DOS 6.0 LOGIN.EXE-secured Huge partition"),
    PARTTYPE_CYRNIX(0xC7, "Cyrnix Boot;"),
    PARTTYPE_DR_DOS_SECURED_FAT32(0xCB, "DR-DOS secured FAT32"),
    PARTTYPE_DR_DOS_SECURED_FAT32X(0xCC, "DR-DOS secured FAT32X"),
    PARTTYPE_DR_DOS_SECURED_FAT16X(0xCE, "DR-DOS secured FAT16X"),
    PARTTYPE_DR_DOS_SECURED_PARTITION_LBA(0xCF, "DR-DOS secured extended partition LBA"),
    PARTTYPE_NOVELL_DOS_SECURED_FAT(0xD0, "Novell Multiuser DOS secured FAT"),
    PARTTYPE_NOVELL_DOS_SECURED_FAT12(0xD1, "Novell Multiuser DOS secured FAT12"),
    PARTTYPE_NOVELL_DOS_SECURED_FAT16(0xD4, "Novell Multiuser DOS secured FAT16"),
    PARTTYPE_NOVELL_DOS_SECURED_PARTITION_CHS(0xD5, "Novell Multiuser DOS secured partition CHS"),
    PARTTYPE_NOVELL_DOS_SECURED_FAT16B(0xD6, "Novell Multiuser DOS secured FAT16B"),
    PARTTYPE_NON_FS(0xDA, "Non FS Data;"),
    PARTTYPE_CPM_DOS(0xDB, "CP/M, Concurrent CP/M, Concurrent DOS; CTOS (Convergent Technologies OS)"),
    PARTTYPE_DELL_UTILITY(0xDE, "DELL Utility partition"),
    PARTTYPE_BOOT_IT(0xDF, "Boot it"),
    PARTTYPE_SPEEDSTOR_FAT_12(0xE1, "SpeedStor 12-bit FAT extended partition"),
    PARTTYPE_DOS_R_O(0xE2, "Readonly Dos Partition"),
    PARTTYPE_DOS_R_O_ALT(0xE3, "Readonly Dos Partition"),
    PARTTYPE_SPEEDSTOR_FAT_16(0xE4, "SpeedStor 16-bit FAT extended partition"),
    PARTTYPE_TANDY_FAT(0xE5, "Tandy FAT12/16"),
    PARTTYPE_LINUX_LUKS(0xE8, "Linux Unified Key Setup"),
    PARTTYPE_BEOS_FS(0xEB, "BeOS BFS"),
    PARTTYPE_SKYFS(0xEC, "SkyOS SkyFS"),
    PARTTYPE_EFI_GPT_HYBRID(0xED, "EFI GPT hybrid MBR"),
    PARTTYPE_EFI_GPT(0xEE, "EFI GPT protective MBR"),
    PARTTYPE_EFI_FAT(0xEF, "EFI system partition FAT12/16/32"),
    PARTTYPE_LINUX_PA_RISK(0xF0, "Linux PA Risk"),
    PARTTYPE_SPEEDSTORE_A(0xF1, "Speedstore ???"),
    PARTTYPE_DOS3_3_SECONDARY(0xF2, "DOS 3.3+ secondary"),
    PARTTYPE_SPEEDSTORE_B(0xF4, "Speedstore ???"),
    PARTTYPE_VMWARE_VMFS(0xFB, "VMware VMFS"),
    PARTTYPE_VMWARE_SWAP(0xFC, "VMware swap"),
    PARTTYPE_LINUX_RAID(0xFD, "Linux Raid"),
    PARTTYPE_LANSTEP(0xFE, "LANstep"),
    PARTTYPE_XENIX_BAD_BLOCK(0xFF, "Xenix bad block table");

    public static IBMPartitionTypes valueOf(int code) {
        for (IBMPartitionTypes type : IBMPartitionTypes.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException(code + " isn't a partition code");
    }

    private final int code;
    private final String name;

    private IBMPartitionTypes(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return Integer.toHexString(code) + " - " + name;
    }
}
