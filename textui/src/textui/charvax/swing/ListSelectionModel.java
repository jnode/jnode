/* interface ListSelectionModel
 *
 * Copyright (C) 2001-2003  R M Pitman
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

import charvax.swing.event.ListSelectionListener;

public interface ListSelectionModel
{
    /**
     * Add a listener to the list that is notified each time the selection
     * changes.
     */
    public void addListSelectionListener(ListSelectionListener l);

    /** Remove the specified listener from the list of listeners.
     */
    public void removeListSelectionListener(ListSelectionListener l);

    /** Change the selection to be the empty set. If this represents a change
     * to the selection then notify each ListSelectionListener.
     */
    public void clearSelection();

    /** Change the selection to be the set union between the current
     * selection and the indices between index0 and index1 inclusive. If
     * this represents a change to the current selection, then notify each
     * ListSelectionListener. Note that index0 does not have to be less 
     * than or equal to index1.
     */
    public void addSelectionInterval(int index0, int index1);

    /** Change the selection to be the set difference between the current
     * selection and the indices between index0 and index1 inclusive. If
     * this represents a change to the current selection, then notify each
     * ListSelectionListener. Note that index0 does not have to be less 
     * than or equal to index1.
     */
    public void removeSelectionInterval(int index0, int index1);

    /** Change the selection to be between index0 and index1 inclusive.
     * If this represents a change to the selection, then notify each
     * ListSelectionListener. Note that index0 doesn't have to be less than or
     * equal to index1.
     */
    public void setSelectionInterval(int index0, int index1);

    /** Set the selection mode. The following modes are allowed:
     * <ul>
     * <li> SINGLE_SELECTION. Only one list index can be selected at a time.
     * <li> SINGLE_INTERVAL_SELECTION. One contiguous index interval can be set
     * at at time.
     * </ul>
     */
    public void setSelectionMode(int mode_);

    /** Returns the current selection mode.
     */
    public int getSelectionMode();

    /** Returns true if the specified index is selected.
     */
    public boolean isSelectedIndex(int index);

    /** Returns true if no indices are selected.
     */
    public boolean isSelectionEmpty();

    /** Returns the first selected index, or -1 if the selection is empty.
     */
    public int getMinSelectionIndex();

    /** Returns the last selected index, or -1 if the selection is empty.
     */
    public int getMaxSelectionIndex();

    /** Insert length indices beginning before/after index, without notifying
     * the ListSelectionListeners. This 
     * is typically called to sync the selection model with a 
     * corresponding change in the data model.
     */
    public void insertIndexInterval(int index, int length, boolean before);

    /** Remove the indices in the interval index0,index1 (inclusive) 
     * from the selection model, without notifying
     * the ListSelectionListeners. This is typically called to sync the 
     * selection model width a corresponding change in the data model.
     */
    public void removeIndexInterval(int index0, int index1);

    public static final int SINGLE_SELECTION = 201;
    public static final int SINGLE_INTERVAL_SELECTION = 202;
    public static final int MULTIPLE_INTERVAL_SELECTION = 203;

}
