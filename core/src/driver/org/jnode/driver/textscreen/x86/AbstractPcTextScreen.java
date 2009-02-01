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
 
package org.jnode.driver.textscreen.x86;

import org.jnode.driver.textscreen.ScrollableTextScreen;
import org.jnode.driver.textscreen.TextScreen;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class AbstractPcTextScreen implements TextScreen {

    private final int width;
    private final int height;

    /**
     * Initialize this instance.
     *
     * @param width
     * @param height
     */
    public AbstractPcTextScreen(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Gets the height of the virtual screen in characters.
     * 
     * @return Returns the height.
     */
    @Override
    public final int getHeight() {
        return height;        
    }

    /**
     * Gets the width of the virtual screen in characters.
     * 
     * @return Returns the width.
     */
    @Override
    public final int getWidth() {
        return width;
    }
    
    /**
     * Gets the height of the 'physical' screen in characters.  Unless overriden, 
     * this delegates to getHeight().
     * 
     * @return Returns the height.
     */
    @Override
    public int getDeviceHeight() {
        return getHeight();        
    }

    /**
     * Gets the width of the 'physical' screen in characters.  Unless overriden, 
     * this delegates to getWidth().
     * 
     * @return Returns the width.
     */
    @Override
    public int getDeviceWidth() {
        return getWidth();
    }
    
    
    
    /**
     * Calculate the offset for a given x,y coordinate.
     *
     * @param x
     * @param y
     * @return
     */
    @Override
    public final int getOffset(int x, int y) {
        return (y * width) + x;
    }

    /**
     * Copy the content of the given rawData into this screen.
     *
     * @param rawData
     * @param rawDataOffset
     */
    public abstract void copyFrom(char[] rawData, int rawDataOffset);

    /**
     * @see org.jnode.driver.textscreen.TextScreen#createCompatibleBufferScreen()
     */
    @Override
    public final TextScreen createCompatibleBufferScreen() {
        return new PcBufferTextScreen(getWidth(), getHeight(), this);
    }
        
    /**
     * Create an in-memory buffer text screen that is compatible
     * with the system screen, but larges and supports scrolling.
     *
     * @return
     */
    @Override
    public final ScrollableTextScreen createCompatibleScrollableBufferScreen(int height) {
        if (height < getHeight()) {
            throw new IllegalArgumentException("Invalid height " + height);
        }
        return new PcScrollableTextScreen(getWidth(), height, this);        
    }
}
