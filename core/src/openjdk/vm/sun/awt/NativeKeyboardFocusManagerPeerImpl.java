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
 
package sun.awt;

import java.awt.Window;
import java.awt.Component;

/**
 * @see sun.awt.KeyboardFocusManagerPeerImpl
 * @author Levente S\u00e1ntha
 */
class NativeKeyboardFocusManagerPeerImpl {
    /**
     * @see sun.awt.KeyboardFocusManagerPeerImpl#getNativeFocusedWindow()
     */
    private static Window getNativeFocusedWindow() {
        //todo implement it
        return null;
    }
    /**
     * @see sun.awt.KeyboardFocusManagerPeerImpl#getNativeFocusOwner()
     */
    private static Component getNativeFocusOwner() {
        //todo implement it
        return null;
    }
    /**
     * @see sun.awt.KeyboardFocusManagerPeerImpl#clearNativeGlobalFocusOwner(java.awt.Window)
     */
    private static void clearNativeGlobalFocusOwner(Window arg1) {
        //todo implement it
    }
}
