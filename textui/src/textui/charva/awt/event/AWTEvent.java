/* class AWTEvent
 *
 * Copyright (C) 2001, 2002  R M Pitman
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


/**
 * This is the base class for all the CHARVA user-interface events.
 * It encapsulates information about the event.
 */
public abstract class AWTEvent extends java.util.EventObject
{
    private static final long serialVersionUID = 1L;
    /**
     * Construct an event with the specified source and ID.
     * @param source_ The component that emitted this event.
     * @param id_ Identifies the event type.
     */
    public AWTEvent(Object source_, int id_)
    {
	super(source_);
	_id = id_;
    }

    public int getID() { return _id; }

    //====================================================================
    // INSTANCE VARIABLES

    private int _id;

    /** 
     * We may define other event types later.
     */
    public static final int KEY_PRESSED = 1;
    public static final int KEY_TYPED = 2;
    public static final int WINDOW_CLOSING = 3;
    public static final int WINDOW_OPENED = 4;
    public static final int ACTION_PERFORMED = 5;
    public static final int FOCUS_LOST = 6;
    public static final int FOCUS_GAINED = 7;
    public static final int ITEM_STATE_CHANGED = 8;
    public static final int PAINT_EVENT = 9;
    public static final int SYNC_EVENT = 10;
    public static final int ADJUSTMENT_EVENT = 11;
    public static final int SCROLL_EVENT = 12;
    public static final int LIST_SELECTION = 13;
    public static final int GARBAGE_COLLECTION = 14;
    public static final int INVOCATION_EVENT = 15;
    public static final int MOUSE_EVENT = 16;

    /**
     * Users can define their own events as long as the ID is greater
     * than RESERVED_ID_MAX.
     */
    public static final int RESERVED_ID_MAX = 100;  // leave room for expansion
}
