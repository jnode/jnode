/*
 * Copyright 1997-2001 Sun Microsystems, Inc.  All Rights Reserved.
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
package javax.swing.event;

import java.util.EventObject;
import java.net.URL;
import javax.swing.text.Element;


/**
 * HyperlinkEvent is used to notify interested parties that 
 * something has happened with respect to a hypertext link.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @author  Timothy Prinzing
 */
public class HyperlinkEvent extends EventObject {

    /**
     * Creates a new object representing a hypertext link event.
     * The other constructor is preferred, as it provides more
     * information if a URL could not be formed.  This constructor
     * is primarily for backward compatibility.
     *
     * @param source the object responsible for the event
     * @param type the event type
     * @param u the affected URL
     */
    public HyperlinkEvent(Object source, EventType type, URL u) {
        this(source, type, u, null);
    }

    /**
     * Creates a new object representing a hypertext link event.
     *
     * @param source the object responsible for the event
     * @param type the event type
     * @param u the affected URL.  This may be null if a valid URL
     *   could not be created.
     * @param desc the description of the link.  This may be useful
     *   when attempting to form a URL resulted in a MalformedURLException.
     *   The description provides the text used when attempting to form the
     *   URL.
     */
    public HyperlinkEvent(Object source, EventType type, URL u, String desc) {
        this(source, type, u, desc, null);
    }

    /**
     * Creates a new object representing a hypertext link event.
     *
     * @param source the object responsible for the event
     * @param type the event type
     * @param u the affected URL.  This may be null if a valid URL
     *   could not be created.
     * @param desc the description of the link.  This may be useful
     *   when attempting to form a URL resulted in a MalformedURLException.
     *   The description provides the text used when attempting to form the
     *   URL.
     * @param sourceElement Element in the Document representing the
     *   anchor
     * @since 1.4
     */
    public HyperlinkEvent(Object source, EventType type, URL u, String desc,
                          Element sourceElement) {
        super(source);
	this.type = type;
	this.u = u;
	this.desc = desc;
        this.sourceElement = sourceElement;
    }

    /**
     * Gets the type of event.
     *
     * @return the type
     */
    public EventType getEventType() {
	return type;
    }

    /**
     * Get the description of the link as a string.
     * This may be useful if a URL can't be formed
     * from the description, in which case the associated
     * URL would be null.
     */
    public String getDescription() {
	return desc;
    }
	
    /**
     * Gets the URL that the link refers to.
     *
     * @return the URL
     */
    public URL getURL() {
	return u;
    }

    /**
     * Returns the <code>Element</code> that corresponds to the source of the
     * event. This will typically be an <code>Element</code> representing
     * an anchor. If a constructur that is used that does not specify a source
     * <code>Element</code>, or null was specified as the source
     * <code>Element</code>, this will return null.
     *
     * @return Element indicating source of event, or null
     * @since 1.4
     */
    public Element getSourceElement() {
	return sourceElement;
    }

    private EventType type;
    private URL u;
    private String desc;
    private Element sourceElement;

	
    /**
     * Defines the ENTERED, EXITED, and ACTIVATED event types, along
     * with their string representations, returned by toString().
     */
    public static final class EventType {

        private EventType(String s) {
	    typeString = s;
	}

        /**
         * Entered type.
         */
	public static final EventType ENTERED = new EventType("ENTERED");

        /**
         * Exited type.
         */
	public static final EventType EXITED = new EventType("EXITED");

        /**
         * Activated type.
         */
	public static final EventType ACTIVATED = new EventType("ACTIVATED");

        /**
         * Converts the type to a string.
         *
         * @return the string
         */
        public String toString() {
	    return typeString;
	}

	private String typeString;
    }
}
