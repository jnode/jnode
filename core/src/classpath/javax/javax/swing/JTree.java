/*
 * JTree.java -- Copyright (C) 2002, 2004, 2005 Free Software Foundation, Inc.
 * 
 * This file is part of GNU Classpath.
 * 
 * GNU Classpath is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2, or (at your option) any later version.
 * 
 * GNU Classpath is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * GNU Classpath; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package javax.swing;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.TreeUI;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class JTree
		extends	JComponent
  implements Scrollable, Accessible
{
  /**
   * Listens to the model of the JTree and updates the property
   * <code>expandedState</code> if nodes are removed or changed.
   */
  protected class TreeModelHandler
			implements
			TreeModelListener
  {

    /**
     * Creates a new instance of TreeModelHandler.
     */
    protected TreeModelHandler()
    {
    }

    /**
     * Notifies when a node has changed in some ways. This does not include
     * that a node has changed its location or changed it's children. It
     * only means that some attributes of the node have changed that might
     * affect its presentation.
     *
     * This method is called after the actual change occured.
     *
     * @param ev the TreeModelEvent describing the change
     */
    public void treeNodesChanged(TreeModelEvent ev)
    {
      // nothing to do here
    }

    /**
     * Notifies when a node is inserted into the tree.
     *
     * This method is called after the actual change occured.
     *
     * @param ev the TreeModelEvent describing the change
     */
    public void treeNodesInserted(TreeModelEvent ev)
    {
      // nothing to do here
    }

    /**
     * Notifies when a node is removed from the tree.
     *
     * This method is called after the actual change occured.
     *
     * @param ev the TreeModelEvent describing the change
     */
    public void treeNodesRemoved(TreeModelEvent ev)
    {
      // TODO: The API docs suggest that this method should do something
      // but I cannot really see what has to be done here ...
    }

    /**
     * Notifies when the structure of the tree is changed.
     *
     * This method is called after the actual change occured.
     *
     * @param ev the TreeModelEvent describing the change
     */
    public void treeStructureChanged(TreeModelEvent ev)
    {
      // set state of new path
      TreePath path = ev.getTreePath();
      setExpandedState(path, isExpanded(path));
    }
  } // TreeModelHandler

  /**
	 * This redirects TreeSelectionEvents and rewrites the source of it to be
	 * this JTree. This is typically done when the tree model generates an
	 * event, but the JTree object associated with that model should be listed
	 * as the actual source of the event.
   */
  protected class TreeSelectionRedirector
			implements
			TreeSelectionListener,
			Serializable
  {
    /** The serial version UID. */
    private static final long serialVersionUID = -3505069663646241664L;

    /**
     * Creates a new instance of TreeSelectionRedirector
     */
    protected TreeSelectionRedirector()
    {
    }

    /**
     * Notifies when the tree selection changes.
     *
     * @param ev the TreeSelectionEvent that describes the change
     */
    public void valueChanged(TreeSelectionEvent ev)
    {
			TreeSelectionEvent rewritten = (TreeSelectionEvent) ev
					.cloneWithSource(JTree.this);
      fireValueChanged(rewritten);
			JTree.this.repaint();
    }
  } // TreeSelectionRedirector

  /**
   * A TreeModel that does not allow anything to be selected.
   */
  protected static class EmptySelectionModel
			extends
				DefaultTreeSelectionModel
  {
    /** The serial version UID. */
    private static final long serialVersionUID = -5815023306225701477L;

    /**
     * The shared instance of this model.
     */
		protected static final EmptySelectionModel sharedInstance = new EmptySelectionModel();

    /**
     * Creates a new instance of EmptySelectionModel.
     */
    protected EmptySelectionModel()
    {
    }

    /**
     * Returns the shared instance of EmptySelectionModel.
     *
     * @return the shared instance of EmptySelectionModel
     */
    public static EmptySelectionModel sharedInstance()
    {
      return sharedInstance;
    }

    /**
		 * This catches attempts to set a selection and sets nothing instead.
     *
     * @param paths not used here
     */
    public void setSelectionPaths(TreePath[] paths)
    {
      // we don't allow selections in this class
    }

    /**
     * This catches attempts to add something to the selection.
     *
     * @param paths not used here
     */
    public void addSelectionPaths(TreePath[] paths)
    {
      // we don't allow selections in this class
    }

    /**
     * This catches attempts to remove something from the selection.
     *
     * @param paths not used here
     */
    public void removeSelectionPaths(TreePath[] paths)
    {
      // we don't allow selections in this class
    }
  }// EmptySelectionModel

  private static final long serialVersionUID = 7559816092864483649L;
  public static final String CELL_EDITOR_PROPERTY = "cellEditor";
  public static final String CELL_RENDERER_PROPERTY = "cellRenderer";
  public static final String EDITABLE_PROPERTY = "editable";
  public static final String INVOKES_STOP_CELL_EDITING_PROPERTY = "invokesStopCellEditing";
  public static final String LARGE_MODEL_PROPERTY = "largeModel";
  public static final String ROOT_VISIBLE_PROPERTY = "rootVisible";
  public static final String ROW_HEIGHT_PROPERTY = "rowHeight";
  public static final String SCROLLS_ON_EXPAND_PROPERTY = "scrollsOnExpand";
  public static final String SELECTION_MODEL_PROPERTY = "selectionModel";
  public static final String SHOWS_ROOT_HANDLES_PROPERTY = "showsRootHandles";
  public static final String TOGGLE_CLICK_COUNT_PROPERTY = "toggleClickCount";
  public static final String TREE_MODEL_PROPERTY = "model";
  public static final String VISIBLE_ROW_COUNT_PROPERTY = "visibleRowCount";

  /** @since 1.3 */
  public static final String ANCHOR_SELECTION_PATH_PROPERTY = "anchorSelectionPath";

  /** @since 1.3 */
  public static final String LEAD_SELECTION_PATH_PROPERTY = "leadSelectionPath";

  /** @since 1.3 */
  public static final String EXPANDS_SELECTED_PATHS_PROPERTY = "expandsSelectedPaths";
  private static final Object EXPANDED = new Object();
  private static final Object COLLAPSED = new Object();
  private boolean dragEnabled;
  private boolean expandsSelectedPaths;
  private TreePath anchorSelectionPath;
  private TreePath leadSelectionPath;

  /*
   * This contains the state of all nodes in the tree. Al/ entries map the
   * TreePath of a note to to its state. Valid states are EXPANDED and
   * COLLAPSED.  Nodes not in this Hashtable are assumed state COLLAPSED.
   */
	private Hashtable nodeStates = new Hashtable();
  protected transient TreeCellEditor cellEditor;
  protected transient TreeCellRenderer cellRenderer;
  protected boolean editable;
  protected boolean invokesStopCellEditing;
  protected boolean largeModel;
  protected boolean rootVisible;
  protected int rowHeight;
  protected boolean scrollsOnExpand;
  protected transient TreeSelectionModel selectionModel;
  protected boolean showsRootHandles;
  protected int toggleClickCount;
  protected transient TreeModel treeModel;
  protected int visibleRowCount;

  /**
   * Handles TreeModelEvents to update the expandedState.
   */
  protected transient TreeModelListener treeModelListener;

  /**
   * Redirects TreeSelectionEvents so that the source is this JTree.
   */
  protected TreeSelectionRedirector selectionRedirector =
    new TreeSelectionRedirector();

  /**
   * Creates a new <code>JTree</code> object.
   */
  public JTree()
  {
    this(createTreeModel(null));
  }

  /**
   * Creates a new <code>JTree</code> object.
   *
   * @param value the initial nodes in the tree
   */
  public JTree(Hashtable value)
  {
    this(createTreeModel(value));
  }

  /**
   * Creates a new <code>JTree</code> object.
   *
   * @param value the initial nodes in the tree
   */
  public JTree(Object[] value)
  {
    this(createTreeModel(value));
  }

  /**
   * Creates a new <code>JTree</code> object.
   *
   * @param model the model to use
   */
  public JTree(TreeModel model)
  {
    setModel(model);
    setSelectionModel(EmptySelectionModel.sharedInstance());
    selectionModel.addTreeSelectionListener(selectionRedirector);
    setCellRenderer(new DefaultTreeCellRenderer());
    updateUI();
  }

  /**
   * Creates a new <code>JTree</code> object.
   *
   * @param root the root node
   */
  public JTree(TreeNode root)
  {
    this(root, false);
  }

  /**
   * Creates a new <code>JTree</code> object.
   *
   * @param root the root node
	 * @param asksAllowChildren if false, all nodes without children are leaf
	 *        nodes. If true, only nodes that do not allow children are leaf
	 *        nodes.
   */
  public JTree(TreeNode root, boolean asksAllowChildren)
  {
    this(new DefaultTreeModel(root, asksAllowChildren));
  }

  /**
   * Creates a new <code>JTree</code> object.
   *
   * @param value the initial nodes in the tree
   */
  public JTree(Vector value)
  {
    this(createTreeModel(value));
  }

  public static class DynamicUtilTreeNode 
			extends
				DefaultMutableTreeNode
  {
    protected Object childValue;
    protected boolean loadedChildren;

    /**
		 * Currently not set or used by this class. It might be set and used in
		 * later versions of this class.
     */
    protected boolean hasChildren;
    
		public DynamicUtilTreeNode(Object value, Object children)
    {
      super(value);
      childValue = children;
      loadedChildren = false;
    }

    public int getChildCount()
    {
      loadChildren();
      return super.getChildCount();
    }

    protected void loadChildren()
    {
      if (!loadedChildren)
        {
          createChildren(this, childValue);
          loadedChildren = true;
        }
    }
    
    public Enumeration children()
    {
      loadChildren();
      return super.children();
    }

    /**
		 * Returns the child node at position <code>pos</code>. Subclassed
		 * here to load the children if necessary.
     *
     * @param pos the position of the child node to fetch
     *
     * @return the childnode at the specified position
     */
    public TreeNode getChildAt(int pos)
    {
      loadChildren();
      return super.getChildAt(pos);
    }

    public boolean isLeaf() 
    {
			return (childValue == null || !(childValue instanceof Hashtable
					|| childValue instanceof Vector || childValue.getClass()
					.isArray()));
    }

    public static void createChildren(DefaultMutableTreeNode parent,
                                      Object children)
    {
      if (children instanceof Hashtable)
        {
          Hashtable tab = (Hashtable) children;
          Enumeration e = tab.keys();
          while (e.hasMoreElements()) 
            {
              Object key = e.nextElement();
              Object val = tab.get(key);
              parent.add(new DynamicUtilTreeNode(key, val));
            }
			} else if (children instanceof Vector)
        {
				Iterator i = ((Vector) children).iterator();
          while (i.hasNext())
            {
              Object n = i.next();
					parent.add(new DynamicUtilTreeNode(n, n));
            }
			} else if (children != null && children.getClass().isArray())
        {
          Object[] arr = (Object[]) children;
          for (int i = 0; i < arr.length; ++i)
            parent.add(new DynamicUtilTreeNode(arr[i], arr[i]));
      }
    }
  }

  public int getRowForPath(TreePath path)
  {
    TreeUI ui = getUI();

    if (ui != null)
      return ui.getRowForPath(this, path);

    return -1;
  }
  
  public TreePath getPathForRow(int row)
  {
    TreeUI ui = getUI();
    return ui != null ? ui.getPathForRow(this, row) : null;
  }

  protected TreePath[] getPathBetweenRows(int index0, int index1)
  {
    TreeUI ui = getUI();
    
    if (ui == null)
      return null;
    
    int minIndex = Math.min(index0, index1);
    int maxIndex = Math.max(index0, index1);
    TreePath[] paths = new TreePath[maxIndex - minIndex + 1];
    
    for (int i = minIndex; i <= maxIndex; ++i)
      paths[i - minIndex] = ui.getPathForRow(this, i);

    return paths;
  }

  /**
   * Creates a new <code>TreeModel</code> object.
   *
   * @param value the values stored in the model
   */
  protected static TreeModel createTreeModel(Object value)
    {
    return new DefaultTreeModel(new DynamicUtilTreeNode(value, value));
    }
    
  /**
   * Return the UI associated with this <code>JTree</code> object.
   *
   * @return the associated <code>TreeUI</code> object
   */
    public TreeUI getUI()
    {
        return (TreeUI) ui;
    }

  /**
   * Sets the UI associated with this <code>JTree</code> object.
   *
   * @param ui the <code>TreeUI</code> to associate
   */
    public void setUI(TreeUI ui)
    {
        super.setUI(ui);
    }

  /**
   * This method resets the UI used to the Look and Feel defaults..
   */
    public void updateUI()
    {
    setUI((TreeUI) UIManager.getUI(this));
    revalidate();
    repaint();
    }

  /**
   * This method returns the String ID of the UI class of  Separator.
   *
   * @return The UI class' String ID.
   */
    public String getUIClassID()
    {
	return "TreeUI";
    }

  /**
	 * Gets the AccessibleContext associated with this
	 * <code>JToggleButton</code>.
   *
   * @return the associated context
   */
    public AccessibleContext getAccessibleContext()
    {
      return null;
    }

  /**
   * Returns the preferred viewport size..
   *
   * @return the preferred size
   */
    public Dimension getPreferredScrollableViewportSize()
    {
	return null;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                        int orientation, int direction)
    {
	return 1;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                         int orientation, int direction)
    {
	return 1;
    }

    public boolean getScrollableTracksViewportWidth()
    {
	return false;
    }

    public boolean getScrollableTracksViewportHeight()
    {
	return false;
    }

  /**
   * Adds a <code>TreeExpansionListener</code> object to the tree.
   *
   * @param listener the listener to add
   */
  public void addTreeExpansionListener(TreeExpansionListener listener)
  {
    listenerList.add(TreeExpansionListener.class, listener);
  }

  /**
   * Removes a <code>TreeExpansionListener</code> object from the tree.
   *
   * @param listener the listener to remove
   */
  public void removeTreeExpansionListener(TreeExpansionListener listener)
  {
    listenerList.remove(TreeExpansionListener.class, listener);
  }

  /**
   * Returns all added <code>TreeExpansionListener</code> objects.
   *
   * @return an array of listeners
   */
  public TreeExpansionListener[] getTreeExpansionListeners()
  {
    return (TreeExpansionListener[]) getListeners(TreeExpansionListener.class);
  }

  /**
   * Notifies all listeners that the tree was collapsed.
   *
   * @param path the path to the node that was collapsed
   */
  public void fireTreeCollapsed(TreePath path)
  {
    TreeExpansionEvent event = new TreeExpansionEvent(this, path);
    TreeExpansionListener[] listeners = getTreeExpansionListeners();

    for (int index = 0; index < listeners.length; ++index)
      listeners[index].treeCollapsed(event);
  }
  
  /**
   * Notifies all listeners that the tree was expanded.
   *
   * @param path the path to the node that was expanded
   */
  public void fireTreeExpanded(TreePath path)
  {
    TreeExpansionEvent event = new TreeExpansionEvent(this, path);
    TreeExpansionListener[] listeners = getTreeExpansionListeners();

    for (int index = 0; index < listeners.length; ++index)
      listeners[index].treeExpanded(event);
  }

  /**
   * Adds a <code>TreeSelctionListener</code> object to the tree.
   *
   * @param listener the listener to add
   */
  public void addTreeSelectionListener(TreeSelectionListener listener)
  {
    listenerList.add(TreeSelectionListener.class, listener);
  }

  /**
   * Removes a <code>TreeSelectionListener</code> object from the tree.
   *
   * @param listener the listener to remove
   */
  public void removeTreeSelectionListener(TreeSelectionListener listener)
  {
    listenerList.remove(TreeSelectionListener.class, listener);
  }

  /**
   * Returns all added <code>TreeSelectionListener</code> objects.
   *
   * @return an array of listeners
   */
  public TreeSelectionListener[] getTreeSelectionListeners()
  {
		return (TreeSelectionListener[]) 
					getListeners(TreeSelectionListener.class);
  }

  /**
   * Notifies all listeners when the selection of the tree changed.
   *
   * @param event the event to send
   */
  protected void fireValueChanged(TreeSelectionEvent event)
  {
    TreeSelectionListener[] listeners = getTreeSelectionListeners();

    for (int index = 0; index < listeners.length; ++index)
      listeners[index].valueChanged(event);
  }

  /**
   * Adds a <code>TreeWillExpandListener</code> object to the tree.
   *
   * @param listener the listener to add
   */
  public void addTreeWillExpandListener(TreeWillExpandListener listener)
  {
    listenerList.add(TreeWillExpandListener.class, listener);
  }

  /**
   * Removes a <code>TreeWillExpandListener</code> object from the tree.
   *
   * @param listener the listener to remove
   */
  public void removeTreeWillExpandListener(TreeWillExpandListener listener)
  {
    listenerList.remove(TreeWillExpandListener.class, listener);
  }

  /**
   * Returns all added <code>TreeWillExpandListener</code> objects.
   *
   * @return an array of listeners
   */
  public TreeWillExpandListener[] getTreeWillExpandListeners()
  {
		return (TreeWillExpandListener[]) 
					getListeners(TreeWillExpandListener.class);
  }

  /**
   * Notifies all listeners that the tree will collapse.
   *
   * @param path the path to the node that will collapse
   */
	public void fireTreeWillCollapse(TreePath path) throws ExpandVetoException
  {
    TreeExpansionEvent event = new TreeExpansionEvent(this, path);
    TreeWillExpandListener[] listeners = getTreeWillExpandListeners();

    for (int index = 0; index < listeners.length; ++index)
      listeners[index].treeWillCollapse(event);
  }

  /**
   * Notifies all listeners that the tree will expand.
   *
   * @param path the path to the node that will expand
   */
	public void fireTreeWillExpand(TreePath path) throws ExpandVetoException
  {
    TreeExpansionEvent event = new TreeExpansionEvent(this, path);
    TreeWillExpandListener[] listeners = getTreeWillExpandListeners();

    for (int index = 0; index < listeners.length; ++index)
      listeners[index].treeWillExpand(event);
  }

  /**
   * Returns the model of this <code>JTree</code> object.
   *
   * @return the associated <code>TreeModel</code>
   */
  public TreeModel getModel()
  {
    return treeModel;
  }

  /**
   * Sets the model to use in <code>JTree</code>.
   *
   * @param model the <code>TreeModel</code> to use
   */
  public void setModel(TreeModel model)
  {
    if (treeModel == model)
      return;

    TreeModel oldValue = treeModel;
    treeModel = model;

    firePropertyChange(TREE_MODEL_PROPERTY, oldValue, model);

    // add treeModelListener to the new model
    if (treeModelListener == null)
      treeModelListener = createTreeModelListener();
    model.addTreeModelListener(treeModelListener);
  }

  /**
   * Checks if this <code>JTree</code> object is editable.
   *
   * @return <code>true</code> if this tree object is editable,
   * <code>false</code> otherwise
   */
  public boolean isEditable()
  {
    return editable;
  }

  /**
   * Sets the <code>editable</code> property.
   *
   * @param flag <code>true</code> to make this tree object editable,
   * <code>false</code> otherwise
   */
  public void setEditable(boolean flag)
  {
    if (editable == flag)
      return;

    boolean oldValue = editable;
    editable = flag;
    firePropertyChange(EDITABLE_PROPERTY, oldValue, editable);
  }

  /**
   * Checks if the root element is visible.
   *
   * @return <code>true</code> if the root element is visible,
   * <code>false</code> otherwise
   */
  public boolean isRootVisible()
  {
    return rootVisible;
  }

  public void setRootVisible(boolean flag)
  {
    if (rootVisible == flag)
      return;
    
    boolean oldValue = rootVisible;
    rootVisible = flag;
    firePropertyChange(ROOT_VISIBLE_PROPERTY, oldValue, flag);
  }

  public boolean getShowsRootHandles()
  {
    return showsRootHandles;
  }

  public void setShowsRootHandles(boolean flag)
  {
    if (showsRootHandles == flag)
      return;

    boolean oldValue = showsRootHandles;
    showsRootHandles = flag;
    firePropertyChange(SHOWS_ROOT_HANDLES_PROPERTY, oldValue, flag);
  }

  public TreeCellEditor getCellEditor()
  {

    return cellEditor;
  }

  public void setCellEditor(TreeCellEditor editor)
  {
    if (cellEditor == editor)
      return;

    TreeCellEditor oldValue = cellEditor;
    cellEditor = editor;
    firePropertyChange(CELL_EDITOR_PROPERTY, oldValue, editor);
  }
  
  public TreeCellRenderer getCellRenderer()
  {
    return cellRenderer;
  }
  
  public void setCellRenderer(TreeCellRenderer newRenderer)
  {
    if (cellRenderer == newRenderer)
      return;

    TreeCellRenderer oldValue = cellRenderer;
    cellRenderer = newRenderer;
    firePropertyChange(CELL_RENDERER_PROPERTY, oldValue, newRenderer);
  }

  public TreeSelectionModel getSelectionModel()
  {
    return selectionModel;
  }

  public void setSelectionModel(TreeSelectionModel model)
  {
    if (selectionModel == model)
      return;
    
		if (selectionModel != null)
			selectionModel.removeTreeSelectionListener(selectionRedirector);

    TreeSelectionModel oldValue = selectionModel;
    selectionModel = model;

		if (selectionModel != null)
			selectionModel.addTreeSelectionListener(selectionRedirector);

    firePropertyChange(SELECTION_MODEL_PROPERTY, oldValue, model);
		revalidate();
		repaint();
  }

  public int getVisibleRowCount()
  {
    return visibleRowCount;
  }

  public void setVisibleRowCount(int rows)
  {
    if (visibleRowCount == rows)
      return;

    int oldValue = visibleRowCount;
    visibleRowCount = rows;
    firePropertyChange(VISIBLE_ROW_COUNT_PROPERTY, oldValue, rows);
  }

  public boolean isLargeModel()
  {
    return largeModel;
  }

  public void setLargeModel(boolean large)
  {
    if (largeModel == large)
      return;

    boolean oldValue = largeModel;
    largeModel = large;
    firePropertyChange(LARGE_MODEL_PROPERTY, oldValue, large);
  }

  public int getRowHeight()
  {

    return rowHeight;
  }

  public void setRowHeight(int height)
  {
    if (rowHeight == height)
      return;
    
    int oldValue = rowHeight;
    rowHeight = height;
    firePropertyChange(ROW_HEIGHT_PROPERTY, oldValue, height);
  }

  public boolean isFixedRowHeight()
  {
    return rowHeight > 0;
  }

  public boolean getInvokesStopCellEditing()
  {
    return invokesStopCellEditing;
  }

  public void setInvokesStopCellEditing(boolean invoke)
  {
    if (invokesStopCellEditing == invoke)
     return;

    boolean oldValue = invokesStopCellEditing;
    invokesStopCellEditing = invoke;
		firePropertyChange(INVOKES_STOP_CELL_EDITING_PROPERTY, 
												oldValue, invoke);
  }

  /**
   * @since 1.3
   */
  public int getToggleClickCount()
  {
    return toggleClickCount;
  }

  /**
   * @since 1.3
   */
  public void setToggleClickCount(int count)
  {
    if (toggleClickCount == count)
      return;
    
    int oldValue = toggleClickCount;
    toggleClickCount = count;
    firePropertyChange(TOGGLE_CLICK_COUNT_PROPERTY, oldValue, count);
  }

  public void scrollPathToVisible(TreePath path)
  {
    if (path == null)
      return;

    Rectangle rect = getPathBounds(path);

    if (rect == null)
      return;

    scrollRectToVisible(rect);
  }

  public void scrollRowToVisible(int row)
  {
    scrollPathToVisible(getPathForRow(row));
  }

  public boolean getScrollsOnExpand()
  {
    return scrollsOnExpand;
  }

  public void setScrollsOnExpand(boolean scroll)
  {
    if (scrollsOnExpand == scroll)
      return;

    boolean oldValue = scrollsOnExpand;
    scrollsOnExpand = scroll;
    firePropertyChange(SCROLLS_ON_EXPAND_PROPERTY, oldValue, scroll);
  }

  public void setSelectionPath(TreePath path)
  {
    selectionModel.setSelectionPath(path);
  }

  public void setSelectionPaths(TreePath[] paths)
  {
    selectionModel.setSelectionPaths(paths);
  }
  
  public void setSelectionRow(int row)
  {
    TreePath path = getPathForRow(row);

    if (path != null)
      selectionModel.setSelectionPath(path);
  }

  public void setSelectionRows(int[] rows)
  {
    // Make sure we have an UI so getPathForRow() does not return null.
    if (rows == null || getUI() == null)
      return;

    TreePath[] paths = new TreePath[rows.length];

    for (int i = rows.length - 1; i >= 0; --i)
      paths[i] = getPathForRow(rows[i]);

    setSelectionPaths(paths);
  }

  public void setSelectionInterval(int index0, int index1)
  {
    TreePath[] paths = getPathBetweenRows(index0, index1);

    if (paths != null)
      setSelectionPaths(paths);
  }

  public void addSelectionPath(TreePath path)
  {
    selectionModel.addSelectionPath(path);
  }

  public void addSelectionPaths(TreePath[] paths)
  {
    selectionModel.addSelectionPaths(paths);
  }

  public void addSelectionRow(int row)
  {
    TreePath path = getPathForRow(row);

    if (path != null)
      selectionModel.addSelectionPath(path);
  }

  public void addSelectionRows(int[] rows)
  {
    // Make sure we have an UI so getPathForRow() does not return null.
    if (rows == null || getUI() == null)
      return;

    TreePath[] paths = new TreePath[rows.length];

    for (int i = rows.length - 1; i >= 0; --i)
      paths[i] = getPathForRow(rows[i]);

    addSelectionPaths(paths);
  }

  public void addSelectionInterval(int index0, int index1)
  {
    TreePath[] paths = getPathBetweenRows(index0, index1);

    if (paths != null)
      addSelectionPaths(paths);
  }

  public void removeSelectionPath(TreePath path)
  {
    selectionModel.removeSelectionPath(path);
  }

  public void removeSelectionPaths(TreePath[] paths)
  {
    selectionModel.removeSelectionPaths(paths);
  }

  public void removeSelectionRow(int row)
  {
    TreePath path = getPathForRow(row);

    if (path != null)
      selectionModel.removeSelectionPath(path);
  }

  public void removeSelectionRows(int[] rows)
  {
    if (rows == null || getUI() == null)
      return;

    TreePath[] paths = new TreePath[rows.length];

    for (int i = rows.length - 1; i >= 0; --i)
      paths[i] = getPathForRow(rows[i]);

    removeSelectionPaths(paths);
  }

  public void removeSelectionInterval(int index0, int index1)
  {
    TreePath[] paths = getPathBetweenRows(index0, index1);

    if (paths != null)
      removeSelectionPaths(paths);
  }

  public void clearSelection()
  {
    selectionModel.clearSelection();
      setLeadSelectionPath(null);
  }
  
  public TreePath getLeadSelectionPath()
  {
    return leadSelectionPath;
  }

  /**
   * @since 1.3
   */
  public void setLeadSelectionPath(TreePath path)
  {
    if (leadSelectionPath == path)
      return;

    TreePath oldValue = leadSelectionPath;
    leadSelectionPath = path;
    firePropertyChange(LEAD_SELECTION_PATH_PROPERTY, oldValue, path);
  }

  /**
   * @since 1.3
   */
  public TreePath getAnchorSelectionPath()
  {
    return anchorSelectionPath;
  }

  /**
   * @since 1.3
   */
  public void setAnchorSelectionPath(TreePath path)
  {
    if (anchorSelectionPath == path)
      return;

    TreePath oldValue = anchorSelectionPath;
    anchorSelectionPath = path;
    firePropertyChange(ANCHOR_SELECTION_PATH_PROPERTY, oldValue, path);
  }

  public int getLeadSelectionRow()
  {
    return selectionModel.getLeadSelectionRow();
  }

  public int getMaxSelectionRow()
  {
    return selectionModel.getMaxSelectionRow();
  }

  public int getMinSelectionRow()
  {
    return selectionModel.getMinSelectionRow();
  }

  public int getSelectionCount()
  {
    return selectionModel.getSelectionCount();
  }

  public TreePath getSelectionPath()
  {
    return selectionModel.getSelectionPath();
  }

  public TreePath[] getSelectionPaths()
  {
    return selectionModel.getSelectionPaths();
  }

  public int[] getSelectionRows()
  {
    return selectionModel.getSelectionRows();
  }

  public boolean isPathSelected(TreePath path)
  {
    return selectionModel.isPathSelected(path);
  }

  public boolean isRowSelected(int row)
  {
		return selectionModel.isPathSelected(getPathForRow(row));
  }

  public boolean isSelectionEmpty()
  {
    return selectionModel.isSelectionEmpty();
  }
  
  /**
   * Return the value of the <code>dragEnabled</code> property.
   *
   * @return the value
   * 
   * @since 1.4
   */
  public boolean getDragEnabled()
  {
    return dragEnabled;
  }

  /**
   * Set the <code>dragEnabled</code> property.
   *
   * @param enabled new value
   * 
   * @since 1.4
   */
  public void setDragEnabled(boolean enabled)
  {

    dragEnabled = enabled;
  }

  public int getRowCount()
  {
    TreeUI ui = getUI();

    if (ui != null)
      return ui.getRowCount(this);
    
    return 0;
  }
  
  public void collapsePath(TreePath path)
  {
    setExpandedState(path, false);
  }
  
  public void collapseRow(int row)
  {
    if (row < 0 || row >= getRowCount())
      return;

    TreePath path = getPathForRow(row);

    if (path != null)
      collapsePath(path);
  }

  public void expandPath(TreePath path)
  {
    // Don't expand if last path component is a leaf node.
		if ((path == null) || (treeModel.isLeaf(path.getLastPathComponent())))
      return;

    setExpandedState(path, true);
  }
  
  public void expandRow(int row)
  {
    if (row < 0 || row >= getRowCount())
      return;

    TreePath path = getPathForRow(row);

    if (path != null)
      expandPath(path);
  }

  public boolean isCollapsed(TreePath path)
  {
		return !isExpanded(path);
  }

  public boolean isCollapsed(int row)
  {
    if (row < 0 || row >= getRowCount())
      return false;

    TreePath path = getPathForRow(row);

    if (path != null)
      return isCollapsed(path);

    return false;
  }

  public boolean isExpanded(TreePath path)
  {
    if (path == null)
      return false;

    Object state = nodeStates.get(path);

    if ((state == null) || (state != EXPANDED))
      return false;

    TreePath parent = path.getParentPath();

    if (parent != null)
      return isExpanded(parent);
    
    return true;
  }
  
  public boolean isExpanded(int row)
  {
    if (row < 0 || row >= getRowCount())
      return false;

    TreePath path = getPathForRow(row);

    if (path != null)
      return isExpanded(path);

    return false;
  }

  /**
   * @since 1.3
   */
  public boolean getExpandsSelectedPaths()
  {
    return expandsSelectedPaths;
  }

  /**
   * @since 1.3
   */
  public void setExpandsSelectedPaths(boolean flag)
  {
    if (expandsSelectedPaths == flag)
      return;

    boolean oldValue = expandsSelectedPaths;
    expandsSelectedPaths = flag;
    firePropertyChange(EXPANDS_SELECTED_PATHS_PROPERTY, oldValue, flag);
  }

  public Rectangle getPathBounds(TreePath path)
  {
    TreeUI ui = getUI();

    if (ui == null)
      return null;

    return ui.getPathBounds(this, path);
  }

  public Rectangle getRowBounds(int row)
  {
    TreePath path = getPathForRow(row);

    if (path != null)
      return getPathBounds(path);

    return null;
  }

  public boolean isEditing()
  {
    TreeUI ui = getUI();

    if (ui != null)
      return ui.isEditing(this);

    return false;
  }

  public boolean stopEditing()
  {
    TreeUI ui = getUI();

    if (ui != null)
      return ui.stopEditing(this);

   return false;
  }

  public void cancelEditing()
  {
    TreeUI ui = getUI();

    if (ui != null)
      ui.cancelEditing(this);
  }

  public void startEditingAtPath(TreePath path)
  {
    TreeUI ui = getUI();

    if (ui != null)
      ui.startEditingAtPath(this, path);
  }

  public TreePath getEditingPath()
  {
    TreeUI ui = getUI();

    if (ui != null)
      return ui.getEditingPath(this);

    return null;
  }

  public TreePath getPathForLocation(int x, int y)
  {
    TreePath path = getClosestPathForLocation(x, y);

    if (path != null)
      {
	 Rectangle rect = getPathBounds(path);

	 if ((rect != null) && rect.contains(x, y))
	   return path;
      }

    return null;
  }

  public int getRowForLocation(int x, int y)
  {
    TreePath path = getPathForLocation(x, y);

    if (path != null)
      return getRowForPath(path);

    return -1;
  }
  
  public TreePath getClosestPathForLocation(int x, int y)
  {
    TreeUI ui = getUI();

    if (ui != null)
      return ui.getClosestPathForLocation(this, x, y);
    
    return null;
  }

  public int getClosestRowForLocation(int x, int y)
  {
    TreePath path = getClosestPathForLocation(x, y);

    if (path != null)
      return getRowForPath(path);

    return -1;
  }

  public Object getLastSelectedPathComponent()
  {
    TreePath path = getSelectionPath();

    if (path != null)
      return path.getLastPathComponent();

    return null;
  }

	private void checkExpandParents(TreePath path) throws ExpandVetoException
  {

    TreePath parent = path.getParentPath();

    if (parent != null)
      checkExpandParents(parent);

    fireTreeWillExpand(path);
  }

  private void doExpandParents(TreePath path, boolean state)
  {
    TreePath parent = path.getParentPath();

    if (isExpanded(parent))
		{
			nodeStates.put(path, state ? EXPANDED : COLLAPSED);
      return;
		}
    
    if (parent != null)
      doExpandParents(parent, false);

    nodeStates.put(path, state ? EXPANDED : COLLAPSED);
  }
  
  protected void setExpandedState(TreePath path, boolean state)
  {
    if (path == null)
      return;

    TreePath parent = path.getParentPath();

    try
      {
			if (parent != null)
	  checkExpandParents(parent);
      }
    catch (ExpandVetoException e)
      {
	// Expansion vetoed.
	return;
      }

    doExpandParents(path, state);
  }

  protected void clearToggledPaths()
  {
    nodeStates.clear();
  }

  protected Enumeration getDescendantToggledPaths(TreePath parent)
  {
    if (parent == null)
      return null;

    Enumeration nodes = nodeStates.keys();
    Vector result = new Vector();

    while (nodes.hasMoreElements())
      {
        TreePath path = (TreePath) nodes.nextElement();

        if (path.isDescendant(parent))
          result.addElement(path);
      }
    
    return result.elements();
  }

  public boolean hasBeenExpanded(TreePath path)
  {
    if (path == null)
      return false;

    return nodeStates.get(path) != null;
  }

  public boolean isVisible(TreePath path)
  {
    if (path == null)
      return false;

    TreePath parent = path.getParentPath();

    if (parent == null)
      return true; // Is root node.

    return isExpanded(parent);
  }

  public void makeVisible(TreePath path)
  {
    if (path == null)
      return;

    expandPath(path.getParentPath());
  }

  public boolean isPathEditable(TreePath path)
  {    
    return isEditable();
  }

  /**
   * Creates and returns an instance of {@link TreeModelHandler}.
   *
   * @returns an instance of {@link TreeModelHandler}
   */
  protected TreeModelListener createTreeModelListener()
  {
    return new TreeModelHandler();
  }

  /**
	 * Returns a sample TreeModel that can be used in a JTree. This can be used
	 * in Bean- or GUI-Builders to show something interesting.
   *
   * @return a sample TreeModel that can be used in a JTree
   */
  protected static TreeModel getDefaultTreeModel()
  {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root node");
		DefaultMutableTreeNode child1 = new DefaultMutableTreeNode(
				"Child node 1");
		DefaultMutableTreeNode child11 = new DefaultMutableTreeNode(
				"Child node 1.1");
		DefaultMutableTreeNode child12 = new DefaultMutableTreeNode(
				"Child node 1.2");
		DefaultMutableTreeNode child13 = new DefaultMutableTreeNode(
				"Child node 1.3");
		DefaultMutableTreeNode child2 = new DefaultMutableTreeNode(
				"Child node 2");
		DefaultMutableTreeNode child21 = new DefaultMutableTreeNode(
				"Child node 2.1");
		DefaultMutableTreeNode child22 = new DefaultMutableTreeNode(
				"Child node 2.2");
		DefaultMutableTreeNode child23 = new DefaultMutableTreeNode(
				"Child node 2.3");
		DefaultMutableTreeNode child24 = new DefaultMutableTreeNode(
				"Child node 2.4");

		DefaultMutableTreeNode child3 = new DefaultMutableTreeNode(
				"Child node 3");
    root.add(child1);
    root.add(child2);
    root.add(child3);
    child1.add(child11);
    child1.add(child12);
    child1.add(child13);
    child2.add(child21);
    child2.add(child22);
    child2.add(child23);
    child2.add(child24);
    return new DefaultTreeModel(root);
  }

  /**
	 * Converts the specified value to a String. This is used by the renderers
	 * of this JTree and its nodes.
   *
   * This implementation simply returns <code>value.toString()</code> and
	 * ignores all other parameters. Subclass this method to control the
	 * conversion.
   *
   * @param value the value that is converted to a String
   * @param selected indicates if that value is selected or not
   * @param expanded indicates if that value is expanded or not
   * @param leaf indicates if that value is a leaf node or not
   * @param row the row of the node
   * @param hasFocus indicates if that node has focus or not
   */
  public String convertValueToText(Object value, boolean selected,
			boolean expanded, boolean leaf, int row, boolean hasFocus)
  {
    return value.toString();
  }

  /**
	 * A String representation of this JTree. This is intended to be used for
	 * debugging. The returned string may be empty but may not be
   * <code>null</code>.
   *
   * @return a String representation of this JTree
   */
  public String paramString()
  {
    // TODO: this is completely legal, but it would possibly be nice
    // to return some more content, like the tree structure, some properties
    // etc ...
    return "";
  }

  /**
	 * Returns all TreePath objects which are a descendants of the given path
	 * and are exapanded at the moment of the execution of this method. If the
	 * state of any node is beeing toggled while this method is executing this
   * change may be left unaccounted.
   *  
   * @param path The parent of this request
   * @return An Enumeration containing TreePath objects
   */
  public Enumeration getExpandedDescendants(TreePath path) 
  {
    Enumeration paths = nodeStates.keys();
    Vector relevantPaths = new Vector();
		while (paths.hasMoreElements())
      {
        TreePath nextPath = (TreePath) paths.nextElement();
			if (nodeStates.get(nextPath) == EXPANDED
					&& path.isDescendant(nextPath))
          {
            relevantPaths.add(nextPath);
          }
      }
    return relevantPaths.elements();
  }

  /**
   * Returns the next table element (beginning from the row
	 * <code>startingRow</code> that starts with <code>prefix</code>.
	 * Searching is done in the direction specified by <code>bias</code>.
   *
   * @param prefix the prefix to search for in the cell values
   * @param startingRow the index of the row where to start searching from
	 * @param bias the search direction, either {@link Position.Bias.Forward} or
	 *        {@link Position.Bias.Backward}
   *
	 * @return the path to the found element or -1 if no such element has been
	 *         found
   *
   * @throws IllegalArgumentException if prefix is <code>null</code> or
   *     startingRow is not valid
   *
   * @since 1.4
   */
  public TreePath getNextMatch(String prefix, int startingRow,
                               Position.Bias bias)
  {
    if (prefix == null)
			throw new IllegalArgumentException(
					"The argument 'prefix' must not be" + " null.");
    if (startingRow < 0)
			throw new IllegalArgumentException(
					"The argument 'startingRow' must not"
                                         + " be less than zero.");

    int size = getRowCount();
    if (startingRow > size)
			throw new IllegalArgumentException(
					"The argument 'startingRow' must not"
                                         + " be greater than the number of"
                                         + " elements in the TreeModel.");

    TreePath foundPath = null;
    if (bias == Position.Bias.Forward)
      {
        for (int i = startingRow; i < size; i++)
          {
            TreePath path = getPathForRow(i);
            Object o = path.getLastPathComponent();
            // FIXME: in the following call to convertValueToText the
            // last argument (hasFocus) should be done right.
            String item = convertValueToText(o, isRowSelected(i),
						isExpanded(i), treeModel.isLeaf(o), i, false);
            if (item.startsWith(prefix))
              {
                foundPath = path;
                break;
              }
          }
		} else
      {
        for (int i = startingRow; i >= 0; i--)
          {
            TreePath path = getPathForRow(i);
            Object o = path.getLastPathComponent();
            // FIXME: in the following call to convertValueToText the
            // last argument (hasFocus) should be done right.
            String item = convertValueToText(o, isRowSelected(i),
						isExpanded(i), treeModel.isLeaf(o), i, false);
            if (item.startsWith(prefix))
              {
                foundPath = path;
                break;
              }
          }
      }
    return foundPath;
  }

  /**
   * Removes any paths in the current set of selected paths that are
	 * descendants of <code>path</code>. If <code>includePath</code> is set
	 * to <code>true</code> and <code>path</code> itself is selected, then
	 * it will be removed too.
	 * 
	 * @param path the path from which selected descendants are to be removed
	 * @param includePath if <code>true</code> then <code>path</code> itself
	 *        will also be remove if it's selected
   *
   * @return <code>true</code> if something has been removed,
   *     <code>false</code> otherwise
   *
   * @since 1.3
   */
  protected boolean removeDescendantSelectedPaths(TreePath path,
                                                    boolean includeSelected)
  {
    boolean removedSomething = false;
    TreePath[] selected = getSelectionPaths();
    for (int index = 0; index < selected.length; index++)
      {
        if ((selected[index] == path && includeSelected)
            || (selected[index].isDescendant(path)))
          {
            removeSelectionPath(selected[index]);
            removedSomething = true;
          }
      }
    return removedSomething;
  }
}
