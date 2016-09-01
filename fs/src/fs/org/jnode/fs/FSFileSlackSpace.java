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
 
package org.jnode.fs;

import java.io.IOException;

/**
 * <p>
 * Interface for {@link FSFile} implementation that support reading "slack space" at the end of the file data.
 * </p>
 * <p>
 * For example if a filesystem has a block size of 512 bytes and a file is only using 500 bytes then there will be 12
 * bytes of unallocated space at the end of the block that may contain data from a previous file.
 * </p>
 */
public interface FSFileSlackSpace {
    /**
     * Gets the file slack space.
     *
     * @return the slack space.
     * @throws IOException if an error occurs reading the file.
     */
    byte[] getSlackSpace() throws IOException;
}
