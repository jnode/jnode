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
 
package org.jnode.test.fs.filesystem.config;

import java.io.File;
import java.io.IOException;
import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.test.support.TestUtils;
import org.jnode.util.NumberUtils;

/**
 * @author Fabien DUMINY
 */
public class FileParam extends DeviceParam {
    /**
     *
     *
     */
    public FileParam(String fileName, String fileSize) {
        setFile(new File(fileName));
        setSize(fileSize);
    }

    /**
     *
     */
    public Device createDevice() throws IOException {
        String mode = isInput() ? "r" : "rw";
        if (!isInput() && (fileSize > 0L) && (!file.exists() || (file.length() != fileSize)))
            TestUtils.makeFile(file.getAbsolutePath(), fileSize);

        return new FileDevice(file, mode);
    }

    /**
     *
     */
    public long getDeviceSize() {
        return fileSize;
    }

    /**
     * @param file The file to set.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @param size The fileSize to set.
     */
    public void setSize(String size) {
        this.fileSize = NumberUtils.getSize(size);
    }

    /**
     *
     */
    public void tearDown(Device device) {
        if (!isInput())
            file.delete();
    }

    /**
     *
     */
    public String toString() {
        return "File[" + file.getName() + ' ' + NumberUtils.size(fileSize) + "]";
    }

    private File file;
    private long fileSize;
}
