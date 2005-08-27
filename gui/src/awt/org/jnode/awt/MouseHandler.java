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

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.PointerAPI;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;
import org.jnode.driver.video.HardwareCursorAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
public class MouseHandler implements PointerListener {

    private static final int[] BUTTON_MASK = { PointerEvent.BUTTON_LEFT,
            PointerEvent.BUTTON_RIGHT, PointerEvent.BUTTON_MIDDLE };

    private static final int[] BUTTON_NUMBER = { 1, 2, 3 };

    /** Queue where to post my events */
    private final EventQueue eventQueue;

    private int lastButtons = 0;
    private boolean[] buttonState = new boolean[3];

    private final HardwareCursorAPI hwCursor;

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(MouseHandler.class);

    private final PointerAPI pointerAPI;

    private final Dimension screenSize;

    private Component lastSource;

    private int x;

    private int y;
    private AWTMouseEventGenerator mouseEventGenerator;

    /**
     * Create a new instance
     * 
     * @param fbDevice
     * @param screenSize
     */
    public MouseHandler(Device fbDevice, Dimension screenSize,
                        EventQueue eventQueue, KeyboardHandler keyboardHandler) {
        this.eventQueue = eventQueue;
        HardwareCursorAPI hwCursor = null;
        Device pointerDevice = null;
        PointerAPI pointerAPI = null;
        try {
            hwCursor = (HardwareCursorAPI) fbDevice
                    .getAPI(HardwareCursorAPI.class);
        } catch (ApiNotFoundException ex) {
            log.info("No hardware-cursor found on device " + fbDevice.getId());
        }
        if (hwCursor != null) {
            try {
                final Collection<Device> pointers = DeviceUtils
                        .getDevicesByAPI(PointerAPI.class);
                if (!pointers.isEmpty()) {
                    pointerDevice = (Device) pointers.iterator().next();
                    pointerAPI = (PointerAPI) pointerDevice
                            .getAPI(PointerAPI.class);
                }
            } catch (ApiNotFoundException ex) {
                log.error("Strange...", ex);
            }
        }
        this.hwCursor = hwCursor;
        this.pointerAPI = pointerAPI;
        this.screenSize = screenSize;
        if (pointerAPI != null) {
            log.debug("Using PointerDevice " + pointerDevice.getId());
            hwCursor.setCursorImage(JNodeCursors.ARROW);
            hwCursor.setCursorVisible(true);
            hwCursor.setCursorPosition(0, 0);
            //pointerAPI.addPointerListener(this);
            mouseEventGenerator = new AWTMouseEventGenerator(eventQueue, screenSize, hwCursor, keyboardHandler);
            pointerAPI.addPointerListener(mouseEventGenerator);
        }
    }

    /**
     * Close this handler
     */
    public void close() {
        if (pointerAPI != null) {
            //pointerAPI.removePointerListener(this);
            if(mouseEventGenerator != null){
                pointerAPI.removePointerListener(mouseEventGenerator);
            }
        }
    }
    
    /**
     * Move the mouse pointer to absolute coordinates (x, y).
     *
     * @param x the destination x coordinate
     * @param y the destination y coordinate
     * 
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
     * these {@link InputEvent} fields:
     *
     * <ul>
     *   <li>BUTTON1_MASK</li>
     *   <li>BUTTON2_MASK</li>
     *   <li>BUTTON3_MASK</li>
     * </ul>
     * 
     * @see java.awt.peer.RobotPeer#mousePress(int)
     */
    void mousePress(int buttons) {
        // x, y and z unchanged (false means relative values)
        pointerStateChanged(buttons|lastButtons, 0, 0, 0, false);
    }

    /**
     * Release one or more mouse buttons.
     *
     * @param buttons the buttons to release; a bitmask of one or more
     * of these {@link InputEvent} fields:
     *
     * <ul>
     *   <li>BUTTON1_MASK</li>
     *   <li>BUTTON2_MASK</li>
     *   <li>BUTTON3_MASK</li>
     * </ul>
     * 
     * @see java.awt.peer.RobotPeer#mouseRelease(int)
     */
    void mouseRelease(int buttons) {
        // x, y and z unchanged (false means relative values)
        pointerStateChanged(lastButtons&(buttons^0xFFFFFFFF), 0, 0, 0, false);
    }

    /**
     * Rotate the mouse scroll wheel.
     *
     * @param wheelAmt number of steps to rotate mouse wheel.  negative
     * to rotate wheel up (away from the user), positive to rotate wheel
     * down (toward the user). So, this is a relative value. 
     *
     * @see java.awt.peer.RobotPeer#mouseWheel(int)
     */
    void mouseWheel(int wheelAmt) {
        // buttons, x and y unchanged (false means relative values)
        pointerStateChanged(lastButtons, 0, 0, wheelAmt, false);
    }

    /**
     * @param event
     * @see org.jnode.driver.input.PointerListener#pointerStateChanged(org.jnode.driver.input.PointerEvent)
     */
    /*
     * public void pointerStateChanged(PointerEvent event) { x =
     * Math.min(screenSize.width - 1, Math.max(0, x + event.getX())); y =
     * Math.min(screenSize.height - 1, Math.max(0, y + event.getY()));
     * hwCursor.setCursorPosition(x, y); }
     */
    public void pointerStateChanged(PointerEvent event) 
    {
        pointerStateChanged(event.getButtons(), event.getX(), event.getY(), 
                event.getZ(), event.isAbsolute());
        event.consume();
        System.out.println("event="+event);
    }

    /**
     * TODO handle wheel mouse events (given by newZ)
     * @param buttons
     * @param newX a relative or absolute value for x coordinate
     * @param newY a relative or absolute value for y coordinate
     * @param newZ a relative or absolute value for wheel mouse
     * @param absolute are newX, newY and newZ relative or absolute values ?
     */
    private void pointerStateChanged(int buttons, int newX, int newY, int newZ, 
            boolean absolute)        
    {
        absolute = false;
        final int newAbsX = absolute ? newX : x + newX;
        final int newAbsY = absolute ? newY : y + newY;
        x = Math.min(screenSize.width - 1, Math.max(0, newAbsX));
        y = Math.min(screenSize.height - 1, Math.max(0, newAbsY));
        hwCursor.setCursorPosition(x, y);
        lastButtons = buttons;
        
        if(newZ != 0)
        {
            lastSource = postEvent(null, MouseEvent.MOUSE_WHEEL, 0, newZ);
        }
        
        boolean eventFired = false;
        for (int i = 0; i < BUTTON_MASK.length; i++) {
            final int mask = BUTTON_MASK[i];
            final int nr = BUTTON_NUMBER[i];
            if ((buttons & mask) != 0) {
                if (!buttonState[i]) {
                    lastSource = postEvent(null, MouseEvent.MOUSE_PRESSED, nr, 0);
                    buttonState[i] = true;
                    eventFired = true;
                }
            } else if (buttonState[i]) {
                lastSource = postEvent(null, MouseEvent.MOUSE_RELEASED, nr, 0);
                postEvent(lastSource, MouseEvent.MOUSE_CLICKED, nr, 0);
                buttonState[i] = false;
                eventFired = true;
            }
        }
        /* Must have been a drag or move. */
        if (!eventFired) {
            if (buttonState[0]) {
                postEvent(lastSource, MouseEvent.MOUSE_DRAGGED,
                        MouseEvent.BUTTON1, 0);
            } else if (buttonState[1]) {
                postEvent(lastSource, MouseEvent.MOUSE_DRAGGED,
                        MouseEvent.BUTTON2, 0);
            } else if (buttonState[2]) {
                postEvent(lastSource, MouseEvent.MOUSE_DRAGGED,
                        MouseEvent.BUTTON3, 0);
            } else {
                final Component c = findSource();
                if (c != lastSource) {
                    if (lastSource != null) {
                        // Notify mouse exited
                        postEvent(lastSource, MouseEvent.MOUSE_EXITED,
                                MouseEvent.NOBUTTON, 0);
                    }
                    if (c != null) {
                        // Notify mouse entered
                        postEvent(c, MouseEvent.MOUSE_ENTERED,
                                MouseEvent.NOBUTTON, 0);
                    }
                }
                postEvent(c, MouseEvent.MOUSE_MOVED, MouseEvent.NOBUTTON, 0);
            }
        }
    }

    /**
     * Post a mouse event with the given id and button.
     * 
     * @param id
     * @param button
     * @return The source component used to send the event to.
     */
private Component postEvent(Component source, int id, int button, int wheelAmt) {
        if (source == null) {
            source = findSource();
        }
        // log.debug("Source: " + (source !=
        // null?source.getClass().getName():"source is NULL"));
        // TODO full support for modifiers
        if (source != null && source.isShowing()) {
            final Window w = (Window) SwingUtilities.getAncestorOfClass(
                    Window.class, source);
            Point pw = new Point(-1, -1);
            Point pwo = pw;
            if( w != null && w.isShowing() ) {
                // pw = w.getLocation();
                pwo = w.getLocationOnScreen();
            }
            final Point p = source.getLocationOnScreen();
            final boolean popupTrigger = (button == MouseEvent.BUTTON2);

            final int ex = x - p.x - pwo.x;
            final int ey = y - p.y - pwo.y;
            final int modifiers = buttonToModifiers(button);

            MouseEvent me;
            if(id == MouseEvent.MOUSE_WHEEL)
            {
                me = new MouseWheelEvent(source, id, System
                    .currentTimeMillis(), modifiers, ex, ey, 1, popupTrigger,
                    MouseWheelEvent.WHEEL_UNIT_SCROLL, 
                    wheelAmt, //TODO check what to put here 
                    wheelAmt);//TODO check what to put here
            }
            else
            {
                me = new MouseEvent(source, id, System
                    .currentTimeMillis(), modifiers, ex, ey, 1, popupTrigger,
                    button);
            }

            if (id == MouseEvent.MOUSE_CLICKED) {
                // log.info("MouseClicked to " + source + " at " + ex + "," + ey
                // + " (" + x + "," + y + ")(" + p.x + "," + p.y + ")("
                // + pw.x + "," + pw.y + ")(" + pwo.x + "," + pwo.y + ")");
            }

            eventQueue.postEvent(me);
            // if (id == MouseEvent.MOUSE_CLICKED) {
            // log.info("MouseClicked to " + source + " at " + ex + "," + ey);
            // }
            // } else {
            // log.info("NO MouseEvent, " + source + " not visible");
        }
        return source;
    }

    private final Component findSource() {
        final JNodeToolkit tk = (JNodeToolkit) Toolkit.getDefaultToolkit();
        Component source = tk.getTopComponentAt(x, y);
        if ((source != null) && source.isShowing()) {
            return source;
        } else {
            return null;
        }
    }

    private final int buttonToModifiers(int button) {
        switch (button) {
        case MouseEvent.BUTTON1:
            return MouseEvent.BUTTON1_MASK;
        case MouseEvent.BUTTON2:
            return MouseEvent.BUTTON2_MASK;
        case MouseEvent.BUTTON3:
            return MouseEvent.BUTTON3_MASK;
        default:
            return 0;
        }
    }
}
