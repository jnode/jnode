/*
 * $Id$
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
import org.jnode.vm.Address;
import org.jnode.vm.Unsafe;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PcTextScreen extends AbstractPcTextScreen implements TextScreen {

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
        Address ptr = Address.valueOf(0xb8000);
        try {
            final ResourceManager rm = (ResourceManager) InitialNaming
                    .lookup(ResourceManager.NAME);
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
    public void copyContent(int srcOffset, int destOffset, int length) {
        memory.copy(srcOffset * 2, destOffset * 2, length * 2);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getChar(int)
     */
    public char getChar(int offset) {
        return (char) (memory.getByte(offset * 2) & 0xFF);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getColor(int)
     */
    public int getColor(int offset) {
        return memory.getByte(offset * 2 + 1) & 0xFF;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char, int, int)
     */
    public void set(int offset, char ch, int count, int color) {
        final char v = (char) ((ch & 0xFF) | ((color & 0xFF) << 8));
        memory.setChar(offset * 2, v, count);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int,
     *      int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        color = (color & 0xFF) << 8;
        for (int i = 0; i < length; i++) {
            final int v = (ch[chOfs + i] & 0xFF) | color;
            memory.setChar((offset + i) * 2, (char) v);
        }
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int,
     *      int[], int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int[] colors,
            int colorsOfs) {
        for (int i = 0; i < length; i++) {
            final int v = (ch[chOfs + i] & 0xFF)
                    | ((colors[colorsOfs + i] & 0xFF) << 8);
            memory.setChar((offset + i) * 2, (char) v);
        }
    }

    /**
     * Copy the content of the given rawData into this screen.
     * 
     * @param rawData
     * @param rawDataOffset
     */
    final void copyFrom(char[] rawData, int rawDataOffset) {
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
    public void copyTo(TextScreen dst) {
        throw new UnsupportedOperationException();
    }

    /**
     * Synchronize the state with the actual device.
     */
    public void sync() {
        // Nothing to do here
    }
}