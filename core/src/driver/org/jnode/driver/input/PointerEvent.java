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
 
package org.jnode.driver.input;

/**
 * Event used by pointer devices.
 * 
 * @author qades
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PointerEvent {

	/** Left button bit-pattern */
	public static final int BUTTON_LEFT = 1;
	/** Right button bit-pattern */
	public static final int BUTTON_RIGHT = 2;
	/** Middle button bit-pattern */
	public static final int BUTTON_MIDDLE = 4;

	/** The x, y, z elements are absolute position values */
	public static final boolean ABSOLUTE = true;
	/** The x, y, z elements are relative displacement values */
	public static final boolean RELATIVE = false;

	/** The state of the buttons */
	private final int buttons;
	/** The X value */
	private final int x;
	/** The Y value */
	private final int y;
	/** The Z value (wheel) */
	private final int z;
	/** Is this an absolute position, or relative displacement */
	private final boolean absolute;
	/** Has this even been consumed. */
	private boolean consumed = false;

	/**
	 * Initialize this instance.
	 * 
	 * @param buttons
	 * @param x
	 * @param y
	 * @param z
	 * @param absolute
	 */
	public PointerEvent(int buttons, int x, int y, int z, boolean absolute) {
		this.buttons = buttons;
		this.x = x;
		this.y = y;
		this.z = z;
		this.absolute = absolute;
	}

	/**
	 * Initialize this instance.
	 * 
	 * @param buttons
	 * @param x
	 * @param y
	 * @param absolute
	 */
	public PointerEvent(int buttons, int x, int y, boolean absolute) {
		this(buttons, x, y, 0, absolute);
	}

	/**
	 * Mark this event as consumed.
	 */
	public void consume() {
		consumed = true;
	}

	/**
	 * Has this event been marked as consumed.
	 * 
	 * @return True if this event has been consumed, false otherwise.
	 */
	public boolean isConsumed() {
		return consumed;
	}

	/**
	 * Convert to a String representation
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String btn = ((buttons & BUTTON_LEFT) != 0 ? "L" : "l") + ((buttons & BUTTON_MIDDLE) != 0 ? "M" : "m") + ((buttons & BUTTON_RIGHT) != 0 ? "R" : "r");

		String pos = x + " " + y + " " + z + (absolute ? " ABS" : " REL");

		return "{" + btn + "}" + pos;
	}

	/**
	 * Are the x, y, z values absolute positions.
	 * 
	 * @return If true, the x, y, z values are absolute positions, otherwise they are relative
	 *         displacements.
	 */
	public final boolean isAbsolute() {
		return (this.absolute == ABSOLUTE);
	}

	/**
	 * Are the x, y, z values relative displacements.
	 * 
	 * @return If true, the x, y, z values are relative displacements, otherwise they are absolute
	 *         positions.
	 */
	public final boolean isRelative() {
		return (this.absolute == RELATIVE);
	}

	/**
	 * Gets the state of the buttons.
	 * 
	 * @see #BUTTON_LEFT
	 * @see #BUTTON_RIGHT
	 * @see #BUTTON_MIDDLE
	 * @return Returns the buttons.
	 */
	public final int getButtons() {
		return this.buttons;
	}

	/**
	 * Is the left button pressed.
	 */
	public final boolean isLeftButtonPressed() {
		return ((this.buttons & BUTTON_LEFT) != 0);
	}

	/**
	 * Is the right button pressed.
	 */
	public final boolean isRightButtonPressed() {
		return ((this.buttons & BUTTON_RIGHT) != 0);
	}

	/**
	 * Is the middle button pressed.
	 */
	public final boolean isMiddleButtonPressed() {
		return ((this.buttons & BUTTON_MIDDLE) != 0);
	}

	/**
	 * Gets the X value. If this is a relative event, this value is a relative displacement,
	 * otherwise it is an absolute position.
	 * 
	 * @return Returns the x.
	 */
	public final int getX() {
		return this.x;
	}

	/**
	 * Gets the Y value. If this is a relative event, this value is a relative displacement,
	 * otherwise it is an absolute position.
	 * 
	 * @return Returns the y.
	 */
	public final int getY() {
		return this.y;
	}

	/**
	 * Gets the Z value (wheel). If this is a relative event, this value is a relative
	 * displacement, otherwise it is an absolute position.
	 * 
	 * @return Returns the z.
	 */
	public final int getZ() {
		return this.z;
	}
}
