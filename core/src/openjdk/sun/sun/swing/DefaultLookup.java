/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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
package sun.swing;

import java.awt.Color;
import java.awt.Insets;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import sun.awt.AppContext;

/**
 * DefaultLookup provides a way to customize the lookup done by the
 * UIManager. The default implementation of DefaultLookup forwards
 * the call to the UIManager.
 * <p>
 * <b>WARNING:</b> While this class is public, it should not be treated as
 * public API and its API may change in incompatable ways between dot dot
 * releases and even patch releases. You should not rely on this class even
 * existing.
 *
 * @author Scott Violet
 */
public class DefaultLookup {
    /**
     * Key used to store DefaultLookup for AppContext.
     */
    private static final Object DEFAULT_LOOKUP_KEY = new
                                        StringBuffer("DefaultLookup");
    /**
     * Thread that last asked for a default.
     */
    private static Thread currentDefaultThread;
    /**
     * DefaultLookup for last thread.
     */
    private static DefaultLookup currentDefaultLookup;

    /**
     * If true, a custom DefaultLookup has been set.
     */
    private static boolean isLookupSet;


    /**
     * Sets the DefaultLookup instance to use for the current
     * <code>AppContext</code>. Null implies the UIManager should be
     * used.
     */
    public static void setDefaultLookup(DefaultLookup lookup) {
        synchronized(DefaultLookup.class) {
            if (!isLookupSet && lookup == null) {
                // Null was passed in, and no one has invoked setDefaultLookup
                // with a non-null value, we don't need to do anything.
                return;
            }
            else if (lookup == null) {
                // null was passed in, but someone has invoked setDefaultLookup
                // with a non-null value, use an instance of DefautLookup
                // which will fallback to UIManager.
                lookup = new DefaultLookup();
            }
            isLookupSet = true;
            AppContext.getAppContext().put(DEFAULT_LOOKUP_KEY, lookup);
            currentDefaultThread = Thread.currentThread();
            currentDefaultLookup = lookup;
        }
    }

    public static Object get(JComponent c, ComponentUI ui, String key) {
        boolean lookupSet;
        synchronized(DefaultLookup.class) {
            lookupSet = isLookupSet;
        }
        if (!lookupSet) {
            // No one has set a valid DefaultLookup, use UIManager.
            return UIManager.get(key, c.getLocale());
        }
        Thread thisThread = Thread.currentThread();
        DefaultLookup lookup;
        synchronized(DefaultLookup.class) {
            // See if we've already cached the DefaultLookup for this thread,
            // and use it if we have.
            if (thisThread == currentDefaultThread) {
                // It is cached, use it.
                lookup = currentDefaultLookup;
            }
            else {
                // Not cached, get the DefaultLookup to use from the AppContext
                lookup = (DefaultLookup)AppContext.getAppContext().get(
                                                   DEFAULT_LOOKUP_KEY);
                if (lookup == null) {
                    // Fallback to DefaultLookup, which will redirect to the
                    // UIManager.
                    lookup = new DefaultLookup();
                    AppContext.getAppContext().put(DEFAULT_LOOKUP_KEY, lookup);
                }
                // Cache the values to make the next lookup easier.
                currentDefaultThread = thisThread;
                currentDefaultLookup = lookup;
            }
        }
        return lookup.getDefault(c, ui, key);
    }

    //
    // The following are convenience method that all use getDefault.
    //
    public static int getInt(JComponent c, ComponentUI ui, String key,
                             int defaultValue) {
        Object iValue = get(c, ui, key);

        if (iValue == null || !(iValue instanceof Number)) {
            return defaultValue;
        }
        return ((Number)iValue).intValue();
    }

    public static Insets getInsets(JComponent c, ComponentUI ui, String key,
                                   Insets defaultValue) {
        Object iValue = get(c, ui, key);

        if (iValue == null || !(iValue instanceof Insets)) {
            return defaultValue;
        }
        return (Insets)iValue;
    }

    public static boolean getBoolean(JComponent c, ComponentUI ui, String key,
                                     boolean defaultValue) {
        Object iValue = get(c, ui, key);

        if (iValue == null || !(iValue instanceof Boolean)) {
            return defaultValue;
        }
        return ((Boolean)iValue).booleanValue();
    }

    public static Color getColor(JComponent c, ComponentUI ui, String key,
                                 Color defaultValue) {
        Object iValue = get(c, ui, key);

        if (iValue == null || !(iValue instanceof Color)) {
            return defaultValue;
        }
        return (Color)iValue;
    }



    public Object getDefault(JComponent c, ComponentUI ui, String key) {
        // basic
        return UIManager.get(key, c.getLocale());
    }
}
