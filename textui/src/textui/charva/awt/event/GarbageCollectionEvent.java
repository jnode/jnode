/* class GarbageCollectionEvent
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
 * An event which can be posted onto the event queue by an application
 * to trigger garbage-collection. If the event is posted onto the event 
 * queue as the last action in an actionPerformed() method (e.g. after
 * a menu option or button is invoked), the garbage collection will
 * take place <strong>after</strong> the actionPerformed() method has 
 * returned. This is a useful but optional way of limiting the growth 
 * of the heap without causing noticeable delays.
 */
public class GarbageCollectionEvent extends AWTEvent
{
    private static final long serialVersionUID = 1L;
    public GarbageCollectionEvent(Component source_) {
	super(source_, AWTEvent.GARBAGE_COLLECTION);
    }
}
