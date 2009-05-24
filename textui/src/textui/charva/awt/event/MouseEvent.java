/* class MouseEvent
 *
 * Copyright (C) 2001-2003  R M Pitman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package charva.awt.event;
import charva.awt.Component;

/**
 * An event which encapsulates information about a mouse-click.
 */
public class MouseEvent extends InputEvent
{
    private static final long serialVersionUID = 1L;

    public MouseEvent(Component source_, int modifiers_, 
	    int x_, int y_, int clickcount_, int button_) {

	super(source_, AWTEvent.MOUSE_EVENT);
	modifiers = modifiers_;
	x = x_;
	y = y_;
	clickcount = clickcount_;
	button = button_;
    }

    public int getModifiers() {
	return modifiers;
    }

    public boolean isLeftButtonPressed()
    {
    	return (button == BUTTON1) && (modifiers == MOUSE_PRESSED);
    }
    
    public boolean isMiddleButtonPressed()
    {
    	return (button == BUTTON2) && (modifiers == MOUSE_PRESSED);
    }

    public boolean isRightButtonPressed()
    {
    	return (button == BUTTON3) && (modifiers == MOUSE_PRESSED);
    }
	
    public int getX() {
	return x;
    }

    public int getY() {
	return y;
    }

    public int getClickCount() {
	return clickcount;
    }

    public int getButton() {
	return button;
    }

    public String toString() {
	return ("MouseEvent: x=" + x + " y=" + y + 
	    " modifiers=" + modifiers + " clickcount=" + clickcount +
	    " button=" + button + " source=[" + getSource() + "]");
    }

    // INSTANCE VARIABLES ================================================

    /** Specifies whether the button was pressed, released or clicked. */
    protected int modifiers;

    protected int x;
    protected int y;

    /** Specified which button was pressed, released or clicked. */
    protected int button;

    protected int clickcount;

    // STATIC CONSTANTS ==================================================

    // Buttons
    public static final int BUTTON1 = 1;
    public static final int BUTTON2 = 2;
    public static final int BUTTON3 = 3;

    // Modifiers
    public static final int MOUSE_PRESSED = 100;
    public static final int MOUSE_RELEASED = 101;
    public static final int MOUSE_CLICKED = 102;
}
