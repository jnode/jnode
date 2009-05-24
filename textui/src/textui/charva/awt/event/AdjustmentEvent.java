/* class AdjustmentEvent
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
 * The adjustment event emitted by Adjustable objects (eg a scrollbar).
 */
public class AdjustmentEvent extends AWTEvent
{
    private static final long serialVersionUID = 1L;
    /**
     * Create a new AdjustmentEvent.
     * @param source_ The component to which the event must be delivered.
     * @param value_ The value of the adjustable component.
     */
    public AdjustmentEvent(Component source_, int value_) {
	super(source_, AWTEvent.ADJUSTMENT_EVENT);
	_value = value_;
    }

    public Component getAdjustable() {
	return (Component) super.getSource();
    }

    public int getValue() { return _value; }

    private int _value;
}
