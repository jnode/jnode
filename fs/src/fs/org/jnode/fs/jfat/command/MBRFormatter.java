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
 
package org.jnode.fs.jfat.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.partitions.ibm.MasterBootRecord;
import org.jnode.util.FileUtils;

/**
 * The MBRFormatter is the main class for writing the stage1 and stage1.5 to the
 * MBR.
 * 
 * @author tango
 */
class MBRFormatter {
    private static final Logger log = Logger.getLogger(MBRFormatter.class);
    private MasterBootRecord stage1;

    static final String GRUB_HOME = "/devices/sg0/boot/grub/";
    
    /**
     * The Source path for the Grub in CD://devices/sg0/boot/grub/STAGE1.
     * Because the grub can installed from the Live Boot CD.
     */
    private final String stageResourceName1 = GRUB_HOME + "grub.s1";

    /**
     * Reading the Grub stages from the Rescue Device.
     * 
     * @param stage1ResourceName
     * @return
     * @throws java.io.IOException
     */
    private MasterBootRecord getStage1(String stage1ResourceName) throws GrubException {
        if (stage1 == null) {
            try {
                File file = new File(stage1ResourceName);
                InputStream is = new FileInputStream(file);
                byte[] buf = new byte[512];
                FileUtils.copy(is, buf);
                is.close();
                stage1 = new MasterBootRecord(buf);
            } catch (IOException e) {
                throw new GrubException("error while reading stage1", e);
            }
        }
        return stage1;
    }

    /**
     * 
     * @param device
     * @param bsize
     * @throws FileSystemException
     * @throws IOException
     */
    public void format(BlockDeviceAPI devApi) throws GrubException {

        log.info("Checking the old MBR...");
        MasterBootRecord oldMbr;
        try {
            oldMbr = new MasterBootRecord(devApi);
        } catch (IOException e) {
            throw new GrubException("error while reading MBR", e);
        }

        if (!oldMbr.containsPartitionTable()) {
            throw new GrubException("This device doesn't contain a valid MBR.");
        }
        log.info("done.");

        /*
         * int add=LittleEndian.getInt32(MBR.array(),0x44);
         * System.out.println("The value at the position 0x44 is-> "
         * +Integer.toHexString(add));
         */

        stage1 = getStage1(stageResourceName1);
        /**
         * The BPB stands for the Bios Parameter Block.As the BPB of a disk is
         * fixed and it is written to the disk during the partitioning of the
         * disk. The BPB is present between the position of 0x3 to 0x48
         * position.
         * 
         * NOTE: 1) Here need to make the BPB more independently(ie without
         * array of the BPB using it in MBR)
         * 
         * 2)The next Important matter is here that in the MBR's <b> 0x44 th</b>
         * position we setting the position of the stage1.5 or Stage2.here as i
         * used the Stage1.5 at the Sector 1(second sector) so The Value is set
         * here as 01 00 00 00
         * 
         * 3)In the Position of the 0x40: The boot drive. If it is 0xFF, use a
         * drive passed by BIOS. The value is 0x80 for HDD.I kept it default
         * here.
         * 
         * 4)0x42: The starting address of Stage 2 or Stage1.5. As here i used
         * the Stage1.5;hence the value i set here 0x2000 If it is Stage2 then
         * it will be 0x8000.
         * 
         * 5)0x48: The starting segment of Stage 2 or Stage1.5. Here for
         * stage1.5 i used the value 0x20 For stage2 it will be 0x80.
         * 
         * TODO: In this portion we need to use dynamically the BPB values. And,
         * that time at the stage1 buffer the EMBEDDED variables need to set
         * here individually.
         * 
         * BUGs REPORT: Using statically the value of the BPB.and setted the
         * EMBEDDED variables in that array statically.It is not good. ;-)
         * 
         */
        stage1.setBPB(BPB);

        /**
         * The Partition table is the cruisal part of the HDD formatted with
         * different FS.For the grub disk the Stage1 is kept upto the first
         * 446bytes to the MBR.Then after the 64 bytes are kept for Setting the
         * PArtition table.
         * 
         * N.B. : The grub will be written actually always after the Partition
         * Table written to the HDD.IT is very IMPORTANT.
         * 
         */
        stage1.copyPartitionTableFrom(oldMbr);

        /**
         * Checking the BootSector is Valid or not. Actually here need to check
         * the Sector Signature. 0x55AA --->
         * 
         */
        if (!stage1.containsPartitionTable()) {
            throw new GrubException("The new boot sector is not valid");
        }

        try {
            /**
             * write the GRUB's stage1 to the MBR
             */
            System.out.print("Writing stage 1 ... ");
            stage1.write(devApi);
            System.out.println("done.");

        } catch (IOException e) {
            throw new GrubException("Failed writing boot sector");
        }

        System.out.println("Writing stage 1 has been completed.");
    }

    /**
     * The BPB-Bios Parameter Block is kept here. I am Confusing yet with That.I
     * will Change it soon.
     */
    private static final byte[] BPB = {
        (byte) 0xD0, (byte) 0xBC, (byte) 0x00, (byte) 0x7C, (byte) 0xFB, (byte) 0x50,
        (byte) 0x07, (byte) 0x50, (byte) 0x1F, (byte) 0xFC, (byte) 0xBE, (byte) 0x1B,
        (byte) 0x7C, (byte) 0xBF, (byte) 0x1B, (byte) 0x06, (byte) 0x50, (byte) 0x57,
        (byte) 0xB9, (byte) 0xE5, (byte) 0x01, (byte) 0xF3, (byte) 0xA4, (byte) 0xCB,
        (byte) 0xBE, (byte) 0xBE, (byte) 0x07, (byte) 0xB1, (byte) 0x04, (byte) 0x38,
        (byte) 0x2C, (byte) 0x7C, (byte) 0x09, (byte) 0x75, (byte) 0x15, (byte) 0x83,
        (byte) 0xC6, (byte) 0x10, (byte) 0xE2, (byte) 0xF5, (byte) 0xCD, (byte) 0x18,
        (byte) 0x8B, (byte) 0x14, (byte) 0x8B, (byte) 0xEE, (byte) 0x83, (byte) 0xC6,
        (byte) 0x10, (byte) 0x49, (byte) 0x74, (byte) 0x16, (byte) 0x38, (byte) 0x2C,
        (byte) 0x74, (byte) 0xF6, (byte) 0xBE, (byte) 0x10, (byte) 0x07, (byte) 0x03,
        (byte) 0x02, (byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x01,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02
    };
}
