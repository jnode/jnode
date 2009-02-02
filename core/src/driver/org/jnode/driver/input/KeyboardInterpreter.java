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
 
package org.jnode.driver.input;

/**
 * A KeyboardInterpreter translates a sequence of scancodes to the corresponding
 * KeyboardEvent's.  An instance is stateful, remembering the state of the SHIFT, 
 * CTRL and other 'modifier' keys for example.
 *
 * @author epr
 * @author Martin Husted Hartvig
 * @author crawley@jnode.org
 */
public interface KeyboardInterpreter {
    
    /**
     * A factory API for keyboard interpreters
     */
    public interface Factory {
        /**
         * Create a new interpreter instance.
         */
        public KeyboardInterpreter create() throws KeyboardInterpreterException;
        
        /**
         * Provide a short description for the layout.
         */
        public String describe();
    }

    public static final int XT_RELEASE = 0x80;
    public static final int XT_EXTENDED = 0xE0;

    /**
     * Interpret a given scancode into a keyevent.
     *
     * @param scancode
     */
    public KeyboardEvent interpretScancode(int scancode);

    /**
     * @param keycode
     * @return
     * @throws UnsupportedKeyException
     */
    public KeyboardEvent interpretKeycode(int keycode);

}
