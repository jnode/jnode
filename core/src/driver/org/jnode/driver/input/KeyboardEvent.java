/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package org.jnode.driver.input;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.jnode.system.event.SystemEvent;

/**
 * @author epr
 */
public class KeyboardEvent extends SystemEvent {

	private int modifiers;
	private int keyCode;
	private char keyChar;
	
	public KeyboardEvent(int id, long time, int modifiers, int keyCode, char keyChar) {
		super(id, time);
		this.modifiers = modifiers;
		this.keyCode = keyCode;
		this.keyChar = keyChar;
	}
	
	/**
	 * @return char
	 */
	public char getKeyChar() {
		return keyChar;
	}

	/**
	 * @return int
	 */
	public int getKeyCode() {
		return keyCode;
	}

	/**
	 * @return int
	 */
	public int getModifiers() {
		return modifiers;
	}

	public boolean isAltDown() {
		return ((modifiers & InputEvent.ALT_DOWN_MASK) != 0);
	}
	
	public boolean isControlDown() {
		return ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0);
	}
	
	public boolean isShiftDown() {
		return ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0);
	}
	
	public String toString() {
		return "KeyboardEvent[" + KeyEvent.getKeyText(keyCode) + "='" + keyChar + "']";
	}
	
	public boolean isKeyPressed() {
		return (id == KeyEvent.KEY_PRESSED);
	}
	
	public boolean isKeyReleased() {
		return (id == KeyEvent.KEY_RELEASED);
	}
}
