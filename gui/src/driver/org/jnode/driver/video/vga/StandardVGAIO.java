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
 
package org.jnode.driver.video.vga;

import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.jnode.driver.DriverException;
import org.jnode.driver.video.vgahw.VgaConstants;
import org.jnode.driver.video.vgahw.VgaIO;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;

/**
 * @author epr
 */
public class StandardVGAIO implements VgaConstants, VgaIO {

    /** VGA IO Ports */
    private final IOResource vgaIO;
    private final MemoryResource mem;

    /* current state information */
    private int current_mode;

    /**
     * Create a new instance
     */
    public StandardVGAIO(ResourceOwner owner, MemoryResource mem) throws ResourceNotFreeException,
            DriverException {
        try {
            ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            vgaIO = claimPorts(rm, owner, VGA_FIRST_PORT, VGA_LAST_PORT - VGA_FIRST_PORT + 1);
            this.mem = mem;
            current_mode = getColorMode();
        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException("Cannot find ResourceManager", ex);
        }
    }

    /**
     * Release all resource
     */
    public void release() {
        vgaIO.release();
    }

    public final void setATTIndex(int index) {
        getSTAT();
        vgaIO.outPortByte(ATT_I, index);
    }

    public final void setATT(int index, int val) {
        getSTAT();
        vgaIO.outPortByte(ATT_I, index);
        vgaIO.outPortByte(ATT_I, (byte) val);
    }

    public final int getATT(int index) {
        getSTAT();
        vgaIO.outPortByte(ATT_I, (byte) (index));
        return vgaIO.inPortByte(ATT_DR);
    }

    public final void setMISC(int val) {
        setSEQ(0, 1);
        vgaIO.outPortByte(MISC_W, (byte) val);
        setSEQ(0, 3);
        current_mode = val & 0x01;
    }

    public final int getMISC() {
        return vgaIO.inPortByte(MISC_R);
    }

    public final void setSEQ(int index, int val) {
        vgaIO.outPortByte(SEQ_I, (byte) index);
        vgaIO.outPortByte(SEQ_D, (byte) val);
    }

    public final int getSEQ(int index) {
        vgaIO.outPortByte(SEQ_I, (byte) index);
        return vgaIO.inPortByte(SEQ_D);
    }

    public final int getSTAT() {
        if (current_mode == 0) {
            return vgaIO.inPortByte(STATM);
        } else {
            return vgaIO.inPortByte(STATC);
        }
    }

    public final void setGRAF(int index, int val) {
        vgaIO.outPortByte(GRAF_I, (byte) index);
        vgaIO.outPortByte(GRAF_D, (byte) val);
    }

    public final int getGRAF(int index) {
        vgaIO.outPortByte(GRAF_I, (byte) index);
        return vgaIO.inPortByte(GRAF_D);
    }

    public final void setCRT(int index, int val) {
        if (current_mode == 1) {
            vgaIO.outPortByte(CRTC_I, (byte) index);
            vgaIO.outPortByte(CRTC_D, (byte) val);
        } else {
            vgaIO.outPortByte(CRTC_IM, (byte) index);
            vgaIO.outPortByte(CRTC_DM, (byte) val);
        }
    }

    public final int getCRT(int index) {
        if (current_mode == 1) {
            vgaIO.outPortByte(CRTC_I, (byte) index);
            return vgaIO.inPortByte(CRTC_D);
        } else {
            vgaIO.outPortByte(CRTC_IM, (byte) index);
            return vgaIO.inPortByte(CRTC_DM);
        }
    }

    public final void setDACReadIndex(int index) {
        vgaIO.outPortByte(DAC_RI, index & 0xFF);
    }

    public final void setDACWriteIndex(int index) {
        vgaIO.outPortByte(DAC_WI, index & 0xFF);
    }

    public final void setDACData(int data) {
        vgaIO.outPortByte(DAC_D, data & 0xFF);
    }

    public final int getDACData() {
        return vgaIO.inPortByte(DAC_D) & 0xFF;
    }

    public final MemoryResource getVideoMem() {
        return mem;
    }

    private final int getColorMode() {
        return (vgaIO.inPortByte(MISC_R) & 1);
    }

    private IOResource claimPorts(final ResourceManager rm, final ResourceOwner owner,
            final int low, final int length) throws ResourceNotFreeException, DriverException {
        try {
            return (IOResource) AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ResourceNotFreeException {
                    return rm.claimIOResource(owner, low, length);
                }
            });
        } catch (ResourceNotFreeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DriverException("Unknown exception", ex);
        }

    }
}
