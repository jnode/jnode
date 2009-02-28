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

import java.io.IOException;
import java.security.Principal;

/**
 * <tt>FSAccessRights</tt> interface described the access rights for a given {@link FSEntry}.
 * 
 * @author epr
 */
public interface FSAccessRights extends FSObject {

    /**
     * Gets the owner of the entry.
     * 
     * @return {@link Principal} represent owner of the entry. 
     * 
     * @throws IOException if error occurs during retrieve of the owner.
     */
    public Principal getOwner() throws IOException;

    /**
     * Returns <tt>true</tt> if read is allow for the related entry.
     * 
     * @return <tt>true</tt> if read is allow for the related entry.
     */
    public boolean canRead();

    /**
     * Returns <tt>true</tt> if write is allow for the related entry.
     * 
     * @return <tt>true</tt> if write is allow for the related entry.
     */
    public boolean canWrite();

    /**
     * Returns <tt>true</tt> if execution is allow for the related entry.
     * 
     * @return <tt>true</tt> if execution is allow for the related entry.
     */
    public boolean canExecute();

    /**
     * Set related entry as readable. This right can be limited to the owner.
     * 
     * @param enable <tt>true</tt> to allow right to read the related entry.
     * @param owneronly <tt>true</tt> to limit the read to the owner.
     * 
     * @return <tt>true</tt> if read is allowed.
     */
    public boolean setReadable(boolean enable, boolean owneronly);

    /**
     * Set related entry as writable. This right can be limited to the owner.
     * 
     * @param enable <tt>true</tt> to allow right to write the related entry.
     * @param owneronly <tt>true</tt> to limit the write to the owner.
     * 
     * @return <tt>true</tt> if write is allowed.
     */
    public boolean setWritable(boolean enable, boolean owneronly);

    /**
     * Set related entry as executable. This right can be limited to the owner.
     * 
     * @param enable <tt>true</tt> to allow right to execute the related entry.
     * @param owneronly <tt>true</tt> to limit the read to the owner.
     * 
     * @return <tt>true</tt> if execution is allowed.
     */
    public boolean setExecutable(boolean enable, boolean owneronly);
}
