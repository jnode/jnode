/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm;

import org.jnode.system.resource.IOResource;
import org.jnode.system.resource.Resource;
import org.jnode.system.resource.ResourceNotFreeException;
import org.jnode.system.resource.ResourceOwner;
import org.jnode.util.NumberUtils;


/**
 * Default implementation of IOResource.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class IOResourceImpl extends Region implements IOResource {

    /**
     * The first port of this resource
     */
    private int startPort;
    /**
     * The length of the port region
     */
    private int length;
    /**
     * Has this resource been released?
     */
    private boolean released;
    /**
     * The list of active IOResource's
     */
    private static Region resources;

    /**
     * Create a new instance
     *
     * @param owner
     * @param startPort
     * @param length
     */
    private IOResourceImpl(ResourceOwner owner, int startPort, int length) {
        super(owner);
        this.startPort = startPort;
        this.length = length;
        this.released = false;
    }

    /**
     * Claim a range of IO ports
     *
     * @param owner
     * @param startPort
     * @param length
     * @return The claimed resource
     * @throws ResourceNotFreeException
     */
    protected static synchronized IOResource claimIOResource(ResourceOwner owner, int startPort, int length)
        throws ResourceNotFreeException {
        final IOResourceImpl res = new IOResourceImpl(owner, startPort, length);
        if (isFree(resources, res)) {
            resources = add(resources, res);
            return res;
        } else {
            final Object curOwner = get(resources, res).getOwner();
            throw new ResourceNotFreeException("port " + NumberUtils.hex(startPort) + '-' +
                NumberUtils.hex(startPort + length - 1) + " is owned by " + curOwner);
        }
    }

    /**
     * Returns the length.
     *
     * @return int
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the startPort.
     *
     * @return int
     */
    public int getStartPort() {
        return startPort;
    }

    public int inPortByte(int portNr) {
        testPort(portNr, 1);
        return Unsafe.inPortByte(portNr);
    }

    public int inPortWord(int portNr) {
        testPort(portNr, 2);
        return Unsafe.inPortWord(portNr);
    }

    public int inPortDword(int portNr) {
        testPort(portNr, 4);
        return Unsafe.inPortDword(portNr);
    }

    public void outPortByte(int portNr, int value) {
        testPort(portNr, 1);
        Unsafe.outPortByte(portNr, value);
    }

    public void outPortWord(int portNr, int value) {
        testPort(portNr, 2);
        Unsafe.outPortWord(portNr, value);
    }

    public void outPortDword(int portNr, int value) {
        testPort(portNr, 4);
        Unsafe.outPortDword(portNr, value);
    }

    /**
     * Compare to regions.
     *
     * @param otherRegion
     * @return a negative integer, zero, or a positive integer as
     *         this object is less than, equal to, or greater than the
     *         specified region. If the regions overlap, 0 is returned.
     */
    public int compareTo(Region otherRegion) {
        final IOResourceImpl other = (IOResourceImpl) otherRegion;
        if (this.startPort + this.length <= other.startPort) {
            return -1;
        } else if (this.startPort >= other.startPort + other.length) {
            return 1;
        } else {
            return 0;
        }

    }

    /**
     * Give up this resource. After this method has been called, the resource
     * cannot be used anymore.
     */
    public void release() {
        if (!this.released) {
            this.released = true;
            synchronized (getClass()) {
                resources = remove(resources, this);
            }
        }
    }

    /**
     * Gets the parent resource if any.
     *
     * @return The parent resource, or null if this resource has no parent.
     */
    public Resource getParent() {
        return null;
    }

    private void testPort(int portNr, int size) {
        if (released) {
            throw new IndexOutOfBoundsException("Resource is released");
        }
        if ((portNr < startPort) || ((portNr + size) > (startPort + length))) {
            throw new IndexOutOfBoundsException();
        }
    }
}
