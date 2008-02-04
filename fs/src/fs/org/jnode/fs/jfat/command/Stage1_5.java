/*
 * $Id: MBRFormatter.java  7-5-2007 Tanmoy $
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
package org.jnode.fs.jfat.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.fs.FileSystemException;
import org.jnode.util.FileUtils;
import org.jnode.util.LittleEndian;

/**
 * The MBRFormatter is the main class for writing the stage1 and stage1.5
 * to the MBR.
 *
 * @author tango
 */
class Stage1_5 {
	private static final Logger log = Logger.getLogger(Stage1_5.class);
	private byte[] stage1_5;

    final static String GRUB_HOME = "/devices/sg0/boot/grub/";
    /**
     * The Source path for the Grub in CD://devices/sg0/boot/grub/STAGE1.
     * Because the grub can installed from the Live Boot CD.
     */
    final private String stageResourceName2 =  GRUB_HOME + "fat.s15";

    private static int INSTALL_PARTITION = 0xFFFFFF;
    private String configFile;
    //The Embedded Variables values in Jnode
    final private int SAVED_ENTRY_NUMBER=0xe;
    final private String CONFIG_FILE_NAME="/boot/grub/menu.lst";

    /**
     * The Method for reading the stage1.5 from the Rescue Disk.
     * @param stage2ResourceName
     * @return
     * @throws java.io.IOException
     */
    private byte[] getStage1_5(String stage2ResourceName) throws GrubException {
        if (stage1_5 == null) {
            byte[] buf;
            try {
				File file = new File(stage2ResourceName);
				InputStream is = new FileInputStream(file);
				buf = new byte[(int) file.length()];
				FileUtils.copy(is, buf);
				is.close();
			} catch (IOException e) {
	        	throw new GrubException("error while reading stage 1.5", e);
			}
			stage1_5 = buf;
        }
        return stage1_5;
    }

    /**
     * The method that will write the stage1.5 for the File System
     * specific  to the  Boot-sector to the block device.
     * @throws GrubException
     *
     *
     */
    private final static void writeStage1_5(long stage1_5_start,
    		                               ByteBuffer stage1_5,
                                           BlockDeviceAPI devApi) throws GrubException {
        try {
            devApi.write(stage1_5_start,(stage1_5));
        }catch (IOException e) {
        	throw new GrubException("error while writing stage 1.5", e);
        }
    }

    /**
     *
     * @param device
     * @param bsize
     * @throws FileSystemException
     * @throws IOException
     */
    public  void format(BlockDeviceAPI devApi, int partitionNumber) throws GrubException {
        System.out.println("Installation of Stage1.5");
        stage1_5 = getStage1_5(stageResourceName2);

        int size=stage1_5.length/IDEConstants.SECTOR_SIZE;
        log.info("The Size of the stage1_5 is  : "+size);



        /**
         * The most important stage of the GRUB BOOTING. THE stage1.5.
         *
         * The Embedded variables for the grub setting into
         * the JNode's grub stage1.5
         */


        /**
         * The Blocklists for JNode grub installer is setting to
         * the (512-4)th position of the Sector1 of the Stage1.5.
         * Blocklists is the size of the stage1.5 in the sectors unit.
         *
         **/
        setLittleEnd_BlockLists(stage1_5,size);



        /** Fixup the install partition */
        setLittleEnd_InstallPartition(stage1_5,INSTALL_PARTITION);



        setConfigFile(CONFIG_FILE_NAME);

        /** The Saved Entry Number **/
        setLittleEnd_EntryNumber(stage1_5,SAVED_ENTRY_NUMBER);


        /**
         * The most important section of the Grub
         * The path of the stage2 in the stage1.5
         *
         * NOTE: Here at the ox19 offset of the second
         * Sector of the stage1.5. the value of the Drive Path
         * is kept where the stage2 is kept.
         * Ex: as here the /dev/hd0 is used (ie the partition where
         * the FATfs is kept and where the stage2 will keep.
         * So here the value set as 0x00.
         *
         * The path of the stage2 is very important.Otherwise it will can
         * create ERROR 17.
         *
         * Suppose (hd0,1)/boot/grub/stage2--
         * (hd0,1) corresponds to linux partition /dev/hda2
         * (or /dev/sda2, depending on bios).So hd0 is the first hard disk found by bios.
         * The "1" stands for partition number starting from "0".
         * Under linux partition   numbers start with 1. Therefore,
         * the number differs.When this path
         * is patched into stage1.5 at position 512+0x12+5, then the device specification
         * (hd0,1) is converted to binary, e.g. 0x8001ffff
         * (0x80 first hard disk, 0x01 first partition, 0xffff
         * only for BSD partition).The directory /boot/grub/stage2 is relative to the
         * partition, so if you have a /boot partition, then the path would be just /grub/stage2.
         * Normally grub should detect the mapping of unix partition to its own
         * partition numbering scheme automatically. In some cases this does
         * not work, e.g. if you have multiple hard disks, the numbering of your
         * BIOS is hard to predict.  Grub uses a file device.map where you can change
         * the numbering manually.
         *
         *
         * <b>BUGS:</b>1) As currently it is only statically written here the
         * value of the 0x00; so the stage2 is need to only kept at
         * the /devices/hdb0.For supporting it in the any partition
         * here need to change once little bit logic.
         *
         * 2)Need to support the Device.map for MUltiple Disk supporting in
         * the JNODE.
         *
         **/
        setLittleEnd_DrivePath(stage1_5, partitionNumber);


        /**
         * Fixup the config file
         * TODO: here to be change that the Config File
         * will write after skipping the /boot/grub/stage2
         *
         */
        if (configFile != null) {
            int ofs = 512 + 0x27;
            while (stage1_5[ofs] != 0) {
                ofs++;
            }
            ofs++; /* Skip '\0' */
            for (int i = 0; i < configFile.length(); i++) {
                stage1_5[ofs++] = (byte) configFile.charAt(i);
            }
            stage1_5[ofs] = 0;
        }


        /**
         * The Method for writing the Stage1.5 to
         * the Sector 1 actually to the second sector.
         *
         *
         */
        writeStage1_5(IDEConstants.SECTOR_SIZE, ByteBuffer.wrap(stage1_5), devApi);

        System.out.println("Writing stage 1.5 has been completed.");
    }

    /**
     * The Install Partition setting
     * @arch i386
     * @param stage1_5
     * @param installPartition2
     */
    private void setLittleEnd_InstallPartition(byte[] stage1_5, int installPartition) {
    	LittleEndian.setInt32(stage1_5, 512 + 0x08, installPartition);

	}
    /**
     * The saved Entry Number setting.
     * @arch i386
     * @param stage1_5
     * @param i
     */
	private void setLittleEnd_EntryNumber(byte[] stage1_5, int i) {
    	LittleEndian.setInt32(stage1_5,512+0xc,i);

	}
    /**
     * The BlockLists if the stage1.5 is setting here.
     * @arch:i386
     * @param stage1_5
     * @param size
     */
	private void setLittleEnd_BlockLists(byte[] stage1_5, int size) {
    	LittleEndian.setInt16(stage1_5,512-4 ,size);

	}
    /**
     *
     * Setting the Drive path to the stage1.5.Though it is BUGGY yet.
     * @arch i386
     * @param stage1_5
     * @param i
     */
	private void setLittleEnd_DrivePath(byte[] stage1_5, int i) {
    	LittleEndian.setInt8(stage1_5,512+0x19,i);
	}


	/**
	 * The reading method of the Configuration File.
	 * @return
	 */
	public String getConfigFile() {
        return configFile;
    }

	/**
	 * The writing method of the Configuration file to the disk.
	 * @param configFile
	 */
    private void setConfigFile(String configFile) {
        this.configFile = configFile;
    }
}
