/* Window.java --
   Copyright (C) 1999, 2000, 2002, 2003 Free Software Foundation

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.awt;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.peer.WindowPeer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.EventListener;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;

/**
 * This class represents a top-level window with no decorations.
 *
 * @author Aaron M. Renn <arenn@urbanophile.com>
 * @author Warren Levy  <warrenl@cygnus.com>
 */
public class Window extends Container implements Accessible
{
  private static final long serialVersionUID = 4497834738069338734L;

  // Serialized fields, from Sun's serialization spec.
  private String warningString = null;
  private int windowSerializedDataVersion = 0; // FIXME
  /** @since 1.2 */
  // private FocusManager focusMgr;  // FIXME: what is this?  
  /** @since 1.2 */
  private int state = 0;
  /** @since 1.4 */
  private boolean focusableWindowState = true;

  // A list of other top-level windows owned by this window.
  private transient Vector ownedWindows = new Vector();

  private transient WindowListener windowListener;
  private transient WindowFocusListener windowFocusListener;
  private transient WindowStateListener windowStateListener;
  private transient GraphicsConfiguration graphicsConfiguration;
  private transient AccessibleContext accessibleContext;

  /** 
   * This (package access) constructor is used by subclasses that want
   * to build windows that do not have parents.  Eg. toplevel
   * application frames.  Subclasses cannot call super(null), since
   * null is an illegal argument.
   */
  Window()
  {
    visible = false;
    setLayout(new BorderLayout());
  }

  Window(GraphicsConfiguration gc)
  {
    this();
    graphicsConfiguration = gc;
  }

  /**
   * Initializes a new instance of <code>Window</code> with the specified
   * parent.  The window will initially be invisible.
   *
   * @param parent The owning <code>Frame</code> of this window.
   *
   * @exception IllegalArgumentException If the owner's GraphicsConfiguration
   * is not from a screen device, or if owner is null; this exception is always
   * thrown when GraphicsEnvironment.isHeadless returns true.
   */
  public Window(Frame owner)
  {
    this (owner, owner.getGraphicsConfiguration ());
  }

  /**
   * Initializes a new instance of <code>Window</code> with the specified
   * parent.  The window will initially be invisible.   
   *
   * @exception IllegalArgumentException If the owner's GraphicsConfiguration
   * is not from a screen device, or if owner is null; this exception is always
   * thrown when GraphicsEnvironment.isHeadless returns true.
   *
   * @since 1.2
   */
  public Window(Window owner)
  {
    this (owner, owner.getGraphicsConfiguration ());
  }
  
  /**
   * Initializes a new instance of <code>Window</code> with the specified
   * parent.  The window will initially be invisible.   
   *
   * @exception IllegalArgumentException If owner is null or if gc is not from a
   * screen device; this exception is always thrown when
   * GraphicsEnvironment.isHeadless returns true.
   *
   * @since 1.3
   */
  public Window(Window owner, GraphicsConfiguration gc)
  {
    this ();

    synchronized (getTreeLock())
      {
	if (owner == null)
	  throw new IllegalArgumentException ("owner must not be null");

	parent = owner;
        owner.ownedWindows.add(new WeakReference(this));
      }

    // FIXME: make this text visible in the window.
    SecurityManager s = System.getSecurityManager();
    if (s != null && ! s.checkTopLevelWindow(this))
      warningString = System.getProperty("awt.appletWarning");

    if (gc != null
        && gc.getDevice().getType() != GraphicsDevice.TYPE_RASTER_SCREEN)
      throw new IllegalArgumentException ("gc must be from a screen device");

    // FIXME: until we implement this, it just causes AWT to crash.
//     if (gc == null)
//       graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
//         .getDefaultScreenDevice()
//         .getDefaultConfiguration();
//     else
      graphicsConfiguration = gc;
  }

  GraphicsConfiguration getGraphicsConfigurationImpl()
  {
    if (graphicsConfiguration != null)
	return graphicsConfiguration;

    return super.getGraphicsConfigurationImpl();
  }

  /**
   * Creates the native peer for this window.
   */
  public void addNotify()
  {
    if (peer == null)
      peer = getToolkit().createWindow(this);
    super.addNotify();
  }

  /**
   * Relays out this window's child components at their preferred size.
   *
   * @specnote pack() doesn't appear to be called internally by show(), so
   *             we duplicate some of the functionality.
   */
  public void pack()
  {
    if (parent != null && !parent.isDisplayable())
      parent.addNotify();
    if (peer == null)
      addNotify();

    setSize(getPreferredSize());

    validate();
  }

  /**
   * Shows on-screen this window and any of its owned windows for whom
   * isVisible returns true.
   */
  public void show()
  {
    if (parent != null && !parent.isDisplayable())
      parent.addNotify();
    if (peer == null)
      addNotify();

    // Show visible owned windows.
    synchronized (getTreeLock())
      {
	Iterator e = ownedWindows.iterator();
	while(e.hasNext())
	  {
	    Window w = (Window)(((Reference) e.next()).get());
	    if (w != null)
	      {
		if (w.isVisible())
		  w.getPeer().setVisible(true);
	      }
     	    else
	      // Remove null weak reference from ownedWindows.
	      // Unfortunately this can't be done in the Window's
	      // finalize method because there is no way to guarantee
	      // synchronous access to ownedWindows there.
	      e.remove();
	  }
      }
    validate();
    super.show();
    toFront();
  }

  public void hide()
  {
    // Hide visible owned windows.
    synchronized (getTreeLock ())
      {
	Iterator e = ownedWindows.iterator();
	while(e.hasNext())
	  {
	    Window w = (Window)(((Reference) e.next()).get());
	    if (w != null)
	      {
		if (w.isVisible() && w.getPeer() != null)
		  w.getPeer().setVisible(false);
	      }
     	    else
	      e.remove();
	  }
      }
    super.hide();
  }

  public boolean isDisplayable()
  {
    if (super.isDisplayable())
      return true;
    return peer != null;
  }

  /**
   * Destroys any resources associated with this window.  This includes
   * all components in the window and all owned top-level windows.
   */
  public void dispose()
  {
    hide();

    synchronized (getTreeLock ())
      {
	Iterator e = ownedWindows.iterator();
	while(e.hasNext())
	  {
	    Window w = (Window)(((Reference) e.next()).get());
	    if (w != null)
	      w.dispose();
	    else
	      // Remove null weak reference from ownedWindows.
	      e.remove();
	  }

	for (int i = 0; i < ncomponents; ++i)
	  component[i].removeNotify();
	this.removeNotify();

        // Post a WINDOW_CLOSED event.
        WindowEvent we = new WindowEvent(this, WindowEvent.WINDOW_CLOSED);
        getToolkit().getSystemEventQueue().postEvent(we);
      }
  }

  /**
   * Sends this window to the back so that all other windows display in
   * front of it.
   */
  public void toBack()
  {
    if (peer != null)
      {
	WindowPeer wp = (WindowPeer) peer;
	wp.toBack();
      }
  }

  /**
   * Brings this window to the front so that it displays in front of
   * any other windows.
   */
  public void toFront()
  {
    if (peer != null)
      {
        WindowPeer wp = (WindowPeer) peer;
        wp.toFront();
      }
  }

  /**
   * Returns the toolkit used to create this window.
   *
   * @return The toolkit used to create this window.
   *
   * @specnote Unlike Component.getToolkit, this implementation always 
   *           returns the value of Toolkit.getDefaultToolkit().
   */
  public Toolkit getToolkit()
  {
    return Toolkit.getDefaultToolkit();    
  }

  /**
   * Returns the warning string that will be displayed if this window is
   * popped up by an unsecure applet or application.
   *
   * @return The unsecure window warning message.
   */
  public final String getWarningString()
  {
    return warningString;
  }

  /**
   * Returns the locale that this window is configured for.
   *
   * @return The locale this window is configured for.
   */
  public Locale getLocale()
  {
    return locale == null ? Locale.getDefault() : locale;
  }

  /*
  /** @since 1.2
  public InputContext getInputContext()
  {
    // FIXME
  }
  */

  /**
   * Sets the cursor for this window to the specifiec cursor.
   *
   * @param cursor The new cursor for this window.
   */
  public void setCursor(Cursor cursor)
  {
    super.setCursor(cursor);
  }

  public Window getOwner()
  {
    return (Window) parent;
  }

  /** @since 1.2 */
  public Window[] getOwnedWindows()
  {
    Window [] trimmedList;
    synchronized (getTreeLock ())
      {
	// Windows with non-null weak references in ownedWindows.
	Window [] validList = new Window [ownedWindows.size()];

	Iterator e = ownedWindows.iterator();
	int numValid = 0;
	while (e.hasNext())
	  {
	    Window w = (Window)(((Reference) e.next()).get());
	    if (w != null)
	      validList[numValid++] = w;
	    else
	      // Remove null weak reference from ownedWindows.
	      e.remove();
	  }

	if (numValid != validList.length)
	  {
	    trimmedList = new Window [numValid];
	    System.arraycopy (validList, 0, trimmedList, 0, numValid);
	  }
	else
	  trimmedList = validList;
      }
    return trimmedList;
  }

  /**
   * Adds the specified listener to the list of <code>WindowListeners</code>
   * that will receive events for this window.
   *
   * @param listener The <code>WindowListener</code> to add.
   */
  public synchronized void addWindowListener(WindowListener listener)
  {
    windowListener = AWTEventMulticaster.add(windowListener, listener);
  }

  /**
   * Removes the specified listener from the list of
   * <code>WindowListeners</code> that will receive events for this window.
   *
   * @param listener The <code>WindowListener</code> to remove.
   */
  public synchronized void removeWindowListener(WindowListener listener)
  {
    windowListener = AWTEventMulticaster.remove(windowListener, listener);
  }

  /**
   * Returns an array of all the window listeners registered on this window.
   *
   * @since 1.4
   */
  public synchronized WindowListener[] getWindowListeners()
  {
    return (WindowListener[])
      AWTEventMulticaster.getListeners(windowListener,
                                       WindowListener.class);
  }

  /**
   * Returns an array of all the window focus listeners registered on this
   * window.
   *
   * @since 1.4
   */
  public synchronized WindowFocusListener[] getWindowFocusListeners()
  {
    return (WindowFocusListener[])
      AWTEventMulticaster.getListeners(windowFocusListener,
                                       WindowFocusListener.class);
  }
  
  /**
   * Returns an array of all the window state listeners registered on this
   * window.
   *
   * @since 1.4
   */
  public synchronized WindowStateListener[] getWindowStateListeners()
  {
    return (WindowStateListener[])
      AWTEventMulticaster.getListeners(windowStateListener,
                                       WindowStateListener.class);
  }

  /**
   * Adds the specified listener to this window.
   */
  public void addWindowFocusListener (WindowFocusListener wfl)
  {
    windowFocusListener = AWTEventMulticaster.add (windowFocusListener, wfl);
  }
  
  /**
   * Adds the specified listener to this window.
   *
   * @since 1.4
   */
  public void addWindowStateListener (WindowStateListener wsl)
  {
    windowStateListener = AWTEventMulticaster.add (windowStateListener, wsl);  
  }
  
  /**
   * Removes the specified listener from this window.
   */
  public void removeWindowFocusListener (WindowFocusListener wfl)
  {
    windowFocusListener = AWTEventMulticaster.remove (windowFocusListener, wfl);
  }
  
  /**
   * Removes the specified listener from this window.
   *
   * @since 1.4
   */
  public void removeWindowStateListener (WindowStateListener wsl)
  {
    windowStateListener = AWTEventMulticaster.remove (windowStateListener, wsl);
  }

  /**
   * Returns an array of all the objects currently registered as FooListeners
   * upon this Window. FooListeners are registered using the addFooListener
   * method.
   *
   * @exception ClassCastException If listenerType doesn't specify a class or
   * interface that implements java.util.EventListener.
   *
   * @since 1.3
   */
  public EventListener[] getListeners(Class listenerType)
  {
    if (listenerType == WindowListener.class)
      return getWindowListeners();
    return super.getListeners(listenerType);
  }

  void dispatchEventImpl(AWTEvent e)
  {
    // Make use of event id's in order to avoid multiple instanceof tests.
    if (e.id <= WindowEvent.WINDOW_LAST 
        && e.id >= WindowEvent.WINDOW_FIRST
        && (windowListener != null
	    || windowFocusListener != null
	    || windowStateListener != null
	    || (eventMask & AWTEvent.WINDOW_EVENT_MASK) != 0))
      processEvent(e);
    else
      super.dispatchEventImpl(e);
  }

  /**
   * Processes the specified event for this window.  If the event is an
   * instance of <code>WindowEvent</code>, then
   * <code>processWindowEvent()</code> is called to process the event,
   * otherwise the superclass version of this method is invoked.
   *
   * @param event The event to process.
   */
  protected void processEvent(AWTEvent evt)
  {
    if (evt instanceof WindowEvent)
      processWindowEvent((WindowEvent) evt);
    else
      super.processEvent(evt);
  }

  /**
   * Dispatches this event to any listeners that are listening for
   * <code>WindowEvents</code> on this window.  This method only gets
   * invoked if it is enabled via <code>enableEvents()</code> or if
   * a listener has been added.
   *
   * @param event The event to process.
   */
  protected void processWindowEvent(WindowEvent evt)
  {
    int id = evt.getID();

    if (id == WindowEvent.WINDOW_GAINED_FOCUS
	|| id == WindowEvent.WINDOW_LOST_FOCUS)
      processWindowFocusEvent (evt);
    else if (id == WindowEvent.WINDOW_STATE_CHANGED)
      processWindowStateEvent (evt);
    else
      {
	if (windowListener != null)
	  {
	    switch (evt.getID())
	      {
	      case WindowEvent.WINDOW_ACTIVATED:
		windowListener.windowActivated(evt);
		break;

	      case WindowEvent.WINDOW_CLOSED:
		windowListener.windowClosed(evt);
		break;

	      case WindowEvent.WINDOW_CLOSING:
		windowListener.windowClosing(evt);
		break;

	      case WindowEvent.WINDOW_DEACTIVATED:
		windowListener.windowDeactivated(evt);
		break;

	      case WindowEvent.WINDOW_DEICONIFIED:
		windowListener.windowDeiconified(evt);
		break;

	      case WindowEvent.WINDOW_ICONIFIED:
		windowListener.windowIconified(evt);
		break;

	      case WindowEvent.WINDOW_OPENED:
		windowListener.windowOpened(evt);
		break;

	      default:
		break;
	      }
	  }
      }
  }

  /**
   * Returns the child window that has focus if this window is active.
   * This method returns <code>null</code> if this window is not active
   * or no children have focus.
   *
   * @return The component that has focus, or <code>null</code> if no
   * component has focus.
   */
  public Component getFocusOwner()
  {
    // FIXME
    return null;
  }

  /**
   * Post a Java 1.0 event to the event queue.
   *
   * @param event The event to post.
   *
   * @deprecated
   */
  public boolean postEvent(Event e)
  {
    // FIXME
    return false;
  }

  /**
   * Tests whether or not this window is visible on the screen.
   *
   * @return <code>true</code> if this window is visible, <code>false</code>
   * otherwise.
   */
  public boolean isShowing()
  {
    return super.isShowing();
  }

  /**
   * @since 1.2
   *
   * @deprecated
   */
  public void applyResourceBundle(ResourceBundle rb)
  {
    throw new Error ("Not implemented");
  }

  /**
   * @since 1.2
   *
   * @deprecated
   */
  public void applyResourceBundle(String rbName)
  {
    ResourceBundle rb = ResourceBundle.getBundle(rbName);
    if (rb != null)
      applyResourceBundle(rb);    
  }

  public AccessibleContext getAccessibleContext()
  {
    // FIXME
    //return null;
    throw new Error ("Not implemented");
  }

  /** 
   * Get graphics configuration.  The implementation for Window will
   * not ask any parent containers, since Window is a toplevel
   * window and not actually embedded in the parent component.
   */
  public GraphicsConfiguration getGraphicsConfiguration()
  {
    if (graphicsConfiguration != null) return graphicsConfiguration;
    if (peer != null) return peer.getGraphicsConfiguration();
    return null;
  }

  protected void processWindowFocusEvent(WindowEvent event)
  {
    if (windowFocusListener != null)
      {
        switch (event.getID ())
          {
          case WindowEvent.WINDOW_GAINED_FOCUS:
            windowFocusListener.windowGainedFocus (event);
            break;
            
          case WindowEvent.WINDOW_LOST_FOCUS:
            windowFocusListener.windowLostFocus (event);
            break;
            
          default:
            break;
          }
      }
  }
  
  /**
   * @since 1.4
   */
  protected void processWindowStateEvent(WindowEvent event)
  {
    if (windowStateListener != null
        && event.getID () == WindowEvent.WINDOW_STATE_CHANGED)
      windowStateListener.windowStateChanged (event);
  }

  /**
   * Returns whether this <code>Window</code> can get the focus or not.
   *
   * @since 1.4
   */
  public final boolean isFocusableWindow ()
  {
    if (getFocusableWindowState () == false)
      return false;

    if (this instanceof Dialog
        || this instanceof Frame)
      return true;

    // FIXME: Implement more possible cases for returning true.

    return false;
  }
  
  /**
   * Returns the value of the focusableWindowState property.
   * 
   * @since 1.4
   */
  public boolean getFocusableWindowState ()
  {
    return focusableWindowState;
  }

  /**
   * Sets the value of the focusableWindowState property.
   * 
   * @since 1.4
   */
  public void setFocusableWindowState (boolean focusableWindowState)
  {
    this.focusableWindowState = focusableWindowState;
  }

  // setBoundsCallback is needed so that when a user moves a window,
  // the Window's location can be updated without calling the peer's
  // setBounds method.  When a user moves a window the peer window's
  // location is updated automatically and the windowing system sends
  // a message back to the application informing it of its updated
  // dimensions.  We must update the AWT Window class with these new
  // dimensions.  But we don't want to call the peer's setBounds
  // method, because the peer's dimensions have already been updated.
  // (Under X, having this method prevents Configure event loops when
  // moving windows: Component.setBounds -> peer.setBounds ->
  // postConfigureEvent -> Component.setBounds -> ...  In some cases
  // Configure event loops cause windows to jitter back and forth
  // continuously).
  void setBoundsCallback (int x, int y, int w, int h)
  {
    if (this.x == x && this.y == y && width == w && height == h)
      return;
    invalidate();
    this.x = x;
    this.y = y;
    width = w;
    height = h;
  }
}
