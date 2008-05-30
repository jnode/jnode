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

package org.jnode.driver.textscreen.x86;

import org.jnode.driver.textscreen.TextScreen;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PcBufferTextScreen extends AbstractPcBufferTextScreen implements TextScreen {

    /**
     * My parent
     */
    private final AbstractPcTextScreen parent;

    /**
     * Initialize this instance.
     *
     * @param width
     * @param height
     */
    public PcBufferTextScreen(int width, int height, AbstractPcTextScreen parent) {
        super(width, height);
        this.parent = parent;
    }

    /**
     * Synchronize the state with the actual device.
     */
    public void sync() {
        copyTo(parent);
    }

    protected void setParentCursor(int x, int y) {
        parent.setCursor(x, y);
    }

    /**
     * @return Returns the parent.
     */
    protected final AbstractPcTextScreen getParent() {
        return this.parent;
    }
}
