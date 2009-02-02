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
 
package org.jnode.driver.textscreen.x86;

import javax.naming.NameNotFoundException;

import org.jnode.driver.textscreen.TextScreen;
import org.jnode.naming.InitialNaming;
import org.jnode.system.BootLog;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.vm.Unsafe;
import org.vmmagic.unboxed.Address;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PcTextScreen extends AbstractPcTextScreen {

    /**
     * The MemoryResource we use to store (and display) characters.
     */
    private final MemoryResource memory;

    /**
     * For now, we use singleton (only one screen monitor). May change in the
     * future.
     */
    private static TextScreen instance;

    /**
     * Initialize this instance.
     */
    private PcTextScreen() throws ResourceNotFreeException {
        super(80, 25);
        Address ptr = Address.fromIntZeroExtend(0xb8000);
        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            final ResourceOwner owner = new SimpleResourceOwner("Screen");
            memory = rm.claimMemoryResource(owner, ptr, getWidth()
                    * getHeight() * 2, ResourceManager.MEMMODE_NORMAL);
        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException("ResourceManager not found", ex);
        }
    }

    /**
     * Get the singleton instance and create it if necessary.
     *
     * @return @throws
     *         PragmaUninterruptible
     */
    public static TextScreen getInstance() {
        if (instance == null) {
            try {
                instance = new PcTextScreen();
                if (instance == null) {
                    Unsafe.debug("oops new does not work");
                }
            } catch (ResourceNotFreeException ex) {
                BootLog.error("Screen memory not free!");
            }
        }
        return instance;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#copyContent(int, int, int)
     */
    @Override
    public void copyContent(int srcOffset, int destOffset, int length) {
        memory.copy(srcOffset * 2, destOffset * 2, length * 2);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getChar(int)
     */
    @Override
    public char getChar(int offset) {
        return (char) (memory.getByte(offset * 2) & 0xFF);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getColor(int)
     */
    @Override
    public int getColor(int offset) {
        return memory.getByte(offset * 2 + 1) & 0xFF;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char, int, int)
     */
    @Override
    public void set(int offset, char ch, int count, int color) {
        final char v = (char) ((ch & 0xFF) | ((color & 0xFF) << 8));
        memory.setChar(offset * 2, v, count);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int,
     *      int)
     */
    @Override
    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        color = (color & 0xFF) << 8;
        
        int chOffset = chOfs;
        int ofs = offset * 2;
        for (int i = 0; i < length; i++) {
            final int v = (ch[chOffset++] & 0xFF) | color;
            memory.setChar(ofs, (char) v);
            ofs += 2;
        }
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int,
     *      int[], int)
     */
    @Override
    public void set(int offset, char[] ch, int chOfs, int length, int[] colors,
            int colorsOfs) {
        int chOffset = chOfs;
        int ofs = offset * 2;
        int colOfs = colorsOfs;        
        for (int i = 0; i < length; i++) {
            final int v = (ch[chOffset++] & 0xFF)
                    | ((colors[colOfs++] & 0xFF) << 8);
            memory.setChar(ofs, (char) v);
            ofs += 2;
        }
    }

    /**
     * Copy the content of the given rawData into this screen.
     *
     * @param rawData
     * @param rawDataOffset
     */
    @Override
    public final void copyFrom(char[] rawData, int rawDataOffset) {
        if (rawDataOffset < 0) {
            Unsafe.die("Screen:rawDataOffset = " + rawDataOffset);
        }
        memory.setChars(rawData, rawDataOffset, 0, getWidth() * getHeight());
    }

    /**
     * Copies the entire screen to the given destination. For this operation to
     * succeed, the screens involved must be compatible.
     *
     * @param dst
     */
    @Override
    public void copyTo(TextScreen dst, int offset, int length) {
        throw new UnsupportedOperationException();
    }

    /**
     * Synchronize the state with the actual device.
     */
    @Override
    public void sync(int offset, int length) {
        // Nothing to do here
    }

    @Override
    public int setCursor(int x, int y) {
        return 0; // TODO what should we return if we don't call instance.setCursor ?
    }

    @Override
    public int setCursorVisible(boolean visible) {
        return 0; //TODO
    }
}
