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

package org.jnode.test.fs.filesystem.config;

import java.io.File;
import java.io.IOException;
import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;


/**
 * @author Fabien DUMINY
 */
public class ResourceParam extends DeviceParam {
    public ResourceParam() {
    }

    /**
     *
     */
    public Device createDevice() throws IOException {
        return new FileDevice(file, "r");
    }

    /**
     * @param tmpInputFile
     */
    public void setFile(File tmpInputFile) {
        this.file = tmpInputFile;
    }

    /**
     *
     */
    public long getDeviceSize() {
        return file.length();
    }

    /**
     *
     */
    public void tearDown(Device device) {
        // nothing to do
    }

    public String toString() {
        return "Resource[" + " tmpFile=" + file.getAbsolutePath() + ")";
    }

    /**
     * @return always return true.
     */
    public boolean isInput() {
        return true;
    }

    private File file;
}
