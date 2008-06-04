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

package org.jnode.test.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.ramdisk.RamDiskDevice;
import org.jnode.driver.block.ramdisk.RamDiskDriver;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.util.FSUtils;
import org.jnode.naming.InitialNaming;
import org.jnode.util.NumberUtils;

/**
 * @author Fabien DUMINY
 */
public class TestUtils {
    /**
     * @param filename
     * @param size     a number eventually followed by a multiplier (K: Kilobytes, M:
     *                 Megabytes, G:Gigabytes)
     * @return
     * @throws IOException
     */
    public static File makeTempFile(String filename, String size)
        throws IOException {
        File tempFile = File.createTempFile(filename, "");
        tempFile.deleteOnExit();

        return makeFile(tempFile.getAbsolutePath(), NumberUtils.getSize(size));
    }

    public static File makeFile(String filename, long size) throws IOException {
        byte[] buf = new byte[1024];
        File file = new File(filename);
        FileOutputStream output = new FileOutputStream(file);

        long nbBlocks = size / buf.length;
        for (long i = 0; i < nbBlocks; i++) {
            output.write(buf);
        }

        int remain = (int) (size % 1024);
        if (remain != 0)
            output.write(buf, 0, remain);

        output.flush();
        output.close();

        return file;
    }

    public static void listEntries(Iterator<? extends FSEntry> iterator) throws Exception {
        log.debug("<<< BEGIN listEntries >>>");
        int i = 0;
        log.debug("------- entries ------");
        while (iterator.hasNext()) {
            FSEntry entry = iterator.next();
            log.debug(i + ":" + entry);
            i++;
        }
        log.debug("--- End of entries ---");
        log.debug("<<< END listEntries >>>");
    }

    public static List<String> getEntryNames(Iterator<? extends FSEntry> it) {
        List<String> names = new ArrayList<String>();
        while (it.hasNext()) {
            FSEntry entry = it.next();
            names.add((entry == null) ? null : entry.getName());
        }
        return names;
    }

    public static String toString(String filename, int offset, int length)
        throws IOException {
        // byte[] buf = new byte[1024];
        File file = new File(filename);
        FileInputStream input = new FileInputStream(file);
        byte[] data = new byte[length];
        int nb = input.read(data);
        String dump = FSUtils.toString(data, 0, nb);
        input.close();
        return dump;
    }

    public static String toString(String[] array) {
        StringBuffer sb = new StringBuffer("[");

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }

                sb.append(array[i]);
            }
        }

        sb.append("]");
        return sb.toString();
    }

    public static String toString(List<String> list) {
        StringBuffer sb = new StringBuffer("[");

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(list.get(i));
        }

        sb.append("]");
        return sb.toString();
    }

    public static File copyFile(String srcFile, String destFile)
        throws SecurityException, IOException {
        return copyInputStreamToFile(new FileInputStream(new File(srcFile)),
            destFile);
    }

    public static File copyInputStreamToFile(InputStream src, String destFile)
        throws SecurityException, IOException {
        File dest = new File(destFile);

        if (dest.exists())
            dest.delete();

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];
            int read = -1;
            do {
                read = src.read(buffer, 0, buffer.length);
                if (read > 0) {
                    fos.write(buffer, 0, read);
                }
            } while (read != -1);
        } finally {
            if (src != null)
                src.close();

            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }

        return dest;
    }

    public static File copyDeviceToFile(Device imageDevice, String destFile)
        throws SecurityException, IOException, ApiNotFoundException {
        File dest = new File(destFile);
        BlockDeviceAPI imgApi = imageDevice
            .getAPI(BlockDeviceAPI.class);

        if (dest.exists())
            dest.delete();

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];
            int toRead = 0;
            long devOffset = 0;
            long remaining = imgApi.getLength();
            while (remaining > 0) {
                toRead = (int) Math.min(buffer.length, remaining);
                imgApi.read(devOffset, ByteBuffer.wrap(buffer, 0, toRead));
                fos.write(buffer, 0, toRead);

                devOffset += toRead;
                remaining -= toRead;
            }
        } finally {
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }

        return dest;
    }

    /*
     * public static Device getFormattedPhysicalDevice(TestConfig config) throws
     * SecurityException, NameNotFoundException, IOException,
     * FileSystemException { Class fsClass = config.getFsClass(); Device device =
     * config.getDevice(); // create a formatted FileSystem // by using the
     * appropriate JNode FileSystem formatter FileSystem fs = null; //
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! //
     * This operation is VERY DANGEROUS !!! It format a device. // And it can be
     * VERY LONG TO PROCESS (if device has a big capacity) // But all this is
     * needed for JUnit test on physical devices. //
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * FileSystemType type = TestUtils.getFsType(fsClass); try { fs =
     * TestUtils.formatDevice(device, fsClass); } finally { if(fs != null)
     * fs.close(); } return device; }
     */

    public static void copyInputStreamToDevice(InputStream imageStream,
                                               Device workDevice) throws ApiNotFoundException,
        NameNotFoundException, IOException, FileSystemException {
        BlockDeviceAPI wrkApi = workDevice
            .getAPI(BlockDeviceAPI.class);

        int sectorSize = 512;
        byte[] sector = new byte[sectorSize];
        long nbSectors = wrkApi.getLength() / sectorSize;
        long devOffset = 0;
        for (int s = 0; s < nbSectors; s++) {
            // log.debug("copying sector "+s);
            int nbRead = imageStream.read(sector);
            if (nbRead < 0)
                break;

            wrkApi.write(devOffset, ByteBuffer.wrap(sector, 0, nbRead));
            devOffset += nbRead;
        }
    }

    public static void copyDevice(Device imageDevice, Device workDevice)
        throws ApiNotFoundException, IOException {
        BlockDeviceAPI imgApi = imageDevice
            .getAPI(BlockDeviceAPI.class);
        BlockDeviceAPI wrkApi = workDevice
            .getAPI(BlockDeviceAPI.class);

        if (imgApi.getLength() != wrkApi.getLength())
            throw new IllegalArgumentException("devices of different length");
        // if(imgApi.getSectorSize() != wrkApi.getSectorSize())
        // throw new IllegalArgumentException("devices of different sector
        // size");

        // int sectorSize = imgApi.getSectorSize();
        int sectorSize = 512;
        byte[] sector = new byte[sectorSize];
        long nbSectors = imgApi.getLength() / sectorSize;
        long devOffset = 0;
        for (int s = 0; s < nbSectors; s++) {
            // log.debug("copying sector "+s);
            imgApi.read(devOffset, ByteBuffer.wrap(sector, 0, sector.length));
            wrkApi.write(devOffset, ByteBuffer.wrap(sector, 0, sector.length));
            devOffset += sectorSize;
        }
    }

    /*
     * static protected InputStream createImageFileDevice(TestConfig config)
     * throws NameNotFoundException, IOException, FileSystemException { Class
     * fsClass = config.getFsClass(); int sizeInKb = config.getSizeInKb();
     * Object formatOptions = config.getFormatOptions(); // create an image file
     * of a formatted FileSystem // by using the appropriate JNode FileSystem
     * formatter File origFile = TestUtils.makeFile(config.getImageFile(),
     * sizeInKb); InputStream imageFile = new FileInputStream(origFile);
     * FileDevice device = null; FileSystem fs = null; try { device = new
     * FileDevice(origFile, "rw"); fs = TestUtils.formatDevice(device, fsClass,
     * formatOptions); } finally { if(device != null) device.close(); if(fs !=
     * null) fs.close(); } return imageFile; }
     */
    /*
     * public static void deleteFile(TestConfig config, FileDevice device) { new
     * File(device.getFileName()).delete(); }
     */
    public static String[] append(String[] a1, String[] a2) {
        if (a1 == null)
            return a2;
        if (a2 == null)
            return a1;

        String[] a = new String[a1.length + a2.length];
        System.arraycopy(a1, 0, a, 0, a1.length);
        System.arraycopy(a2, 0, a, a1.length, a2.length);

        return a;
    }

    public static byte[] getTestData(int lengthInWords) {
        byte[] data = new byte[lengthInWords * 2];
        int index = 0;
        for (int i = 0; i < lengthInWords; i++) {
            data[index] = (byte) (0xFF00 & i);
            index++;

            data[index] = (byte) (0x00FF & i);
            index++;
        }

        return data;
    }

    public static boolean equals(byte[] origData, byte[] data) {
        // return Arrays.equals(origData, data);
        if (origData == data)
            return true;
        if (origData == null || data == null) {
            // log.debug("equals(byte[], byte[]): only one array is null");
            return false;
        }

        int length = origData.length;
        if (data.length != length) {
            // log.debug("equals(byte[], byte[]): array lengths are different");
            return false;
        }

        for (int i = 0; i < length; i++)
            if (origData[i] != data[i]) {
                // log.debug("equals(byte[], byte[]): array are different at
                // index 0x"+Integer.toHexString(i));
                // log.debug("origData:\n"+FSUtils.toString(origData, 0, 512));
                // log.debug("data:\n"+FSUtils.toString(data, 0, 512));
                return false;
            }

        return true;
    }

    public static RamDiskDevice createRamDisk(int size) {
        RamDiskDevice dev = null;
        try {
            final DeviceManager dm = InitialNaming
                .lookup(DeviceManager.NAME);
            dev = new RamDiskDevice(null, "dummy", size);
            dev.setDriver(new RamDiskDriver(null));
            dm.register(dev);
        } catch (NameNotFoundException e) {
            log.error(e);
        } catch (DeviceAlreadyRegisteredException e) {
            log.error(e);
        } catch (DriverException e) {
            log.error(e);
        }

        /*
         * FileSystemService fileSystemService = (FileSystemService)
         * InitialNaming .lookup(FileSystemService.NAME); FileSystemType type =
         * fileSystemService
         * .getFileSystemType(FatFileSystemType.NAME);
         * type.format(dev, new Integer(Fat.FAT16)); // restart the device
         * log.info("Restart initrd ramdisk"); dm.stop(dev); dm.start(dev);
         */
        return dev;
    }

    /**
     * @param className
     * @param cls
     * @return
     * @throws Exception
     */
    public static Object newInstance(String className, Class<?> cls)
        throws Exception {
        Class<?> clazz = Class.forName(className);
        Object instance = clazz.newInstance();
        if ((instance != null) && !cls.isAssignableFrom(instance.getClass()))
            throw new IllegalArgumentException(className
                + " is not an instanceof " + cls.getName());

        return instance;
    }

    private static final Logger log = Logger.getLogger(TestUtils.class);
}
