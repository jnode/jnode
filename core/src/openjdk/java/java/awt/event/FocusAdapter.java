/*
 * Copyright 1996-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 * An abstract adapter class for receiving keyboard focus events.
 * The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 * <P>
 * Extend this class to create a <code>FocusEvent</code> listener 
 * and override the methods for the events of interest. (If you implement the 
 * <code>FocusListener</code> interface, you have to define all of
 * the methods in it. This abstract class defines null methods for them
 * all, so you can only have to define methods for events you care about.)
 * <P>
 * Create a listener object using the extended class and then register it with 
 * a component using the component's <code>addFocusListener</code> 
 * method. When the component gains or loses the keyboard focus,
 * the relevant method in the listener object is invoked,
 * and the <code>FocusEvent</code> is passed to it.
 *
 * @see FocusEvent
 * @see FocusListener
 * @see <a href="http://java.sun.com/docs/books/tutorial/post1.0/ui/focuslistener.html">Tutorial: Writing a Focus Listener</a>
 *
 * @author Carl Quinn
 * @since 1.1
 */
public abstract class FocusAdapter implements FocusListener {
    /**
     * Invoked when a component gains the keyboard focus.
     */
    public void focusGained(FocusEvent e) {}

    /**
     * Invoked when a component loses the keyboard focus.
     */
    public void focusLost(FocusEvent e) {}
}
