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
 
package org.jnode.driver.console.textscreen;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.textscreen.ScrollableTextScreen;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
class ScrollableTextScreenConsole extends TextScreenConsole {

    /**
     * @param mgr
     * @param name
     * @param screen
     */
    public ScrollableTextScreenConsole(ConsoleManager mgr, String name,
            ScrollableTextScreen screen, int options) {
        super(mgr, name, screen, options);
    }
    
    /**
     * Scroll a given number of rows up.
     * @param rows
     */
    public void scrollUp(int rows) {
	    final ScrollableTextScreen screen = (ScrollableTextScreen)getScreen();
        screen.scrollUp(rows);
	    screen.sync();
    }
    
    /**
     * Scroll a given number of rows down.
     * @param rows
     */
    public void scrollDown(int rows) {
	    final ScrollableTextScreen screen = (ScrollableTextScreen)getScreen();
	    screen.scrollDown(rows);
	    screen.sync();
    }
    
    /**
     * @see org.jnode.driver.input.KeyboardListener#keyPressed(org.jnode.driver.input.KeyboardEvent)
     */
    public void keyPressed(KeyboardEvent event) {
        if (isFocused() && !event.isConsumed()) {
    		final int modifiers = event.getModifiers();
    		if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
    			switch (event.getKeyCode()) {
    				case KeyEvent.VK_PAGE_UP :
    					scrollUp(10);
    					event.consume();
    					break;
    				case KeyEvent.VK_PAGE_DOWN :
    				    scrollDown(10);
    					event.consume();
    					break;
    				case KeyEvent.VK_UP :
    				    scrollUp(1);
    					event.consume();
    					break;
    				case KeyEvent.VK_DOWN :
    				    scrollDown(1);
    					event.consume();
    					break;
    			}
    		}            
        }
        if (!event.isConsumed()) {
            super.keyPressed(event);
        }
    }

    /**
     * @see org.jnode.driver.input.PointerListener#pointerStateChanged(org.jnode.driver.input.PointerEvent)
     */
    public void pointerStateChanged(PointerEvent event) {
        if (isFocused() && (event.getZ() != 0)) {
            final int z = event.getZ();
            if (z < 0) {
                scrollDown(Math.abs(z));
            } else {
                scrollUp(Math.abs(z));                
            }
            event.consume();
        }
        if (!event.isConsumed()) {
            super.pointerStateChanged(event);
        }
    }
}
