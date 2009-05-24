/* class DefaultTreeModel
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

import java.util.ArrayList;

import charvax.swing.event.TreeModelListener;

/** (UNDER CONSTRUCTION) A simple tree data model that uses TreeNodes.
 */
public class DefaultTreeModel
    implements TreeModel
{
    /** Creates a tree in which any node can have children.
     */
    DefaultTreeModel(TreeNode root)
    {
	this(root, false);
    }

    /** Creates a tree specifying whether any node can have children, or
     * whether only certain nodes can have children.
     */
    DefaultTreeModel(TreeNode root, boolean asksAllowsChildren) 
    {
	_root = root;
	_asksAllowsChildren = asksAllowsChildren;
    }

    /** Returns the root of the tree. 
     * Returns null only if the tree has no nodes. 
     */
    public Object getRoot()
    {
	return _root;
    }

    /** Returns the child of parent at index index in the parent's child 
     * array.  parent must be a node previously obtained from this data 
     * source. This should not return null if index is a valid index for 
     * parent (that is index &gt;= 0 &amp;&amp; index &lt; getChildCount(parent)). 
     */
    public Object getChild(Object parent, int index)
    {
	TreeNode parentNode = (TreeNode) parent;
	return parentNode.getChildAt(index);
    }

    /** Returns the number of children of parent. Returns 0 if the node 
     * is a leaf or if it has no children. parent must be a node previously 
     * obtained from this data source. 
     */
    public int getChildCount(Object parent)
    {
	TreeNode parentNode = (TreeNode) parent;
	return parentNode.getChildCount();
    }

    /** Returns true if node is a leaf. The way the test is performed 
     * depends on the asksAllowsChildren setting.
     */
    public boolean isLeaf(Object node_)
    {
	TreeNode node = (TreeNode) node_;
	if (_asksAllowsChildren) {
	    return ( ! node.getAllowsChildren());
	}
	else {
	    return (node.getChildCount() == 0);
	}
    }

    /** Called when the user has altered the value for the item 
     * identified by path to newValue. If newValue signifies a truly new 
     * value the model should post a treeNodesChanged event. 
     */
    public void valueForPathChanged(TreePath path, Object newValue)
    {
    }

    /** Adds a listener for the TreeModelEvent posted after the tree changes. 
     */
    public void addTreeModelListener(TreeModelListener l)
    {
    }

    /** Removes a listener previously added with addTreeModelListener. 
     */
    public void removeTreeModelListener(TreeModelListener l)
    {
    }

    //====================================================================
    // INSTANCE VARIABLES

    /** Determines how the isLeaf method figures out if a node is a leaf 
     * node. If true, a node is a leaf node if it does not allow children. 
     * (If it allows children, it is not a leaf node, even if no children 
     * are present.) That lets you distinguish between folder nodes and 
     * file nodes in a file system, for example.<p>
     *
     * If this value is false, then any node which has no children is a 
     * leaf node, and any node may acquire children. 
     */
    protected boolean _asksAllowsChildren ;

    protected ArrayList<TreeModelListener> _listeners;

    protected TreeNode _root;
}
