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
 
package org.jnode.driver.textscreen;

/**
 * A ScrollableScreen has more lines then an actual (device)
 * screen and maps a visible portion of its screen onto the 
 * actual (device)screen.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ScrollableTextScreen extends TextScreen {
    
    /**
     * Scroll a given number of rows up.
     * @param rows
     */
    public void scrollUp(int rows);
    
    /**
     * Scroll a given number of rows down.
     * @param rows
     */
    public void scrollDown(int rows);
}
