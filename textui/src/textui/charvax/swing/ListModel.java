/* interface ListModel
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

package charvax.swing;

import charvax.swing.event.ListDataListener;

/**
 * This interface defines the methods by which components such as JList
 * get the value of each cell in a list, and the length of the list.<p>
 * Conceptually, the model is a Vector; indices vary from 0 to 
 * <code>getSize() - 1</code>.
 */
public interface ListModel
{
    /** Register an object that will be notified when the list contents
     * change.
     */
    public void addListDataListener(ListDataListener l);

    /** Returns the value at the specified index.
     */
    public Object getElementAt(int index);

    /** Returns the length of the list.
     */
    public int getSize();

    /** Remove the specified ListDataListener.
     */
    public void removeListDataListener(ListDataListener l);
}
