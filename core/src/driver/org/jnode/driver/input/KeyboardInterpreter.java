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
	
	private int flags;
	
	private final int[] vkMap;
	protected final char[] lcharMap;
	private final char[] ucharMap;
	private final char[] altGrCharMap;
	private boolean extendedMode;
	
	public final static int XT_RELEASE = 0x80;
	public final static int XT_EXTENDED = 0xE0;
	
	public KeyboardInterpreter() {
		vkMap = new int[256];
		lcharMap = new char[256];
		ucharMap = new char[256];
		altGrCharMap = new char[256];
		initVkMap(vkMap, lcharMap, ucharMap, altGrCharMap);
	}
	
	/**
	 * Interpret a given scancode into a keyevent.
	 * @param scancode
	 */
	public final KeyboardEvent interpretScancode(int scancode) {		
		final boolean extendedMode = this.extendedMode;

		if (scancode == XT_EXTENDED) {
		    this.extendedMode = true;
			return null;
		} else {
		    this.extendedMode = false;
		}
		final boolean released = ((scancode & XT_RELEASE) != 0);
		final long time = System.currentTimeMillis();		
		scancode &= 0x7f;
		
		final int vk = deriveKeyCode(scancode, extendedMode);
		// debug output to find new keycodes
		//VmSystem.getOut().println("[" + (extendedMode ? "E" : "N") + scancode + "," + vk + "] " /*+ KeyEvent.getKeyText(vk)*/);
		adjustFlags(vk, released);
		if (vk != 0) {
			try {
				final char ch;
				ch = interpretExtendedScanCode(scancode, vk, released);
				return new KeyboardEvent(released ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED, time, flags, vk, ch);
			} catch (UnsupportedKeyException e) {
				final char ch;
				if ((flags & InputEvent.SHIFT_DOWN_MASK) != 0) {
					ch = ucharMap[scancode];
				} else if((flags & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
					ch = altGrCharMap[scancode];
				} else {
					ch = lcharMap[scancode];
				}
				return new KeyboardEvent(released ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED, time, flags, vk, ch);
			}
			catch (DeadKeyException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private void adjustFlags(int vk, boolean released) {
		final int mask;
		switch (vk) {
			case KeyEvent.VK_ALT :
			    //VmSystem.getOut().println("VK_ALT");
				mask = InputEvent.ALT_DOWN_MASK;
				break;
			case KeyEvent.VK_ALT_GRAPH :
			    //VmSystem.getOut().println("VK_ALT_GRAPH");
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
				this.flags &= ~mask;
			} else {
				this.flags |= mask;
			}
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
	protected abstract void initVkMap(int []vkMap, char[] lcharMap, char[] ucharMap, char[] altGrCharMap);
	
	
	/**
	 * Method interpretExtendedScanCode this method sould be used to handle the dead keys and other special keys
	 *
	 * @param    scancode            an int
	 * @param    vk                  an int
	 * @param    released            a  boolean
	 *
	 * @return   the char to use or throws an Exception
	 * @exception   UnsupportedKeyException is thrown if the current key is not handled by this method
	 * @exception   DeadKeyException is thrown if the current key is a dead key
	 *
	 * @author 	Marc DENTY
	 * @version  2/8/2004
	 * @since 0.15
	 */
	protected abstract char interpretExtendedScanCode(int scancode, int vk, boolean released) throws UnsupportedKeyException, DeadKeyException;
	
    /**
     * @return Returns the flags.
     */
    protected final int getFlags() {
        return this.flags;
    }
}


