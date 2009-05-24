/* class PaintEvent
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
import charva.awt.Rectangle;

/**
 * An event which is used to ensure that painting of components is
 * serialized, i.e that it occurs in the main event-processing loop.
 */
public class PaintEvent extends AWTEvent
{
    private static final long serialVersionUID = 1L;
    Rectangle _updateRect;

    public PaintEvent(Component source_, Rectangle rect_) {
	super(source_, AWTEvent.PAINT_EVENT);
	_updateRect = rect_;
    }

    /**
     * Returns the rectangle representing the area that needs to be
     * repainted in response to this event.
     */
    public Rectangle getUpdateRect() { return new Rectangle(_updateRect); }
}
