/* interface ListDataListener
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

package charvax.swing.event;

import charva.awt.event.EventListener;

/**
 * Classes that need to be notified of changes in the contents of a list 
 * must implement this interface.
 */
public interface ListDataListener
    extends EventListener
{
    /** This method is called when the contents of the list have changed.
     */
    public void contentsChanged(ListDataEvent evt);

    // This method is not used in CHARVA.
    //public void intervalAdded(ListDataEvent evt);

    // This method is not used in CHARVA.
    //public void intervalRemoved(ListDataEvent evt);
}
