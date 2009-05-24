/* class ItemEvent
 *
 * Copyright (C) 2001  R M Pitman
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
 * An event which indicates that an item was selected or deselected.
 */
public class ItemEvent extends AWTEvent
{
    private static final long serialVersionUID = 1L;
    /**
     * Constructs an ItemEvent object.
     * @param source_ The object (such as a List) that originated the event.
     * @param item_ The object affected by the event.
     * @param statechange_ An integer that indicates whether the item was
     * selected or deselected.
     */
    public ItemEvent(Component source_, Object item_, int statechange_) {
	super(source_, AWTEvent.ITEM_STATE_CHANGED);
	_item = item_;
	_statechange = statechange_;
    }

    /** 
     * Provides a way to flag the event as having been consumed,
     * so that it never reaches its destination component.
     */
    public void consume() { _consumed = true; }

    public boolean isConsumed() { return _consumed; }

    /**
     * Get the item affected by the event.
     */
    public Object getItem() { return _item; }

    /* Get the state change (SELECTED or DESELECTED).
     */
    public int getStateChange() { return _statechange; }

    private int _statechange;

    private boolean _consumed = false;

    /** The item affected by the event. */
    private Object _item;

    public static final int SELECTED = 100;
    public static final int DESELECTED = 101;

}
