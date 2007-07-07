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
package sun.awt;

import java.awt.event.*;
import java.awt.*;
import java.util.logging.*;
import java.lang.reflect.*;
import java.awt.peer.*;

public class KeyboardFocusManagerPeerImpl implements KeyboardFocusManagerPeer {
    static native Window getNativeFocusedWindow();
    static native Component getNativeFocusOwner();
    static native void clearNativeGlobalFocusOwner(Window activeWindow);

    KeyboardFocusManagerPeerImpl(KeyboardFocusManager manager) {
    }

    public void setCurrentFocusedWindow(Window win) {
    }

    public Window getCurrentFocusedWindow() {
        return getNativeFocusedWindow();
    }
    
    public void setCurrentFocusOwner(Component comp) {
    }

    public Component getCurrentFocusOwner() {
        return getNativeFocusOwner();
    }    
    public void clearGlobalFocusOwner(Window activeWindow) {
        clearNativeGlobalFocusOwner(activeWindow);
    }

    static Method m_removeLastFocusRequest = null;
    public static void removeLastFocusRequest(Component heavyweight) {
        try {
            if (m_removeLastFocusRequest == null) {
                m_removeLastFocusRequest = SunToolkit.getMethod(KeyboardFocusManager.class, "removeLastFocusRequest",
                                                              new Class[] {Component.class});
            }
            m_removeLastFocusRequest.invoke(null, new Object[]{heavyweight});
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }        
    }
}
