/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.awt;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;

/**
 * @author Levente S\u00e1ntha
 */
public class KeyboardHandler implements
        KeyboardListener {

    /** My logger */
    private static final Logger log = Logger.getLogger(KeyboardHandler.class);

    /** The queue where to post the events */
    private final EventQueue eventQueue;

    /** The API of the actual keyboard */
    private KeyboardAPI keyboardAPI;

    /**
     * Initialize this instance.
     * 
     * @param eventQueue
     */
    public KeyboardHandler(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
        try {
            final Collection<Device> keyboards = DeviceUtils
                    .getDevicesByAPI(KeyboardAPI.class);
            if (!keyboards.isEmpty()) {
                Device keyboardDevice = (Device) keyboards.iterator().next();
                keyboardAPI = (KeyboardAPI) keyboardDevice
                        .getAPI(KeyboardAPI.class);
                keyboardAPI.addKeyboardListener(this);
                AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        keyboardAPI.setPreferredListener(KeyboardHandler.this);
                        return null;
                    }
                });
            }
        } catch (ApiNotFoundException ex) {
            log.error("Strange...", ex);
        }
    }

    /**
     * @param event
     */
    public void keyPressed(KeyboardEvent event) {
        final int key_code = event.getKeyCode();
        if (event.isAltDown() && key_code == KeyEvent.VK_F12) {
            event.consume();
            JNodeToolkit.stopGui();
        } else if (event.isControlDown() && event.isAltDown()
                && key_code == KeyEvent.VK_F5) {
            event.consume();
            JNodeToolkit.refreshGui();
        } else {
            postEvent(KeyEvent.KEY_PRESSED, event.getTime(), event
                    .getModifiers(), key_code, event.getKeyChar());
            event.consume();
        }
    }

    /**
     * @param event
     */
    public void keyReleased(KeyboardEvent event) {
        postEvent(KeyEvent.KEY_RELEASED, event.getTime(), event.getModifiers(),
                event.getKeyCode(), event.getKeyChar());
        char ch = event.getKeyChar();
        if (ch != KeyEvent.CHAR_UNDEFINED) {
            postEvent(KeyEvent.KEY_TYPED, event.getTime(),
                    event.getModifiers(), KeyEvent.VK_UNDEFINED, ch);
        }
    }

    /**
     * @param id
     * @param modifiers
     * @param keyCode
     * @param keyChar
     */
    private void postEvent(int id, long time, int modifiers, int keyCode,
            char keyChar) {
        JNodeToolkit tk = (JNodeToolkit) Toolkit.getDefaultToolkit();
        KeyEvent me = new KeyEvent(tk.getTop(), id, time, modifiers, keyCode,
                keyChar);
        eventQueue.postEvent(me);
    }

    /**
     * Install this handler as current keyboard focus manager.
     */
    public void install() {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                //setCurrentKeyboardFocusManager(KeyboardHandler.this);
                return null;
            }            
        });        
    }

    /**
     * Uninstall this handler as current keyboard focus manager.
     */
    public void uninstall() {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                //setCurrentKeyboardFocusManager(null);
                return null;
            }            
        });        
    }

    /**
     * Close this handler
     */
    public void close() {
        if (keyboardAPI != null) {
            keyboardAPI.removeKeyboardListener(this);
        }
        uninstall();
    }
}
