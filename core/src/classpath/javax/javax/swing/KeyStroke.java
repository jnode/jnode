/* KeyStroke.java --
   Copyright (C) 2002 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package javax.swing;

import java.awt.AWTKeyStroke;
import java.awt.event.KeyEvent;
import java.io.Serializable;

/**
 * KeyStroke
 * @author	Andrew Selkirk
 * @version	1.0
 */
public class KeyStroke implements Serializable
{
  static final long serialVersionUID = -9060180771037902530L;

	//-------------------------------------------------------------
	// Variables --------------------------------------------------
	//-------------------------------------------------------------

	/**
	 * keyChar
	 */
	private	char	keyChar			= 0;

	/**
	 * keyCode
	 */
	private	int		keyCode			= 0;

	/**
	 * modifiers
	 */
	private	int		modifiers		= 0;

	/**
	 * onKeyRelease
	 */
	private	boolean	onKeyRelease	= false;


	//-------------------------------------------------------------
	// Initialization ---------------------------------------------
	//-------------------------------------------------------------

	/**
	 * Constructor KeyStroke
	 */
	private KeyStroke() {
	} // KeyStroke()


	//-------------------------------------------------------------
	// Methods ----------------------------------------------------
	//-------------------------------------------------------------

	/**
	 * hashCode
	 * @returns int
	 */
	public int hashCode() {
		return 0; // TODO
	} // hashCode()

	/**
	 * equals
	 * @param object TODO
	 * @returns boolean
	 */
	public boolean equals(Object object) {

		// Variables
		KeyStroke	key;

		if (object instanceof KeyStroke) {
			key = (KeyStroke) object;
			if (key.keyChar == keyChar &&
				key.keyCode == keyCode &&
				key.modifiers == modifiers &&
				key.onKeyRelease == onKeyRelease) {
				return true;
			} // if
		} // if
		return false;

	} // equals()

	/**
	 * toString
	 * @returns String
	 */
	public String toString() {
		return null; // TODO
	} // toString()

	/**
	 * getKeyStroke
	 * @param keyChar TODO
	 * @returns KeyStroke
	 */
	public static KeyStroke getKeyStroke(char keyChar) {

		// Variables
		KeyStroke	key;

		key = new KeyStroke();
		key.keyChar = keyChar;
		return key;

	} // getKeyStroke()

	/**
	 * getKeyStroke - deprecated
	 * @param keyChar TODO
	 * @param onKeyRelease TODO
	 * @returns KeyStroke
	 */
	public static KeyStroke getKeyStroke(char keyChar, boolean onKeyRelease) {
		KeyStroke key = getKeyStroke(keyChar);
		key.onKeyRelease = onKeyRelease;
		return key;
	} // getKeyStroke()

	/**
	 * getKeyStroke
	 * @param keyChar TODO
	 * @param modifiers TODO
	 * @returns KeyStroke
	 */
	public static KeyStroke getKeyStroke(Character keyChar, int modifiers) {
		KeyStroke key = getKeyStroke(keyChar.charValue());
		key.modifiers = modifiers;
		return key;
	} // getKeyStroke()

	/**
	 * getKeyStroke
	 * @param keyCode TODO
	 * @param modifiers TODO
	 * @param onKeyRelease TODO
	 * @returns KeyStroke
	 */
	public static KeyStroke getKeyStroke(int keyCode, int modifiers, 
				boolean onKeyRelease) {

		// Variables
		KeyStroke	key;

		key = new KeyStroke();
		key.keyCode = keyCode;
		key.modifiers = modifiers;
		key.onKeyRelease = onKeyRelease;
		return key;

	} // getKeyStroke()

	/**
	 * getKeyStroke
	 * @param keyCode TODO
	 * @param modifiers TODO
	 * @returns KeyStroke
	 */
	public static KeyStroke getKeyStroke(int keyCode, int modifiers) {
		return getKeyStroke(keyCode, modifiers, false);
	} // getKeyStroke()

	/**
	 * getKeyStroke
	 * @param string TODO
	 * @returns KeyStroke
	 */
	public static KeyStroke getKeyStroke(String string) {
		return null; // TODO
	} // getKeyStroke()

	/**
	 * getKeyStrokeForEvent
	 * @param event TODO
	 * @returns KeyStroke
	 */
	public static KeyStroke getKeyStrokeForEvent(KeyEvent event) {

		// Variables
		int	eventID;
		int	eventMod;

		// Get Event ID
		eventID = event.getID();
		eventMod = event.getModifiers();

		// Check for KEY_TYPED event
		if (eventID == KeyEvent.KEY_TYPED) {
			return getKeyStroke(event.getKeyChar(), eventMod);

		// KEY_PRESSED or KEY_RELEASED event
		} else {
			return getKeyStroke(event.getKeyCode(), eventMod);
		} // if

	} // getKeyStrokeForEvent()

	/**
	 * getKeyChar
	 * @returns char
	 */
	public char getKeyChar() {
		return keyChar;
	} // getKeyChar()

	/**
	 * getKeyCode
	 * @returns int
	 */
	public int getKeyCode() {
		return keyCode;
	} // getKeyCode()

	/**
	 * getModifiers
	 * @returns int
	 */
	public int getModifiers() {
		return modifiers; // TODO
	} // getModifiers()

	/**
	 * isOnKeyRelease
	 * @returns boolean
	 */
	public boolean isOnKeyRelease() {
		return onKeyRelease;
	} // isOnKeyRelease()


} // KeyStroke
