/* interface TreeSelectionModel
 *
 * Copyright (C) 2003  R M Pitman
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

package charvax.swing.tree;

import charvax.swing.event.TreeSelectionListener;

/**
 * This interface represents the current state of the selection for 
 * the tree component. 
 *
 * The Charva version of the TreeSelectionModel supports
 * SINGLE_TREE_SELECTION only. As a result, the state of the tree selection 
 * is characterized by a single TreePath, 
 */
public interface TreeSelectionModel
{
    /** Returns the first path in the selection.
     */
    public TreePath getSelectionPath();

    /** Sets the selection to "path".
     */
    public void setSelectionPath(TreePath path);

    /** Empties the current selection.
     */
    public void clearSelection();

    /** Returns true if "path" is in the current selection.
     */
    public boolean isPathSelected(TreePath path);

    /** Returns true if the selection is currently empty.
     */
    public boolean isSelectionEmpty();

    /** Adds "listener" to the list of listeners that are notified each 
     * time the set of selected TreePaths changes.
     */
    public void addTreeSelectionListener(TreeSelectionListener listener);

    /** Removes "listener" from the list of listeners that are notified 
     * each time the set of selected TreePaths changes.
     */
    public void removeTreeSelectionListener(TreeSelectionListener listener);
}
