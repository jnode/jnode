/*
 * Created on Mar 5, 2003
 * $Id$
 */
package org.jnode.driver.input;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * A KeyboardInterpreter translate scancodes into KeyboardEvent's.
 * @author epr
 */
public abstract class KeyboardInterpreter {
	
	protected int flags;
	
	protected int[] vkMap;
	protected char[] lcharMap;
	protected char[] ucharMap;
	protected char[] altGrCharMap;
	protected int lastScancode;
	
	public final static int XT_RELEASE = 0x80;
	public final static int XT_EXTENDED = 0xE0;
	
	public KeyboardInterpreter() {
		vkMap = new int[256];
		lcharMap = new char[256];
		ucharMap = new char[256];
		altGrCharMap = new char[256];
		initVkMap();
	}
	
	/**
	 * Interpret a given scancode into a keyevent.
	 * @param scancode
	 */
	public KeyboardEvent interpretScancode(int scancode) {
		
		if (scancode == XT_EXTENDED) {
			lastScancode = scancode;
			return null;
		}
		
		boolean released = ((scancode & XT_RELEASE) != 0);
		scancode &= 0x7f;
		int vk = deriveKeyCode(scancode, (lastScancode == XT_EXTENDED));
		// debug output to find new keycodes
//		System.err.println("[" + (lastScancode == XT_EXTENDED ? "E" : "N") + scancode + "] " /*+ KeyEvent.getKeyText(vk)*/);
		int mask;
		switch (vk) {
			case KeyEvent.VK_ALT :
				mask = InputEvent.ALT_DOWN_MASK;
				break;
			case KeyEvent.VK_ALT_GRAPH :
				mask = InputEvent.ALT_GRAPH_DOWN_MASK;
				break;
			case KeyEvent.VK_CONTROL :
				mask = InputEvent.CTRL_DOWN_MASK;
				break;
			case KeyEvent.VK_SHIFT :
				mask = InputEvent.SHIFT_DOWN_MASK;
				break;
			default :
				mask = 0;
		}
		
		if (mask != 0) {
			if (released) {
				flags &= ~mask;
			} else {
				flags |= mask;
			}
		}
		if (vk != 0) {
			char ch = (char)-1;
			try {
				ch = interpretExtendedScanCode();
			} catch (UnsupportedKeyException e) {
				if ((flags & InputEvent.SHIFT_DOWN_MASK) != 0) {
					ch = ucharMap[scancode];
				} else if((flags & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
					ch = altGrCharMap[scancode];
				} else {
					ch = lcharMap[scancode];
				}
			}
			catch (DeadKeyException e) {}
			long time = System.currentTimeMillis();
			lastScancode = scancode;
			return new KeyboardEvent(released ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED, time, flags, vk, ch);
		} else {
			lastScancode = scancode;
			return null;
		}
	}
	
	protected int deriveKeyCode(int scancode, boolean extended) {
		int vk = vkMap[scancode];
		if (extended) {
			switch (scancode) {
				case 82 :
					vk = KeyEvent.VK_INSERT;
					break;
				case 71 :
					vk = KeyEvent.VK_HOME;
					break;
				case 73 :
					vk = KeyEvent.VK_PAGE_UP;
					break;
				case 83 :
					vk = KeyEvent.VK_DELETE;
					break;
				case 79 :
					vk = KeyEvent.VK_END;
					break;
				case 81 :
					vk = KeyEvent.VK_PAGE_DOWN;
					break;
				case 72 :
					vk = KeyEvent.VK_UP;
					break;
				case 75 :
					vk = KeyEvent.VK_LEFT;
					break;
				case 80 :
					vk = KeyEvent.VK_DOWN;
					break;
				case 77 :
					vk = KeyEvent.VK_RIGHT;
					break;
				case 28 :
					vk = KeyEvent.VK_ENTER;
					break;
				case 55 :
					vk = KeyEvent.VK_PRINTSCREEN;
					break;
				case 56 :
					vk = KeyEvent.VK_ALT_GRAPH;
					break;
				case 29 :
					vk = KeyEvent.VK_CONTROL;
					break;
				case 93 :
					vk = KeyEvent.VK_PROPS;
					break;
				case 53 :
					vk = KeyEvent.VK_DIVIDE;
					break;
				default :
					vk = 0;
			}
		}
		return vk;
	}
	
	/**
	 * Initialize the mapping between scancode and virtual key code.
	 */
	protected abstract void initVkMap();
	
	
	/**
	 * Method interpretExtendedScanCode this method sould be used to handle the dead keys and other special keys
	 *
	 * @return   the char to use or throws an Exception
	 * @exception   UnsupportedKeyException is thrown if the current key is not handled by this method
	 * @exception   DeadKeyException is thrown if the current key is a dead key
	 *
	 * @author 	Marc DENTY
	 * @version  2/8/2004
	 * @since 0.15
	 */
	protected abstract char interpretExtendedScanCode() throws UnsupportedKeyException, DeadKeyException;
}

