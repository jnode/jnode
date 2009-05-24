/* class Component
 *
 * Copyright (C) 2001  R M Pitman
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

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Vector;

import charva.awt.event.AWTEvent;
import charva.awt.event.FocusEvent;
import charva.awt.event.FocusListener;
import charva.awt.event.KeyEvent;
import charva.awt.event.KeyListener;
import charva.awt.event.MouseEvent;
import charva.awt.event.PaintEvent;
import charva.awt.event.SyncEvent;

/**
 * Component is the abstract superclass of all the other CHARVA widgets.
 */
public abstract class Component
{
    /** Constructor
     */
    public Component() {
    }

    /**
     * Shows or hides this component depending on the value of the
     * parameter <code>visible_</code>
     */
    public void setVisible(boolean visible_)
    {
	if (visible_)
	    show();
	else
	    hide();
    }

    /**
     * @deprecated This method has been replaced by 
     * <code>setVisible(boolean)</code>.
     */
    public void show()
    {
	if ( !_visible) {
	    _visible = true;
	    repaint();	// post a PaintEvent
	}
    }

    /**
     * @deprecated This method has been replaced by 
     * <code>setVisible(boolean)</code>.
     */
    public void hide()
    {
	if (_visible) {
	    _visible = false;

	    // try to move focus to next focusTraversable component
	    if (hasFocus()) {
		getParent().nextFocus();
		if (hasFocus()) {
		    // there was no next focusTraversable component
		    getParent().previousFocus();
		    if (hasFocus()) {
			throw new IllegalComponentStateException(
			"cannot hide component; it was the only " +
			"focusTraversable component in this window");
		    }
		}
	    }

        Component parent = getParent();
        if(parent != null){
            parent.repaint();
        }
	    //repaint();	// post a PaintEvent
	}
    }

    /**
     * Returns true if this component is displayed when its parent
     * container is displayed.
     */
    public boolean isVisible() { return _visible; }

    /** To be implemented by concrete subclasses.
     * @param toolkit
     */
    public abstract void draw(Toolkit toolkit);

    /**
     * A component is "displayed" if it is contained within a displayed Window.
     * Even though it may be "displayed", it may be obscured by other
     * Windows that are on top of it; and it may not be visible to the user
     * because the <code>_visible</code> flag may be false.
     */
    public boolean isDisplayed() { 
	/*
	 * Every component that has been added to a Container has a parent.
	 * The Window class overrides this method because it is never added to
	 * a Container.
	 */
	Container parent = getParent();
	if (parent == null)
	    return false;
	return parent.isDisplayed();
    }

    public Point getLocation() { 
	return new Point(_origin); 
    }

    public void setLocation(Point origin_) {
	_origin = new Point(origin_); 
    }

    public void setLocation(int x_, int y_) { 
	_origin.x = x_; 
	_origin.y = y_; 
    }

    /**
     * Return the absolute coordinates of this component's origin.
     * Note that Window (which is a subclass of Container)
     * has a _parent value of null, but it overrides this method.
     */
    public Point getLocationOnScreen() {
	Container parent = getParent();
	if (parent == null) {
	    throw new IllegalComponentStateException(
		    "cannot get component location " +
		    "before it has been added to a container");
	}

	return parent.getLocationOnScreen().addOffset(_origin);
    }

    public abstract Dimension getSize();

    public abstract int getWidth();

    public abstract int getHeight();

    /**
     * Get the bounding rectangle of this component, relative to
     * the origin of its parent Container.
     */
    public Rectangle getBounds() {
	return new Rectangle(_origin, getSize());
    }

    /** Checks whether this component "contains" the specified point, 
     * where the point's x and y coordinates are defined to be relative 
     * to the top left corner of the parent Container.
     */
    public boolean contains(Point p) {
	return this.contains(p.x, p.y);
    }

    public boolean contains(int x, int y) {
	return this.getBounds().contains(x, y);
    }

    public abstract Dimension minimumSize();

    /**
     * Set the parent container of this component.  This is intended to
     * be called by Container objects only.
     * Note that we use a WeakReference so that the parent can be garbage-
     * collected when there are no more strong references to it.
     */
    public void setParent(Container container_) {
	_parent = new WeakReference<Container>(container_);

	// If this component's colors have not been set yet, inherit
	// the parent container's colors.
	if (getForeground() == null)
	    setForeground(container_.getForeground());

	if (getBackground() == null)
	    setBackground(container_.getBackground());
    }

    /**
     * Get the parent container of this component. Can return null if the
     * component has no parent.
     */
    protected Container getParent() {
	if (_parent == null)
	    return null;

	/* Note that _parent is a WeakReference.
	 */
	return (Container) _parent.get();
    }

    /**
     * Register a KeyListener object for this component.
     */
    public void addKeyListener(KeyListener kl_) {
	if (_keyListeners == null)
	    _keyListeners = new Vector<KeyListener>();
	_keyListeners.add(kl_);
    }

    /**
     * Register a FocusListener object for this component.
     */
    public void addFocusListener(FocusListener fl_) {
	if (_focusListeners == null)
	    _focusListeners = new Vector<FocusListener>();
	_focusListeners.add(fl_);
    }

    /**
     * Process events that are implemented by all components.
     * This can be overridden by subclasses, to handle custom events.
     */
    protected void processEvent(AWTEvent evt_) {
	if (evt_ instanceof KeyEvent) {
	    KeyEvent ke = (KeyEvent) evt_;

	    /* Find the ancestor Window that contains the component that
	     * generated the keystroke. 
	     * Then we call the processKeyEvent method
	     * of the ancestor Window, which calls the same method in its
	     * current-focus container, and so on, until the KeyEvent
	     * gets down to the component that generated the keystroke.
	     * This allows KeyEvents to be processed by outer enclosing
	     * containers, then by inner containers, and finally by the
	     * component that generated the KeyEvent.
	     */
	    this.getAncestorWindow().processKeyEvent(ke);
	}
	else if (evt_ instanceof FocusEvent)
	    processFocusEvent((FocusEvent) evt_);
	else if (evt_ instanceof MouseEvent) {

	    MouseEvent e = (MouseEvent) evt_;
//	    if (e.getModifiers() != MouseEvent.MOUSE_PRESSED)
//		return;

	    processMouseEvent(e);
	}
    }

    /** Invoke all the KeyListener callbacks that may have been registered
     * for this component. The KeyListener objects may modify the 
     * keycodes, and can also set the "consumed" flag.
     */
    public void processKeyEvent(KeyEvent ke_) {
	if (_keyListeners != null) {
	    for (Enumeration<KeyListener> e = _keyListeners.elements(); 
		    e.hasMoreElements(); ) {

		KeyListener kl = (KeyListener) e.nextElement();
		if (ke_.getID() == AWTEvent.KEY_PRESSED)
		    kl.keyPressed(ke_);
		else if (ke_.getID() == AWTEvent.KEY_TYPED)
		    kl.keyTyped(ke_);

		if (ke_.isConsumed())
		    break;
	    }
	}
    }

    /** Process a MouseEvent that was generated by clicking the mouse
     * somewhere inside this component.
     */
    public void processMouseEvent(MouseEvent e) {

	// The default for a left-button-press is to request the focus; 
	// this is overridden by components such as buttons.
	if (e.getButton() == MouseEvent.BUTTON1 &&
		e.getModifiers() == MouseEvent.MOUSE_PRESSED &&
		this.isFocusTraversable())

	    requestFocus();
    }

    /**
     * Invoke all the FocusListener callbacks that may have been registered
     * for this component. 
     */
    public void processFocusEvent(FocusEvent fe_) {
	if (_focusListeners != null) {
	    for (Enumeration<FocusListener> e = _focusListeners.elements(); 
		    e.hasMoreElements(); ) {

		FocusListener fl = (FocusListener) e.nextElement();
		if (fe_.getID() == AWTEvent.FOCUS_GAINED)
		    fl.focusGained(fe_);
		else
		    fl.focusLost(fe_);
	    }
	}
    }

    /** Get the Window that contains this component.
     */
    public Window getAncestorWindow() {
	Container ancestor;
	Container nextancestor;

	if (this instanceof Window)
	    return (Window) this;

	for (ancestor = getParent(); 
	    (ancestor instanceof Window) == false;
	    ancestor = nextancestor) {

	    if (ancestor == null)
		return null;

	    if ((nextancestor = ancestor.getParent()) == null)
		return null;
	}
	return (Window) ancestor;
    }

    /** This method should be invoked by all subclasses of Component
     * which override this method; because this method generates the
     * FOCUS_GAINED event when the component gains the keyboard focus.
     */
    public void requestFocus() {

	/* Generate the FOCUS_GAINED only if the component does not 
	 * already have the focus.
	 */
	Window ancestor = getAncestorWindow();
	Component currentFocus = ancestor.getCurrentFocus();
	if ( currentFocus != this) {
	    EventQueue evtQueue =
		    Toolkit.getDefaultToolkit().getSystemEventQueue();
	    FocusEvent evt = new FocusEvent(AWTEvent.FOCUS_LOST, currentFocus);
	    evtQueue.postEvent(evt);

	    evt = new FocusEvent(AWTEvent.FOCUS_GAINED, this);
	    evtQueue.postEvent(evt);

//	    if (getParent() != null)
		getParent().setFocus(this);

//	    requestSync();
	    repaint();
	}
    }

    /**
     * Returns true if this Component has the keyboard input focus.
     */
    public boolean hasFocus()
    {
	// Modified 19-Feb-02 by rgittens to handle null ancestor.
	Window ancestor = getAncestorWindow();
	if (ancestor == null)
	    return false;

	return (ancestor.getCurrentFocus() == this);
    }

    /**
     * Indicates whether this component can be traversed using Tab or 
     * Shift-Tab keyboard focus traversal. If this
     * method returns "false" it can still request focus using requestFocus(),
     * but it will not automatically be assigned focus during keyboard focus
     * traversal.
     */
    public boolean isFocusTraversable()
    {
	return (_enabled && _visible);
    }

    /**
     * Return true if this component is totally obscured by one or more
     * windows that are stacked above it.
     */
    public boolean isTotallyObscured() {
	Rectangle bounds = getBounds();
	Window ancestor = getAncestorWindow();

	Vector<Window> windowList = Toolkit.getDefaultToolkit().getWindowList();
	boolean obscured = false;
	synchronized (windowList) {

	    /* Ignore windows that are stacked below this component's 
	     * ancestor.
	     */
	    int i;
	    for (i=0; i<windowList.size(); i++) {
		Window w = (Window) windowList.elementAt(i);

		if (w == ancestor)
		    break;
	    }
	    i++;

	    /* Return true if any of the overlying windows totally obscures
	     * this component.
	     */
	    for ( ; i<windowList.size(); i++) {
		Window w = (Window) windowList.elementAt(i);
		Rectangle windowRect = w.getBounds();
		if (bounds.equals(windowRect.intersection(bounds))) {
		    obscured = true;
		    break;
		}
	    }
	}
	return obscured;
    }

    /** Returns the alignment along the X axis.  This indicates how the
     * component would like to be aligned relative to ther components.
     * 0 indicates left-aligned, 0.5 indicates centered and 1 indicates
     * right-aligned.
     */
    public float getAlignmentX() { return _alignmentX; }

    /** Returns the alignment along the Y axis.  This indicates how the
     * component would like to be aligned relative to ther components.
     * 0 indicates top-aligned, 0.5 indicates centered and 1 indicates
     * bottom-aligned.
     */
    public float getAlignmentY() { return _alignmentY; }

    /**
     * Get the foreground color of this component. If it is null,
     * the component will inherit the foreground color of its
     * parent container.
     */
    public Color getForeground() {
	return _foreground;
    }

    /**
     * Get the background color of this component. If it is null,
     * the component will inherit the background color of its
     * parent container.
     */
    public Color getBackground() {
	return _background;
    }

    /** Set the foreground color of this component.
     */
    public void setForeground(Color color_) { 
	_foreground = color_;
	validateCursesColor();
    }

    /** Set the background color of this component.
     */
    public void setBackground(Color color_) { 
	_background = color_;
	validateCursesColor();
    }

    /** Enable this component to react to user input. Components
     * are enabled by default.
     */
    public void setEnabled(boolean flag_) {
	_enabled = flag_;

	/* If this component is already displayed, generate a PaintEvent
	 * and post it onto the queue.
	 */
	this.repaint();
    }

    /**
     * Determine whether this component can react to user input.
     */
    public boolean isEnabled() { return _enabled; }

    /**
     * Marks the component and all parents above it as needing to be laid out
     * again. This method is overridden by Container.
     */
    public void invalidate() {
	Container parent = getParent();
	if (parent != null)
	    parent.invalidate();
    }

    /**
     * Ensures that this component is laid out correctly.
     * This method is primarily intended to be used on instances of 
     * Container. The default implementation does nothing; it is 
     * overridden by Container.
     */
    public void validate() { }

    /** Causes this component to be repainted as soon as possible
     * (this is done by posting a RepaintEvent onto the system queue).
     */
    public void repaint()
    {
	if (isDisplayed() == false)
	    return;

	PaintEvent evt = new PaintEvent(this, getBounds());
	EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
	queue.postEvent(evt);
    }

    /** Causes a SyncEvent to be posted onto the AWT queue, thus requesting
     * a refresh of the physical screen.
     */
    public void requestSync() {
	SyncEvent evt = new SyncEvent(this);
	EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
	queue.postEvent(evt);
    }

    /**
     * Determines whether this component has a valid layout.  A component 
     * is valid when it is correctly sized and positioned within its 
     * parent container and all its children (in the case of a Container) 
     * are also valid. The default implementation returns true; this method 
     * is overridden by Container.
     */
    public boolean isValid() { return true; }

    public abstract void debug(int level_);

    /** Sets the name of the component.
     */
    public void setName(String name_) {
	_name = name_;
    }

    /** Returns the name of the component.
     */
    public String getName() {
	return _name;
    }

    /** Compute the component's ncurses color-pair from its foreground
     * and background colors. If either color is null, it means that the
     * component has not been added to a container yet, so don't do 
     * anything (the colors will be validated when the component is added
     * to the container).
     */
    public void validateCursesColor() {
	if (_foreground == null || _background == null)
	    return;

	_cursesColor = Color.getCursesColor(_foreground, _background);
    }

    public int getCursesColor() {
	return _cursesColor;
    }

    //====================================================================
    // PRIVATE METHODS

    //====================================================================
    // INSTANCE VARIABLES

    /**
     * The coordinates of the top-left corner of the component, relative to
     * its parent container.
     */
    protected Point _origin = new Point(0,0);

    /**
     * A WeakReference to the Container (e.g Window, Panel or Dialog) 
     * that contains us. The reason that we use a WeakReference is to 
     * allow the parent to be garbage-collected when there are no more
     * strong references to it.
     */
    protected WeakReference<Container> _parent = null;

    /**
     * This flag is true if this component can react to user input.
     */
    protected boolean _enabled = true;

    /**
     * A flag that determines whether this component should be displayed
     * (if its parent is displayed).
     * This flag is set to true by default, except for Window which is
     * initially invisible.
     * @see #setVisible(boolean)
     * @see #isVisible()
     */
    protected boolean _visible = true;

    /**
     * A list of KeyListeners registered for this component.
     */
    protected Vector<KeyListener> _keyListeners = null;

    /**
     * A list of FocusListeners registered for this component.
     */
    protected Vector<FocusListener> _focusListeners = null;

    /**
     * the X-alignment of this component
     */
    protected float _alignmentX = LEFT_ALIGNMENT;

    /**
     * the Y-alignment of this component
     */
    protected float _alignmentY = TOP_ALIGNMENT;

    /** The name of this component.
     */
    private String _name = "";

    /** If the foreground color is null, this component inherits the
     * foreground color of its parent Container.
     */
    protected Color _foreground = null;

    /** If the background color is null, this component inherits the
     * background color of its parent Container.
     */
    protected Color _background = null;

    /**
     * The number of this component's color-pair, as computed by the
     * ncurses COLOR_PAIR macro.  This is set when the component
     * is added to a container and whenever the colors are changed after 
     * that, so we don't have to re-determine it every time we draw the 
     * component.<p>
     * A value of -1 indicates that the color-pair number needs to be
     * recomputed.
     */
    protected int _cursesColor = 0;

    public static final float TOP_ALIGNMENT = (float) 0.0;
    public static final float CENTER_ALIGNMENT = (float) 0.5;
    public static final float BOTTOM_ALIGNMENT = (float) 1.0;
    public static final float LEFT_ALIGNMENT = (float) 0.0;
    public static final float RIGHT_ALIGNMENT = (float) 1.0;

}
