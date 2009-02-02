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
 
package org.jnode.driver.video.spi;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DpmsState {

    public static final DpmsState OFF = new DpmsState(false, false, false);

    private final boolean display;
    private final boolean hsync;
    private final boolean vsync;

    public DpmsState(boolean display, boolean hsync, boolean vsync) {
        this.display = display;
        this.hsync = hsync;
        this.vsync = vsync;
    }

    /**
     * @return True if the display is enabled, false otherwise
     */
    public final boolean isDisplay() {
        return this.display;
    }

    /**
     * @return True if the hsync is enabled, false otherwise
     */
    public final boolean isHsync() {
        return this.hsync;
    }

    /**
     * @return True if the vsync is enabled, false otherwise
     */
    public final boolean isVsync() {
        return this.vsync;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "DPMS[display:" + display + ", hsync:" + hsync + ", vsync:" + vsync + "]";
    }

}
