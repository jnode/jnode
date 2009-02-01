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
 
package org.jnode.test.fs.filesystem.config;

import java.io.IOException;
import javax.naming.NameNotFoundException;
import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.Formatter;

/**
 * @author Fabien DUMINY
 */
public class FS {
    private final FSType type;
    private final boolean readOnly;
    private final Formatter<? extends FileSystem<?>> formatter;

    public FS(FSType type, boolean readOnly, Formatter<? extends FileSystem<?>> formatter) {
        this.type = type;
        this.readOnly = readOnly;
        this.formatter = formatter;
    }

    /**
     * @return Returns the type.
     */
    public FSType getType() {
        return type;
    }

    /**
     * @return Returns the readOnly.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    public FileSystem<?> format(Device device) throws FileSystemException {
        FileSystem<?> fs = null;
        
        if (formatter != null) {
            formatter.format(device);
        }
        
        return fs;
    }

    public FileSystem<?> mount(Device device)
        throws FileSystemException, IOException, InstantiationException, IllegalAccessException, NameNotFoundException {
        return type.mount(device, readOnly);
    }

    public String toString() {
        return type.toString() +
            (readOnly ? " ro" : " rw") +
            ((formatter == null) ? "" : "not") + " formatted";
    }
}
