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
 
package org.jnode.partitions.gpt;

import java.util.UUID;
import org.apache.log4j.Logger;
import org.jnode.util.NumberUtils;

/**
 * Known GPT partition types.
 *
 * @author Luke Quinane
 */
public enum GptPartitionTypes {

    // General
    UNKNOWN(),
    UNUSED_ENTRY("00000000-0000-0000-0000-000000000000"),
    MBR_PARTITION_SCHEME("024DEE41-33E7-11D3-9D69-0008C781F39F"),
    EFI_SYSTEM_PARTITION("C12A7328-F81F-11D2-BA4B-00A0C93EC93B"),
    BIOS_BOOT_PARTITION("21686148-6449-6E6F-744E-656564454649"),

    // Windows
    MICROSOFT_RESERVED_PARTITION("E3C9E316-0B5C-4DB8-817D-F92DF00215AE"),
    MICROSOFT_BASIC_DATA_PARTITION("EBD0A0A2-B9E5-4433-87C0-68B6B72699C7"),
    MICROSOFT_LOGICAL_DISK_MANAGER_METADATA_PARTITION("5808C8AA-7E8F-42E0-85D2-E1E90434CFB3"),
    MICROSOFT_LOGICAL_DISK_MANAGER_DATA_PARTITION("AF9B60A0-1431-4F62-BC68-3311714A69AD"),
    WINDOWS_RECOVERY_ENVIRONMENT("DE94BBA4-06D1-4D40-A16A-BFD50179D6AC"),
    IBM_GENERAL_PARALLEL_FILE_SYSTEM_PARTITION("37AFFC90-EF7D-4E96-91C3-2D7AE055B174"),

    // HP-UX
    HP_UX_DATA_PARTITION("75894C1E-3AEB-11D3-B7C1-7B03A0000000"),
    HP_UX_SERVICE_PARTITION("E2A1E728-32E3-11D6-A682-7B03A0000000"),

    // Linux
    LINUX_FILESYSTEM_DATA("0FC63DAF-8483-4772-8E79-3D69D8477DE4"),
    LINUX_RAID_PARTITION("A19D880F-05FC-4D3B-A006-743F0F84911E"),
    LINUX_SWAP_PARTITION("0657FD6D-A4AB-43C4-84E5-0933C84B4F4F"),
    LINUX_LOGICAL_VOLUME_MANAGER_PARTITION("E6D6D379-F507-44C2-A23C-238F2A3DF928"),
    LINUX_RESERVED("8DA63339-0007-60C0-C436-083AC8230908"),

    // FreeBSD
    FREEBSD_BOOT_PARTITION("83BD6B9D-7F41-11DC-BE0B-001560B84F0F"),
    FREEBSD_DATA_PARTITION("516E7CB4-6ECF-11D6-8FF8-00022D09712B"),
    FREEBSD_SWAP_PARTITION("516E7CB5-6ECF-11D6-8FF8-00022D09712B"),
    FREEBSD_UNIX_FILE_SYSTEM_PARTITION("516E7CB6-6ECF-11D6-8FF8-00022D09712B"),
    FREEBSD_VINUM_VOLUME_MANAGER_PARTITION("516E7CB8-6ECF-11D6-8FF8-00022D09712B"),
    FREEBSD_ZFS_PARITION("516E7CBA-6ECF-11D6-8FF8-00022D09712B"),

    // Apple
    HFS_PLUS_PARTITION("48465300-0000-11AA-AA11-00306543ECAC"),
    APPLE_UFS("55465300-0000-11AA-AA11-00306543ECAC"),
    APPLE_ZFS("6A898CC3-1DD2-11B2-99A6-080020736631"), // This is the same GUID as SOLARIS_USR_PARTITION
    APPLE_RAID_PARTITION("52414944-0000-11AA-AA11-00306543ECAC"),
    APPLE_RAID_PARTITION_OFFLINE("52414944-5F4F-11AA-AA11-00306543ECAC"),
    APPLE_BOOT_PARTITION("426F6F74-0000-11AA-AA11-00306543ECAC"),
    APPLE_lABEL("4C616265-6C00-11AA-AA11-00306543ECAC"),
    APPLE_TV_RECOVERY_PARTITION("5265636F-7665-11AA-AA11-00306543ECAC"),
    APPLE_CORE_STORAGE_PARTITION("53746F72-6167-11AA-AA11-00306543ECAC"),

    // Solaris
    SOLARIS_BOOT_PARTITION("6A82CB45-1DD2-11B2-99A6-080020736631"),
    SOLARIS_ROOT_PARTITION("6A85CF4D-1DD2-11B2-99A6-080020736631"),
    SOLARIS_SWAP_PARTITION("6A87C46F-1DD2-11B2-99A6-080020736631"),
    SOLARIS_BACKUP_PARTITION("6A8B642B-1DD2-11B2-99A6-080020736631"),
    SOLARIS_USR_PARTITION("6A898CC3-1DD2-11B2-99A6-080020736631"), // This is the same GUID as APPLE_ZFS
    SOLARIS_VAR_PARTITION("6A8EF2E9-1DD2-11B2-99A6-080020736631"),
    SOLARIS_HOME_PARTITION("6A90BA39-1DD2-11B2-99A6-080020736631"),
    SOLARIS_ALTERNATE_SECTOR("6A9283A5-1DD2-11B2-99A6-080020736631"),
    SOLARIS_RESERVED_PARTITION_1("6A945A3B-1DD2-11B2-99A6-080020736631"),
    SOLARIS_RESERVED_PARTITION_2("6A9630D1-1DD2-11B2-99A6-080020736631"),
    SOLARIS_RESERVED_PARTITION_3("6A980767-1DD2-11B2-99A6-080020736631"),
    SOLARIS_RESERVED_PARTITION_4("6A96237F-1DD2-11B2-99A6-080020736631"),
    SOLARIS_RESERVED_PARTITION_5("6A8D2AC7-1DD2-11B2-99A6-080020736631"),

    // NetBSD
    NETBSD_SWAP_PARTITION("49F48D32-B10E-11DC-B99B-0019D1879648"),
    NETBSD_FFS_PARTITION("49F48D5A-B10E-11DC-B99B-0019D1879648"),
    NETBSD_LFS_PARTITION("49F48D82-B10E-11DC-B99B-0019D1879648"),
    NETBSD_RAID_PARTITION("49F48DAA-B10E-11DC-B99B-0019D1879648"),
    NETBSD_CONCATENATED_PARTITION("2DB519C4-B10F-11DC-B99B-0019D1879648"),
    NETBSD_ENCRYPTED_PARTITION("2DB519EC-B10F-11DC-B99B-0019D1879648"),

    // ChromeOS
    CHROMEOS_KERNEL("FE3A2A5D-4F32-41A7-B725-ACCC3285A309"),
    CHROMEOS_ROOTFS("3CB8E202-3B7E-47DD-8A3C-7FF2A13CFCEC"),
    CHROMEOS_FUTURE_USE("2E0A753D-9E48-43B0-8337-B15192CB1B5E");

    /** Logger */
    private static final Logger log = Logger.getLogger(GptPartitionTypes.class);

    private UUID uuid;

    private GptPartitionTypes() {
    }

    private GptPartitionTypes(String guid) {
        this.uuid = UUID.fromString(guid);
    }

    /**
     * Looks up a partition type from its GUID.
     *
     * @param partitionTypeGuid the GUID to lookup.
     * @return the type or {@link #UNKNOWN} if no match is found.
     */
    public static GptPartitionTypes lookUp(byte[] partitionTypeGuid) {

        StringBuilder uuidBuilder = new StringBuilder();

        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[3], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[2], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[1], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[0], 2));
        uuidBuilder.append("-");
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[5], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[4], 2));
        uuidBuilder.append("-");
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[7], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[6], 2));
        uuidBuilder.append("-");
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[8], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[9], 2));
        uuidBuilder.append("-");
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[10], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[11], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[12], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[13], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[14], 2));
        uuidBuilder.append(NumberUtils.hex(partitionTypeGuid[15], 2));

        try {
            UUID uuidToMatch = UUID.fromString(uuidBuilder.toString());

            for (GptPartitionTypes type : GptPartitionTypes.values()) {
                if (uuidToMatch.equals(type.uuid)) {
                    return type;
                }
            }
        }
        catch (Exception e) {
            log.warn("Exception checking uuid: " + uuidBuilder.toString());
        }

        return UNKNOWN;
    }
}
