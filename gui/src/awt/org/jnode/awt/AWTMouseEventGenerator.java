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
 
package org.jnode.awt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;
import org.jnode.driver.video.HardwareCursorAPI;

/**
 * @author Levente S\u00e1ntha
 */
public class AWTMouseEventGenerator implements PointerListener {
    private static final Logger log = Logger.getLogger(AWTMouseEventGenerator.class);

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

    private boolean[] buttonPressed = new boolean[3];
    private int[] buttonClickCount = new int[3];
    private long[] buttonClickTime = new long[3];
    private boolean postClicked = false;

    private Component lastSource;
    private Component dragSource;

    private int x;

    private int y;

    public AWTMouseEventGenerator(EventQueue eventQueue, Dimension screenSize, HardwareCursorAPI hwCursor,
                                  KeyboardHandler keyboardHandler) {
        this.eventQueue = eventQueue;
        this.screenSize = screenSize;
        this.hwCursor = hwCursor;
        this.keyboardHandler = keyboardHandler;
    }

    public void pointerStateChanged(PointerEvent event) {
        long time = System.currentTimeMillis();
        x = Math.min(screenSize.width - 1, Math.max(0, x + event.getX()));
        y = Math.min(screenSize.height - 1, Math.max(0, y + event.getY()));
        hwCursor.setCursorPosition(x, y);
        Component source = findSource();
        if (source == null || !source.isShowing()) return;

        final int buttons = event.getButtons();
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
                    postEvent(source, MouseEvent.MOUSE_PRESSED, time, buttonClickCount[i], BUTTON_NUMBER[i]);
                    dragSource = source;
                    buttonPressed[i] = true;
                    eventFired = true;
                    postClicked = true;
                    break;
                }
            } else if (buttonPressed[i]) {
                postEvent(dragSource, MouseEvent.MOUSE_RELEASED, time, buttonClickCount[i], BUTTON_NUMBER[i]);
                if (postClicked || !postClicked && buttonClickCount[i] > 0) {
                    postEvent(source, MouseEvent.MOUSE_CLICKED, time, buttonClickCount[i], BUTTON_NUMBER[i]);
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
                postEvent(lastSource, MouseEvent.MOUSE_EXITED, time, 0, MouseEvent.NOBUTTON);
            }
            // Notify mouse entered
            postEvent(source, MouseEvent.MOUSE_ENTERED, time, 0, MouseEvent.NOBUTTON);
            for (int i = buttonClickTime.length; --i > 0; buttonClickTime[i] = 0) ;
            eventFired = true;
            postClicked = false;
        }

        if (!eventFired) {
            if (buttonPressed[0]) {
                postEvent(dragSource, MouseEvent.MOUSE_DRAGGED, time, buttonClickCount[0], MouseEvent.BUTTON1);
            } else if (buttonPressed[1]) {
                postEvent(dragSource, MouseEvent.MOUSE_DRAGGED, time, buttonClickCount[1], MouseEvent.BUTTON2);
            } else if (buttonPressed[2]) {
                postEvent(dragSource, MouseEvent.MOUSE_DRAGGED, time, buttonClickCount[2], MouseEvent.BUTTON3);
            } else {
                postEvent(source, MouseEvent.MOUSE_MOVED, time, 0, MouseEvent.NOBUTTON);
            }
            postClicked = false;
        }
        lastSource = source;
    }

    /**
     * Post a mouse event with the given id and button.
     *
     * @param id
     * @param button
     */
    private void postEvent(Component source, int id, long time, int clickCount, int button) {
        if (!source.isShowing()) return;
        final Window w = SwingUtilities.getWindowAncestor(source);
        Point pwo = null;
        if (w != null && w.isShowing()) {
            pwo = w.getLocationOnScreen();
        } else {
            pwo = new Point(-1, -1);
        }

        final Point p = source.getLocationOnScreen();
        final boolean popupTrigger = (button == MouseEvent.BUTTON2);

        final int ex = x - p.x; // - pwo.x;
        final int ey = y - p.y; // - pwo.y;
        final int modifiers = getModifiers();

        final MouseEvent event = new MouseEvent(source, id, time, modifiers, ex, ey,
            clickCount, popupTrigger, button);

        eventQueue.postEvent(event);
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

    private final int getModifiers() {
        int modifiers = 0;
        if (buttonPressed[0]) modifiers |= MouseEvent.BUTTON1_MASK;
        if (buttonPressed[1]) modifiers |= MouseEvent.BUTTON2_MASK;
        if (buttonPressed[2]) modifiers |= MouseEvent.BUTTON3_MASK;
        return modifiers | keyboardHandler.getModifiers();
    }
}
