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
 * An abstract adapter class for receiving mouse events.
 * The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 * <P>
 * Mouse events let you track when a mouse is pressed, released, clicked, 
 * moved, dragged, when it enters a component, when it exits and 
 * when a mouse wheel is moved.
 * <P>
 * Extend this class to create a {@code MouseEvent}
 * (including drag and motion events) or/and {@code MouseWheelEvent} 
 * listener and override the methods for the events of interest. (If you implement the 
 * {@code MouseListener},
 * {@code MouseMotionListener} 
 * interface, you have to define all of
 * the methods in it. This abstract class defines null methods for them
 * all, so you can only have to define methods for events you care about.)
 * <P>
 * Create a listener object using the extended class and then register it with 
 * a component using the component's {@code addMouseListener} 
 * {@code addMouseMotionListener}, {@code addMouseWheelListener}
 * methods. 
 * The relevant method in the listener object is invoked  and the {@code MouseEvent}
 * or {@code MouseWheelEvent}  is passed to it in following cases:
 * <p><ul>
 * <li>when a mouse button is pressed, released, or clicked (pressed and  released)
 * <li>when the mouse cursor enters or exits the component
 * <li>when the mouse wheel rotated, or mouse moved or dragged
 * </ul>
 *
 * @author Carl Quinn
 * @author Andrei Dmitriev
 *
 * @see MouseEvent 
 * @see MouseWheelEvent 
 * @see MouseListener
 * @see MouseMotionListener
 * @see MouseWheelListener
 * @see <a href="http://java.sun.com/docs/books/tutorial/post1.0/ui/mouselistener.html">Tutorial: Writing a Mouse Listener</a>
 *
 * @since 1.1
 */
public abstract class MouseAdapter implements MouseListener, MouseWheelListener, MouseMotionListener {
    /**
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mousePressed(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    public void mouseWheelMoved(MouseWheelEvent e){}

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    public void mouseDragged(MouseEvent e){}

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    public void mouseMoved(MouseEvent e){}
}
