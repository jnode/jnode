/*
 * Copyright 1997-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
import java.awt.event.InputEvent;
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;

 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.awt.dnd;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.dnd.peer.DragSourceContextPeer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.util.TooManyListenersException;

/**
 * The <code>DragSourceContext</code> class is responsible for managing the
 * initiator side of the Drag and Drop protocol. In particular, it is responsible
 * for managing drag event notifications to the <code>DragSourceListener</code>s
 * and <code>DragSourceMotionListener</code>s, and providing the
 * <code>Transferable</code> representing the source data for the drag operation.
 * <p>
 * Note that the <code>DragSourceContext</code> itself 
 * implements the <code>DragSourceListener</code> and
 * <code>DragSourceMotionListener</code> interfaces. 
 * This is to allow the platform peer  
 * (the <code>DragSourceContextPeer</code> instance)
 * created by the <code>DragSource</code> to notify
 * the <code>DragSourceContext</code> of
 * state changes in the ongoing operation. This allows the
 * <code>DragSourceContext</code> to interpose
 * itself between the platform and the
 * listeners provided by the initiator of the drag operation.
 *
 * @see DragSourceListener
 * @see DragSourceMotionListener
 * @since 1.2
 */

public class DragSourceContext 
    implements DragSourceListener, DragSourceMotionListener, Serializable {

    private static final long serialVersionUID = -115407898692194719L;

    // used by updateCurrentCursor

    /**
     * An <code>int</code> used by updateCurrentCursor() 
     * indicating that the <code>Cursor</code> should change
     * to the default (no drop) <code>Cursor</code>.
     */
    protected static final int DEFAULT = 0;

    /**
     * An <code>int</code> used by updateCurrentCursor()
     * indicating that the <code>Cursor</code> 
     * has entered a <code>DropTarget</code>. 
     */
    protected static final int ENTER   = 1;

    /**
     * An <code>int</code> used by updateCurrentCursor()
     * indicating that the <code>Cursor</code> is 
     * over a <code>DropTarget</code>. 
     */
    protected static final int OVER    = 2;

    /**
     * An <code>int</code> used by updateCurrentCursor()
     * indicating that the user operation has changed. 
     */ 

    protected static final int CHANGED = 3;
	
    /**
     * Called from <code>DragSource</code>, this constructor creates a new
     * <code>DragSourceContext</code> given the
     * <code>DragSourceContextPeer</code> for this Drag, the
     * <code>DragGestureEvent</code> that triggered the Drag, the initial
     * <code>Cursor</code> to use for the Drag, an (optional)
     * <code>Image</code> to display while the Drag is taking place, the offset
     * of the <code>Image</code> origin from the hotspot at the instant of the
     * triggering event, the <code>Transferable</code> subject data, and the 
     * <code>DragSourceListener</code> to use during the Drag and Drop
     * operation.
     * <br>
     * If <code>DragSourceContextPeer</code> is <code>null</code>
     * <code>NullPointerException</code> is thrown.
     * <br>
     * If <code>DragGestureEvent</code> is <code>null</code>
     * <code>NullPointerException</code> is thrown.
     * <br>
     * If <code>Cursor</code> is <code>null</code> no exception is thrown and
     * the default drag cursor behavior is activated for this drag operation.
     * <br>
     * If <code>Image</code> is <code>null</code> no exception is thrown.
     * <br>
     * If <code>Image</code> is not <code>null</code> and the offset is
     * <code>null</code> <code>NullPointerException</code> is thrown.
     * <br>
     * If <code>Transferable</code> is <code>null</code>
     * <code>NullPointerException</code> is thrown.
     * <br>
     * If <code>DragSourceListener</code> is <code>null</code> no exception 
     * is thrown.
     *
     * @param dscp       the <code>DragSourceContextPeer</code> for this drag
     * @param trigger    the triggering event
     * @param dragCursor the initial <code>Cursor</code>
     * @param dragImage  the <code>Image</code> to drag (or <code>null</code>)
     * @param offset     the offset of the image origin from the hotspot at the
     *                   instant of the triggering event
     * @param t          the <code>Transferable</code>
     * @param dsl        the <code>DragSourceListener</code>
     *
     * @throws IllegalArgumentException if the <code>Component</code> associated
     *         with the trigger event is <code>null</code>.
     * @throws IllegalArgumentException if the <code>DragSource</code> for the 
     *         trigger event is <code>null</code>.
     * @throws IllegalArgumentException if the drag action for the
     *         trigger event is <code>DnDConstants.ACTION_NONE</code>.
     * @throws IllegalArgumentException if the source actions for the
     *         <code>DragGestureRecognizer</code> associated with the trigger
     *         event are equal to <code>DnDConstants.ACTION_NONE</code>. 
     * @throws NullPointerException if dscp, trigger, or t are null, or
     *         if dragImage is non-null and offset is null
     */
    public DragSourceContext(DragSourceContextPeer dscp, 
                             DragGestureEvent trigger, Cursor dragCursor, 
                             Image dragImage, Point offset, Transferable t,
                             DragSourceListener dsl) {
        if (dscp == null) {
            throw new NullPointerException("DragSourceContextPeer");
        }

        if (trigger == null) {
            throw new NullPointerException("Trigger");
        }

        if (trigger.getDragSource() == null) {
            throw new IllegalArgumentException("DragSource");
        }

        if (trigger.getComponent() == null) {
            throw new IllegalArgumentException("Component");
        }

        if (trigger.getSourceAsDragGestureRecognizer().getSourceActions() ==
                 DnDConstants.ACTION_NONE) {
            throw new IllegalArgumentException("source actions");
        }

        if (trigger.getDragAction() == DnDConstants.ACTION_NONE) {
            throw new IllegalArgumentException("no drag action");
        }

        if (t == null) {
            throw new NullPointerException("Transferable");
        }

        if (dragImage != null && offset == null) {
            throw new NullPointerException("offset");
        }
	
        peer         = dscp;
        this.trigger = trigger;
        cursor       = dragCursor;
        transferable = t;
        listener     = dsl;
        sourceActions =
            trigger.getSourceAsDragGestureRecognizer().getSourceActions();

        useCustomCursor = (dragCursor != null);

        updateCurrentCursor(trigger.getDragAction(), getSourceActions(), DEFAULT);
    }

    /**
     * Returns the <code>DragSource</code> 
     * that instantiated this <code>DragSourceContext</code>.
     * 
     * @return the <code>DragSource</code> that 
     *   instantiated this <code>DragSourceContext</code>
     */

    public DragSource   getDragSource() { return trigger.getDragSource(); }

    /**
     * Returns the <code>Component</code> associated with this 
     * <code>DragSourceContext</code>.
     * 
     * @return the <code>Component</code> that started the drag
     */

    public Component    getComponent() { return trigger.getComponent(); }

    /**
     * Returns the <code>DragGestureEvent</code>
     * that initially triggered the drag.
     * 
     * @return the Event that triggered the drag
     */

    public DragGestureEvent getTrigger() { return trigger; }

    /**
     * Returns a bitwise mask of <code>DnDConstants</code> that
     * represent the set of drop actions supported by the drag source for the
     * drag operation associated with this <code>DragSourceContext</code>.
     * 
     * @return the drop actions supported by the drag source
     */
    public int  getSourceActions() {
        return sourceActions;
    }

    /**
     * Sets the cursor for this drag operation to the specified
     * <code>Cursor</code>.  If the specified <code>Cursor</code>
     * is <code>null</code>, the default drag cursor behavior is
     * activated for this drag operation, otherwise it is deactivated.
     * 
     * @param c the <code>Cursor</code> to display, or
     *   <code>null</code> to activate the default drag cursor
     *   behavior
     * 
     */

    public synchronized void setCursor(Cursor c) {
        useCustomCursor = (c != null);
        setCursorImpl(c);
    }

    /**
     * Returns the current drag <code>Cursor</code>. 
     * <P>
     * @return the current drag <code>Cursor</code>
     */

    public Cursor getCursor() { return cursor; }

    /**
     * Add a <code>DragSourceListener</code> to this
     * <code>DragSourceContext</code> if one has not already been added.
     * If a <code>DragSourceListener</code> already exists, 
     * this method throws a <code>TooManyListenersException</code>.
     * <P>
     * @param dsl the <code>DragSourceListener</code> to add.
     * Note that while <code>null</code> is not prohibited,
     * it is not acceptable as a parameter.
     * <P>
     * @throws <code>TooManyListenersException</code> if
     * a <code>DragSourceListener</code> has already been added
     */

    public synchronized void addDragSourceListener(DragSourceListener dsl) throws TooManyListenersException {
	if (dsl == null) return;

	if (equals(dsl)) throw new IllegalArgumentException("DragSourceContext may not be its own listener");

	if (listener != null)
	    throw new TooManyListenersException();
	else
	    listener = dsl;
    }

    /**
     * Removes the specified <code>DragSourceListener</code>
     * from  this <code>DragSourceContext</code>.
     * 
     * @param dsl the <code>DragSourceListener</code> to remove;
     *     note that while <code>null</code> is not prohibited,
     *     it is not acceptable as a parameter
     */

    public synchronized void removeDragSourceListener(DragSourceListener dsl) {
	if (listener != null && listener.equals(dsl)) {
	    listener = null;
	} else
	    throw new IllegalArgumentException();
    }

    /**
     * Notifies the peer that the <code>Transferable</code>'s
     * <code>DataFlavor</code>s have changed.
     */

    public void transferablesFlavorsChanged() {
	if (peer != null) peer.transferablesFlavorsChanged();
    }

    /**
     * Calls <code>dragEnter</code> on the
     * <code>DragSourceListener</code>s registered with this
     * <code>DragSourceContext</code> and with the associated
     * <code>DragSource</code>, and passes them the specified
     * <code>DragSourceDragEvent</code>.
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dragEnter(DragSourceDragEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragEnter(dsde);
        }
        getDragSource().processDragEnter(dsde);

	updateCurrentCursor(getSourceActions(), dsde.getTargetActions(), ENTER);
    }

    /**
     * Calls <code>dragOver</code> on the
     * <code>DragSourceListener</code>s registered with this
     * <code>DragSourceContext</code> and with the associated
     * <code>DragSource</code>, and passes them the specified
     * <code>DragSourceDragEvent</code>.
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dragOver(DragSourceDragEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragOver(dsde);
        }
        getDragSource().processDragOver(dsde);

	updateCurrentCursor(getSourceActions(), dsde.getTargetActions(), OVER);
    }

    /**
     * Calls <code>dragExit</code> on the
     * <code>DragSourceListener</code>s registered with this
     * <code>DragSourceContext</code> and with the associated
     * <code>DragSource</code>, and passes them the specified
     * <code>DragSourceEvent</code>.
     *
     * @param dse the <code>DragSourceEvent</code>
     */
    public void dragExit(DragSourceEvent dse) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragExit(dse);
        }
        getDragSource().processDragExit(dse);

	updateCurrentCursor(DnDConstants.ACTION_NONE, DnDConstants.ACTION_NONE, DEFAULT);
    }

    /**
     * Calls <code>dropActionChanged</code> on the
     * <code>DragSourceListener</code>s registered with this
     * <code>DragSourceContext</code> and with the associated
     * <code>DragSource</code>, and passes them the specified
     * <code>DragSourceDragEvent</code>.
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     */
    public void dropActionChanged(DragSourceDragEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dropActionChanged(dsde);
        }
        getDragSource().processDropActionChanged(dsde);

	updateCurrentCursor(getSourceActions(), dsde.getTargetActions(), CHANGED);
    }

    /**
     * Calls <code>dragDropEnd</code> on the
     * <code>DragSourceListener</code>s registered with this
     * <code>DragSourceContext</code> and with the associated
     * <code>DragSource</code>, and passes them the specified
     * <code>DragSourceDropEvent</code>.
     *
     * @param dsde the <code>DragSourceDropEvent</code>
     */
    public void dragDropEnd(DragSourceDropEvent dsde) {
        DragSourceListener dsl = listener;
        if (dsl != null) {
            dsl.dragDropEnd(dsde);
        }
        getDragSource().processDragDropEnd(dsde);
    }

    /**
     * Calls <code>dragMouseMoved</code> on the
     * <code>DragSourceMotionListener</code>s registered with the
     * <code>DragSource</code> associated with this
     * <code>DragSourceContext</code>, and them passes the specified
     * <code>DragSourceDragEvent</code>.
     *
     * @param dsde the <code>DragSourceDragEvent</code>
     * @since 1.4
     */
    public void dragMouseMoved(DragSourceDragEvent dsde) {
        getDragSource().processDragMouseMoved(dsde);
    }

    /**
     * Returns the <code>Transferable</code> associated with
     * this <code>DragSourceContext</code>.
     * 
     * @return the <code>Transferable</code>
     */
    public Transferable getTransferable() { return transferable; }
   
    /**
     * If the default drag cursor behavior is active, this method
     * sets the default drag cursor for the specified actions
     * supported by the drag source, the drop target action,
     * and status, otherwise this method does nothing.
     * 
     * @param sourceAct the actions supported by the drag source
     * @param targetAct the drop target action
     * @param status one of the fields <code>DEFAULT</code>,
     *               <code>ENTER</code>, <code>OVER</code>, 
     *               <code>CHANGED</code>
     */

    protected synchronized void updateCurrentCursor(int sourceAct, int targetAct, int status) {

	// if the cursor has been previously set then dont do any defaults
	// processing.

	if (useCustomCursor) {
	    return;
	}

	// do defaults processing

	Cursor c = null;

	switch (status) {
	    default:
		targetAct = DnDConstants.ACTION_NONE;
	    case ENTER:
	    case OVER:
	    case CHANGED:
		int    ra = sourceAct & targetAct;

		if (ra == DnDConstants.ACTION_NONE) { // no drop possible
		    if ((sourceAct & DnDConstants.ACTION_LINK) == DnDConstants.ACTION_LINK)
		        c = DragSource.DefaultLinkNoDrop;
		    else if ((sourceAct & DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE)
		        c = DragSource.DefaultMoveNoDrop;
		    else
		        c = DragSource.DefaultCopyNoDrop;
		} else { // drop possible
		    if ((ra & DnDConstants.ACTION_LINK) == DnDConstants.ACTION_LINK)
		        c = DragSource.DefaultLinkDrop;
		    else if ((ra & DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE)
		        c = DragSource.DefaultMoveDrop;
		    else
		        c = DragSource.DefaultCopyDrop;
		}
	}

        setCursorImpl(c);
    }

    private void setCursorImpl(Cursor c) {
        if (cursor == null || !cursor.equals(c)) {
            cursor = c;
            if (peer != null) peer.setCursor(cursor);
        }
    }

    /**
     * Serializes this <code>DragSourceContext</code>. This method first
     * performs default serialization. Next, this object's
     * <code>Transferable</code> is written out if and only if it can be
     * serialized. If not, <code>null</code> is written instead. In this case,
     * a <code>DragSourceContext</code> created from the resulting deserialized
     * stream will contain a dummy <code>Transferable</code> which supports no
     * <code>DataFlavor</code>s. Finally, this object's
     * <code>DragSourceListener</code> is written out if and only if it can be
     * serialized. If not, <code>null</code> is written instead.
     *
     * @serialData The default serializable fields, in alphabetical order,
     *             followed by either a <code>Transferable</code> instance, or
     *             <code>null</code>, followed by either a
     *             <code>DragSourceListener</code> instance, or
     *             <code>null</code>.
     * @since 1.4
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        s.writeObject(SerializationTester.test(transferable)
                      ? transferable : null);
        s.writeObject(SerializationTester.test(listener)
                      ? listener : null);
    }

    /**
     * Deserializes this <code>DragSourceContext</code>. This method first
     * performs default deserialization for all non-<code>transient</code>
     * fields. This object's <code>Transferable</code> and
     * <code>DragSourceListener</code> are then deserialized as well by using
     * the next two objects in the stream. If the resulting
     * <code>Transferable</code> is <code>null</code>, this object's
     * <code>Transferable</code> is set to a dummy <code>Transferable</code>
     * which supports no <code>DataFlavor</code>s.
     *
     * @since 1.4
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        s.defaultReadObject();

        transferable = (Transferable)s.readObject();
        listener = (DragSourceListener)s.readObject();

        // Implementation assumes 'transferable' is never null.
        if (transferable == null) {
            if (emptyTransferable == null) {
                emptyTransferable = new Transferable() {
                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[0];
                        }
                        public boolean isDataFlavorSupported(DataFlavor flavor)
                        {
                            return false;
                        }
                        public Object getTransferData(DataFlavor flavor)
                            throws UnsupportedFlavorException
                        {
                            throw new UnsupportedFlavorException(flavor);
                        }
                    };
            }
            transferable = emptyTransferable;
        }
    }

    private static Transferable emptyTransferable;

    /*
     * fields
     */

    private transient DragSourceContextPeer peer;

    /**
     * The event which triggered the start of the drag.
     *
     * @serial
     */
    private DragGestureEvent    trigger;

    /**
     * The current drag cursor.
     *
     * @serial
     */
    private Cursor              cursor;

    private transient Transferable      transferable;

    private transient DragSourceListener    listener;

    /**
     * <code>true</code> if the custom drag cursor is used instead of the
     * default one.
     *
     * @serial
     */
    private boolean useCustomCursor;

    /**
     * A bitwise mask of <code>DnDConstants</code> that represents the set of
     * drop actions supported by the drag source for the drag operation associated
     * with this <code>DragSourceContext.</code>  
     *
     * @serial
     */
    private final int sourceActions;
}
