/*
 * Copyright 1998-1999 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.swing.tree;

import javax.swing.event.TreeExpansionEvent;

/**
 * Exception used to stop and expand/collapse from happening.
 * See <a
 href="http://java.sun.com/docs/books/tutorial/uiswing/events/treewillexpandlistener.html">How to Write a Tree-Will-Expand Listener</a>
 * in <em>The Java Tutorial</em>
 * for further information and examples.
 *
 * @author Scott Violet
 */
public class ExpandVetoException extends Exception {
    /** The event that the exception was created for. */
    protected TreeExpansionEvent      event;

    /**
     * Constructs an ExpandVetoException object with no message.
     *
     * @param event  a TreeExpansionEvent object
     */

    public ExpandVetoException(TreeExpansionEvent event) {
	this(event, null);
    }

    /**
     * Constructs an ExpandVetoException object with the specified message.
     *
     * @param event    a TreeExpansionEvent object
     * @param message  a String containing the message
     */
    public ExpandVetoException(TreeExpansionEvent event, String message) {
	super(message);
	this.event = event;
    }
}
