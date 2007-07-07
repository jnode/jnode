/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.awt;

import java.awt.AWTEvent;
import java.awt.Component;

/**
 * Sent when one of the following events occur on the grabbed window: <ul>
 * <li> it looses focus, but not to one of the owned windows
 * <li> mouse click on the outside area happens (except for one of the owned windows)
 * <li> switch to another application or desktop happens
 * <li> click in the non-client area of the owning window or this window happens
 * </ul>
 *
 * <p>Notice that this event is not generated on mouse click inside of the window area.
 * <p>To listen for this event, install AWTEventListener with {@value sun.awt.SunToolkit#GRAB_EVENT_MASK}
 */
public class UngrabEvent extends AWTEvent {
    public UngrabEvent(Component source) {
        super(source, 0xffff);
    }

    public String toString() {
        return "sun.awt.UngrabEvent[" + getSource() + "]";
    }
}
