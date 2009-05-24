/* class Container
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

package charva.awt;

import java.util.Enumeration;
import java.util.Vector;

import charva.awt.event.KeyEvent;

/**
 * Container is the abstract superclass of Window and Panel.
 */
public abstract class Container 
    extends Component
{
    public Container() {}

    /**
     * doLayout is intended to be used by subclasses of Container, such
     * as Window, JPanel and JScrollPane.
     */
    public void doLayout() {

	if (_isValid)
	    return;

	if (_layoutMgr != null) {
	    _layoutMgr.doLayout(this);

	    /* Don't set the _isValid flag if the layout manager flag
	     * is an instance of LayoutManager2; the doLayout method must
	     * be called every time because the parent window may have been
	     * resized.
	     * Instances of LayoutManager, on the other hand, are not affected
	     * be resizing of the parent window.
	     */
	    if (_layoutMgr instanceof LayoutManager2 == false)
		_isValid = true;
	}
    }

    public Dimension getSize() { return new Dimension (_size); }

    public int getHeight() { return _size.height; }

    public int getWidth() { return _size.width; }

    public void setSize(Dimension size_) { 
	_size = new Dimension(size_); 
	invalidate();
    }
    public void setSize(int width_, int height_) {
	_size.width = width_;
	_size.height = height_;
	invalidate();
    }
    public void setHeight(int height_) { 
	_size.height = height_; 
	invalidate();
    }
    public void setWidth(int width_) { 
	_size.width = width_; 
	invalidate();
    }

    public Dimension minimumSize() {
	if (_layoutMgr == null)
	    return _size;

	if (_isValid == false)
	    _minimumSize = _layoutMgr.minimumSize(this);
	return _minimumSize;
    }

    /** Returns the component at the specified index.
     */
    public Component getComponent(int n) {
	return (Component) _components.elementAt(n);
    }

    /** Returns the component that contains the specified point, or null
     * if no component contains the point. The x and y coordinates of
     * the point are relative to the origin of this container.
     */
    public Component getComponentAt(Point p) {
	return getComponentAt(p.x, p.y);
    }

    /** Returns the component that contains the specified point, or null
     * if no component contains the point. The x and y coordinates of
     * the point are relative to the origin of this container.
     */
    public Component getComponentAt(int x, int y) {
	Enumeration<Component> e = _components.elements();
	while (e.hasMoreElements()) {
	    Component c = (Component) e.nextElement();
	    if (c.contains(x, y)) {
		if (c instanceof Container) {
		    // Calculate the coordinates of the point relative
		    // to the origin of the container
		    Point origin = c.getLocation();
		    return ((Container) c).getComponentAt(x-origin.x, y-origin.y);
		}
		else
		    return c;
	    }
	}
	return null;
    }

    /** The contained component will inherit the foreground and background 
     * colors of the container if they have not been set yet.
     */
    public Component add(Component component_) {

	_addComponent(component_);
	if (_layoutMgr != null && _layoutMgr instanceof LayoutManager2) {
	    if (_layoutMgr instanceof BorderLayout) {
		((BorderLayout) _layoutMgr).addLayoutComponent(
		    component_, BorderLayout.CENTER);
	    }
	    else {
		throw new IllegalArgumentException(
			"LayoutManager2 requires a constraint object");
	    }
	}
	invalidate();
	return component_;
    }

    /**
     * Removes the specified component from this container.
     */
    public void remove(Component component_) {
	_components.remove(component_);
	component_.setParent(null);
	if (_currentFocus == component_) {
	    _currentFocus = null;
	    _currentFocus = getCurrentFocus();
	}
	invalidate();
    }

    private void _addComponent(Component component_) {

	/* Add the specified component to the list of components in this
	 * container.
	 */
	_components.addElement(component_);

	/* Set this container as the parent of the component.
	 */
	component_.setParent(this);
    }

    /**
     * Adds the specified component to the end of this container. Also
     * notifies the layout manager to add this component to the layout
     * using the specified constraint.
     * If the layout manager does not implement the LayoutManager2 interface,
     * i.e. does not know about layout constraints, we silently ignore the
     * constraint (maybe fix this to throw an exception?).
     */
    public void add(Component component_, Object constraint_) {

	_addComponent(component_);    // add to this container.

	if (_layoutMgr != null && _layoutMgr instanceof LayoutManager2) {
	    ((LayoutManager2) _layoutMgr).addLayoutComponent(
		    component_, constraint_);
	}
    }

    public void setLayout(LayoutManager mgr_) { _layoutMgr = mgr_; }

    /**
     * Returns an array of all the components in this container.
     */
    public Component[] getComponents() { 
	int arraylen = _components.size();
	Component[] array = new Component[arraylen];
	for (int i=0; i<arraylen; i++) {
	    array[i] = (Component) _components.elementAt(i);
	}
	return array;
    }

    /** Returns the number of components in this Container.
     */
    public int getComponentCount() {
	return _components.size();
    }

    /**
     * Draw all the components in this container.
     * @param toolkit
     */
    public void draw(Toolkit toolkit) {

	if ( !isVisible())
	    return;
	
	Enumeration<Component> e = _components.elements();
	while (e.hasMoreElements()) {
	    Component c = (Component) e.nextElement();
	    if (c.isVisible())
		c.draw(toolkit);
	}
    }

    /**
     * Sets the foreground color of this container and all its
     * contained components that do not yet have their foreground 
     * color set.  Overrides the same method in the Component class.
     */
    public void setForeground(Color color_)
    {
	super.setForeground(color_);

	Enumeration<Component> e = _components.elements();
	while (e.hasMoreElements()) {
	    Component c = (Component) e.nextElement();
	    if (c.getForeground() == null)
	    	c.setForeground(color_);
	}
    }

    /**
     * Sets the background color of this container and all its
     * contained components that do not yet have their background
     * color set.  Overrides the same method in the Component class.
     */
    public void setBackground(Color color_)
    {
	super.setBackground(color_);

	Enumeration <Component>e = _components.elements();
	while (e.hasMoreElements()) {
	    Component c = (Component) e.nextElement();
	    if (c.getBackground() == null)
		c.setBackground(color_);
	}
    }

    public void processKeyEvent(KeyEvent ke_) {

	/** Invoke all the KeyListener callbacks that may have been registered
	 * for this Container. 
	 */
	super.processKeyEvent(ke_);
	if (ke_.isConsumed())
	    return;

	/* Propagate the KeyEvent down to the current focus component
	 * inside this container.
	 */
	if (_currentFocus != null) {
	    _currentFocus.processKeyEvent(ke_);
	}
    }

    public void requestFocus() {
	getCurrentFocus().requestFocus();
    }

    /**
     * Return a reference to the (non-container) component inside this 
     * Container that has the keyboard input focus (or would have it, 
     * if the focus was inside this container). If no component inside 
     * the container has the focus, choose the first FocusTraversable 
     * component.
     * @return the Component in this container that would have the focus;
     * never null.
     * @exception IllegalComponentStateException if there is no
     * focus-traversable component in this container.
     */
    public Component getCurrentFocus() {

	if (_currentFocus == null) {
	    /* _currentFocus is not yet set. Try to set it to the first
	     * FocusTraversable component contained in this container.
	     */
	    Enumeration<Component> e = _components.elements();
	    while (e.hasMoreElements()) {
		Component c = (Component) e.nextElement();
		if (c.isFocusTraversable()) {
		    _currentFocus = c;
		    break;
		}
	    }
	}

	if (_currentFocus == null) {
	    throw new IllegalComponentStateException(
		"no focus-traversable components inside this Container");
	}
	if (_currentFocus instanceof Container) {
	    return ((Container) _currentFocus).getCurrentFocus();
	}
	else
	    return _currentFocus;
    }

    /**
     * Set the _currentFocus to refer to the next focus-traversable component
     * in the list of contained components, and put FocusEvents on the queue,
     * one for the component that is losing the focus and one for the component
     * gaining the focus.
     */
    public void nextFocus() {

	/* Put a FOCUS_LOST event on the queue for the component that is
	 * losing the focus.
	 * If the current focus is a Container, then this method will have been
	 * called by that container (which would already have posted a 
	 * FOCUS_LOST event for its own contained component that was losing
	 * focus).
	if ((_currentFocus instanceof Container) == false) {
	    FocusEvent evt = new FocusEvent(AWTEvent.FOCUS_LOST, _currentFocus);
	    EventQueue evtQueue =
		    Toolkit.getDefaultToolkit().getSystemEventQueue();
	    evtQueue.postEvent(evt);
	}
	 */

	/* Determine which component should get focus next.
	 */
	int index = _components.indexOf(_currentFocus);
	if (index == -1) {
	    throw new IllegalComponentStateException(
		"focus component not found in parent");
	}

	Component focusCandidate;

	for (;;) {
	    /* If the focus was owned by the last component in this container, 
	     * the new focus should go to the next component in the parent 
	     * container, IF THERE IS A PARENT (this container may be a 
	     * Window, in which case the parent is null).
	     */
	    if (++index >= _components.size()) {
		if (getParent() != null) {
		    getParent().nextFocus();
		    return;
		}
		else {
		    /* Don't need to worry about infinite loops. Worst case, we
		     * should just end up where we started.
		     */
		    index = 0;
		}
	    }
	    
	    focusCandidate = (Component) _components.elementAt(index);

	    /* If the next component will not accept the focus, continue
	     * trying until we get one that does.
	     */
	    if (focusCandidate.isFocusTraversable())
		break;
	}
	if (focusCandidate instanceof Container)
	    ((Container) focusCandidate).firstFocus();

	focusCandidate.requestFocus();
    }

    /**
     * Set the _currentFocus to refer to the previous focus-traversable 
     * component in the list of contained components, and put FocusEvents on 
     * the queue, one for the component that is losing the focus and one for 
     * the component gaining the focus.
     */
    public void previousFocus() {

	/* Put a FOCUS_LOST event on the queue for the component that is
	 * losing the focus.
	 * If the current focus is a Container, then this method will have been
	 * called by that container (which would already have posted a 
	 * FOCUS_LOST event for its own contained component that was losing
	 * focus).
	if ((_currentFocus instanceof Container) == false) {
	    FocusEvent evt = new FocusEvent(AWTEvent.FOCUS_LOST, _currentFocus);
	    EventQueue evtQueue =
		    Toolkit.getDefaultToolkit().getSystemEventQueue();
	    evtQueue.postEvent(evt);
	}
	 */

	/* Determine which component should get focus next.
	 */
	int index = _components.indexOf(_currentFocus);
	if (index == -1) {
	    throw new IllegalArgumentException(
		"focus component not found in parent");
	}

	Component focusCandidate;

	for (;;) {
	    /* If the focus was owned by the first component in this container, 
	     * the new focus should go to the previous component in the parent 
	     * container, IF THERE IS A PARENT (this container may be a 
	     * Window, in which case the parent is null).
	     */
	    if (--index < 0) {
		if (getParent() != null) {
		    getParent().previousFocus();
		    return;
		}
		else {
		    index = _components.size() - 1;
		}
	    }
	    
	    focusCandidate = (Component) _components.elementAt(index);

	    /* If the next component will not accept the focus, continue
	     * trying until we get one that does.
	     */
	    if (focusCandidate.isFocusTraversable())
		break;
	}
	if (focusCandidate instanceof Container)
	    ((Container) focusCandidate).lastFocus();

	focusCandidate.requestFocus();
    }

    /**
     * Set this container's current keyboard focus. Called by the
     * requestFocus() method of the contained component.
     */
    public void setFocus(Component focus_) {
	_currentFocus = focus_;
	if (getParent() != null)
	    getParent().setFocus(this);
    }

    /**
     * Return true if any of the components within this Container
     * are focus-traversable (i.e. will accept keyboard input focus when
     * TAB or SHIFT-TAB is pressed).
     */
    public boolean isFocusTraversable() {
	if ( !super.isFocusTraversable())
	    return false;

	Enumeration<Component> e = _components.elements();
	while (e.hasMoreElements()) {
	    Component c = (Component) e.nextElement();
	    if (c.isFocusTraversable())
		return true;
	}
	return false;
    }

    public Insets getInsets() { return _insets; }

    /* Default implementation of debug, gets overridden by subclasses.
     */
    public void debug(int level_) {
	Enumeration<Component> e = _components.elements();
	while (e.hasMoreElements()) {
	    Component c = (Component) e.nextElement();
	    c.debug(level_ + 1);
	}
    }

    /**
     * Sets the keyboard focus to the first component that is focusTraversable.
     * Called by the nextFocus() method when it runs out of components in 
     * the current container to move the focus to.  The nextFocus() method
     * first checks that this container contains a focusTraversable component
     * before calling this.
     */
    private void firstFocus() {

	Enumeration<Component> e = _components.elements();
	while (e.hasMoreElements()) {
	    Component c = (Component) e.nextElement();
	    if (c.isFocusTraversable()) {
		if (c instanceof Container) {
		    ((Container) c).firstFocus();
		}

		_currentFocus = c;
		return;
	    }
	}
    }

    /**
     * Sets the keyboard focus to the last component that is focusTraversable.
     * Called by the previousFocus() method when it runs out of components in 
     * the current container to move the focus to.  The previousFocus() method
     * first checks that this container contains a focusTraversable component
     * before calling this.
     */
    private void lastFocus() {

	for (int i=_components.size() - 1; i >= 0; i--) {
	    Component c = (Component) _components.elementAt(i);
	    if (c.isFocusTraversable()) {
		if (c instanceof Container) {
		    ((Container) c).lastFocus();
		}

		_currentFocus = c;
		return;
	    }
	}
    }

    /**
     * Validates this container and all of its contained components.
     * The programmer must call validate() on a container to cause it 
     * to re-layout its contained components after components have 
     * been added, removed or resized.
     */
    public void validate() {
	if (_isValid)
	    return;

	/* doLayout sets the validate flag (unless the layout manager is
	 * an instance of LayoutManager2).
	 */
	doLayout();
    }

    /**
     * Determines whether this component is valid.  A container is valid when
     * it is correctly sized and positioned within its parent container and 
     * all its children are also valid.
     */
    public boolean isValid() { return _isValid; }

    /**
     * Marks the container and all parents above it as needing to be laid out
     * again.
     */
    public void invalidate() {
	_isValid = false;
	super.invalidate();
    }

    //====================================================================
    // INSTANCE VARIABLES

    /**
     * The list of components contained within this Container.
     */
    protected Vector<Component> _components = new Vector<Component>();

    /** The container's size
     */
    protected Dimension _size = new Dimension(1, 1);

    /**
     * The layout manager that will be used to lay out the components.
     */ 
    protected LayoutManager _layoutMgr = null;

    /**
     * The component (which may itself be a Container) inside this Container
     * that currently has the input focus (or, if the input focus is 
     * currently outside this Container, the component to which focus will
     * return if and when this Container regains focus).
     */
    protected Component _currentFocus = null;

    /**
     * The insets define how much padding to insert inside the Container,
     * to take into account the border frame (if any).
     * For a Window they will be (1,1); for a Panel, they will be (0,0).
     */
    protected Insets _insets = new Insets(0,0,0,0);

    /**
     * A flag that is set to true when the container is laid out, and set to
     * false when a component is added or removed from the container
     * (indicating that it needs to be laid out again).
     */
    protected boolean _isValid = false;

    /**
     * Used for caching the minimum size of this container, so that we don't
     * have to keep recalculating it. This dimension is valid only if _isValid
     * is true.
     */
    protected Dimension _minimumSize;

}
