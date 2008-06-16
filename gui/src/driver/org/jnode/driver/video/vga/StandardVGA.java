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

package org.jnode.driver.video.vga;

import java.awt.image.IndexColorModel;

import javax.naming.NameNotFoundException;

import org.jnode.driver.video.vgahw.VgaIO;
import org.jnode.driver.video.vgahw.VgaState;
import org.jnode.driver.video.vgahw.VgaUtils;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.vmmagic.unboxed.Address;

/**
 * @author epr
 */
public class StandardVGA {

    /*
     * The SEQuencer, CRT, GRA, and ATTribute register sets for 640x480x16 color
     * mode.
     */
    private static final int[] seq = {0x03, 0x01, 0x0F, 0x00, 0x06};
    private static final int[] crt = {0x5F, 0x4F, 0x50, 0x82, 0x54, 0x80, 0x0B, 0x3E, 0x00, 0x40, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0xEA, 0x8C, 0xDF, 0x28, 0x00, 0xE7, 0x04, 0xE3, 0xFF};
    private static final int[] gra = {0x00, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x05, 0x0F, 0xFF};
    private static final int[] att = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C,
        0x0D, 0x0E, 0x0F, 0x01, 0x00, 0x0F, 0x00, 0x00};

    private final MemoryResource vgaMem;
    private final VgaState state640x480x16;
    private final VgaState oldState;

    public StandardVGA(ResourceOwner owner, IndexColorModel cm) throws ResourceNotFreeException {
        try {
            ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            vgaMem = rm.claimMemoryResource(owner, Address.fromIntZeroExtend(0xa0000), 0x9600,
                ResourceManager.MEMMODE_NORMAL);
            state640x480x16 = new VgaState(seq, crt, gra, att, 0xe3, cm);
            oldState = new VgaState();
        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException("ResourceManager not found", ex);
        }
    }

    public void startService(VgaIO io) {
        VgaUtils.screenOff(io);
        oldState.saveFromVGA(io);
        state640x480x16.restoreToVGA(io);
        VgaUtils.screenOn(io);
        cls(io);
    }

    public void stopService(VgaIO io) {
        VgaUtils.screenOff(io);
        oldState.restoreToVGA(io);
        VgaUtils.screenOn(io);
    }

    /**
     * Release all resources
     */
    public void release() {
        vgaMem.release();
    }

    private void cls(VgaIO io) {
        io.setGRAF(0x08, 0xFF);
        vgaMem.clear(0, vgaMem.getSize().toInt());
    }

    final MemoryResource getVgaMem() {
        return this.vgaMem;
    }

}
