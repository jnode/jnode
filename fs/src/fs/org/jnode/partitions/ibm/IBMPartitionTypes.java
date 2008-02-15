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
 
package org.jnode.partitions.ibm;

/**
 * @author epr
 *
 **/
public enum IBMPartitionTypes {
    PARTTYPE_EMPTY                  (0x00, "empty"),
    PARTTYPE_DOS_FAT12              (0x01, "DOS 12-bit FAT"),
    PARTTYPE_XENIX_ROOT             (0x02, "XENIX root file system"),
    PARTTYPE_XENIX_USR              (0x03, "XENIX /usr file system (obsolete)"),
    PARTTYPE_DOS_FAT16_LT32M        (0x04, "DOS 16-bit FAT (up to 32M)"),
    PARTTYPE_DOS_EXTENDED           (0x05, "DOS 3.3+ extended partition"),
    PARTTYPE_DOS_FAT16_GT32M        (0x06, "DOS 3.31+ Large File System (16-bit FAT, over 32M)"),
    PARTTYPE_NTFS                   (0x07, "NTFS, OS/2 HPFS, Advanced Unix"),
    PARTTYPE_AIX_BOOTABLE           (0x08, "AIX bootable partition, SplitDrive"),
    PARTTYPE_AIX_DATA               (0x09, "AIX data partition, Coherent filesystem"),
    PARTTYPE_OS2_BOOT_MANAGER       (0x0A, "OS/2 Boot Manager, OPUS, Coherent swap partition"),
    PARTTYPE_WIN95_FAT32            (0x0B, "Win 95 Fat Partition"),
    PARTTYPE_WIN95_FAT32_LBA        (0x0C, "Win 95 Fat 32 Partition (LBA)"),
    PARTTYPE_WIN95_FAT16_LBA        (0x0E, "Win 95 Fat 16 Partition (LBA)"),
    PARTTYPE_WIN95_FAT32_EXTENDED   (0x0F, "WIN95 EXTENDED"),
    PARTTYPE_OPUS                   (0x10, "OPUS"),
    PARTTYPE_OS2_BOOT_HIDDEN_12     (0x11, "OS/2 Boot Manager hidden 12-bit FAT partition"),
    PARTTYPE_COMPAQ_DIAG            (0x12, "Compaq Diagnostics partition"),
    PARTTYPE_OS2_BOOT_HIDDEN_16_S32 (0x14, "(resulted from using Novell DOS 7.0 FDISK to delete Linux Native part), OS/2 Boot Manager hidden sub-32M 16-bit FAT partition"),
    PARTTYPE_OS2_BOOT_HIDDEN_16_O32 (0x16, "OS/2 Boot Manager hidden over-32M 16-bit FAT partition"),
    PARTTYPE_OS2_BOOT_HIDDEN_HPFS   (0x17, "OS/2 Boot Manager hidden HPFS partition"),
    PARTTYPE_WINDOWS_SWAP           (0x18, "AST special Windows swap file"),
    PARTTYPE_WIN95_FAT32_HIDDEN     (0x1B, "Hidden Win 95 Fat Partition"),
    PARTTYPE_WIN95_FAT32_LBA_HIDDEN (0x1C, "Hidden Win 95 Fat 32 Partition (LBA)"),
    PARTTYPE_WIN95_FAT16_LBA_HIDDEN (0x1E, "Hidden Win 95 Fat 16 Partition (LBA)"),
    PARTTYPE_NEC_MSDOS              (0x24, "NEC MS-DOS 3.x"),
    PARTTYPE_PLAN_9                 (0x39, "Plan 9"),
    PARTTYPE_POWERQUEST_RECOVERY    (0x3C, "PowerQuest PartitionMagic recovery partition"),
    PARTTYPE_VENIX80286             (0x40, "VENIX 80286"),
    PARTTYPE_PPC_BOOT               (0x41, "PPC_BOOT"),
    PARTTYPE_SFS                    (0x42, "SFS (Secure File System) by Peter Gutmann"),
    PARTTYPE_QNX                    (0x4d, "QNX"),
    PARTTYPE_QNX_SECOND             (0x4e, "QNX second Part"),
    PARTTYPE_QNX_THIRD              (0x4f, "QNX third Part"),
    PARTTYPE_DISK_MANAGER_RO        (0x50, "Disk Manager, read-only partition"),
    PARTTYPE_DISK_MANAGER_RW        (0x51, "Disk Manager, read/write partition,  Novell???"),
    PARTTYPE_CPM                    (0x52, "CP/M,  Microport System V/386"),
    PARTTYPE_ONTRACK_AUX            (0x53, "Ontrack ?"),
    PARTTYPE_ONTRACK                (0x54, "Ontrack ?"),
    PARTTYPE_EZ_DRIVE               (0x55, "EZ_DRIVE"),
    PARTTYPE_VFEATURE               (0x56, "GoldenBow VFeature"),
    PARTTYPE_PRIAM_EDISK            (0x5c, "Priam Edisk"),
    PARTTYPE_SPEEDSTOR              (0x61, "SpeedStor"),
    PARTTYPE_UNIX_SYS_V             (0x63, "Unix SysV/386, 386/ix; ach, MtXinu BSD 4.3 on Mach; GNU HURD"),
    PARTTYPE_NOVELL                 (0x64, "Novell NetWare"),
    PARTTYPE_NOVELL_31              (0x65, "Novell NetWare (3.11)"),
    PARTTYPE_DISK_SECURE            (0x70, "DiskSecure Multi-Boot"),
    PARTTYPE_PC_IX                  (0x75, "PC/IX"),
    PARTTYPE_MINIX                  (0x80, "Minix v1.1 - 1.4a"),
    PARTTYPE_LINUX                  (0x81, "Linux; Mitac Advanced Disk Manager"),
    PARTTYPE_LINUX_SWAP             (0x82, "Linux Swap partition"),
    PARTTYPE_LINUXNATIVE            (0x83, "Linux native file system (ext2fs/xiafs)"),
    PARTTYPE_OS2_HIDING_DOS         (0x84, "OS/2-renumbered type 04h partition (related to hiding DOS C: drive);"),
    PARTTYPE_LINUX_EXTENDED         (0x85, "Linux extendet partition"),
    PARTTYPE_LINUX_LVM              (0x8E, "Linux LVM"),
    PARTTYPE_AMOEBA                 (0x93, "Amoeba file system"),
    PARTTYPE_AMOEBA_BAD_BLOCK       (0x94, "Amoeba bad block table"),
    PARTTYPE_BSD                    (0x9F, "BSD"),
    PARTTYPE_THIK_PAD_HIDDEN        (0xA0, "IBM Thinkpad hidden partition"),
    PARTTYPE_FREE_BSD               (0xA5, "FreeBSD"),
    PARTTYPE_OPEN_BSD               (0xA6, "OpenBSD"),
    PARTTYPE_NEXT_STEP              (0xA7, "NextStep"),
    PARTTYPE_NETBSD                 (0xA9, "NetBSD"),
    PARTTYPE_BSDI                   (0xB7, "BSDI file system (secondarily swap)"),
    PARTTYPE_BSDI_SWAP              (0xB8, "BSDI swap partition (secondarily file system)"),
    PARTTYPE_DR_DOS_12              (0xC1, "DR-DOS 6.0 LOGIN.EXE-secured 12-bit FAT partition;"),
    PARTTYPE_DR_DOS_16              (0xC4, "DR-DOS 6.0 LOGIN.EXE-secured 16-bit FAT partition"),
    PARTTYPE_DR_DOS_HUGE            (0xC6, "DR-DOS 6.0 LOGIN.EXE-secured Huge partition"),
    PARTTYPE_CYRNIX                 (0xC7, "Cyrnix Boot;"),
    PARTTYPE_NON_FS                 (0xDA, "Non FS Data;"),
    PARTTYPE_CPM_DOS                (0xDB, "CP/M, Concurrent CP/M, Concurrent DOS; CTOS (Convergent Technologies OS)"),
    PARTTYPE_DELL_UTILITY           (0xDE, "DELL Utility partition"),
    PARTTYPE_BOOT_IT                (0xDF, "Boot it"),
    PARTTYPE_SPEEDSTOR_FAT_12       (0xE1, "SpeedStor 12-bit FAT extended partition"),
    PARTTYPE_DOS_R_O                (0xE3, "Readonly Dos Partition"),
    PARTTYPE_SPEEDSTOR_FAT_16       (0xE4, "SpeedStor 16-bit FAT extended partition"),
    PARTTYPE_BEOS_FS                (0xEB, "BEO_FS"),
    PARTTYPE_EFI_GPT                (0xEE, "EFI GPT"),
    PARTTYPE_EFI_FAT                (0xEF, "BEO_FS"),
    PARTTYPE_LINUX_PA_RISK          (0xF0, "Linux PA Risk"),
    PARTTYPE_SPEEDSTORE_A           (0xF1, "Speedstore ???"),
    PARTTYPE_DOS3_3_SECENDORY       (0xF2, "DOS 3.3+ secondary"),
    PARTTYPE_SPEEDSTORE_B           (0xF4, "Speedstore ???"),
    PARTTYPE_LINUX_RAID             (0xFD, "Linux Raid"),
    PARTTYPE_LANSTEP                (0xFE, "LANstep"),
    PARTTYPE_XENIX_BAD_BLOCK        (0xFF, "Xenix bad block table");

	public static IBMPartitionTypes valueOf(int code) {
		for(IBMPartitionTypes type : IBMPartitionTypes.values())
		{
			if(type.getCode() == code)
			{
				return type;
			}
		}
		throw new IllegalArgumentException(code+" isn't a partition code");
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
	
	public String toString()
	{
		return Integer.toHexString(code) + " - " + name;		
	}
}
