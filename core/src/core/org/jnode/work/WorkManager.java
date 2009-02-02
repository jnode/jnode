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
 
package org.jnode.work;

/**
 * Manager of small asynchronous bits of work.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface WorkManager {

    /**
     * Name used to bind this manager in the InitialNaming namespace.
     */
    public static final Class<WorkManager> NAME = WorkManager.class;

    /**
     * Add a bit of work to the qork queue.
     *
     * @param work
     */
    public void add(Work work);

    /**
     * Gets the number of entries in the work queue.
     *
     * @return
     */
    public int queueSize();

    /**
     * Is the work queue empty.
     *
     * @return
     */
    public boolean isEmpty();
}
