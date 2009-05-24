/* class ScrollEvent
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
import charva.awt.Point;
import charva.awt.Scrollable;

/**
 * A ScrollEvent is posted on the event queue by a scrollable object
 * such as a Table, when its state changes in such a way that it may
 * need to be scrolled (if it is contained within a ScrollPane).
 */
public class ScrollEvent extends AWTEvent
{
    private static final long serialVersionUID = 1L;
    /**
     * Construct a ScrollEvent with the specified direction and limit
     * parameters.
     * @param direction_ The direction that the ScrollPane must (potentially)
     * scroll the scrollable component (eg LEFT, RIGHT, UP or DOWN).
     * @param limit_ A Point representing the row and column that must 
     * appear at the edge of the Viewport if the component is scrolled.  
     * For examplem, if direction_ is LEFT, the limit_ is the rightmost 
     * column that must appear in the viewport.
     */
    public ScrollEvent(Component source_, int direction_, Point limit_) {
	super(source_, AWTEvent.SCROLL_EVENT);
	_direction = direction_;
	_limit = limit_;
    }

    public Scrollable getScrollable() { 
	return (Scrollable) super.getSource();
    }

    public int getDirection() { return _direction; }

    public Point getLimit() { return _limit; }

    //====================================================================
    // INSTANCE VARIABLES

    private int _direction;
    private Point _limit;

    public static final int LEFT = 100;
    public static final int RIGHT = 101;
    public static final int UP = 102;
    public static final int DOWN = 103;
    public static final int UP_LEFT = 104;
    public static final int UP_RIGHT = 105;
    public static final int DOWN_LEFT = 106;
    public static final int DOWN_RIGHT = 107;
}
