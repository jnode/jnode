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
 
package org.jnode.driver.input;

public class LogitechProtocol implements MouseProtocolHandler {

    static final int BIT_LEFT = 0x01;
    static final int BIT_RIGHT = 0x02;
    static final int BIT_MIDDLE = 0x04;
    static final int BIT_BUTTON_MASK = BIT_LEFT | BIT_RIGHT | BIT_MIDDLE;
    static final int BIT_X_SIGN = 0x10;
    static final int BIT_Y_SIGN = 0x20;

    public boolean supportsId(int id) {
        return id == 0;
    }

    public String getName() {
        return "Logitech Mouse";
    }

    public int getPacketSize() {
        return 3;
    }

    public PointerEvent buildEvent(byte[] data) {
        final int d0 = data[0] & 0xFF;
        final int d1 = data[1] & 0xFF;
        final int d2 = data[2] & 0xFF;

        final int buttons = d0 & BIT_BUTTON_MASK;
        final int x;
        final int y;
        if ((d0 & BIT_X_SIGN) != 0) {
            x = d1 - 256;
        } else {
            x = d1;
        }
        if ((d0 & BIT_Y_SIGN) != 0) {
            y = d2 - 256;
        } else {
            y = d2;
        }

        return new PointerEvent(buttons, x, -y, PointerEvent.RELATIVE);
    }
}
