/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.driver.console.spi;

import java.util.ArrayList;

import org.jnode.driver.console.Console;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.ConsoleListener;
import org.jnode.driver.console.ConsoleEvent;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;
import org.jnode.system.event.FocusEvent;
import org.jnode.util.QueueProcessorThread;
import org.jnode.util.QueueProcessor;

/**
 * @author epr
 */
public abstract class AbstractConsole implements Console {

    private final ArrayList<KeyboardListener> keyboardListeners = new ArrayList<KeyboardListener>();

    private final ArrayList<PointerListener> pointerListeners = new ArrayList<PointerListener>();

    private final ArrayList<ConsoleListener> consoleListeners = new ArrayList<ConsoleListener>();

    private final String consoleName;

    private final ConsoleManager mgr;

    private int acceleratorKeyCode = 0;

    /**
     * Initialize this instance.
     * 
     * @param mgr
     * @param name
     */
    public AbstractConsole(ConsoleManager mgr, String name) {
        this.mgr = mgr;
        this.consoleName = name;
        this.keyboardEventProcessor.start();
    }

    /**
     * Does this console has the focus?
     * 
     * @return True if this console has the focus, false otherwise
     */
    public boolean isFocused() {
        return (mgr.getFocus() == this);
    }

    /**
     * Add a PointerListener
     * 
     * @param l
     */
    public void addPointerListener(PointerListener l) {
        synchronized (pointerListeners) {
            if (!pointerListeners.contains(l)) {
                pointerListeners.add(l);
            }
        }
    }

    /**
     * Remove a PointerListener
     * 
     * @param l
     */
    public void removePointerListener(PointerListener l) {
        synchronized (pointerListeners) {
            pointerListeners.remove(l);
        }
    }

    /**
     * Send the PointerEvent to all the PointerListeners
     * 
     * @param event
     */
    protected void dispatchPointerEvent(PointerEvent event) {
        if (event.isConsumed()) {
            return;
        }
        synchronized (pointerListeners) {
            for (PointerListener l : pointerListeners) {
                l.pointerStateChanged(event);
                if (event.isConsumed()) {
                    break;
                }
            }
        }
    }

    /**
     * @param l
     * @see org.jnode.driver.console.Console#addKeyboardListener(org.jnode.driver.input.KeyboardListener)
     */
    public void addKeyboardListener(KeyboardListener l) {
        synchronized (keyboardListeners) {
            if (!keyboardListeners.contains(l)) {
                keyboardListeners.add(l);
            }
        }
    }

    /**
     * @param l
     * @see org.jnode.driver.console.Console#removeKeyboardListener(org.jnode.driver.input.KeyboardListener)
     */
    public void removeKeyboardListener(KeyboardListener l) {
        synchronized (keyboardListeners) {
            keyboardListeners.remove(l);
        }
    }

    /**
     * @param l
     * @see org.jnode.driver.console.Console#addConsoleListener(org.jnode.driver.console.ConsoleListener)
     */
    public void addConsoleListener(ConsoleListener l) {
        synchronized (consoleListeners) {
            if (!consoleListeners.contains(l)) {
                consoleListeners.add(l);
            }
        }
    }

    /**
     * @param l
     * @see org.jnode.driver.console.Console#removeConsoleListener(org.jnode.driver.console.ConsoleListener)
     */
    public void removeConsoleListener(ConsoleListener l) {
        synchronized (consoleListeners) {
            consoleListeners.remove(l);
        }
    }

    /**
     * Dispatch a given keyboard event to all known listeners.
     *
     * @param event
     */
    protected void dispatchConsoleEvent(ConsoleEvent event) {
        if (event.isConsumed()) {
            return;
        }

        synchronized (consoleListeners) {
            for (ConsoleListener l : consoleListeners) {
                l.consoleClosed(event);
            }
        }
    }

    /**
     * @param event
     * @see org.jnode.driver.input.KeyboardListener#keyPressed(org.jnode.driver.input.KeyboardEvent)
     */
    public void keyPressed(KeyboardEvent event) {
        if (isFocused()) {
            keyboardEventProcessor.getQueue().add(event);
        }
    }

    /**
     * @param event
     * @see org.jnode.driver.input.KeyboardListener#keyReleased(org.jnode.driver.input.KeyboardEvent)
     */
    public void keyReleased(KeyboardEvent event) {
        if (isFocused()) {
            keyboardEventProcessor.getQueue().add(event);
        }
    }

    /**
     * @param event
     * @see org.jnode.system.event.FocusListener#focusGained(org.jnode.system.event.FocusEvent)
     */
    public void focusGained(FocusEvent event) {
    }

    /**
     * @param event
     * @see org.jnode.system.event.FocusListener#focusLost(org.jnode.system.event.FocusEvent)
     */
    public void focusLost(FocusEvent event) {
    }

    private QueueProcessorThread<KeyboardEvent> keyboardEventProcessor = new QueueProcessorThread<KeyboardEvent>("console-keyboard-event-processor", new QueueProcessor<KeyboardEvent>() {
        public void process(KeyboardEvent event) throws Exception {
            dispatchKeyboardEvent(event);
        }
    });

    /**
     * Dispatch a given keyboard event to all known listeners.
     * 
     * @param event
     */
    protected void dispatchKeyboardEvent(KeyboardEvent event) {
        if (event.isConsumed()) {
            return;
        }

        synchronized (keyboardListeners) {
            for (KeyboardListener l : keyboardListeners) {
                if (event.isKeyPressed()) {
                    l.keyPressed(event);
                } else if (event.isKeyReleased()) {
                    l.keyReleased(event);
                }
                if (event.isConsumed()) {
                    break;
                }
            }
        }
    }

    /**
     * Respond to scroll events from the mouse.
     * 
     * @param event
     * @see org.jnode.driver.input.PointerListener#pointerStateChanged(org.jnode.driver.input.PointerEvent)
     */
    public void pointerStateChanged(PointerEvent event) {
        if (isFocused()) {
            dispatchPointerEvent(event);
        }
    }

    /**
     * Close this console.
     * 
     * @see org.jnode.driver.console.Console#close()
     */
    public void close() {
        mgr.unregisterConsole(this);
        dispatchConsoleEvent(new ConsoleEvent(this));
    }

    public void setAcceleratorKeyCode(int keyCode) {
        this.acceleratorKeyCode = keyCode;
        if(mgr instanceof AbstractConsoleManager)
            ((AbstractConsoleManager)mgr).restack(this);
    }

    public int getAcceleratorKeyCode() {
        return acceleratorKeyCode;
    }

    /**
     * @return Returns the consoleName.
     */
    public String getConsoleName() {
        return consoleName;
    }
    /**
     * @see org.jnode.driver.console.Console#getManager()
     */
    public final ConsoleManager getManager() {
        return mgr;
    }
}
