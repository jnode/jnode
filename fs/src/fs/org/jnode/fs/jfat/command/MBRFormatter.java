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

/**
 * The MBRFormatter is the main class for writing the stage1 and stage1.5
 * to the MBR.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.fs.FileSystemException;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.util.FileUtils;
import org.jnode.util.LittleEndian;

/**
 * @author tango
 */

public class MBRFormatter {
	private static final Logger log = Logger.getLogger(MBRFormatter.class);
	IBMPartitionTableEntry oldEntry;
    byte[] stage1;
    byte[] stage1_5;
    /**
     * The Source path for the Grub in CD://devices/sg0/boot/grub/STAGE1.
     * Because the grub can installed from the Live Boot CD. 
     */
    final String stageResourceName1 =  "/devices/sg0/boot/grub/grub.s1";
    final String stageResourceName2 =  "/devices/sg0/boot/grub/fat.s15";
    private static int INSTALL_PARTITION = 0xFFFFFF;
    private String configFile;
    //The Embedded Variables values in Jnode
    final int SAVED_ENTRY_NUMBER=0xe;
    final String CONFIG_FILE_NAME="/boot/grub/menu.lst";

    /**
     * 
     *  The reading of the OLD MBR
     * @throws java.io.IOException
     * 
     */
    private void checkMBR(ByteBuffer MBR) throws IOException
	{
		if (!IBMPartitionTable.containsPartitionTable(MBR.array()))
			throw new IOException("This device doesn't contain a valid MBR.");
	}    
    
    /**
     * Reading the Grub stages from the Rescue Device.
     *
     * @param stage1ResourceName
     * @return
     * @throws java.io.IOException
     */
    public byte[] getStage1(String stage1ResourceName) throws IOException {
        if (stage1 == null) {
            File file = new File(stage1ResourceName);
            InputStream is = new FileInputStream(file);
            byte[] buf = new byte[512];
            FileUtils.copy(is, buf);
            is.close();
            stage1 = buf;
        }
        return stage1;
    }
    
    /**
     * The Method for reading the stage1.5 from the Rescue Disk.
     * @param stage2ResourceName
     * @return
     * @throws java.io.IOException
     */
    public byte[] getStage1_5(String stage2ResourceName) throws IOException {
        if (stage1_5 == null) {
            File file = new File(stage2ResourceName);            
            InputStream is = new FileInputStream(file);
            byte[] buf = new byte[(int)file.length()];
            FileUtils.copy(is, buf);
            is.close();
            stage1_5 = buf;
        }
        return stage1_5;
        
    }
        
    /**
     * The method that will write the stage1.5 for the File System
     * specific  to the  Boot-sector to the block device.
     * 
     * 
     */
    public final static void writeStage1_5(long stage1_5_start, 
    		                               ByteBuffer stage1_5,
                                           BlockDeviceAPI devApi) {
        try {
            devApi.write(stage1_5_start,(stage1_5));        
        }catch (IOException e) {           
            e.printStackTrace();
            
        }
    }
    
    /**
     * @throws org.jnode.fs.FileSystemException
     * @throws org.jnode.fs.FileSystemException
     * @throws org.jnode.fs.FileSystemException
     * @throws java.io.IOException
     * @throws java.io.IOException
     * @see org.jnode.fs.fat.FatFormatter#format(org.jnode.driver.block.BlockDeviceAPI)
     */
    public  void format(Device device,int bsize) throws FileSystemException, IOException {
        BlockDeviceAPI devApi;
        try {
            devApi = device.getAPI(BlockDeviceAPI.class);           
        } catch (ApiNotFoundException e) {
            throw new FileSystemException("Device is not a partition!", e);
        } 

        log.info("Checking the old MBR...");
        ByteBuffer MBR=ByteBuffer.allocate(IDEConstants.SECTOR_SIZE);               
        devApi.read(0, MBR);        
        checkMBR(MBR);        
        log.info("done.");
        
        /*int add=LittleEndian.getInt32(MBR.array(),0x44);
        System.out.println("The value at the position 0x44 is-> "  +Integer.toHexString(add));*/
        byte[] partition=getPartitionTable(0x1be,64,MBR);
        
        if(!isaValidBootSector(MBR.array())){        	
        	log.error("The OLD Boot Sector is not valid.");        	
        }
        try{
        stage1=getStage1(stageResourceName1);
        }catch(FileNotFoundException e){
        	log.error("The stage1 is not available.");
        }
        /**
         * The BPB stands for the Bios Parameter Block.As the BPB of 
         * a disk is fixed and it is written to the disk during the 
         * partitioning of the disk. The BPB is present between the 
         * position of 0x3 to 0x48 position.
         * 
         * NOTE:
         * 1) Here need to make the BPB more independently(ie without 
         * array of the BPB using it in MBR)
         * 
         * 2)The next Important matter is here that in the MBR's
         * <b> 0x44 th</b> position we setting the position of the 
         * stage1.5 or Stage2.here as i used the Stage1.5 at the Sector 
         * 1(second sector) so The Value is set here as 01 00 00 00
         * 
         * 3)In the Position of the 0x40: 
         * The boot drive. If it is 0xFF, use a drive passed by BIOS.
         * The value is 0x80 for HDD.I kept it default here.
         * 
         * 4)0x42: The starting address of Stage 2 or Stage1.5.
         * As here i used the Stage1.5;hence the value i set here 0x2000
         * If it is Stage2 then it will be 0x8000. 
         * 
         * 5)0x48: The starting segment of Stage 2 or Stage1.5.
         * Here for stage1.5 i used the value 0x20
         * For stage2 it will be 0x80.
         * 
         * TODO: In this portion we need to use dynamically the BPB values.
         * And, that time at the stage1 buffer the EMBEDDED variables need to 
         * set here individually.
         * 
         * BUGs REPORT: Using statically the value of the BPB.and setted
         * the EMBEDDED variables in that array statically.It is not good. 
         * ;-)  
         * 
         */
        setBPB(BPB,stage1);
        /**
         * The Partition table is the cruisal part of the HDD formatted with
         * different FS.For the grub disk the Stage1 is kept upto the first 
         * 446bytes to the MBR.Then after the 64 bytes are kept for Setting
         * the  PArtition table.
         * 
         * N.B. : The grub will be written actually always after the 
         * Partition Table written to the HDD.IT is very IMPORTANT.
         * 
         */        
        setPartitionTable(0x1be,64,partition,stage1);
        
        /**
         * Checking the BootSector is Valid or not.
         * Actually here need to check the Sector Signature.
         * 0x55AA ---> 
         * 
         */
        if(!isaValidBootSector(stage1)){
        	log.error("The New Boot Sector Is Not Valid.");        	        	
        }
        
        try {  
        	  /**
        	   * write the GRUB's stage1 to the MBR 
        	   */          	                  	
        	    System.out.print("Writing stage 1 ... ");
            	devApi.write(0,ByteBuffer.wrap(stage1));            
                devApi.flush();
                System.out.println("done.");

                
                
                
                System.out.println("The Stage1.5 is now embedding.");
                try{
                stage1_5 = getStage1_5(stageResourceName2);
                }catch(FileNotFoundException ex){
                	log.error("The Stage1.5 is not available.");
                }
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
                setLittleEnd_DrivePath(stage1_5,bsize);
                

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
                
                
            } catch (IOException e) {
                System.out.println("The Bootsector Failed....");
            }
         

        System.out.println("Writing stage 1 and stage 1.5 has been completed.");

        
    
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
     * The Writing the BPB to the MBR to its Correct Position.
     * @param bpb2
     * @param stage12
     */
	private void setBPB(byte[] bpb2, byte[] stage12) {
    	System.arraycopy (bpb2 ,0, stage12, 0x3, bpb2.length );		
	}
	/**
	 * The Reading the Partition Table.
	 * @param offset
	 * @param len
	 * @param MBR
	 * @return
	 */
	protected byte[] getPartitionTable ( int offset, int len ,ByteBuffer MBR) {
        byte[] v = new byte[len];
        System.arraycopy (MBR.array() ,offset, v, 0, len );
        return v;
    }
	/**
	 * The writting method of the Partition table to the MBR to 
	 * 446bytes position to the (446+64) bytes position.
	 * @param offset
	 * @param len
	 * @param value
	 * @param MBR
	 */
    protected void setPartitionTable ( int offset, int len, byte[] value,byte[] MBR ) {
        System.arraycopy ( value, 0, MBR, offset, len );       
    }   
	/**
	 * The reading methof of the Configuration File.
	 * @return
	 */
	public String getConfigFile() {
        return configFile;
    }
	/**
	 * The writting method of the Configuration file to the disk.
	 * @param configFile
	 */
    private void setConfigFile(String configFile) {
        this.configFile = configFile;
    }
    /**
     * Reading the InstallPartition.
     * @return
     */
    public int getInstallPartition() {
        return INSTALL_PARTITION;
    }
    /**
     * The Writtting of the InstallPartition.
     * @param installPartition1
     */
    public static void setInstallPartition(int installPartition1) {
        INSTALL_PARTITION = installPartition1;
    }
    /**
     * 
     * The mathod for Checking that the Currrent Boot Sector is OK or
     * Not.The  Checking method is simple.Because to the BootSector's 
     * last two bytes if have 0xaa and 0x55; Then the Sector is OK.
     * @param MBR
     * @return
     */
    public boolean isaValidBootSector(byte[] MBR) {
    	if ( MBR.length >= 512 )
    	    return
    		( MBR[510] & 0xFF ) == 0x55 &&
    		( MBR[511] & 0xFF ) == 0xAA;
    	else
    	    return
    		false;
        }
    
    /**
     * The BPB-Bios Parameter Block is kept here.
     * I am Confusing yet with That.I will Change it soon.
     * 
     */
      private static final byte[] BPB={
    	//D0 BC 00 7C FB 50 07 50 1F FC BE 1B 7C
    	(byte)0xD0,
    	(byte)0xBC,
    	(byte)0x00,
    	(byte)0x7C,
    	(byte)0xFB,
    	(byte)0x50,
    	(byte)0x07,
    	(byte)0x50,
    	(byte)0x1F,
    	(byte)0xFC,
    	(byte)0xBE,
    	(byte)0x1B,
    	(byte)0x7C,
    	//BF 1B 06 50 57 B9 E5 01 F3 A4 CB BE BE 07 B1 04
    	(byte)0xBF,
    	(byte)0x1B,
    	(byte)0x06,
    	(byte)0x50,
    	(byte)0x57,
    	(byte)0xB9,
    	(byte)0xE5,
    	(byte)0x01,
    	(byte)0xF3,
    	(byte)0xA4,
    	(byte)0xCB,
    	(byte)0xBE,
    	(byte)0xBE,
    	(byte)0x07,
    	(byte)0xB1,
    	(byte)0x04,
    	//38 2C 7C 09 75 15 83 C6 10 E2 F5 CD 18 8B 14 8B
    	(byte)0x38,
    	(byte)0x2C,
    	(byte)0x7C,
    	(byte)0x09,
    	(byte)0x75,
    	(byte)0x15,
    	(byte)0x83,
    	(byte)0xC6,
    	(byte)0x10,
    	(byte)0xE2,
    	(byte)0xF5,
    	(byte)0xCD,
    	(byte)0x18,
    	(byte)0x8B,
    	(byte)0x14,
    	(byte)0x8B,
    	//EE 83 C6 10 49 74 16 38 2C 74 F6 BE 10 07 03 02
    	(byte)0xEE,
    	(byte)0x83,
    	(byte)0xC6,
    	(byte)0x10,
    	(byte)0x49,
    	(byte)0x74,
    	(byte)0x16,
    	(byte)0x38,
    	(byte)0x2C,
    	(byte)0x74,
    	(byte)0xF6,
    	(byte)0xBE,
    	(byte)0x10,
    	(byte)0x07,
    	(byte)0x03,
    	(byte)0x02,
    	//ff 00 00 20 01 00 00 00 02
    	(byte)0xff,
    	(byte)0x00,
    	(byte)0x00,
    	(byte)0x20,
    	(byte)0x01,
    	(byte)0x00,
    	(byte)0x00,
    	(byte)0x00,
    	(byte)0x00,    	
    	(byte)0x02   
    	};
}
