/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.awt.peer;

import java.awt.Window;
import java.awt.Point;

/**
 * The peer interfaces are intended only for use in porting
 * the AWT. They are not intended for use by application
 * developers, and developers should not implement peers
 * nor invoke any of the peer methods directly on the peer
 * instances.
 */
public interface MouseInfoPeer {

    /**
     * This method does two things: it fills the point fields with
     * the current coordinates of the mouse cursor and returns the
     * number of the screen device where the pointer is located.
     * The number of the screen device is only returned for independent
     * devices (which are not parts of a virtual screen device).
     * For virtual screen devices, 0 is returned.
     * Mouse coordinates are also calculated depending on whether
     * or not the screen device is virtual. For virtual screen
     * devices, pointer coordinates are calculated in the virtual
     * coordinate system. Otherwise, coordinates are calculated in
     * the coordinate system of the screen device where the pointer
     * is located.
     * See java.awt.GraphicsConfiguration documentation for more 
     * details about virtual screen devices.
     */
    int fillPointWithCoords(Point point);

    /**
     * Returns whether or not the window is located under the mouse
     * pointer. The window is considered to be under the mouse pointer
     * if it is showing on the screen, and the mouse pointer is above
     * the part of the window that is not obscured by any other windows.
     */
    boolean isWindowUnderMouse(Window w);

}
