/*
 * $Id$
 */
package org.jnode.driver.textscreen.x86;

import org.jnode.driver.textscreen.ScrollableTextScreen;
import org.jnode.driver.textscreen.TextScreen;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class AbstractPcTextScreen implements TextScreen {

    private final int width;
    private final int height;

    /**
     * Initialize this instance.
     * @param width
     * @param height
     */
    public AbstractPcTextScreen(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Gets the height of the screen in letters.
     * 
     * @return Returns the height.
     */
    public int getHeight() {
        return height;        
    }

    /**
     * Gets the width of the screen in letters.
     * 
     * @return Returns the width.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Calculate the offset for a given x,y coordinate.
     * @param x
     * @param y
     * @return
     */
    public int getOffset(int x, int y) {
        return (y * width) + x;
    }

    /**
     * Copy the content of the given rawData into this screen.
     * @param rawData
     * @param rawDataOffset
     */
    abstract void copyFrom(char[] rawData, int rawDataOffset);

    /**
     * @see org.jnode.driver.textscreen.TextScreen#createCompatibleBufferScreen()
     */
    public TextScreen createCompatibleBufferScreen() {
        return new PcBufferTextScreen(getWidth(), getHeight(), this);
    }
        
    /**
     * Create an in-memory buffer text screen that is compatible
     * with the system screen, but larges and supports scrolling.
     * @return
     */
    public ScrollableTextScreen createCompatibleScrollableBufferScreen(int height) {
        if (height < getHeight()) {
            throw new IllegalArgumentException("Invalid height " + height);
        }
        return new PcScrollableTextScreen(getWidth(), height, this);        
    }
        
    /**
     * Ensure that the given row is visible.
     * @param row
     */
    public void ensureVisible(int row) {
        // do nothing by default
    }
}
