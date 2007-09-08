/*
 * Copyright 2003-2007 Sun Microsystems, Inc.  All Rights Reserved.
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


package javax.management.remote;

import java.io.Serializable;
import javax.management.Notification;

/**
 * <p>A (Notification, Listener ID) pair.</p>
 * <p>This class is used to associate an emitted notification 
 *    with the listener ID to which it is targeted.</p>
 *
 * @since 1.5
 */
public class TargetedNotification implements Serializable {

    private static final long serialVersionUID = 7676132089779300926L;

// If we replace Integer with int...
//     /**
//      * <p>Constructs a <code>TargetedNotification</code> object.  The
//      * object contains a pair (Notification, Listener ID).
//      * The Listener ID identifies the client listener to which that
//      * notification is targeted. The client listener ID is one
//      * previously returned by the connector server in response to an
//      * <code>addNotificationListener</code> request.</p>
//      * @param notification Notification emitted from the MBean server.
//      * @param listenerID   The ID of the listener to which this
//      *        notification is targeted.
//      */
//     public TargetedNotification(Notification notification, 
// 				int listenerID) {
// 	this.notif = notification;
// 	this.id = listenerID;
//     }

    /**
     * <p>Constructs a <code>TargetedNotification</code> object.  The
     * object contains a pair (Notification, Listener ID).
     * The Listener ID identifies the client listener to which that
     * notification is targeted. The client listener ID is one
     * previously returned by the connector server in response to an
     * <code>addNotificationListener</code> request.</p>
     * @param notification Notification emitted from the MBean server.
     * @param listenerID   The ID of the listener to which this
     *        notification is targeted.
     * @exception IllegalArgumentException if the <var>listenerID</var> 
     *        or <var>notification</var> is null.
     */
    public TargetedNotification(Notification notification, 
				Integer listenerID) {
	// If we replace integer with int...
	// this(notification,intValue(listenerID));
 	if (notification == null) throw new 
 	    IllegalArgumentException("Invalid notification: null");
 	if (listenerID == null) throw new 
 	    IllegalArgumentException("Invalid listener ID: null");
 	this.notif = notification;
 	this.id = listenerID;
    }

    /**
     * <p>The emitted notification.</p>
     *
     * @return The notification.
     */
    public Notification getNotification() {
	return notif;
    }

    /**
     * <p>The ID of the listener to which the notification is 
     *    targeted.</p>
     *
     * @return The listener ID.
     */
    public Integer getListenerID() {
	return id;
    }

    /**
     * Returns a textual representation of this Targeted Notification. 
     * 
     * @return a String representation of this Targeted Notification.
     **/
    public String toString() {
	return "{" + notif + ", " + id + "}";
    }

    /**
     * @serial A notification to transmit to the other side.
     * @see #getNotification()
     **/
    private final Notification notif;
    /**
     * @serial The ID of the listener to which the notification is 
     *         targeted.
     * @see #getListenerID()
     **/
    private final Integer id;
    //private final int id;

// Needed if we use int instead of Integer...
//     private static int intValue(Integer id) {
// 	if (id == null) throw new 
// 	    IllegalArgumentException("Invalid listener ID: null");
// 	return id.intValue();
//     }
}
