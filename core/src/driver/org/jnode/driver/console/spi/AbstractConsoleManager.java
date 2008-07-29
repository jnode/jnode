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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
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

    /**
     * My logger
     */
    protected final Logger log = Logger.getLogger(getClass());

    /**
     * All registered consoles
     */
    private final Map<String, Console> consoles = new HashMap<String, Console>();

    /**
     * The current console
     */
    private Console current;

    /**
     * The keyboard api i'm using
     */
    private KeyboardAPI kbApi;

    /**
     * The pointer devices that I'm listener on
     */
    private final LinkedList<Device> pointerDevs = new LinkedList<Device>();

    /**
     * The device manager i'm using
     */
    private final DeviceManager devMan;

    /**
     * The holder for the context console
     */
    private static final InheritableThreadLocal<Console> contextConsole = new InheritableThreadLocal<Console>();

    private AbstractConsoleManager parent;

    private final Map<Integer, Stack<Console>> stackMap = new HashMap<Integer, Stack<Console>>();
    private Stack<Console> currentStack;

    /**
     * Initialize a new instance
     */
    public AbstractConsoleManager()
        throws ConsoleException {
        try {
            devMan = InitialNaming.lookup(DeviceManager.NAME);
            openInput(devMan);
        } catch (NameNotFoundException ex) {
            throw new ConsoleException("DeviceManager not found", ex);
        }
        current = null;
    }

    protected final void initializeKeyboard(Device kbDev) {
        try {
            this.kbApi = kbDev.getAPI(KeyboardAPI.class);
            this.kbApi.addKeyboardListener(this);
        } catch (ApiNotFoundException ex) {
            BootLog.error("KeyboardAPI not found", ex);
        }
    }

    protected KeyboardAPI getKeyboardApi() {
        return kbApi;
    }

    /**
     * Add a pointer device
     *
     * @param pDev
     */
    protected final void addPointerDevice(Device pDev) {
        try {
            final PointerAPI pApi = (PointerAPI) pDev.getAPI(PointerAPI.class);
            pointerDevs.add(pDev);
            pApi.addPointerListener(this);
        } catch (ApiNotFoundException ex) {
            BootLog.error("PointerAPI not found", ex);
        }
    }

    protected void openInput(DeviceManager devMan) {
        final Collection<Device> kbs = devMan.getDevicesByAPI(KeyboardAPI.class);
        if (!kbs.isEmpty()) {
            initializeKeyboard((Device) kbs.iterator().next());
        }
        final Collection<Device> pointers = devMan.getDevicesByAPI(PointerAPI.class);
        for (Device dev : pointers) {
            addPointerDevice(dev);
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
     * Gets the console that "hosts" the current thread.
     *
     * @return Console
     */
    public Console getContextConsole() {
        Console c = contextConsole.get();
        if (c == null) {
            c = getFocus();
            contextConsole.set(c);
        }
        return c;
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
        if (this.current != null && this.current != console) {
            this.current.focusLost(new FocusEvent(FocusEvent.FOCUS_LOST));
        }
        this.current = console;
        if (this.current != null) {
            current.focusGained(new FocusEvent(FocusEvent.FOCUS_GAINED));
        }
    }

    public void printConsoles(PrintStream ps) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.addAll(stackMap.keySet());
        Collections.sort(list);
        for (Integer key : list) {
            ps.println("Screen of " + KeyEvent.getKeyText(key) + ":");
            Stack<Console> stack = stackMap.get(key);
            int t_ind = stack.size();
            for (int i = t_ind; i-- > 0;) {
                Console console = stack.get(i);
                String prefix = console == current ? " > " :
                    i == t_ind - 1 ? " * " : "   ";
                ps.println(prefix + console.getConsoleName());
            }
        }
    }

    public Console getConsoleByAccelerator(int keyCode) {
        Stack<Console> stack = stackMap.get(keyCode);
        if (stack != null && !stack.empty()) {
            currentStack = stack;
            return stack.peek();
        }

        return null;
    }

    protected void setAccelerator(Console console) {
        for (int i = 0; i < 12; i++) {
            final int keyCode = KeyEvent.VK_F1 + i;
            Stack<Console> stack = stackMap.get(keyCode);
            if (stack == null) {
                stack = new Stack<Console>();
            }

            if (stack.empty()) {
                stackMap.put(keyCode, stack);
                stack.push(console);
                currentStack = stack;
                return;
            }
        }
    }

    protected void stackConsole(Console console) {
        if (currentStack != null) currentStack.push(console);
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
        if (contextConsole.get() == console) {
            contextConsole.set(null);
        }
        if (current == console) {
            console.focusLost(new FocusEvent(FocusEvent.FOCUS_LOST));
        }

        consoles.remove(console.getConsoleName());
        if (currentStack != null && !currentStack.empty() && currentStack.peek() == console) {
            currentStack.pop();
            if (!currentStack.empty()) {
                current = currentStack.peek();
                focus(current);
            } else {
                Integer last_key = null;
                for (Iterator<Map.Entry<Integer, Stack<Console>>> it = stackMap.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<Integer, Stack<Console>> entry = it.next();
                    if (entry.getValue().equals(currentStack)) {
                        last_key = entry.getKey();
                        it.remove();
                        break;
                    }
                }
                if (!stackMap.isEmpty()) {
                    Integer new_key = null;
                    List<Integer> keys = new ArrayList<Integer>(stackMap.keySet());
                    Collections.sort(keys);
                    if (last_key == null) {
                        new_key = keys.get(0);
                    } else {
                        Collections.reverse(keys);
                        for (Integer k : keys) {
                            if (k < last_key) {
                                new_key = k;
                                break;
                            }
                        }
                        if (new_key == null) {
                            new_key = keys.get(keys.size() - 1);
                        }
                    }

                    currentStack = stackMap.get(new_key);
                    current = currentStack.peek();
                    focus(current);
                }
            }
        }

        if (current == console) {
            current = null;
            if (!consoles.isEmpty()) {
                focus(consoles.values().iterator().next());
            }
        }

        if (parent != null && consoles.isEmpty()) {
            handleFocus();
        }
    }

    public void registerConsole(Console console) {
        consoles.put(console.getConsoleName(), console);
        if (current == null) {
            current = console;
            current.focusGained(new FocusEvent(FocusEvent.FOCUS_GAINED));
        }
        if (contextConsole.get() == null) {
            contextConsole.set(console);
        }
    }

    private void handleFocus() {
        if (consoles.isEmpty()) {
            if (parent != null)
                parent.handleFocus();
        } else {
            Console c = getFocus();
            if (c == null)
                c = consoles.values().iterator().next();

            focus(c);
        }
    }

    /**
     * Close all consoles.
     */
    public void closeAll() {
        for (Iterator<Console> i = consoles.values().iterator(); i.hasNext();) {
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

    public Set<String> getConsoleNames() {
        return new HashSet<String>(consoles.keySet());
    }

    public Collection<Console> getConsoles() {
        return new ArrayList<Console>(consoles.values());
    }


    public AbstractConsoleManager getParent() {
        return parent;
    }

    public void setParent(ConsoleManager parent) {
        this.parent = (AbstractConsoleManager) parent;
    }

    void restack(final AbstractConsole console) {
        int accel = console.getAcceleratorKeyCode();
        if (accel == 0) return;

        //remove console
        for (Iterator<Integer> iter = stackMap.keySet().iterator(); iter.hasNext();) {
            Integer key = iter.next();
            if (key == accel)
                return; //no restack needed

            Stack<Console> stack = stackMap.get(key);
            if (stack.contains(console)) {
                stack.remove(console);

                if (stack.empty())
                    iter.remove();

                break;
            }
        }

        //add the console to the specified screen
        Stack<Console> stack = stackMap.get(accel);
        if (stack == null) {
            stack = new Stack<Console>();
            stackMap.put(accel, stack);
        }
        stack.push(console);
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
