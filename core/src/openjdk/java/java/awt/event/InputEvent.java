/*
 * Copyright 1996-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.awt.event;

import java.awt.Event;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * The root event class for all component-level input events.
 *
 * Input events are delivered to listeners before they are
 * processed normally by the source where they originated.
 * This allows listeners and component subclasses to "consume"
 * the event so that the source will not process them in their
 * default manner.  For example, consuming mousePressed events
 * on a Button component will prevent the Button from being
 * activated.
 *
 * @author Carl Quinn
 *
 * @see KeyEvent
 * @see KeyAdapter
 * @see MouseEvent
 * @see MouseAdapter
 * @see MouseMotionAdapter
 *
 * @since 1.1
 */
public abstract class InputEvent extends ComponentEvent {
    private static final Logger log = Logger.getLogger("java.awt.event.InputEvent");

    /**
     * The Shift key modifier constant.
     * It is recommended that SHIFT_DOWN_MASK be used instead.
     */
    public static final int SHIFT_MASK = Event.SHIFT_MASK;

    /**
     * The Control key modifier constant.
     * It is recommended that CTRL_DOWN_MASK be used instead.
     */
    public static final int CTRL_MASK = Event.CTRL_MASK;

    /**
     * The Meta key modifier constant.
     * It is recommended that META_DOWN_MASK be used instead.
     */
    public static final int META_MASK = Event.META_MASK;

    /**
     * The Alt key modifier constant.
     * It is recommended that ALT_DOWN_MASK be used instead.
     */
    public static final int ALT_MASK = Event.ALT_MASK;

    /**
     * The AltGraph key modifier constant.
     */
    public static final int ALT_GRAPH_MASK = 1 << 5;

    /**
     * The Mouse Button1 modifier constant.
     * It is recommended that BUTTON1_DOWN_MASK be used instead.
     */
    public static final int BUTTON1_MASK = 1 << 4;

    /**
     * The Mouse Button2 modifier constant.
     * It is recommended that BUTTON2_DOWN_MASK be used instead.
     * Note that BUTTON2_MASK has the same value as ALT_MASK.
     */
    public static final int BUTTON2_MASK = Event.ALT_MASK;

    /**
     * The Mouse Button3 modifier constant.
     * It is recommended that BUTTON3_DOWN_MASK be used instead.
     * Note that BUTTON3_MASK has the same value as META_MASK.
     */
    public static final int BUTTON3_MASK = Event.META_MASK;

    /**
     * The Shift key extended modifier constant.
     * @since 1.4
     */
    public static final int SHIFT_DOWN_MASK = 1 << 6;

    /**
     * The Control key extended modifier constant.
     * @since 1.4
     */
    public static final int CTRL_DOWN_MASK = 1 << 7;

    /**
     * The Meta key extended modifier constant.
     * @since 1.4
     */
    public static final int META_DOWN_MASK = 1 << 8;

    /**
     * The Alt key extended modifier constant.                    
     * @since 1.4
     */
    public static final int ALT_DOWN_MASK = 1 << 9;

    /**
     * The Mouse Button1 extended modifier constant.
     * @since 1.4
     */
    public static final int BUTTON1_DOWN_MASK = 1 << 10;

    /**
     * The Mouse Button2 extended modifier constant.
     * @since 1.4
     */
    public static final int BUTTON2_DOWN_MASK = 1 << 11;

    /**
     * The Mouse Button3 extended modifier constant.
     * @since 1.4
     */
    public static final int BUTTON3_DOWN_MASK = 1 << 12;

    /**
     * The AltGraph key extended modifier constant.
     * @since 1.4
     */
    public static final int ALT_GRAPH_DOWN_MASK = 1 << 13;

    // the constant below MUST be updated if any extra modifier
    // bits are to be added!
    // in fact, it is undesirable to add modifier bits
    // to the same field as this may break applications
    // see bug# 5066958
    
    static final int FIRST_HIGH_BIT = 1 << 14;
    
    static final int JDK_1_3_MODIFIERS = SHIFT_DOWN_MASK - 1;
    static final int HIGH_MODIFIERS = ~( FIRST_HIGH_BIT - 1 );

    /**
     * The input event's Time stamp in UTC format.  The time stamp 
     * indicates when the input event was created.
     *
     * @serial
     * @see #getWhen()
     */
    long when;

    /**
     * The state of the modifier mask at the time the input
     * event was fired.
     *
     * @serial
     * @see #getModifiers()
     * @see #getModifiersEx()
     * @see java.awt.event.KeyEvent
     * @see java.awt.event.MouseEvent
     */
    int modifiers;

    /*
     * A flag that indicates that this instance can be used to access 
     * the system clipboard. 
     */
    private transient boolean canAccessSystemClipboard;

    static {
        /* ensure that the necessary native libraries are loaded */
	NativeLibLoader.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * Initialize JNI field and method IDs for fields that may be
       accessed from C.
     */
    private static native void initIDs();

    /**
     * Constructs an InputEvent object with the specified source component,
     * modifiers, and type.
     * <p>Note that passing in an invalid <code>id</code> results in
     * unspecified behavior. This method throws an
     * <code>IllegalArgumentException</code> if <code>source</code>
     * is <code>null</code>.
     * 
     * @param source the object where the event originated
     * @param id the event type
     * @param when the time the event occurred
     * @param modifiers represents the modifier keys and mouse buttons down 
     *                  while the event occurred
     * @throws IllegalArgumentException if <code>source</code> is null
     */
    InputEvent(Component source, int id, long when, int modifiers) {
        super(source, id);
        this.when = when;
        this.modifiers = modifiers;
        canAccessSystemClipboard = canAccessSystemClipboard();
    }

    private boolean canAccessSystemClipboard() {
        boolean b = false;

        if (!GraphicsEnvironment.isHeadless()) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                try {
                    sm.checkSystemClipboardAccess();
                    b = true;
                } catch (SecurityException se) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "InputEvent.canAccessSystemClipboard() got SecurityException ", se);
                    }
                }
            } else {
                b = true;
            }
        }

        return b;
    }

    /**
     * Returns whether or not the Shift modifier is down on this event.
     */
    public boolean isShiftDown() {
        return (modifiers & SHIFT_MASK) != 0;
    }

    /**
     * Returns whether or not the Control modifier is down on this event.
     */
    public boolean isControlDown() {
        return (modifiers & CTRL_MASK) != 0;
    }

    /**
     * Returns whether or not the Meta modifier is down on this event.
     */
    public boolean isMetaDown() {
        return (modifiers & META_MASK) != 0;
    }

    /**
     * Returns whether or not the Alt modifier is down on this event.
     */
    public boolean isAltDown() {
        return (modifiers & ALT_MASK) != 0;
    }

    /**
     * Returns whether or not the AltGraph modifier is down on this event.
     */
    public boolean isAltGraphDown() {
        return (modifiers & ALT_GRAPH_MASK) != 0;
    }

    /**
     * Returns the timestamp of when this event occurred.
     */
    public long getWhen() {
        return when;
    }

    /**
     * Returns the modifier mask for this event.
     */
    public int getModifiers() {
        return modifiers & (JDK_1_3_MODIFIERS | HIGH_MODIFIERS);
    }

    /**
     * Returns the extended modifier mask for this event.
     * Extended modifiers represent the state of all modal keys, 
     * such as ALT, CTRL, META, and the mouse buttons just after 
     * the event occurred
     * <P> 
     * For example, if the user presses <b>button 1</b> followed by
     * <b>button 2</b>, and then releases them in the same order,
     * the following sequence of events is generated:
     * <PRE>
     *    <code>MOUSE_PRESSED</code>:  <code>BUTTON1_DOWN_MASK</code>
     *    <code>MOUSE_PRESSED</code>:  <code>BUTTON1_DOWN_MASK | BUTTON2_DOWN_MASK</code>
     *    <code>MOUSE_RELEASED</code>: <code>BUTTON2_DOWN_MASK</code>
     *    <code>MOUSE_CLICKED</code>:  <code>BUTTON2_DOWN_MASK</code>
     *    <code>MOUSE_RELEASED</code>: 
     *    <code>MOUSE_CLICKED</code>:  
     * </PRE>
     * <P>
     * It is not recommended to compare the return value of this method
     * using <code>==</code> because new modifiers can be added in the future.
     * For example, the appropriate way to check that SHIFT and BUTTON1 are
     * down, but CTRL is up is demonstrated by the following code:
     * <PRE>
     *    int onmask = SHIFT_DOWN_MASK | BUTTON1_DOWN_MASK;
     *    int offmask = CTRL_DOWN_MASK;
     *    if ((event.getModifiersEx() & (onmask | offmask)) == onmask) {
     *        ...
     *    }
     * </PRE>
     * The above code will work even if new modifiers are added.
     * 
     * @since 1.4
     */
    public int getModifiersEx() {
        return modifiers & ~JDK_1_3_MODIFIERS;
    }

    /**
     * Consumes this event so that it will not be processed
     * in the default manner by the source which originated it.
     */
    public void consume() {
        consumed = true;
    }

    /**
     * Returns whether or not this event has been consumed.
     * @see #consume
     */
    public boolean isConsumed() {
        return consumed;
    }

    // state serialization compatibility with JDK 1.1
    static final long serialVersionUID = -2482525981698309786L;

    /**
     * Returns a String describing the extended modifier keys and 
     * mouse buttons, such as "Shift", "Button1", or "Ctrl+Shift".  
     * These strings can be localized by changing the 
     * awt.properties file.
     *
     * @param modifiers a modifier mask describing the extended
       *                modifier keys and mouse buttons for the event 
     * @return a text description of the combination of extended 
     *         modifier keys and mouse buttons that were held down 
     *         during the event. 
     * @since 1.4
     */
    public static String getModifiersExText(int modifiers) {
        StringBuilder buf = new StringBuilder();
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.meta", "Meta"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.control", "Ctrl"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.alt", "Alt"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.shift", "Shift"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.altGraph", "Alt Graph"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.button1", "Button1"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.button2", "Button2"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.button3", "Button3"));
            buf.append("+");
        }
        if (buf.length() > 0) {
            buf.setLength(buf.length()-1); // remove trailing '+'
        }
        return buf.toString();
    }
}
