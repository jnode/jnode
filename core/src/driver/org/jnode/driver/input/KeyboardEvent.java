/*
 * $Id$
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
