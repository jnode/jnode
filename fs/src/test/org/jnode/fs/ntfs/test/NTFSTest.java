/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.fs.ntfs.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.naming.NameNotFoundException;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.ntfs.NTFSFileSystem;
import org.jnode.fs.ntfs.NTFSFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;


/**
 * Ext2 fs test, reads a disk image
 * to be run outside of JNode
 *
 * @author Andras Nagy
 */
public class NTFSTest {

    org.jnode.fs.ntfs.NTFSFileSystem NTFSfs = null;
    File partImg = new File("e:/image2k.img");

    public NTFSTest() {
        //read the disk image of an ext2fs partition

        //FileDevice implements BlockDeviceAPI
        FileDevice fd = null;
        try {
            fd = new FileDevice(partImg, "r");
        } catch (IOException e) {
            System.out.println("error when reading disk image");
            System.exit(-1);
        }

        try {
            final FileSystemService fSS = InitialNaming.lookup(FileSystemService.NAME);
            NTFSFileSystemType type = fSS.getFileSystemType(NTFSFileSystemType.ID);
            NTFSfs = new NTFSFileSystem(fd, false, type);
        } catch (FileSystemException e) {
            System.out.println("error when constructing Ext2FileSystem");
            e.printStackTrace();
            System.exit(-1);
        } catch (NameNotFoundException e) {
            System.out.println("error while accessing file system service");
            System.exit(-1);
        }

        try {
            //FSDirectory root = NTFSfs.getRootEntry().getDirectory();
            //System.out.println(root.getEntry("text.txt").getName());
            //System.out.println("getRootEntry().isDirectory(): "+NTFSfs.getRootEntry().isDirectory());
            //System.out.println("getRootEntry().isFile(): "+NTFSfs.getRootEntry().isFile());
            list(null, null);

        } catch (IOException e) {
            System.out.println("error when parsing root directory");
            e.printStackTrace();
            System.exit(-1);
        }


    }

    public String[] list(File directory, FilenameFilter filter) throws IOException {
        final FSEntry entry = NTFSfs.getRootEntry();
        if (entry == null) {
            throw new FileNotFoundException(directory.getAbsolutePath());
        }
        if (!entry.isDirectory()) {
            throw new IOException("Cannot list on non-directories " + directory);
        }
        final ArrayList<String> list = new ArrayList<String>();
        for (Iterator<? extends FSEntry> i = entry.getDirectory().iterator(); i.hasNext();) {
            final FSEntry child = i.next();
            final String name = child.getName();
            if ((filter == null) || (filter.accept(directory, name))) {
                list.add(name);
            }
            if (child.isDirectory())
                child.getDirectory();
            else
                child.getFile();

            if (child.isFile())
                System.out.println(
                    "Name = \"" + name + "\" , Size = " + child.getFile().getLength() + ", IsDirectory = " +
                        child.isDirectory());
            else
                System.out.println(
                    "Name = \"" + name + "\" , IsDirectory = " + child.isDirectory());
        }

        return list.toArray(new String[list.size()]);
    }

    public void iterateRoot(FSDirectory root) {
        try {
            Iterator<? extends FSEntry> rootIterator;
            rootIterator = root.iterator();
            while (rootIterator.hasNext()) {
                FSEntry entry = rootIterator.next();
                System.out.println(entry.getName());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void printBootSectorData() {
        /*try
          {

              NTFSVolume volume = new NTFSVolume(this.stream);

              System.out.println("OEM ID = " + volume.getBootRecord().SystemID);
              System.out.println("Bytes Per Sector = " + volume.getBootRecord().BytesPerSector);
              System.out.println("Sectors Per Cluster = " + (int)volume.getBootRecord().SectorPerCluster);
              System.out.println("MediaDescriptor = 0x" + Integer.toHexString(volume.getBootRecord().
              MediaDescriptor).toUpperCase());
              System.out.println("Sector per track = " + volume.getBootRecord().SectorsPerTrack);
              System.out.println("Logical Cluster Number for the file $MFT = " + volume.getBootRecord().MFTPointer);
              System.out.println("Clusters per MFT Record = " + volume.getBootRecord().ClustersPerMFTRecord);
              System.out.println("Total Sectors = " + volume.getBootRecord().TotalSectors);

              System.out.println("$MFT byte start= " + volume.getBootRecord().MFTPointer * volume.getClusterSize() +
              "( 0x"+ Integer.toHexString(volume.getBootRecord().MFTPointer * volume.getClusterSize()) + ")");
              System.out.println("Cluster size = " + volume.getClusterSize());


          } catch (FileNotFoundException e)
          {
              // TODO Auto-generated catch block
              e.printStackTrace();
          } catch (IOException e)
          {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }*/

    }


    public static void main(String[] args) {
        NTFSTest test = new NTFSTest();
        test.printBootSectorData();
    }

}
