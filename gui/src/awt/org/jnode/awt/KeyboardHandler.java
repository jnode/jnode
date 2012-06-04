/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.awt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashSet;
import javax.imageio.ImageIO;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.driver.input.KeyboardListener;

/**
 * @author Levente S\u00e1ntha
 */
public class KeyboardHandler implements KeyboardListener {
    //todo refactor this pattern to be generally available in JNode for AWT and text consoles as well
    private static class KeyboardAPIHandler implements KeyboardAPI, DeviceListener {
        private Collection<KeyboardAPI> keyboardList = new HashSet<KeyboardAPI>();
        private Collection<KeyboardListener> listenerList = new HashSet<KeyboardListener>();
        private KeyboardInterpreter interpreter;

        KeyboardAPIHandler() {
            try {
                DeviceManager dm = DeviceUtils.getDeviceManager();
                dm.addListener(this);
                for (Device device : dm.getDevicesByAPI(KeyboardAPI.class)) {
                    try {
                        keyboardList.add(device.getAPI(KeyboardAPI.class));
                    } catch (ApiNotFoundException anfe) {
                        //ignore
                    }
                }
            } catch (NameNotFoundException nfe) {
                //todo handle it
            }
        }

        public void addKeyboardListener(KeyboardListener listener) {
            listenerList.add(listener);
            for (KeyboardAPI api : keyboardList) {
                api.addKeyboardListener(listener);
            }
        }

        public void removeKeyboardListener(KeyboardListener listener) {
            listenerList.remove(listener);
            for (KeyboardAPI api : keyboardList) {
                api.removeKeyboardListener(listener);
            }
        }

        public void setPreferredListener(KeyboardListener listener) {
            for (KeyboardAPI api : keyboardList) {
                api.setPreferredListener(listener);
            }
        }

        public KeyboardInterpreter getKbInterpreter() {
            return interpreter;
        }

        public void setKbInterpreter(KeyboardInterpreter kbInterpreter) {
            this.interpreter = kbInterpreter;
            for (KeyboardAPI api : keyboardList) {
                api.setKbInterpreter(interpreter);
            }
        }

        public void deviceStarted(Device device) {
            if (device.implementsAPI(KeyboardAPI.class)) {
                try {
                    KeyboardAPI api = device.getAPI(KeyboardAPI.class);
                    keyboardList.add(api);
                    for (KeyboardListener listener : listenerList) {
                        api.addKeyboardListener(listener);
                    }
                } catch (ApiNotFoundException anfe) {
                    //ignore
                }
            }
        }

        public void deviceStop(Device device) {
            if (device.implementsAPI(KeyboardAPI.class)) {
                try {
                    KeyboardAPI api = device.getAPI(KeyboardAPI.class);
                    keyboardList.remove(api);
                    for (KeyboardListener listener : listenerList) {
                        api.removeKeyboardListener(listener);
                    }
                } catch (ApiNotFoundException anfe) {
                    //ignore
                }
            }
        }

        boolean hasPointer() {
            return !keyboardList.isEmpty();
        }
    }


    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(KeyboardHandler.class);

    /**
     * The queue where to post the events
     */
    @SuppressWarnings("unused")
    private final EventQueue eventQueue;

    /**
     * The API of the actual keyboard
     */
    private KeyboardAPI keyboardAPI;

    private int modifiers;
    private boolean pressed;

    /**
     * Initialize this instance.
     *
     * @param eventQueue
     */
    public KeyboardHandler(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
        this.keyboardAPI = new KeyboardAPIHandler();
        this.keyboardAPI.addKeyboardListener(this);
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                keyboardAPI.setPreferredListener(KeyboardHandler.this);
                return null;
            }
        });
    }

    /**
     * @param event
     */
    public void keyPressed(KeyboardEvent event) {

        if (processSystemKey(event))
            return;

        int modifiers = event.getModifiers();
        setModifiers(modifiers);

        postEvent(KeyEvent.KEY_PRESSED, event.getTime(), modifiers, event.getKeyCode(), event.getKeyChar());
        pressed = true;

        event.consume();

        char ch = event.getKeyChar();
        if (ch != KeyEvent.CHAR_UNDEFINED && !event.isAltDown() && !event.isControlDown()) {
            postEvent(KeyEvent.KEY_TYPED, event.getTime(),
                event.getModifiers(), KeyEvent.VK_UNDEFINED, ch);
        }

    }

    /**
     * @param event
     */
    public void keyReleased(KeyboardEvent event) {
        int modifiers = event.getModifiers();
        setModifiers(modifiers);
        event.consume();

        if (pressed) {
            postEvent(KeyEvent.KEY_RELEASED, event.getTime(), modifiers,
                event.getKeyCode(), event.getKeyChar());
            pressed = false;
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

        Component source = null;
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (kfm != null) {
            Component fo = kfm.getFocusOwner();
            if (fo == null) {
                Window win = kfm.getActiveWindow();
                if (win == null) {
                    win = kfm.getFocusedWindow();
                    if (win != null) {
                        source = win;
                    }
                } else {
                    source = win;
                }
            } else {
                source = fo;
            }
        }

        if (source == null) {
            JNodeToolkit tk = (JNodeToolkit) Toolkit.getDefaultToolkit();
            Frame top = tk.getTop();
            if (top == null) {
                //awt is not yet initialized
                //drop this event
                return;
            } else {
                source = top;
            }
        }

        KeyEvent ke = new KeyEvent(source, id, time, modifiers, keyCode, keyChar);

        JNodeToolkit.postToTarget(ke, source);
    }

    private boolean processSystemKey(KeyboardEvent event) {
        final int key_code = event.getKeyCode();
        if (key_code == KeyEvent.VK_F12 && event.isAltDown() ||
            key_code == KeyEvent.VK_BACK_SPACE && event.isAltDown() && event.isControlDown()) {
            event.consume();
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    JNodeToolkit.stopGui();
                    return null;
                }
            });
            return true;
        } else if (key_code == KeyEvent.VK_F11 && event.isAltDown()) {
            event.consume();
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    JNodeToolkit.getJNodeToolkit().leaveGUI();
                    return null;
                }
            });
            return true;
        } else if (key_code == KeyEvent.VK_F5 && event.isControlDown() && event.isAltDown()) {
            event.consume();
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    JNodeToolkit.refreshGui();
                    return null;
                }
            });
            return true;

        } else if (key_code == KeyEvent.VK_PRINTSCREEN ||
            key_code == KeyEvent.VK_F10 && event.isAltDown() && event.isControlDown()) {
            event.consume();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        AccessController.doPrivileged(new PrivilegedAction<Void>() {
                            public Void run() {
                                try {
                                    log.debug("Taking screenshot");
                                    File f = File.createTempFile("screen", ".png",
                                        new File(System.getProperty("java.io.tmpdir")));
                                    Dimension ss = JNodeToolkit.getJNodeToolkit().getScreenSize();
                                    BufferedImage capture =
                                        new Robot().createScreenCapture(new Rectangle(0, 0, ss.width, ss.height));
                                    log.debug("Saving screenshot to " + f);
                                    ImageIO.write(capture, "png", f);
                                    log.debug("Saved " + f);
                                } catch (Exception e) {
                                    log.error("Error taking screenshot", e);
                                }

                                return null;
                            }
                        });
                    } catch (Exception x) {
                        log.error("", x);
                    }
                }
            }, "screenshot").start();

            return true;
        }
        return false;
    }

    /**
     * @return Returns the keyboardAPI.
     */
    final KeyboardAPI getKeyboardAPI() {
        return keyboardAPI;
    }

    synchronized int getModifiers() {
        return modifiers;
    }

    private synchronized void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * Install this handler as current keyboard focus manager.
     */
    public void install() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                //setCurrentKeyboardFocusManager(KeyboardHandler.this);
                return null;
            }
        });
    }

    /**
     * Uninstall this handler as current keyboard focus manager.
     */
    public void uninstall() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
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
