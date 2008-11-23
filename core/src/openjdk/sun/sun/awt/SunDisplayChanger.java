/*
 * Copyright 2000-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.awt.IllegalComponentStateException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.WeakHashMap;

import java.util.logging.*;

/**
 * This class is used to aid in keeping track of DisplayChangedListeners and
 * notifying them when a display change has taken place. DisplayChangedListeners
 * are notified when the display's bit depth is changed, or when a top-level
 * window has been dragged onto another screen.
 *
 * It is safe for a DisplayChangedListener to be added while the list is being
 * iterated.
 * 
 * The displayChanged() call is propagated after some occurrence (either
 * due to user action or some other application) causes the display mode
 * (e.g., depth or resolution) to change.  All heavyweight components need
 * to know when this happens because they need to create new surfaceData
 * objects based on the new depth.
 *
 * displayChanged() is also called on Windows when they are moved from one
 * screen to another on a system equipped with multiple displays.
 */
public class SunDisplayChanger {
    private static final Logger log = Logger.getLogger("sun.awt.multiscreen.SunDisplayChanger");

    // Create a new synchronizedMap with initial capacity of one listener.  
    // It is asserted that the most common case is to have one GraphicsDevice 
    // and one top-level Window.  
    private Map listeners = Collections.synchronizedMap(new WeakHashMap(1));

    public SunDisplayChanger() {}

    /*
     * Add a DisplayChangeListener to this SunDisplayChanger so that it is 
     * notified when the display is changed.
     */
    public void add(DisplayChangedListener theListener) {
        if (log.isLoggable(Level.FINE)) {
            if (theListener == null) {
                log.log(Level.FINE, "Assertion (theListener != null) failed");
            }
        }
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER, "Adding listener: " + theListener);
        }
        listeners.put(theListener, null);
    }

    /*
     * Remove the given DisplayChangeListener from this SunDisplayChanger.
     */
    public void remove(DisplayChangedListener theListener) {
        if (log.isLoggable(Level.FINE)) {
            if (theListener == null) {
                log.log(Level.FINE, "Assertion (theListener != null) failed");
            }
        }
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER, "Removing listener: " + theListener);
        }
        listeners.remove(theListener);
    }

    /*
     * Notify our list of DisplayChangedListeners that a display change has
     * taken place by calling their displayChanged() methods.
     */
    public void notifyListeners() {
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "notifyListeners");
        }
    // This method is implemented by making a clone of the set of listeners,
    // and then iterating over the clone.  This is because during the course
    // of responding to a display change, it may be appropriate for a 
    // DisplayChangedListener to add or remove itself from a SunDisplayChanger.
    // If the set itself were iterated over, rather than a clone, it is 
    // trivial to get a ConcurrentModificationException by having a
    // DisplayChangedListener remove itself from its list.
    // Because all display change handling is done on the event thread, 
    // synchronization provides no protection against modifying the listener
    // list while in the middle of iterating over it.  -bchristi 7/10/2001

        HashMap listClone;
        Set cloneSet;

        synchronized(listeners) {
            listClone = new HashMap(listeners);
        }

        cloneSet = listClone.keySet();
        Iterator itr = cloneSet.iterator();
        while (itr.hasNext()) {
            DisplayChangedListener current =
             (DisplayChangedListener) itr.next();
            try {
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "displayChanged for listener: " + current);
                }
                current.displayChanged();
            } catch (IllegalComponentStateException e) {
                // This DisplayChangeListener is no longer valid.  Most
                // likely, a top-level window was dispose()d, but its
                // Java objects have not yet been garbage collected.  In any
                // case, we no longer need to track this listener, though we
                // do need to remove it from the original list, not the clone.
                listeners.remove(current);
            }
        }
    }

    /*
     * Notify our list of DisplayChangedListeners that a palette change has
     * taken place by calling their paletteChanged() methods.
     */
    public void notifyPaletteChanged() {
        if (log.isLoggable(Level.FINEST)) {
            log.finest("notifyPaletteChanged");
        }
    // This method is implemented by making a clone of the set of listeners,
    // and then iterating over the clone.  This is because during the course
    // of responding to a display change, it may be appropriate for a 
    // DisplayChangedListener to add or remove itself from a SunDisplayChanger.
    // If the set itself were iterated over, rather than a clone, it is 
    // trivial to get a ConcurrentModificationException by having a
    // DisplayChangedListener remove itself from its list.
    // Because all display change handling is done on the event thread, 
    // synchronization provides no protection against modifying the listener
    // list while in the middle of iterating over it.  -bchristi 7/10/2001

        HashMap listClone;
        Set cloneSet;

        synchronized (listeners) {
            listClone = new HashMap(listeners);
        }
        cloneSet = listClone.keySet();
        Iterator itr = cloneSet.iterator();
        while (itr.hasNext()) {
            DisplayChangedListener current = 
             (DisplayChangedListener) itr.next();
            try {
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "paletteChanged for listener: " + current);
                }
                current.paletteChanged();
            } catch (IllegalComponentStateException e) {
                // This DisplayChangeListener is no longer valid.  Most
                // likely, a top-level window was dispose()d, but its
                // Java objects have not yet been garbage collected.  In any
                // case, we no longer need to track this listener, though we
                // do need to remove it from the original list, not the clone.
                listeners.remove(current);
            }
        }
    }
}
