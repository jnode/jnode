/* interface MutableTreeNode
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

/**
 * Defines the requirements for a tree node object that can change -- 
 * by adding or removing child nodes, or by changing the contents of a 
 * user object stored in the node. 
 */
public interface MutableTreeNode
    extends TreeNode
{
    /** Adds child to this MutableTreeNode at index. 
     * The setParent() method of child will be called.
     */
    public void insert(MutableTreeNode child, int index);

    /** Removes the child at index from this MutableTreeNode.
     */
    public void remove(int index);

    /** Removes node from this MutableTreeNode.
     * The setParent() method of "node" will be called.
     */
    public void remove(MutableTreeNode node);

    /** Returns the user object associated with this MutableTreeNode.
     * (this method is not actually in java.swing.tree.MutableTreeNode).
     */
    public Object getUserObject();

    /** Resets the user object of this MutableTreeNode to object. 
     */
    public void setUserObject(Object object);

    /** Removes this MutableTreeNode from its parent. 
     */
    public void removeFromParent();

    /** Sets the parent of this MutableTreeNode to newParent. 
     */
    public void setParent(MutableTreeNode newParent);

}
