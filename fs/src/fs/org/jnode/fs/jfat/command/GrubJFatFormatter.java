package org.jnode.fs.jfat.command;


import java.io.*;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.jfat.BootSector;
import org.jnode.util.FileUtils;
import org.jnode.util.LittleEndian;

/**
 * File :GrubFatFormatter.java
 * <p/>
 * The very important file for the Grub Installation. Here the methods for write
 * to the MBR and the setting the Stage2 are kept.
 *
 * @author Tango Devian
 */
class GrubJFatFormatter {
    private static final Logger log = Logger.getLogger(GrubJFatFormatter.class);

    // The variables parameters declaration
    byte[] stage1;

    byte[] stage1_5;
    /**
     * The Source path for the Grub in CD://devices/sg0/boot/grub/STAGE1.
     * Because the grub can installed from the Live Boot CD.
     */
    final String stageResourceName1 = "//devices/sg0/BOOT/GRUB/STAGE1.";

    final String stageResourceName1_5 = "//devices/sg0/BOOT/GRUB/FAT1_5.";

    private static int installPartition = 0xFFFFFFFF;

    private String configFile;

    private int bootSectorOffset;
    private static boolean clock = true;
    private static boolean verify = true;

    /**
     * Create the actual bootsector.
     */
    private BootSector createBootSector(String stage1Name, String stage1_5Name,
                                        BlockDeviceAPI devApi) throws Exception {
        System.out.println("The createbootsector entered.");
        if (stage1Name == null) {
            System.out.println("hi i am in createbotsector....");
            stage1Name = "//devices/sg0/BOOT/GRUB/STAGE1.";
        }
        if (stage1_5Name == null) {
            stage1_5Name = "//devices/sg0/BOOT/GRUB/FAT1_5.";
        }
        try {
            stage1 = getStage1(stage1Name);
            stage1_5 = getStage1_5(stage1_5Name);
            return new GrubJFatBootSector(getStage1(stageResourceName1));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * Reading the Grub stages from the device
     *
     * @param stage1ResourceName
     * @return
     * @throws IOException
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

    public byte[] getStage1_5(String stage1_5ResourceName) throws IOException {
        if (stage1_5 == null) {
            File file = new File(stage1_5ResourceName);
            InputStream in = new FileInputStream(file);
            byte[] buf = new byte[(int) file.length()];
            FileUtils.copy(in, buf);
            in.close();
            stage1_5 = buf;
        }
        return stage1_5;

    }

    /**
     * The method that will write the stage1.5 for the fat specific to the
     * Boot-sector to the block device.
     */
    public final static void writeStage1_5(long stage1_5_start, byte[] stage1_5,
                                           BlockDeviceAPI devApi) {
        try {
            devApi.write(stage1_5_start, ByteBuffer.wrap(stage1_5));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @throws FileSystemException
     * @throws FileSystemException
     * @throws FileSystemException
     * @throws IOException
     * @throws IOException
     * @see org.jnode.fs.fat.FatFormatter#format(BlockDeviceAPI)
     */
    public final void format(Device device, String path) throws FileSystemException, IOException {

        System.out.println("The format(device) entered.");
        BlockDeviceAPI devApi;
        try {

            devApi = device.getAPI(BlockDeviceAPI.class);
            System.out.println("The devAPI initialization successfully....");
        } catch (ApiNotFoundException e) {
            throw new FileSystemException("Device is not a partition!", e);
        }
        System.out.println("The GrubJFatBootSector starting now......");
        try {
            GrubJFatBootSector bs = (GrubJFatBootSector) createBootSector(
                    stageResourceName1, stageResourceName1_5, devApi);

            try {
                bs.write(devApi);
                // write the GRUB's stage1 to the MBR
                // Writing Stage1
                devApi.flush();

                System.out.println("The stage1 is written successfully...");

                stage1_5 = getStage1_5(stageResourceName1_5);

                System.out.println("The stage1_5 buffer is created successfully....");
                // writting the stage1.5;Here it is FatStage1_5
                LittleEndian.setInt32(stage1_5, 512 - 8, bootSectorOffset + 2);
                /* Fixup the install partition */
                LittleEndian.setInt32(stage1_5, 512 + 0x08, installPartition);

                setConfigFile("/boot/grub/menu.lst");
                /* Fixup the config file */
                if (configFile != null) {
                    int ofs = 512 + 0x12;
                    while (stage1_5[ofs] != 0) {
                        ofs++;
                    }
                    ofs++; /* Skip '\0' */
                    for (int i = 0; i < configFile.length(); i++) {
                        stage1_5[ofs++] = (byte) configFile.charAt(i);
                    }
                    stage1_5[ofs] = 0;
                }
                System.out.println("grub version [");
                int i;
                for (i = 512 + 0x12; stage1_5[i] != 0; i++) {
                    System.out.print((char) stage1_5[i]);
                }
                System.out.println("[ config ]");
                i++;
                for (; stage1_5[i] != 0; i++) {
                    System.out.println((char) stage1_5[i]);
                }
                // writting the stage1.5
                System.out.println("the stage1_5 is writing now...pls wait...");

                writeStage1_5(bs.getBytesPerSector(), stage1_5, devApi);
            } catch (IOException e) {
                System.out.println("The Bootsector Failed....");
            }
        } catch (Exception e) {
            System.out.println("The exception at format()");
        }

        System.out.println("Thanks...The stage1_5 is written successfully.....");

        // writting of the stage2 and menu.LST
        System.out.println("The Stage2 is now writing....");
        copyFAT("//devices/sg0/BOOT/GRUB/STAGE2_E.", path + "/boot/grub/");
        System.out.println("The Menu.LSt is now writting....");
        copyFAT("//devices/sg0/BOOT/GRUB/MENU.LST", path + "/boot/grub/");
        System.out.println("The Stage2 is successfully created....");
    }

    public String getConfigFile() {
        return configFile;
    }

    private void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public int getInstallPartition() {
        return installPartition;
    }

    public static void setInstallPartition(int installPartition1) {
        installPartition = installPartition1;
    }

    public static Long copyFile(File srcFile, File destFile) throws IOException {

        InputStream in = new FileInputStream(srcFile);
        OutputStream out = new FileOutputStream(destFile);
        long millis = System.currentTimeMillis();
        CRC32 checksum = null;
        if (verify) {
            checksum = new CRC32();
            checksum.reset();
        }
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) >= 0) {
            if (verify) {
                checksum.update(buffer, 0, bytesRead);
            }
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        in.close();
        if (clock) {
            millis = System.currentTimeMillis() - millis;
            System.out.println("Second(s): " + (millis / 1000L));
        }
        if (verify) {
            return new Long(checksum.getValue());
        } else {
            return null;
        }
    }

    public static void copyFAT(String srcFileCopy, String destFileCopy) throws IOException {

        // make sure the source file is indeed a readable file
        File srcFile = new File(srcFileCopy);
        if (!srcFile.isFile() || !srcFile.canRead()) {
            System.err.println("Not a readable file: " + srcFile.getName());
            System.exit(1);
        }
        // make sure the second argument is a directory
        File destDir = new File(destFileCopy);
        if (!destDir.exists()) {
            destDir.mkdirs();
            System.out.println("The BOOT/GRUB/ Directory is created...");
        }
        // create File object for destination file
        File destFile = new File(destDir, srcFile.getName());

        // copy file, optionally creating a checksum
        Long checksumSrc = copyFile(srcFile, destFile);
    }
}
