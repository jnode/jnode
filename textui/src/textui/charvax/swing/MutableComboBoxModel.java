/* interface MutableComboBoxModel
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

package charvax.swing;

/**
 * A mutable version of ComboBoxModel. 
 */
public interface MutableComboBoxModel
    extends ComboBoxModel
{
    /** Add an item to the end of the model.
     */
    public void addElement(Object item);

    /** Insert an item at the specified index.
     */
    public void insertElementAt(Object obj, int index);

    /** Remove the specified object from the model.
     */
    public void removeElement(Object obj);

    /** Remove the item at the specified index.
     */
    public void removeElementAt(int index);
}
