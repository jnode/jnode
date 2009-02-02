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
 * This interface described the accessright for a given FSEntry.
 * 
 * @author epr
 */
public interface FSAccessRights extends FSObject {

    /**
     * Gets the owner of the entry.
     * 
     * @throws IOException
     */
    public Principal getOwner() throws IOException;

    public boolean canRead();

    public boolean canWrite();

    public boolean canExecute();

    public boolean setReadable(boolean enable, boolean owneronly);

    public boolean setWritable(boolean enable, boolean owneronly);

    public boolean setExecutable(boolean enable, boolean owneronly);
}
