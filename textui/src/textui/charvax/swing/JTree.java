/* class JTree
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

package charvax.swing;

import charvax.swing.tree.TreeModel;
import charvax.swing.tree.TreeNode;
import charvax.swing.tree.TreePath;
import charvax.swing.tree.TreeSelectionModel;

/**
 * (UNDER CONSTRUCTION) A component that displays hierarchical data.
 */
public class JTree
{
    /** Returns an instance of JTree which displays the root node -- 
     * the tree is created using the specified data model.
     */
    JTree(TreeModel newModel) {
    }

    /** Returns a JTree with the specified TreeNode as its root, which 
     * displays the root node. By default, the tree defines a leaf node 
     * as any node without children. 
     */
    public JTree(TreeNode root) {
    }

    /** Returns a JTree with the specified TreeNode as its root, which 
     * displays the root node and which decides whether a node is a leaf 
     * node in the specified manner. 
     * @param root a TreeNode object
     * @param asksAllowsChildren if false, any node without children is a
     * leaf node; if true, only nodes that do not allow children are leaf nodes
     */
    public JTree(TreeNode root, boolean asksAllowsChildren) {
    }

    /** Returns the TreeModel that is providing the data. 
     */
    public TreeModel getModel() {
	return _treeModel;
    }

    /** Sets the TreeModel that will provide the data.
     */
    public void setModel(TreeModel newModel_) {
	_treeModel = newModel_;
    }

    /** Returns true if the root node of the tree is displayed.
     * @return true if the root node of the tree is displayed
     */
    public boolean isRootVisible() {
	return false;	// temporary kludge...
    }

    /** Determines whether or not the root node from the TreeModel is visible.
     * @param rootVisible true if the root node of the tree is to be
     * displayed
     */
    public void setRootVisible(boolean rootVisible) {
    }

    /** Returns the number of rows that are currently being displayed.
     */
    public int getRowCount() {
	return 0;	// temporary kludge...
    }

    /** Selects the node identified by the specified path. If any component 
     * of the path is hidden (under a collapsed node), and 
     * getExpandsSelectedPaths is true it is exposed (made viewable). 
     */
    public void setSelectionPath(TreePath path) {
    }

    /** Selects the node at the specified row in the display.
     */
    public void setSelectionRow(int row) {
    }

    /** Returns the path to the first selected node. 
     * @return the TreePath for the first selected node, or null if nothing is
     * currently selected
     */
    public TreePath getSelectionPath() {
	return null;	// temporary kludge...
    }

    /** Returns true if the item identified by the path is currently selected.
     */
    public boolean isPathSelected(TreePath path) {
	return false;	// temporary kludge...
    }

    /** Ensures that the node identified by path is currently viewable. 
     */
    public void makeVisible(TreePath path) {
    }

    /** Returns true if the value identified by path is currently viewable, 
     * which means it is either the root or all of its parents are expanded. 
     * Otherwise, this method returns false. 
     */
    public boolean isVisible(TreePath path) {
	return false;	// temporary kludge...
    }

    /** Makes sure all the path components in path are expanded (except 
     * for the last path component) and scrolls so that the node identified 
     * by the path is displayed. Only works when this JTree is contained 
     * in a JScrollPane. 
     */
    public void scrollPathToVisible(TreePath path) {
    }

    /** Scrolls the item identified by row until it is displayed. The 
     * minimum of amount of scrolling necessary to bring the row into view 
     * is performed. Only works when this JTree is contained in a JScrollPane.
     * @param row an integer specifying the row to scroll, where 0 is the
     * first row in the display
     */
    public void scrollRowToVisible(int row) {
    }

    /** Returns the path for the specified row. If row is not visible, 
     * null is returned. 
     * @return the TreePath to the specified node, null if row &lt; 0 or row
     * &gt; getRowCount().
     */
    public TreePath getPathForRow(int row) {
	return null;	// temporary kludge...
    }

    /** Ensures that the node identified by the specified path is expanded 
     * and viewable. If the last item in the path is a leaf, this will have 
     * no effect. 
     */
    public void expandPath(TreePath path) {
    }

    /** Ensures that the node identified by the specified path is collapsed
     */
    public void collapsePath(TreePath path) {
    }

    /** Sets the tree's selection model. When a null value is specified 
     * an empty selectionModel is used, which does not allow selections. 
     */
    public void setSelectionModel(TreeSelectionModel selectionModel) {
    }

    /** Returns the model for selections. This should always return a 
     * non-null value.  If you don't want to allow anything to be selected 
     * set the selection model to null, which forces an empty selection 
     * model to be used.
     */
    public TreeSelectionModel getSelectionModel() {
	return _selectionModel;
    }

    /** Removes the node identified by the specified path from the 
     * current selection. 
     */
    public void removeSelectionPath(TreePath path) {
    }

    /** Removes the row at the index row from the current selection.
     */
    public void removeSelectionRow(int row) {
    }

    /** Clears the selection. 
     */
    public void clearSelection() {
    }

    /** Returns true if the selection is currently empty. 
     */
    public boolean isSelectionEmpty() {
	return false;	// temporary kludge...
    }

    /** Sets the number of rows that are to be displayed. This will only 
     * work if the tree is contained in a JScrollPane, and will adjust the 
     * preferred size and size of that scrollpane. 
     */
    public void setVisibleRowCount(int newCount) {
	_visibleRowCount = newCount;
    }

    /** Returns the maximum number of rows that are displayed in the 
     * display area. 
     */
    public int getVisibleRowCount() {
	return _visibleRowCount;
    }

    /** Returns a TreeModel wrapping the specified object.
     */
    protected static TreeModel createTreeModel(Object value) {
	return null;	// temporary kludge....
    }

    //==================================================================
    // INSTANCE VARIABLES
    protected TreeModel _treeModel;

    protected TreeSelectionModel _selectionModel;

    protected int _visibleRowCount;
}
