/* DefaultKeyboardFocusManager.java -- 
   Copyright (C) 2002 Free Software Foundation, Inc.

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

import java.util.*;
import java.awt.event.*;

// FIXME: finish documentation
public class DefaultKeyboardFocusManager extends KeyboardFocusManager
{
  /**
   * This class models a request to delay the dispatch of events that
   * arrive after a certain time, until a certain component becomes
   * the focus owner.
   */
  private class EventDelayRequest implements Comparable
  {
    /** A {@link java.util.List} of {@link java.awt.event.KeyEvent}s
        that are being delayed, pending this request's {@link
        Component} receiving the keyboard focus. */
    private LinkedList enqueuedKeyEvents = new LinkedList ();

    /** An event timestamp.  All events that arrive after this time
        should be queued in the {@link #enqueuedKeyEvents} {@link
        java.util.List}. */
    public long timestamp;
    /** When this {@link Component} becomes focused, all events
        between this EventDelayRequest and the next one in will be
        dispatched from {@link #enqueuedKeyEvents}. */
    public Component focusedComp;

    /**
     * Construct a new EventDelayRequest.
     *
     * @param timestamp events that arrive after this time will be
     * delayed
     * @param focusedComp the Component that needs to receive focus
     * before events are dispatched
     */
    public EventDelayRequest (long timestamp, Component focusedComp)
    {
      this.timestamp = timestamp;
      this.focusedComp = focusedComp;
    }

    public int compareTo (Object o)
    {
      if (!(o instanceof EventDelayRequest))
        throw new ClassCastException ();

      EventDelayRequest request = (EventDelayRequest) o;

      if (request.timestamp < timestamp)
        return -1;
      else if (request.timestamp == timestamp)
        return 0;
      else
        return 1;
    }

    public boolean equals (Object o)
    {
      if (!(o instanceof EventDelayRequest) || o == null)
        return false;

      EventDelayRequest request = (EventDelayRequest) o;

      return (request.timestamp == timestamp
              && request.focusedComp == focusedComp);
    }

    public void enqueueEvent (KeyEvent e)
    {
      KeyEvent last = (KeyEvent) enqueuedKeyEvents.getLast ();
      if (last != null && e.getWhen () < last.getWhen ())
        throw new RuntimeException ("KeyEvents enqueued out-of-order");

      if (e.getWhen () <= timestamp)
        throw new RuntimeException ("KeyEvents enqueued before starting timestamp");

      enqueuedKeyEvents.add (e);
    }

    public void dispatchEvents ()
    {
      int size = enqueuedKeyEvents.size ();
      for (int i = 0; i < size; i++)
        {
          KeyEvent e = (KeyEvent) enqueuedKeyEvents.remove (0);
          dispatchKeyEvent (e);
        }
    }

    public void discardEvents ()
    {
      enqueuedKeyEvents.clear ();
    }
  }

  /** The {@link java.util.SortedSet} of current {@link
      #EventDelayRequest}s. */
  private SortedSet delayRequests = new TreeSet ();

  public DefaultKeyboardFocusManager ()
  {
  }

  public boolean dispatchEvent (AWTEvent e)
  {
    if (e instanceof WindowEvent)
      {
        Window target = (Window) e.getSource ();

        if (e.id == WindowEvent.WINDOW_ACTIVATED)
          setGlobalActiveWindow (target);
        else if (e.id == WindowEvent.WINDOW_GAINED_FOCUS)
          setGlobalFocusedWindow (target);
        else if (e.id != WindowEvent.WINDOW_LOST_FOCUS
                 && e.id != WindowEvent.WINDOW_DEACTIVATED)
          return false;

        target.dispatchEvent (e);
        return true;
      }
    else if (e instanceof FocusEvent)
      {
        Component target = (Component) e.getSource ();

        if (e.id == FocusEvent.FOCUS_GAINED
            && !(target instanceof Window))
          {
            if (((FocusEvent) e).isTemporary ())
              setGlobalFocusOwner (target);
            else
              setGlobalPermanentFocusOwner (target);
          }

        if (!(target instanceof Window))
          target.dispatchEvent (e);

        return true;
      }
    else if (e instanceof KeyEvent)
      {
        // Loop through all registered KeyEventDispatchers, giving
        // each a chance to handle this event.
        Iterator i = getKeyEventDispatchers().iterator();

        while (i.hasNext ())
          {
            KeyEventDispatcher dispatcher = (KeyEventDispatcher) i.next ();
            if (dispatcher.dispatchKeyEvent ((KeyEvent) e))
              return true;
          }

        // processKeyEvent checks if this event represents a focus
        // traversal key stroke.
        Component focusOwner = getGlobalPermanentFocusOwner ();
        processKeyEvent (focusOwner, (KeyEvent) e);

        if (e.isConsumed ())
          return true;

        if (enqueueKeyEvent ((KeyEvent) e))
          // This event was enqueued for dispatch at a later time.
          return true;
        else
          // This event wasn't handled by any of the registered
          // KeyEventDispatchers, and wasn't enqueued for dispatch
          // later, so send it to the default dispatcher.
          return dispatchKeyEvent ((KeyEvent) e);
      }

    return false;
  }

  private boolean enqueueKeyEvent (KeyEvent e)
  {
    Iterator i = delayRequests.iterator ();
    boolean oneEnqueued = false;
    while (i.hasNext ())
      {
        EventDelayRequest request = (EventDelayRequest) i.next ();
        if (e.getWhen () > request.timestamp)
          {
            request.enqueueEvent (e);
            oneEnqueued = true;
          }
      }
    return oneEnqueued;
  }

  public boolean dispatchKeyEvent (KeyEvent e)
  {
    Component focusOwner = getGlobalPermanentFocusOwner ();

    focusOwner.dispatchEvent (e);

    // Loop through all registered KeyEventPostProcessors, giving
    // each a chance to process this event.
    Iterator i = getKeyEventPostProcessors().iterator();

    while (i.hasNext ())
  {
        KeyEventPostProcessor processor = (KeyEventPostProcessor) i.next ();
        if (processor.postProcessKeyEvent ((KeyEvent) e))
          return true;
  }

    // The event hasn't been consumed yet.  Check if it is an
    // MenuShortcut.
    if (postProcessKeyEvent (e))
      return true;

    // Always return true.
    return true;
  }

  public boolean postProcessKeyEvent (KeyEvent e)
  {
    // Check if this event represents a menu shortcut.

    // MenuShortcuts are activated by Ctrl- KeyEvents.
    int modifiers = e.getModifiers ();
    if ((modifiers & KeyEvent.CTRL_MASK) != 0
        || (modifiers & KeyEvent.CTRL_DOWN_MASK) != 0)
      {
        Window focusedWindow = getGlobalFocusedWindow ();
        if (focusedWindow instanceof Frame)
          {
            MenuBar menubar = ((Frame) focusedWindow).getMenuBar ();

            if (menubar != null)
  {
                // If there's a menubar, loop through all menu items,
                // checking whether each one has a shortcut, and if
                // so, whether this key event should activate it.
                int numMenus = menubar.getMenuCount ();

                for (int i = 0; i < numMenus; i++)
                  {
                    Menu menu = menubar.getMenu (i);
                    int numItems = menu.getItemCount ();

                    for (int j = 0; j < numItems; j++)
                      {
                        MenuItem item = menu.getItem (j);
                        MenuShortcut shortcut = item.getShortcut ();

                        if (shortcut != null)
                          {
                            // Dispatch a new ActionEvent if this is a
                            // Shift- KeyEvent and the shortcut requires
                            // the Shift modifier, or if the shortcut
                            // doesn't require the Shift modifier.
                            if ((shortcut.usesShiftModifier ()
                                 && ((modifiers & KeyEvent.SHIFT_MASK) != 0
                                     || (modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0)
                                 || !shortcut.usesShiftModifier ())
                                && shortcut.getKey () == e.getKeyCode ())
                              {
                                item.dispatchEvent (new ActionEvent (item,
                                                                     ActionEvent.ACTION_PERFORMED,
                                                                     item.getActionCommand (),
                                                                     modifiers));
                                // The event was dispatched.
                                return true;
                              }
  }
                      }
                  }
              }
          }
      }
    return false;
  }

  public void processKeyEvent (Component comp, KeyEvent e)
  {
    AWTKeyStroke eventKeystroke = AWTKeyStroke.getAWTKeyStrokeForEvent (e);
    // For every focus traversal keystroke, we need to also consume
    // the other two key event types for the same key (e.g. if
    // KEY_PRESSED TAB is a focus traversal keystroke, we also need to
    // consume KEY_RELEASED and KEY_TYPED TAB key events).
    AWTKeyStroke oppositeKeystroke = AWTKeyStroke.getAWTKeyStroke (e.getKeyCode (),
                                                                   e.getModifiers (),
                                                                   !(e.id == KeyEvent.KEY_RELEASED));

    Set forwardKeystrokes = comp.getFocusTraversalKeys (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
    Set backwardKeystrokes = comp.getFocusTraversalKeys (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    Set upKeystrokes = comp.getFocusTraversalKeys (KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS);
    Set downKeystrokes = null;
    if (comp instanceof Container)
      downKeystrokes = comp.getFocusTraversalKeys (KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS);

    if (forwardKeystrokes.contains (eventKeystroke))
      {
        focusNextComponent (comp);
        e.consume ();
  }
    else if (backwardKeystrokes.contains (eventKeystroke))
  {
        focusPreviousComponent (comp);
        e.consume ();
  }
    else if (upKeystrokes.contains (eventKeystroke))
  {
        upFocusCycle (comp);
        e.consume ();
  }
    else if (comp instanceof Container
             && downKeystrokes.contains (eventKeystroke))
  {
        downFocusCycle ((Container) comp);
        e.consume ();
  }
    else if (forwardKeystrokes.contains (oppositeKeystroke)
             || backwardKeystrokes.contains (oppositeKeystroke)
             || upKeystrokes.contains (oppositeKeystroke)
             || (comp instanceof Container &&
                 downKeystrokes.contains (oppositeKeystroke)))
      e.consume ();
  }

  protected void enqueueKeyEvents (long after, Component untilFocused)
  {
    delayRequests.add (new EventDelayRequest (after, untilFocused));
  }

  protected void dequeueKeyEvents (long after, Component untilFocused)
  {
    // FIXME: need synchronization on delayRequests and enqueuedKeyEvents.

    // Remove the KeyEvent with the oldest timestamp, which should be
    // the first element in the SortedSet.
    if (after < 0)
      {
        int size = delayRequests.size ();
        if (size > 0)
          delayRequests.remove (delayRequests.first ());
      }
    else
      {
        EventDelayRequest template = new EventDelayRequest (after, untilFocused);
        if (delayRequests.contains (template))
          {
            EventDelayRequest actual = (EventDelayRequest) delayRequests.tailSet (template).first ();
            delayRequests.remove (actual);
            actual.dispatchEvents ();
          }
      }
  }

  protected void discardKeyEvents (Component comp)
  {
    // FIXME: need synchronization on delayRequests and enqueuedKeyEvents.

    Iterator i = delayRequests.iterator ();

    while (i.hasNext ())
  {
        EventDelayRequest request = (EventDelayRequest) i.next ();

        if (request.focusedComp == comp
            || (comp instanceof Container
                && ((Container) comp).isAncestorOf (request.focusedComp)))
          request.discardEvents ();
      }
  }

  public void focusPreviousComponent (Component comp)
  {
    Component focusComp = (comp == null) ? getGlobalFocusOwner () : comp;
    Container focusCycleRoot = focusComp.getFocusCycleRootAncestor ();
    FocusTraversalPolicy policy = focusCycleRoot.getFocusTraversalPolicy ();

    Component previous = policy.getComponentBefore (focusCycleRoot, focusComp);
    previous.requestFocusInWindow ();
  }

  public void focusNextComponent (Component comp)
  {
    Component focusComp = (comp == null) ? getGlobalFocusOwner () : comp;
    Container focusCycleRoot = focusComp.getFocusCycleRootAncestor ();
    FocusTraversalPolicy policy = focusCycleRoot.getFocusTraversalPolicy ();

    Component next = policy.getComponentAfter (focusCycleRoot, focusComp);
    next.requestFocusInWindow ();
  }

  public void upFocusCycle (Component comp)
  {
    Component focusComp = (comp == null) ? getGlobalFocusOwner () : comp;
    Container focusCycleRoot = focusComp.getFocusCycleRootAncestor ();

    if (focusCycleRoot instanceof Window)
  {
        FocusTraversalPolicy policy = focusCycleRoot.getFocusTraversalPolicy ();
        Component defaultComponent = policy.getDefaultComponent (focusCycleRoot);
        defaultComponent.requestFocusInWindow ();
  }
    else
  {
        Container parentFocusCycleRoot = focusCycleRoot.getFocusCycleRootAncestor ();

        focusCycleRoot.requestFocusInWindow ();
        setGlobalCurrentFocusCycleRoot (parentFocusCycleRoot);
      }
  }

  public void downFocusCycle (Container cont)
  {
    if (cont == null)
      return;

    if (cont.isFocusCycleRoot (cont))
      {
        FocusTraversalPolicy policy = cont.getFocusTraversalPolicy ();
        Component defaultComponent = policy.getDefaultComponent (cont);
        defaultComponent.requestFocusInWindow ();
        setGlobalCurrentFocusCycleRoot (cont);
      }
  }
} // class DefaultKeyboardFocusManager
