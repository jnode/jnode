/*
 * $Id$
 */
package org.jnode.driver.console.spi;

import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.console.Console;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.PointerAPI;
import org.jnode.driver.input.PointerEvent;
import org.jnode.naming.InitialNaming;
import org.jnode.system.BootLog;
import org.jnode.system.event.FocusEvent;

/**
 * @author epr
 */
public abstract class AbstractConsoleManager implements ConsoleManager {

    /** My logger */
    protected final Logger log = Logger.getLogger(getClass());
    
    /** All registered consoles */
    private final Map consoles = new HashMap();

    /** The current console */
    private Console current;

    /** The keyboard api i'm using */
    private KeyboardAPI kbApi;

    /** The pointer devices that I'm listener on */
    private final LinkedList pointerDevs = new LinkedList();

    /** The device manager i'm using */
    private final DeviceManager devMan;

    /**
     * Initialize a new instance
     */
    public AbstractConsoleManager()
            throws ConsoleException {
        try {
            devMan = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);
            openInput(devMan);
        } catch (NameNotFoundException ex) {
            throw new ConsoleException("DeviceManager not found", ex);
        }
        current = null;
    }

    final void initializeKeyboard(Device kbDev) {
        try {
            this.kbApi = (KeyboardAPI) kbDev.getAPI(KeyboardAPI.class);
            this.kbApi.addKeyboardListener(this);
        } catch (ApiNotFoundException ex) {
            BootLog.error("KeyboardAPI not found", ex);
        }
    }

    /**
     * Add a pointer device
     * 
     * @param pDev
     */
    final void addPointerDevice(Device pDev) {
        try {
            final PointerAPI pApi = (PointerAPI) pDev.getAPI(PointerAPI.class);
            pointerDevs.add(pDev);
            pApi.addPointerListener(this);
        } catch (ApiNotFoundException ex) {
            BootLog.error("PointerAPI not found", ex);
        }
    }

    private final void openInput(DeviceManager devMan) {
        final Collection kbs = devMan.getDevicesByAPI(KeyboardAPI.class);
        if (!kbs.isEmpty()) {
            initializeKeyboard((Device) kbs.iterator().next());
        }
        final Collection pointers = devMan.getDevicesByAPI(PointerAPI.class);
        for (Iterator i = pointers.iterator(); i.hasNext();) {
            addPointerDevice((Device) i.next());
        }
        devMan.addListener(new DevManListener());
    }

    /**
     * Remove a pointer device
     * 
     * @param pDev
     */
    final void removePointer(Device pDev) {
        if (pointerDevs.remove(pDev)) {
            try {
                final PointerAPI pApi = (PointerAPI) pDev
                        .getAPI(PointerAPI.class);
                pApi.removePointerListener(this);
            } catch (ApiNotFoundException ex) {
                BootLog.error("PointerAPI not found", ex);
            }
        }
    }

    /**
     * Gets the console with the given index
     * 
     * @param name
     * @return The console
     */
    public Console getConsole(String name) {
        return (Console) consoles.get(name);
    }

    /**
     * Gets the currently focused console.
     * 
     * @return Console
     */
    public Console getFocus() {
        return current;
    }

    /**
     * Focus the given console
     * 
     * @param console
     */
    public synchronized void focus(Console console) {
        log.debug("focus(" + console.getConsoleName() + ")");
        if (this.current != null) {
            log.debug("Sending focusLost to " + current.getConsoleName());
            this.current.focusLost(new FocusEvent(FocusEvent.FOCUS_LOST));
        }
        this.current = console;
        if (this.current != null) {
            log.debug("Sending focusGained to " + current.getConsoleName());
            current.focusGained(new FocusEvent(FocusEvent.FOCUS_GAINED));
        }
    }

    public Console getConsoleByAccelerator(int keyCode) {
        for (Iterator iter = consoles.values().iterator(); iter.hasNext();) {
            final Console console = (Console) iter.next();
            if (console.getAcceleratorKeyCode() == keyCode) {
                return console;
            }
        }
        return null;
    }

    /**
     * Just keeping track of the One previous console will lead to lots of
     * problems. We need a stack, at least. Currently it is the client's
     * responsibility to choose the new console.
     * 
     * @param console
     */
    public void unregisterConsole(Console console) {
        log.debug("unregisterConsole(" + console.getConsoleName() + ")");
        if (current == console) {
            console.focusLost(new FocusEvent(FocusEvent.FOCUS_LOST));
        }
        consoles.remove(console.getConsoleName());
        if (current == console) {
            current = null;
            if (!consoles.isEmpty()) {
                focus((Console)consoles.values().iterator().next());
            }
        }
    }

    public void registerConsole(Console console) {
        log.debug("registerConsole(" + console.getConsoleName() + ")");
        consoles.put(console.getConsoleName(), console);
        if (current == null) {
            current = console;
            current.focusGained(new FocusEvent(FocusEvent.FOCUS_GAINED));
        }
    }

    /**
     * Close all consoles.
     */
    public void closeAll() {
        for (Iterator i = consoles.values().iterator(); i.hasNext();) {
            Console console = (Console) i.next();
            i.remove(); // remove from iterator before closing to avoid
            // concurrent modification
            console.close();
        }
    }

    /**
     * @see org.jnode.driver.input.KeyboardListener#keyPressed(org.jnode.driver.input.KeyboardEvent)
     */
    public void keyPressed(KeyboardEvent event) {
        if ((event.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0) {
            final int keyCode = event.getKeyCode();
            final Console c = getConsoleByAccelerator(keyCode);
            if (c != null) {
                focus(c);
                event.consume();
            }
        }
        if (!event.isConsumed() && (current != null)) {
            current.keyPressed(event);
        }
    }

    /**
     * @see org.jnode.driver.input.KeyboardListener#keyReleased(org.jnode.driver.input.KeyboardEvent)
     */
    public void keyReleased(KeyboardEvent event) {
        if (current != null) {
            current.keyReleased(event);
        }
    }

    /**
     * @see org.jnode.driver.input.PointerListener#pointerStateChanged(org.jnode.driver.input.PointerEvent)
     */
    public void pointerStateChanged(PointerEvent event) {
        if (current != null) {
            current.pointerStateChanged(event);
        }
    }

    public Set getConsoleNames() {
        return new HashSet(consoles.keySet());
    }

    public Collection getConsoles() {
        return new ArrayList(consoles.values());
    }

    /**
     * This listener looks for registration of a keyboard device.
     * 
     * @author epr
     */
    class DevManListener implements DeviceListener {

        /**
         * @param device
         * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
         */
        public void deviceStarted(Device device) {
            if (device.implementsAPI(KeyboardAPI.class)) {
                initializeKeyboard(device);
            } else if (device.implementsAPI(PointerAPI.class)) {
                addPointerDevice(device);
            }
        }

        /**
         * @param device
         * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
         */
        public void deviceStop(Device device) {
            if (device.implementsAPI(PointerAPI.class)) {
                removePointer(device);
            }
        }
    }

}