/* interface Adjustable
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

package charva.awt;

import charva.awt.event.AdjustmentEvent;
import charva.awt.event.AdjustmentListener;

/** 
 * The interface for objects that have an adjustable numeric value
 * constrained within a bounded range of values.
 */
public interface Adjustable
{
    public void addAdjustmentListener(AdjustmentListener listener_);

    public void removeAdjustmentListener(AdjustmentListener listener_);

    /** Gets the maximum value of the adjustable object.
     */
    public int getMaximum();

    /** Gets the minimum value of the adjustable object.
     */
    public int getMinimum();

    /** Gets the orientation of the object.
     */
    public int getOrientation();

    /** Gets the value of the adjustable object.
     */
    public int getValue();

    /** Gets the length of the proportional indicator.
     */
    public int getVisibleAmount();

    /** Gets the block increment (the amount by which the value will
     * change when the arrow keys are pressed).
     */
    public int getBlockIncrement();

    /** Sets the maximum value of the adjustable object.
     */
    public void setMaximum(int val_);

    /** Sets the minimum value of the adjustable object.
     */
    public void setMinimum(int val_);

    /** Sets the value of the adjustable object.
     */
    public void setValue(int val_);

    /** Sets the length of the proportional indicator.
     */
    public void setVisibleAmount(int val_);

    /** Sets the block increment
     */
    public void setBlockIncrement(int val_);

    public void processAdjustmentEvent(AdjustmentEvent evt_);

    public static final int VERTICAL = 200;
    public static final int HORIZONTAL = 201;

}
