/*
 * Copyright 1998-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */


package java.awt.dnd;

import java.awt.Component;
import java.io.Serializable;
import java.awt.Cursor;

import java.awt.Image;
import java.awt.Point;

import java.awt.event.InputEvent;

import java.awt.datatransfer.Transferable;

import java.util.EventObject;

import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * A <code>DragGestureEvent</code> is passed 
 * to <code>DragGestureListener</code>'s  
 * dragGestureRecognized() method
 * when a particular <code>DragGestureRecognizer</code> detects that a 
 * platform dependent drag initiating gesture has occurred 
 * on the <code>Component</code> that it is tracking.
 * 
 * @see java.awt.dnd.DragGestureRecognizer
 * @see java.awt.dnd.DragGestureListener
 * @see java.awt.dnd.DragSource
 */

public class DragGestureEvent extends EventObject {

    private static final long serialVersionUID = 9080172649166731306L;

    /**
     * Construct a <code>DragGestureEvent</code> given the
     * <code>DragGestureRecognizer</code> firing this event, 
     * an <code>int</code> representing
     * the user's preferred action, a <code>Point</code> 
     * indicating the origin of the drag, and a <code>List</code> 
     * of events that comprise the gesture.
     * <P>
     * @param dgr The <code>DragGestureRecognizer</code> firing this event
     * @param act The the user's preferred action
     * @param ori The origin of the drag
     * @param evs The <code>List</code> of events that comprise the gesture
     * <P>
     * @throws <code>IllegalArgumentException</code> if
     * input parameters are null
     */

    public DragGestureEvent(DragGestureRecognizer dgr, int act, Point ori,
			    List<? extends InputEvent> evs)
    {
	super(dgr);

	if ((component = dgr.getComponent()) == null)
	    throw new IllegalArgumentException("null component");
	if ((dragSource = dgr.getDragSource()) == null)
	    throw new IllegalArgumentException("null DragSource");

	if (evs == null || evs.isEmpty())
	    throw new IllegalArgumentException("null or empty list of events");

	if (act != DnDConstants.ACTION_COPY &&
	    act != DnDConstants.ACTION_MOVE &&
	    act != DnDConstants.ACTION_LINK)
	    throw new IllegalArgumentException("bad action");

	if (ori == null) throw new IllegalArgumentException("null origin");

	events     = evs;
	action     = act;
	origin     = ori;
    }

    /**
     * Returns the source as a <code>DragGestureRecognizer</code>.
     * <P>
     * @return the source as a <code>DragGestureRecognizer</code>
     */

    public DragGestureRecognizer getSourceAsDragGestureRecognizer() {
	return (DragGestureRecognizer)getSource();
    }

    /**
     * Returns the <code>Component</code> associated 
     * with this <code>DragGestureEvent</code>.
     * <P>
     * @return the Component
     */

    public Component getComponent() { return component; }

    /**
     * Returns the <code>DragSource</code>.
     * <P>
     * @return the <code>DragSource</code>
     */

    public DragSource getDragSource() { return dragSource; }

    /**
     * Returns a <code>Point</code> in the coordinates
     * of the <code>Component</code> over which the drag originated.
     * <P>
     * @return the Point where the drag originated in Component coords.
     */

    public Point getDragOrigin() {
	return origin;
    }

    /**
     * Returns an <code>Iterator</code> for the events
     * comprising the gesture.
     * <P>
     * @return an Iterator for the events comprising the gesture
     */

    public Iterator<InputEvent> iterator() { return events.iterator(); }

    /**
     * Returns an <code>Object</code> array of the 
     * events comprising the drag gesture.
     * <P>
     * @return an array of the events comprising the gesture
     */

    public Object[] toArray() { return events.toArray(); }

    /**
     * Returns an array of the events comprising the drag gesture.
     * <P>
     * @param array the array of <code>EventObject</code> sub(types)
     * <P>
     * @return an array of the events comprising the gesture
     */

    public Object[] toArray(Object[] array) { return events.toArray(array); }

    /**
     * Returns an <code>int</code> representing the 
     * action selected by the user.
     * <P>
     * @return the action selected by the user
     */

    public int getDragAction() { return action; }

    /**
     * Returns the initial event that triggered the gesture. 
     * <P>
     * @return the first "triggering" event in the sequence of the gesture
     */

    public InputEvent getTriggerEvent() {
	return getSourceAsDragGestureRecognizer().getTriggerEvent();
    }

    /**
     * Starts the drag operation given the <code>Cursor</code> for this drag
     * operation and the <code>Transferable</code> representing the source data
     * for this drag operation.
     * <br>
     * If a <code>null</code> <code>Cursor</code> is specified no exception will
     * be thrown and default drag cursors will be used instead.
     * <br>
     * If a <code>null</code> <code>Transferable</code> is specified 
     * <code>NullPointerException</code> will be thrown.
     *
     * @param dragCursor   The <code>Cursor</code> for this drag operation
     * @param transferable The <code>Transferable</code> representing the source
     *                     data for this drag operation.
     *
     * @throws <code>InvalidDnDOperationException</code> if the Drag and Drop
     *         system is unable to initiate a drag operation, or if the user 
     *         attempts to start a drag while an existing drag operation is
     *         still executing. 
     * @throws <code>NullPointerException</code> if the
     *         <code>Transferable</code> is <code>null</code>.
     * @since 1.4
     */
    public void startDrag(Cursor dragCursor, Transferable transferable) 
      throws InvalidDnDOperationException {
        dragSource.startDrag(this, dragCursor, transferable, null);
    }

    /**
     * Starts the drag given the initial <code>Cursor</code> to display, 
     * the <code>Transferable</code> object, 
     * and the <code>DragSourceListener</code> to use.
     * <P>
     * @param dragCursor   The initial drag Cursor
     * @param transferable The source's Transferable
     * @param dsl	   The source's DragSourceListener
     * <P>
     * @throws <code>InvalidDnDOperationException</code> if
     * the Drag and Drop system is unable to
     * initiate a drag operation, or if the user 
     * attempts to start a drag while an existing
     * drag operation is still executing.
     */

    public void startDrag(Cursor dragCursor, Transferable transferable, DragSourceListener dsl) throws InvalidDnDOperationException {
	dragSource.startDrag(this, dragCursor, transferable, dsl);
    }

    /**
     * Start the drag given the initial <code>Cursor</code> to display,
     * a drag <code>Image</code>, the offset of 
     * the <code>Image</code>, 
     * the <code>Transferable</code> object, and 
     * the <code>DragSourceListener</code> to use.
     * <P>
     * @param dragCursor   The initial drag Cursor
     * @param dragImage    The source's dragImage
     * @param imageOffset  The dragImage's offset
     * @param transferable The source's Transferable
     * @param dsl	   The source's DragSourceListener
     * <P>
     * @throws <code>InvalidDnDOperationException</code> if
     * the Drag and Drop system is unable to
     * initiate a drag operation, or if the user 
     * attempts to start a drag while an existing
     * drag operation is still executing.
     */

    public void startDrag(Cursor dragCursor, Image dragImage, Point imageOffset, Transferable transferable, DragSourceListener dsl) throws InvalidDnDOperationException {
	dragSource.startDrag(this,  dragCursor, dragImage, imageOffset, transferable, dsl);
    }

    /**
     * Serializes this <code>DragGestureEvent</code>. Performs default
     * serialization and then writes out this object's <code>List</code> of
     * gesture events if and only if the <code>List</code> can be serialized.
     * If not, <code>null</code> is written instead. In this case, a
     * <code>DragGestureEvent</code> created from the resulting deserialized
     * stream will contain an empty <code>List</code> of gesture events.
     *
     * @serialData The default serializable fields, in alphabetical order,
     *             followed by either a <code>List</code> instance, or
     *             <code>null</code>.
     * @since 1.4
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        s.writeObject(SerializationTester.test(events) ? events : null);
    }

    /**
     * Deserializes this <code>DragGestureEvent</code>. This method first
     * performs default deserialization for all non-<code>transient</code>
     * fields. An attempt is then made to deserialize this object's
     * <code>List</code> of gesture events as well. This is first attempted
     * by deserializing the field <code>events</code>, because, in releases
     * prior to 1.4, a non-<code>transient</code> field of this name stored the
     * <code>List</code> of gesture events. If this fails, the next object in
     * the stream is used instead. If the resulting <code>List</code> is
     * <code>null</code>, this object's <code>List</code> of gesture events
     * is set to an empty <code>List</code>.
     *
     * @since 1.4
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        ObjectInputStream.GetField f = s.readFields();

        dragSource = (DragSource)f.get("dragSource", null);
        component = (Component)f.get("component", null);
        origin = (Point)f.get("origin", null);
        action = f.get("action", 0);

        // Pre-1.4 support. 'events' was previously non-transient
        try {
            events = (List)f.get("events", null);
        } catch (IllegalArgumentException e) {
            // 1.4-compatible byte stream. 'events' was written explicitly
            events = (List)s.readObject();
        }

        // Implementation assumes 'events' is never null.
        if (events == null) {
            events = Collections.EMPTY_LIST;
        }
    }

    /*
     * fields
     */

    private transient List events;

    /**
     * The DragSource associated with this DragGestureEvent.
     *
     * @serial
     */
    private DragSource dragSource;

    /**
     * The Component associated with this DragGestureEvent.
     *
     * @serial
     */
    private Component  component;

    /**
     * The origin of the drag.
     *
     * @serial
     */
    private Point      origin;

    /**
     * The user's preferred action.
     *
     * @serial
     */
    private int	       action;
}
