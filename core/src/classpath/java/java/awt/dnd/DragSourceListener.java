/* DragSourceListener.java -- listen to events during the drag
   Copyright (C) 2002, 2005  Free Software Foundation, Inc.

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


package java.awt.dnd;

import java.util.EventListener;

/**
 * This class allows an object to listen for drag and drop events. It can
 * be used to provide appropriate feedback for "drag over" actions. You can
 * also use a <code>DragSourceAdapter</code> to filter the events you are
 * interested in.
 *
 * @author Eric Blake (ebb9@email.byu.edu)
 * @since 1.2
 * @status updated to 1.4
 */
public interface DragSourceListener extends EventListener
{
  /**
   * Called when the cursor hotspot enters a drop site which will accept the
   * drag.
   *
   * @param e the drag source drag event
   */
  void dragEnter(DragSourceDragEvent e);

  /**
   * Called when the cursor hotspot moves inside of a drop site which will
   * accept the drag.
   *
   * @param e the drag source drag event
   */
  void dragOver(DragSourceDragEvent e);

  /**
   * Called when the user modifies the drop gesture. This is often the case
   * when additional mouse or key events are received during the drag.
   *
   * @param e the drag source drag event
   */
  void dropActionChanged(DragSourceDragEvent e);

  /**
   * Called when the cursor hotspot moves outside of a drop site which will
   * accept the drag. This could also happen if the drop site is no longer
   * active, or no longer accepts the drag.
   *
   * @param e the drag source drag event
   */
  void dragExit(DragSourceEvent e);

  /**
   * Called when the drag and drop operation is complete. After this event,
   * <code>getDropSuccess</code> of the event is valid, and
   * <code>getDropAction</code> holds the action requested by the drop site.
   * Furthermore, the <code>DragSourceContext</code> is invalidated.
   *
   * @param e the drag source drag event
   */
  void dragDropEnd(DragSourceDropEvent e);
} // interface DragSourceListener
