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
 
package org.jnode.fs;

import org.jnode.driver.Device;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 * @param <T>
 */
public abstract class Formatter<T extends FileSystem<?>> implements Cloneable {
    private final FileSystemType<T> type;

    protected Formatter(FileSystemType<T> type) {
        this.type = type;
    }

    /**
     * Format the given device
     * 
     * @param device The device we want to format
     * @return the newly created FileSystem
     * @throws FileSystemException
     */
    public abstract T format(Device device) throws FileSystemException;

    public final FileSystemType<T> getFileSystemType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public Formatter<T> clone() throws CloneNotSupportedException {
        return (Formatter<T>) super.clone();
    }
}
