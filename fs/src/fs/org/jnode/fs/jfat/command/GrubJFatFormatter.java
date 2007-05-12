/*
 * $Id: GrubJFatFormatter.java Tanmoy $
 *
 * JNode.org
 * Copyright (C) 2007 JNode.org
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

import java.io.*;
import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemException;


/**
 * File :GrubFatFormatter.java
 * <p/>
 * The very important file for the Grub Installation. Here the
 * methods for  setting the Stage2 to the partition is  kept.
 *
 * @author Tango Devian
 */
public class GrubJFatFormatter {
    private static final Logger log = Logger.getLogger(GrubJFatFormatter.class);
    private static final String GRUB_STAGE_2 = "/devices/sg0/boot/grub/grub.s2";
    private static final String GRUB_MENU_LST = "/devices/sg0/boot/grub/menu.lst";

    /**
     * @throws org.jnode.fs.FileSystemException
     *
     * @throws org.jnode.fs.FileSystemException
     *
     * @throws org.jnode.fs.FileSystemException
     *
     * @throws java.io.IOException
     * @throws java.io.IOException
     * @see org.jnode.fs.fat.FatFormatter#format(org.jnode.driver.block.BlockDeviceAPI)
     */
    public void format(String path) throws FileSystemException, IOException {
        // writting of the stage2 and menu.LST
        try {
            File destDir = new File(path + "/boot/grub/");
            if(!destDir.exists()){
                System.out.print("Creating directory: " + destDir.getAbsolutePath() + " ... ");
                destDir.mkdirs();
                System.out.println("done.");
            }

            System.out.print("Writing stage 2 ... ");
            copyFAT(GRUB_STAGE_2, destDir.getAbsolutePath(), "stage2");
            System.out.println("done.");

            System.out.print("Writing menu.lst ... ");
            copyFAT(GRUB_MENU_LST, destDir.getAbsolutePath(), "menu.lst");
            System.out.println("done.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(File srcFile, File destFile) throws IOException {

        InputStream in = new FileInputStream(srcFile);
        OutputStream out = new FileOutputStream(destFile);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) >= 0) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        in.close();

    }

    private static void copyFAT(String srcFileCopy, String destFileCopy, String destFileName) throws IOException {

        // make sure the source file is indeed a readable file
        File srcFile = new File(srcFileCopy);
        if (!srcFile.isFile() || !srcFile.canRead()) {
            throw new IllegalArgumentException("Not a readable file: " + srcFile.getName());
        }

        // make sure the second argument is a directory
        File destDir = new File(destFileCopy);

        // create File object for destination file
        File destFile = new File(destDir, destFileName);

        // copy file, optionally creating a checksum
        copyFile(srcFile, destFile);
    }
}
