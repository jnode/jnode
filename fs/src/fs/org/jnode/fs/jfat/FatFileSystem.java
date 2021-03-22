/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.fs.jfat;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * @author gvt
 */
public class FatFileSystem extends AbstractFileSystem<FatRootDirectory> {
    private static final Logger log = Logger.getLogger(FatFileSystem.class);

    private Fat fat;
    private final CodePage cp;

    public FatFileSystem(Device device, String codePageName, boolean readOnly, FatFileSystemType type)
        throws FileSystemException {
        super(device, readOnly, type);

        try {
            fat = Fat.create(getApi());
        } catch (IOException ex) {
            throw new FileSystemException(ex);
        } catch (Exception e) {
            throw new FileSystemException(e);
        }

        cp = CodePage.forName(codePageName);
    }

    public FatFileSystem(Device device, boolean readOnly, FatFileSystemType type) throws FileSystemException {
        this(device, "ISO_8859_1", readOnly, type);
    }

    public int getClusterSize() {
        return fat.getClusterSize();
    }

    public Fat getFat() {
        return fat;
    }

    public BootSector getBootSector() {
        return fat.getBootSector();
    }

    public CodePage getCodePage() {
        return cp;
    }

    protected FSFile createFile(FSEntry entry) throws IOException {
        return entry.getFile();
    }

    protected FSDirectory createDirectory(FSEntry entry) throws IOException {
        return entry.getDirectory();
    }

    protected FatRootDirectory createRootEntry() throws IOException {
        return new FatRootDirectory(this);
    }

    public void flush() throws IOException {
        super.flush();
        fat.flush();
    }

    @Override
    public String toString() {
        return String.format("FAT File System: %s", getFat());
    }

    public String toDebugString() {
        StrWriter out = new StrWriter();

        out.println("********************** FatFileSystem ************************");
        out.println(getFat());
        out.print("*************************************************************");

        return out.toString();
    }

    public long getFreeSpace() {
        // TODO implement me
        return -1;
    }

    public long getTotalSpace() {
        // TODO implement me
        return -1;
    }

    public long getUsableSpace() {
        // TODO implement me
        return -1;
    }

    @Override
    public String getVolumeName() throws IOException {
        return getRootEntry().getLabel();
    }
}
