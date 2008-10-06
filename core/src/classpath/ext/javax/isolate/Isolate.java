/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package javax.isolate;

import java.util.Properties;
import java.security.AccessController;

import org.jnode.vm.isolate.VmIsolate;
import gnu.java.security.action.GetPropertiesAction;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class Isolate {

    /** The actual isolate implementation */
    private final VmIsolate impl;

    //todo hide this constructor
    /**
     * Constructor for the root isolate.
     * 
     * @param impl the JNode speciffic isolate implementation
     */
    public Isolate(VmIsolate impl) {
        this.impl = impl;
    }

    /**
     * Initialize this instance.
     * 
     * @param mainClass
     * @param args
     */
    public Isolate(String mainClass, String... args) {
        this(new StreamBindings(), AccessController.doPrivileged(new GetPropertiesAction()), mainClass, args);
    }

    /**
     * Initialize this instance.
     * 
     * @param mainClass
     * @param args
     * @param properties
     */
    public Isolate(Properties properties, String mainClass, String... args) {
        this(new StreamBindings(), properties, mainClass, args);
    }

    /**
     * Initialize this instance.
     *
     * @param bindings
     * @param properties
     * @param mainClass
     * @param args
     */
    public Isolate(StreamBindings bindings, Properties properties, String mainClass, String... args) {
        this.impl = new VmIsolate(this, bindings.getBindings(), properties, mainClass, args);
    }

    /**
     * Gets the isolate that is running the current thread.
     * 
     * @return
     */
    public static Isolate currentIsolate() {
        return VmIsolate.currentIsolate().getIsolate();
    }

    /**
     * If this object equal to the given object.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other) {
        return (other == this);
    }

    /**
     * Request normal termination of this isolate.
     * 
     * @param status
     */
    public void exit(int status) {
        impl.exit(this, status);
    }

    /**
     * Force termination of this isolate.
     * 
     * @param status
     */
    public void halt(int status) {
        impl.halt(this, status);
    }

    /**
     * Gets a new Link associated with this Isolate from which the current
     * isolate can receive status link messages.
     * 
     * @return
     * @throws ClosedLinkException
     */
    public Link newStatusLink() throws ClosedLinkException {
        return impl.newStatusLink(currentIsolate().impl);
    }

    /**
     * Start this isolate.
     * 
     * @param links
     * @throws IsolateStartupException
     */
    public void start(Link... links) throws IsolateStartupException {
        impl.start(this, links);
    }

    /**
     * Returns an array of active Isolate objects.
     * The array contains one entry for each isolate in the invoker's aggregate that has been started but has not yet
     * terminated. New isolates may have been constructed or existing ones terminated by the time method returns.
     *
     * @return the active Isolate objects present at the time of the call
     * 
     * @throws SecurityException if a security manager is present and permission to query isolates is denied
     */
    public static Isolate[] getIsolates() {
        //todo implement it
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the name of the main class of this isolate.
     * 
     * @return the name of the main class of this isolate
     */
    public String getMainClassName() {
        return impl.getMainClassName(); 
    }

    /**
     * Returns the current state of the isolate.
     *
     * @return the current state of an isolate
     * 
     * @throws IllegalStateException if called before the isolate is started
     * @throws SecurityException if a security manager is present and permission to query isolates is denied
     */
    public IsolateStatus.State	getState() {
        return impl.getIsolateState();
    }
    
    /**
     * Retrieves a copy of the Link array passed to start() by the current
     * isolate's creator. Modification of this array will have no effect on
     * subsequent invocation of this method.
     * 
     * This method never returns null: it will return a zero-length array if
     * this isolate's creator passed null to start().
     * 
     * @return
     */
    public static Link[] getLinks() {
        return VmIsolate.getLinks();
    }

    //todo hide this method
    /**
     * Gets the implementation instance.
     * 
     * @return
     */
    final VmIsolate getImpl() {
        return impl;
    }

    @Override
    public String toString() {
        //todo implement it
        return super.toString();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
