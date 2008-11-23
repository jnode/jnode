/*
 * Copyright 1997-1999 Sun Microsystems, Inc.  All Rights Reserved.
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
package javax.swing.border;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Component;

/**
 * Interface describing an object capable of rendering a border
 * around the edges of a swing component.
 * For examples of using borders see
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/misc/border.html">How to Use Borders</a>,
 * a section in <em>The Java Tutorial.</em>
 * <p>
 * In the Swing component set, borders supercede Insets as the
 * mechanism for creating a (decorated or plain) area around the 
 * edge of a component.
 * <p>
 * Usage Notes:
 * <ul>
 * <li>Use EmptyBorder to create a plain border (this mechanism
 *     replaces its predecessor, <code>setInsets</code>).
 * <li>Use CompoundBorder to nest multiple border objects, creating
 *     a single, combined border.
 * <li>Border instances are designed to be shared. Rather than creating
 *     a new border object using one of border classes, use the
 *     BorderFactory methods, which produces a shared instance of the
 *     common border types.
 * <li>Additional border styles include BevelBorder, SoftBevelBorder,
 *     EtchedBorder, LineBorder, TitledBorder, and MatteBorder.
 * <li>To create a new border class, subclass AbstractBorder.   
 * </ul>
 * 
 * @author David Kloba
 * @author Amy Fowler
 * @see javax.swing.BorderFactory
 * @see EmptyBorder
 * @see CompoundBorder
 */
public interface Border
{
    /**
     * Paints the border for the specified component with the specified 
     * position and size.
     * @param c the component for which this border is being painted
     * @param g the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */
    void paintBorder(Component c, Graphics g, int x, int y, int width, int height);

    /**
     * Returns the insets of the border.  
     * @param c the component for which this border insets value applies
     */
    Insets getBorderInsets(Component c);

    /**
     * Returns whether or not the border is opaque.  If the border
     * is opaque, it is responsible for filling in it's own
     * background when painting.
     */
    boolean isBorderOpaque();
}
