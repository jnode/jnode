/* interface TreeModel
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

import charvax.swing.event.TreeModelListener;

/**
 * This interface defines a suitable data model for a JTree.
 */
public interface TreeModel
{
    /** Returns the root of the tree. 
     * Returns null only if the tree has no nodes. 
     */
    public Object getRoot();

    /** Returns the child of parent at index index in the parent's child 
     * array.  parent must be a node previously obtained from this data 
     * source. This should not return null if index is a valid index for 
     * parent (that is index &gt;= 0 && index &lt; getChildCount(parent)). 
     */
    public Object getChild(Object parent, int index);

    /** Returns the number of children of parent. Returns 0 if the node 
     * is a leaf or if it has no children. parent must be a node previously 
     * obtained from this data source. 
     */
    public int getChildCount(Object parent);

    /** Returns true if node is a leaf. It is possible for this method to 
     * return false even if node has no children. A directory in a 
     * filesystem, for example, may contain no files; the node representing 
     * the directory is not a leaf, but it also has no children.
     */
    public boolean isLeaf(Object node);

    /** Called when the user has altered the value for the item 
     * identified by path to newValue. If newValue signifies a truly new 
     * value the model should post a treeNodesChanged event. 
     */
    public void valueForPathChanged(TreePath path, Object newValue);

    /** Adds a listener for the TreeModelEvent posted after the tree changes. 
     */
    public void addTreeModelListener(TreeModelListener l);

    /** Removes a listener previously added with addTreeModelListener. 
     */
    public void removeTreeModelListener(TreeModelListener l);

}
