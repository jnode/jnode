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

import java.util.Arrays;

/**
 * @author Martin Husted Hartvig
 * @since 0.1.6
 */


public class Keys {
    private Key[] keys = new Key[128];

    public Keys() {
    }

    public Key getKey(int _scancode) {
        Key key = keys[_scancode];

        if (key == null) {
            key = new Key();
            keys[_scancode] = key;
        }
        return key;
    }

    /**
     * @param keycode
     * @return -1 if keycode has no scancode, otherwise return the scancode
     */
    public int getScanCode(int keycode) {
        for (int scancode = 0; scancode < keys.length; scancode++) {
            final Key key = keys[scancode];
            if ((key.getLowerVirtuelKey() == keycode) ||
                (key.getUpperVirtuelKey() == keycode) ||
                (key.getAltGrVirtuelKey() == keycode)) {
                return scancode;
            }
        }

        return -1; // bad keycode
    }


    public void setKey(int _scancode, Key key) {
        keys[_scancode] = key;
    }

    public String toString() {
        return "Keys{" +
            "keys=" + (keys == null ? null : Arrays.asList(keys)) +
            "}";
    }

    public static void main(String[] args) {
        Keys keys = new Keys();

        System.out.println(keys);

        System.out.println(keys.getKey(1));
        System.out.println(keys);
    }
}
