/* class DefaultMutableTreeNode
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
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * A DefaultMutableTreeNode is a general-purpose node in a tree data structure.
 * <p>
 * 
 * A tree node may have at most one parent and 0 or more children.
 * DefaultMutableTreeNode provides operations for examining and modifying a
 * node's parent and children and also operations for examining the tree that
 * the node is a part of. A node's tree is the set of all nodes that can be
 * reached by starting at the node and following all the possible links to
 * parents and children. A node with no parent is the root of its tree; a node
 * with no children is a leaf. A tree may consist of many subtrees, each node
 * acting as the root for its own subtree.
 * <p>
 * 
 * This class provides enumerations for efficiently traversing a tree or
 * subtree in various orders or for following the path between two nodes. A
 * DefaultMutableTreeNode may also hold a reference to a user object, the use
 * of which is left to the user. Asking a DefaultMutableTreeNode for its string
 * representation with toString() returns the string representation of its user
 * object.
 */
public class DefaultMutableTreeNode implements MutableTreeNode {

    /**
     * Creates a tree node that has no parent and no children, but which allows
     * children.
     */
    DefaultMutableTreeNode() {
        this(null, true);
    }

    /**
     * Creates a tree node with no parent, no children, but which allows
     * children, and initializes it with the specified user object.
     */
    DefaultMutableTreeNode(Object userObject) {
        this(userObject, true);
    }

    /**
     * Creates a tree node with no parent, no children, initialized with the
     * specified user object, and that allows children only if specified.
     */
    DefaultMutableTreeNode(Object userObject, boolean allowsChildren) {
        _userObject = userObject;
        _allowsChildren = allowsChildren;
    }

    /**
     * Adds child to this MutableTreeNode at index. The setParent() method of
     * child will be called.
     */
    public void insert(MutableTreeNode child, int index) {
        if (!_allowsChildren)
                throw new IllegalStateException(
                        "TreeNode does not allow children");

        if (_children == null) _children = new Vector<TreeNode>();

        _children.insertElementAt(child, index);
        child.setParent(this);
    }

    /**
     * Removes the child at index from this MutableTreeNode.
     */
    public void remove(int index) {
        MutableTreeNode node = (MutableTreeNode) _children.elementAt(index);
        if (node != null) this.remove(node);
    }

    /**
     * Removes node from this MutableTreeNode, giving it a null parent. The
     * setParent() method of "node" will be called.
     */
    public void remove(MutableTreeNode node) {
        _children.remove(node);
        node.setParent(null);
    }

    /**
     * Resets the user object of this MutableTreeNode to object.
     */
    public void setUserObject(Object object) {
        _userObject = object;
    }

    /**
     * Removes the subtree rooted at this node from the tree, giving this node
     * a null parent. Does nothing if this node is the root of its tree.
     */
    public void removeFromParent() {
        if (_parent == null) return;

        _parent.remove(this);
    }

    /**
     * Sets this node's parent to newParent but does not change the parent's
     * child array. This method is called from insert() and remove() to
     * reassign a child's parent, it should not be called from anywhere else.
     */
    public void setParent(MutableTreeNode newParent) {
        // This cast seems reasonable.
        _parent = (DefaultMutableTreeNode) newParent;
    }

    /**
     * Returns this node's parent or null if this node has no parent.
     */
    public TreeNode getParent() {
        return _parent;
    }

    /**
     * Returns the child at the specified index in this node's child array.
     * 
     * @param index
     *            an index into this node's child array.
     * @return the TreeNode in this node's child array at the specified index
     */
    public TreeNode getChildAt(int index) {
        return (TreeNode) _children.elementAt(index);
    }

    /**
     * Returns the number of children of this node.
     */
    public int getChildCount() {
        if (_children == null) return 0;

        return _children.size();
    }

    /**
     * Returns the index of the specified child in this node's child array. If
     * the specified node is not a child of this node, returns -1. This method
     * performs a linear search and is O(n) where n is the number of children.
     * 
     * @param aChild
     *            the TreeNode to search for among this node's children.
     * @return an int giving the index of the node in this node's child array,
     *         or -1 if the specified node is a not a child of this node
     */
    public int getIndex(TreeNode aChild) {
        if (_children == null) return -1;

        return _children.indexOf(aChild);
    }

    /**
     * Creates and returns a forward-order enumeration of this node's children.
     * Modifying this node's child array invalidates any child enumerations
     * created before the modification.
     */
    public Enumeration<TreeNode> children() {
        if (_children == null) return null;

        return _children.elements();
    }

    /**
     * Determines whether or not this node is allowed to have children. If
     * allows is false, all of this node's children are removed.
     * 
     * @param allows
     *            true if this node is allowed to have children.
     */
    public void setAllowsChildren(boolean allows) {
        _allowsChildren = allows;
    }

    /**
     * Returns true if this node is allowed to have children.
     */
    public boolean getAllowsChildren() {
        return _allowsChildren;
    }

    /**
     * Returns this node's user object.
     */
    public Object getUserObject() {
        return _userObject;
    }

    /**
     * Removes all of this node's children, setting their parents to null. If
     * this node has no children, this method does nothing.
     */
    public void removeAllChildren() {
        if (_children == null) return;

        Enumeration<TreeNode> e = _children.elements();
        while (e.hasMoreElements()) {
            MutableTreeNode node = (MutableTreeNode) e.nextElement();
            node.setParent(null);
        }
        _children.removeAllElements();
    }

    /**
     * Removes newChild from its parent and makes it a child of this node by
     * adding it to the end of this node's child array.
     * 
     * @param newChild
     *            node to add as a child of this node.
     * @exception IllegalArgumentException
     *                if newChild is null.
     * @exception IllegalStateException
     *                if this node does not allow children.
     */
    public void add(MutableTreeNode newChild) {
        if (newChild == null)
                throw new IllegalArgumentException("child node is null");

        if (!_allowsChildren)
                throw new IllegalStateException(
                        "TreeNode does not allow children");

        newChild.setParent(this);
        if (_children == null) _children = new Vector<TreeNode>();

        _children.add(newChild);
    }

    /**
     * Returns true if anotherNode is an ancestor of this node -- if it is this
     * node, this node's parent, or an ancestor of this node's parent. (Note
     * that a node is considered an ancestor of itself.) If anotherNode is
     * null, this method returns false. This operation is at worst O(h) where h
     * is the distance from the root to this node.
     * 
     * @param anotherNode
     *            node to test as an ancestor of this node
     * @return true if this node is a descendant of anotherNode.
     */
    public boolean isNodeAncestor(TreeNode anotherNode) {
        if (anotherNode == null) return false;

        if (anotherNode == this) return true;

        if (_parent == null) return false;

        TreeNode parent;
        for (parent = _parent; parent != null; parent.getParent()) {

            if (parent == anotherNode) return true;
        }
        return false;
    }

    /**
     * Returns true if anotherNode is a descendant of this node -- if it is
     * this node, one of this node's children, or a descendant of one of this
     * node's children. Note that a node is considered a descendant of itself.
     * If anotherNode is null, returns false. This operation is at worst O(h)
     * where h is the distance from the root to anotherNode.
     * 
     * @param anotherNode
     *            node to test as a descendant of this node
     * @return true if this node is an ancestor of anotherNode.
     */
    public boolean isNodeDescendant(TreeNode anotherNode) {
        if (anotherNode == null) return false;

        if (anotherNode == this) return true;

        if (_children == null || _children.size() == 0) return false;

        TreeNode parent;
        for (parent = anotherNode.getParent(); parent != null; parent
                .getParent()) {
            if (parent == this) return true;
        }
        return false;
    }

    /**
     * Returns the depth of the tree rooted at this node -- the longest
     * distance from this node to a leaf. If this node has no children, returns 0.
     * This operation is much more expensive than getLevel() because it must
     * effectively traverse the entire tree rooted at this node.
     * 
     * @return the depth of the tree whose root is this node.
     */
    public int getDepth() {
        return this._depth(this, 0);
    }

    /**
     * Returns the number of levels above this node -- the distance from the
     * root to this node. If this node is the root, returns 0.
     * 
     * @return the number of levels above this node.
     */
    public int getLevel() {

        TreeNode parent = _parent;
        int i;
        for (i = 0; parent != null; i++) {
            // do nothink
        }
        return i;
    }

    /**
     * Returns the path from the root, to get to this node. The last element in
     * the path is this node.
     * 
     * @return an array of TreeNode objects giving the path, where the first
     *         element in the path is the root and the last element is this
     *         node.
     */
    public TreeNode[] getPath() {
        TreeNode[] path = new TreeNode[ this.getLevel() + 1];
        TreeNode node = this;
        for (int i = path.length - 1; i >= 0; i--) {
            path[ i] = node;
            node = node.getParent();
        }
        return path;
    }

    /**
     * Returns the user object path, from the root, to get to this node. If
     * some of the TreeNodes in the path have null user objects, the returned
     * path will contain nulls.
     */
    public Object[] getUserObjectPath() {
        Object[] objectPath = new Object[ this.getLevel() + 1];
        MutableTreeNode node = this;
        for (int i = objectPath.length - 1; i >= 0; i--) {
            objectPath[ i] = node.getUserObject();
            node = (MutableTreeNode) node.getParent();
        }
        return objectPath;
    }

    /**
     * Returns the root of the tree that contains this node. The root is the
     * ancestor with a null parent.
     * 
     * @return the root of the tree that contains this node
     */
    public TreeNode getRoot() {
        TreeNode parent = this.getParent();
        while (parent != null) {
            if (parent.getParent() == null) return parent;
        }
        return null;
    }

    /**
     * Returns true if this node is the root of the tree. The root is the only
     * node in the tree with a null parent; every tree has exactly one root.
     * 
     * @return true if this node is the root of its tree
     */
    public boolean isRoot() {
        return (_parent == null);
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in preorder. The first node returned by the enumeration's
     * nextElement() method is this node.
     * <p>
     * 
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     * 
     * @return an enumeration for traversing the tree in preorder
     */
    public Enumeration<TreeNode> preorderEnumeration() {
        return null; // not implemented yet
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in postorder. The first node returned by the enumeration's
     * nextElement() method is the leftmost leaf. This is the same as a
     * depth-first traversal.
     * <p>
     * 
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     */
    public Enumeration<TreeNode> postorderEnumeration() {
        return null; // not implemented
    }

    /**
     * Returns true if aNode is a child of this node. If aNode is null, this
     * method returns false.
     * 
     * @return true if aNode is a child of this node; false if aNode is null
     */
    public boolean isNodeChild(TreeNode aNode) {
        if (aNode == null) return false;

        return (aNode.getParent() == this);
    }

    /**
     * Returns this node's first child. If this node has no children, throws
     * NoSuchElementException.
     * 
     * @return the first child of this node
     * @exception NoSuchElementException
     *                if this node has no children
     */
    public TreeNode getFirstChild() {
        if (_children == null)
                throw new NoSuchElementException("Node has no child");
        return (TreeNode) _children.firstElement();
    }

    /**
     * Returns this node's last child. If this node has no children, throws
     * NoSuchElementException.
     * 
     * @return the last child of this node
     * @exception NoSuchElementException
     *                if this node has no children
     */
    public TreeNode getLastChild() {
        if (_children == null)
                throw new NoSuchElementException("Node has no child");
        return (TreeNode) _children.lastElement();
    }

    /**
     * Returns the child in this node's child array that immediately follows
     * aChild, which must be a child of this node. If aChild is the last child,
     * returns null. This method performs a linear search of this node's
     * children for aChild and is O(n) where n is the number of children; to
     * traverse the entire array of children, use an enumeration instead.
     * 
     * @return the child of this node that immediately follows aChild
     * @exception IllegalArgumentException -
     *                if aChild is null or is not a child of this node
     */
    public TreeNode getChildAfter(TreeNode aChild) {
        if (!this.isNodeChild(aChild))
                throw new IllegalArgumentException("not a child of this node");

        int index = _children.indexOf(aChild);
        if (index + 1 >= _children.size()) return null;

        return (TreeNode) _children.elementAt(index + 1);
    }

    /**
     * Returns the child in this node's child array that immediately precedes
     * aChild, which must be a child of this node. If aChild is the first
     * child, returns null. This method performs a linear search of this node's
     * children for aChild and is O(n) where n is the number of children.
     * 
     * @return the child of this node that immediately precedes aChild.
     * @exception IllegalArgumentException -
     *                if aChild is null or is not a child of this node
     */
    public TreeNode getChildBefore(TreeNode aChild) {
        if (!this.isNodeChild(aChild))
                throw new IllegalArgumentException("not a child of this node");

        int index = _children.indexOf(aChild);
        if (index == 0) return null;

        return (TreeNode) _children.elementAt(index - 1);
    }

    /**
     * Returns true if this node has no children. To distinguish between nodes
     * that have no children and nodes that cannot have children (e.g. to
     * distinguish files from empty directories), use this method in
     * conjunction with getAllowsChildren
     * 
     * @return true if this node has no children.
     */
    public boolean isLeaf() {
        return (_children != null && _children.size() > 0);
    }

    /**
     * Finds and returns the first leaf that is a descendant of this node --
     * either this node or its first child's first leaf. Returns this node if
     * it is a leaf.
     * 
     * @return the first leaf in the subtree rooted at this node.
     */
    public DefaultMutableTreeNode getFirstLeaf() {

        DefaultMutableTreeNode node = this;
        while (!node.isLeaf()) {
            node = (DefaultMutableTreeNode) node.getChildAt(0);
        }
        return node;
    }

    /**
     * Finds and returns the last leaf that is a descendant of this node --
     * either this node or its last child's last leaf. Returns this node if it
     * is a leaf.
     * 
     * @return the last leaf in the subtree rooted at this node
     */
    public DefaultMutableTreeNode getLastLeaf() {

        DefaultMutableTreeNode node = this;
        while (!node.isLeaf()) {
            node = (DefaultMutableTreeNode) node.getChildAt(node
                    .getChildCount() - 1);
        }
        return node;
    }

    /**
     * Returns the leaf after this node or null if this node is the last leaf
     * in the tree.
     * <p>
     * 
     * In this implementation of the MutableNode interface, this operation is
     * very inefficient. In order to determine the next node, this method first
     * performs a linear search in the parent's child-list in order to find the
     * current node.
     * <p>
     * 
     * That implementation makes the operation suitable for short traversals
     * from a known position. But to traverse all of the leaves in the tree,
     * you should use depthFirstEnumeration to enumerate the nodes in the tree
     * and use isLeaf on each node to determine which are leaves.
     * 
     * @return returns the next leaf past this node
     */
    public DefaultMutableTreeNode getNextLeaf() {
        if (_parent == null) return null;

        // This cast seems reasonable.
        DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) _parent
                .getChildAfter(this);
        if (nextNode == null) return null;
        return nextNode.getFirstLeaf();
    }

    /**
     * Returns the leaf before this node or null if this node is the first leaf
     * in the tree.
     * 
     * In this implementation of the MutableNode interface, this operation is
     * very inefficient. In order to determine the previous node, this method
     * first performs a linear search in the parent's child-list in order to
     * find the current node.
     * 
     * That implementation makes the operation suitable for short traversals
     * from a known position. But to traverse all of the leaves in the tree,
     * you should use depthFirstEnumeration to enumerate the nodes in the tree
     * and use isLeaf on each node to determine which are leaves.
     */
    public DefaultMutableTreeNode getPreviousLeaf() {
        if (_parent == null) return null;

        // This cast seems reasonable.
        DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) _parent
                .getChildBefore(this);
        if (nextNode == null) return null;
        return nextNode.getLastLeaf();
    }

    private int _depth(TreeNode node, int start) {
        if (node.isLeaf()) return start;

        int depth = start;
        for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
            TreeNode child = (TreeNode) e.nextElement();
            int child_depth = this._depth(child, start);
            depth = (child_depth > depth) ? child_depth : depth;
        }
        return depth + 1;
    }

    //====================================================================
    // INSTANCE VARIABLES

    /** Array of children, may be null if this node has no children. */
    protected Vector<TreeNode> _children;

    /**
     * This node's parent, or null if this node has no parent. Note that in
     * javax.swing.tree.DefaultMutableTreeNode, this member is a
     * MutableTreeNode - which makes life difficult.
     */
    protected DefaultMutableTreeNode _parent;

    /** True if this node can have children. */
    protected boolean _allowsChildren = true;

    /** Optional user object. */
    protected Object _userObject;
}
