/*
 * Copyright 2000-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.util.prefs;
import java.util.*;

/**
 * A factory object that generates Preferences objects.  Providers of
 * new {@link Preferences} implementations should provide corresponding
 * <tt>PreferencesFactory</tt> implementations so that the new
 * <tt>Preferences</tt> implementation can be installed in place of the 
 * platform-specific default implementation.
 *
 * <p><strong>This class is for <tt>Preferences</tt> implementers only.
 * Normal users of the <tt>Preferences</tt> facility should have no need to
 * consult this documentation.</strong>
 *
 * @author  Josh Bloch
 * @see     Preferences
 * @since   1.4
 */
public interface PreferencesFactory {
    /**
     * Returns the system root preference node.  (Multiple calls on this
     * method will return the same object reference.)
     */
    Preferences systemRoot();

    /**
     * Returns the user root preference node corresponding to the calling
     * user.  In a server, the returned value will typically depend on
     * some implicit client-context.
     */
    Preferences userRoot();
}
