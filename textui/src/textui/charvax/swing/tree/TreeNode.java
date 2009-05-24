/* interface TreeNode
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

import java.util.Enumeration;

/**
 * This interface defines the requirements for an object that can be 
 * used as a node in a JTree.
 */
public interface TreeNode
{
    /** Returns the child TreeNode at index childIndex.
     */
    public TreeNode getChildAt(int childIndex);

    /** Returns the number of children TreeNodes the TreeNode contains. 
     */
    public int getChildCount();

    /** Returns the parent TreeNode of this TreeNode.
     */
    public TreeNode getParent();

    /** Returns true if this TreeNode allows children. 
     */
    public boolean getAllowsChildren();

    /** Returns true if this TreeNode is a leaf.
     */
    public boolean isLeaf();

    /** Returns the children of the receiver as an Enumeration.
     */
    public Enumeration<TreeNode> children();

}
