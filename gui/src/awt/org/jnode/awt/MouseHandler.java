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

package org.jnode.awt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;
import java.util.HashSet;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.input.PointerAPI;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;
import org.jnode.driver.video.HardwareCursor;
import org.jnode.driver.video.HardwareCursorAPI;
import javax.naming.NameNotFoundException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
public class MouseHandler implements PointerListener {
    //todo refactor this pattern to be generally available in JNode for AWT and text consoles as well
    private static class PointerAPIHandler implements PointerAPI, DeviceListener {
        private Collection<PointerAPI> pointerList = new HashSet<PointerAPI>();
        private Collection<PointerListener> listenerList = new HashSet<PointerListener>();

        PointerAPIHandler() {
            try {
                DeviceManager dm = DeviceUtils.getDeviceManager();
                dm.addListener(this);
                for (Device device : dm.getDevicesByAPI(PointerAPI.class)) {
                    try {
                        pointerList.add(device.getAPI(PointerAPI.class));
                    } catch (ApiNotFoundException anfe) {
                        //ignore
                    }
                }
            } catch (NameNotFoundException nfe) {
                //todo handle it
            }
        }

        public void addPointerListener(PointerListener listener) {
            listenerList.add(listener);
            for (PointerAPI api : pointerList) {
                api.addPointerListener(listener);
            }
        }

        public void removePointerListener(PointerListener listener) {
            listenerList.remove(listener);
            for (PointerAPI api : pointerList) {
                api.removePointerListener(listener);
            }
        }

        public void setPreferredListener(PointerListener listener) {
            for (PointerAPI api : pointerList) {
                api.setPreferredListener(listener);
            }
        }

        public void deviceStarted(Device device) {
            if (device.implementsAPI(PointerAPI.class)) {
                try {
                    PointerAPI api = device.getAPI(PointerAPI.class);
                    pointerList.add(api);
                    for (PointerListener listener : listenerList) {
                        api.addPointerListener(listener);
                    }
                } catch (ApiNotFoundException anfe) {
                    //ignore
                }
            }
        }

        public void deviceStop(Device device) {
            if (device.implementsAPI(PointerAPI.class)) {
                try {
                    PointerAPI api = device.getAPI(PointerAPI.class);
                    pointerList.remove(api);
                    for (PointerListener listener : listenerList) {
                        api.removePointerListener(listener);
                    }
                } catch (ApiNotFoundException anfe) {
                    //ignore
                }
            }
        }

        boolean hasPointer() {
            return !pointerList.isEmpty();
        }
    }

    private final PointerAPI pointerAPI;

    private static final int[] BUTTON_MASK = {
        PointerEvent.BUTTON_LEFT, PointerEvent.BUTTON_RIGHT, PointerEvent.BUTTON_MIDDLE
    };

    private static final int[] BUTTON_NUMBER = {1, 2, 3};
    private static int CLICK_REPEAT_DURATION = 400;

    /**
     * Queue where to post my events
     */
    private final EventQueue eventQueue;
    private final HardwareCursorAPI hwCursor;
    private final KeyboardHandler keyboardHandler;
    private final Dimension screenSize;

    private int lastButtons = 0;
    private final boolean[] buttonPressed = new boolean[3];
    private final int[] buttonClickCount = new int[3];
    private final long[] buttonClickTime = new long[3];
    private boolean postClicked = false;

    private static final Logger log = Logger.getLogger(MouseHandler.class);

    private Component lastSource;
    private Component dragSource;

    private int x;

    private int y;

    /**
     * Create a new instance.
     *
     * @param fbDevice        a frame buffer device
     * @param screenSize      screen size
     * @param eventQueue      the event queue instance
     * @param keyboardHandler keyboard handler
     */
    public MouseHandler(Device fbDevice, Dimension screenSize,
                        EventQueue eventQueue, KeyboardHandler keyboardHandler) {
        this.eventQueue = eventQueue;
        HardwareCursorAPI hwCursor = null;

        try {
            hwCursor = fbDevice.getAPI(HardwareCursorAPI.class);
        } catch (ApiNotFoundException ex) {
            log.info("No hardware-cursor found on device " + fbDevice.getId());
        }

        this.keyboardHandler = keyboardHandler;
        this.hwCursor = hwCursor;
        this.screenSize = screenSize;

        if (hwCursor != null) {
            hwCursor.setCursorImage(JNodeCursors.ARROW);
            hwCursor.setCursorVisible(true);
            hwCursor.setCursorPosition(0, 0);
        }
        this.pointerAPI = new PointerAPIHandler();
        pointerAPI.addPointerListener(this);
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                MouseHandler.this.pointerAPI.setPreferredListener(MouseHandler.this);
                return null;
            }
        });
    }

    void setCursorImage(HardwareCursor ci) {
        if (hwCursor != null) {
            synchronized (this) {
                hwCursor.setCursorImage(ci);
                hwCursor.setCursorVisible(true);
            }
        }
    }

    /**
     * Close this handler
     */
    public void close() {
        if (pointerAPI != null) {
            pointerAPI.removePointerListener(this);            
        }
        
        if (hwCursor != null) {
            // hide the cursor so that it won't stay displayed
            // if we stay in graphic mode
            hwCursor.setCursorVisible(false);
        }
    }

    /**
     * Move the mouse pointer to absolute coordinates (x, y).
     *
     * @param x the destination x coordinate
     * @param y the destination y coordinate
     * @see java.awt.peer.RobotPeer#mouseMove(int, int)
     */
    void mouseMove(int x, int y) {
        // buttons and z unchanged (true means absolute values)
        pointerStateChanged(lastButtons, x, y, 0, true);
    }

    /**
     * Press one or more mouse buttons.
     *
     * @param buttons the buttons to press; a bitmask of one or more of
     *                these {@link java.awt.event.InputEvent} fields:
     *                <p/>
     *                <ul>
     *                <li>BUTTON1_MASK</li>
     *                <li>BUTTON2_MASK</li>
     *                <li>BUTTON3_MASK</li>
     *                </ul>
     * @see java.awt.peer.RobotPeer#mousePress(int)
     */
    void mousePress(int buttons) {
        // x, y and z unchanged (false means relative values)
        pointerStateChanged(buttons | lastButtons, 0, 0, 0, false);
    }

    /**
     * Release one or more mouse buttons.
     *
     * @param buttons the buttons to release; a bitmask of one or more
     *                of these {@link java.awt.event.InputEvent} fields:
     *                <p/>
     *                <ul>
     *                <li>BUTTON1_MASK</li>
     *                <li>BUTTON2_MASK</li>
     *                <li>BUTTON3_MASK</li>
     *                </ul>
     * @see java.awt.peer.RobotPeer#mouseRelease(int)
     */
    void mouseRelease(int buttons) {
        // x, y and z unchanged (false means relative values)
        pointerStateChanged(lastButtons & (buttons ^ 0xFFFFFFFF), 0, 0, 0, false);
    }

    /**
     * Rotate the mouse scroll wheel.
     *
     * @param wheelAmt number of steps to rotate mouse wheel.  negative
     *                 to rotate wheel up (away from the user), positive to rotate wheel
     *                 down (toward the user). So, this is a relative value.
     * @see java.awt.peer.RobotPeer#mouseWheel(int)
     */
    void mouseWheel(int wheelAmt) {
        // buttons, x and y unchanged (false means relative values)
        pointerStateChanged(lastButtons, 0, 0, wheelAmt, false);
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    void setCursor(int x, int y) {
        this.x = x;
        this.y = y;
        if (hwCursor != null)
            hwCursor.setCursorPosition(x, y);
    }

    public void pointerStateChanged(PointerEvent event) {
        pointerStateChanged(event.getButtons(), event.getX(), event.getY(),
            event.getZ(), event.isAbsolute());
        event.consume();
    }

    /**
     * @param buttons  a vlaue encoding the state of mouse buttons
     * @param newX     a relative or absolute value for x coordinate
     * @param newY     a relative or absolute value for y coordinate
     * @param newZ     a relative or absolute value for wheel mouse
     * @param absolute are newX, newY and newZ relative or absolute values ?
     */
    void pointerStateChanged(int buttons, int newX, int newY, int newZ, boolean absolute) {
        long time = System.currentTimeMillis();
        final int newAbsX = absolute ? newX : x + newX;
        final int newAbsY = absolute ? newY : y + newY;
        x = Math.min(screenSize.width - 1, Math.max(0, newAbsX));
        y = Math.min(screenSize.height - 1, Math.max(0, newAbsY));
        if (hwCursor != null) hwCursor.setCursorPosition(x, y);

        lastButtons = buttons;

        Component source = findSource();
        if (source == null || !source.isShowing()) return;
        if (newZ != 0) {
            postEvent(source, MouseEvent.MOUSE_WHEEL, time, 0, MouseEvent.NOBUTTON, newZ);
        }

        boolean eventFired = false;
        for (int i = 0; i < 3; i++) {
            if (time - buttonClickTime[i] > CLICK_REPEAT_DURATION) {
                buttonClickTime[i] = 0;
                buttonClickCount[i] = 0;
            } else {
                buttonClickTime[i] = time;
            }

            if ((buttons & BUTTON_MASK[i]) != 0) {
                if (!buttonPressed[i]) {
                    buttonClickCount[i] += 1;
                    buttonClickTime[i] = time;
                    postEvent(source, MouseEvent.MOUSE_PRESSED, time, buttonClickCount[i], BUTTON_NUMBER[i], 0);
                    dragSource = source;
                    buttonPressed[i] = true;
                    eventFired = true;
                    postClicked = true;
                    break;
                }
            } else if (buttonPressed[i]) {
                postEvent(dragSource, MouseEvent.MOUSE_RELEASED, time, buttonClickCount[i], BUTTON_NUMBER[i], 0);
                if (postClicked || buttonClickCount[i] > 0) {
                    postEvent(source, MouseEvent.MOUSE_CLICKED, time, buttonClickCount[i], BUTTON_NUMBER[i], 0);
                    postClicked = false;
                }
                buttonPressed[i] = false;
                eventFired = true;
                break;
            }
        }

        if (source != lastSource) {
            if (lastSource != null) {
                // Notify mouse exited
                postEvent(lastSource, MouseEvent.MOUSE_EXITED, time, 0, MouseEvent.NOBUTTON, 0);
                if (lastSource.isCursorSet())
                    lastSource.setCursor(lastSource.getCursor());
            }
            // Notify mouse entered
            postEvent(source, MouseEvent.MOUSE_ENTERED, time, 0, MouseEvent.NOBUTTON, 0);
            if (source.isCursorSet())
                source.setCursor(source.getCursor());

            for (int i = buttonClickTime.length; --i > 0; buttonClickTime[i] = 0) ;
            eventFired = true;
            postClicked = false;
        }

        if (!eventFired) {
            if (buttonPressed[0]) {
                postEvent(dragSource, MouseEvent.MOUSE_DRAGGED, time, buttonClickCount[0], MouseEvent.BUTTON1, 0);
            } else if (buttonPressed[1]) {
                postEvent(dragSource, MouseEvent.MOUSE_DRAGGED, time, buttonClickCount[1], MouseEvent.BUTTON2, 0);
            } else if (buttonPressed[2]) {
                postEvent(dragSource, MouseEvent.MOUSE_DRAGGED, time, buttonClickCount[2], MouseEvent.BUTTON3, 0);
            } else {
                postEvent(source, MouseEvent.MOUSE_MOVED, time, 0, MouseEvent.NOBUTTON, 0);
            }
            postClicked = false;
        }
        lastSource = source;
    }

    /**
     * Post a mouse event with the given properties.
     *
     * @param source     the source component of the event
     * @param id         the event identifier
     * @param time       the time of occurence of this event
     * @param clickCount the number of mouse clicks
     * @param button     the mouse button in action
     * @param wheelAmt   the amount that the mouse wheel was rotated
     */
    private void postEvent(Component source, int id, long time, int clickCount, int button, int wheelAmt) {
        if (!source.isShowing()) return;
//        final Window w = SwingUtilities.getWindowAncestor(source);
//        Point pwo;
//        if (w != null && w.isShowing()) {
//            pwo = w.getLocationOnScreen();
//        } else {
//            pwo = new Point(0, 0);
//        }

        final Point p = source.getLocationOnScreen();
        final boolean popupTrigger = (button == MouseEvent.BUTTON2);

        final int ex = x - p.x; // - pwo.x;
        final int ey = y - p.y; // - pwo.y;
        final int modifiers = getModifiers();
        MouseEvent event;
        if (id == MouseEvent.MOUSE_WHEEL) {
            event = new MouseWheelEvent(source, id, time, modifiers, ex, ey,
                1, popupTrigger, MouseWheelEvent.WHEEL_UNIT_SCROLL,
                wheelAmt, //TODO check what to put here
                wheelAmt); //TODO check what to put here
        } else {
            event = new MouseEvent(source, id, time, modifiers, ex, ey,
                clickCount, popupTrigger, button);
            if (id == MouseEvent.MOUSE_PRESSED) {
                ((JNodeToolkit) Toolkit.getDefaultToolkit()).activateWindow(source);
            }
        }

        //debug
        /*
        if (
            id == MouseEvent.MOUSE_PRESSED ||
            id == MouseEvent.MOUSE_RELEASED ||
            id == MouseEvent.MOUSE_CLICKED ||
            id == MouseEvent.MOUSE_ENTERED ||
            id == MouseEvent.MOUSE_EXITED
            ) {

              org.jnode.vm.Unsafe.debug(event.toString()+"\n");
              org.jnode.vm.Unsafe.debug("x="+ x + " y=" + y +" ex="+ ex + " ey=" + ey +
                  " p.x=" + p.x + " p.y=" + p.y +"\n");
        }
        */

        JNodeToolkit.postToTarget(event, source);
    }

    private Component findSource() {
        final JNodeToolkit tk = (JNodeToolkit) Toolkit.getDefaultToolkit();
        Component source = tk.getTopComponentAt(x, y);
        if ((source != null) && source.isShowing()) {
            return source;
        } else {
            return null;
        }
    }

    private int getModifiers() {
        int modifiers = 0;
        if (buttonPressed[0]) modifiers |= MouseEvent.BUTTON1_MASK;
        if (buttonPressed[1]) modifiers |= MouseEvent.BUTTON2_MASK;
        if (buttonPressed[2]) modifiers |= MouseEvent.BUTTON3_MASK;
        return modifiers | keyboardHandler.getModifiers();
    }
}
