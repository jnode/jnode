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

import org.jnode.vm.isolate.VmIsolate;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class Isolate {

    /** The actual isolate implementation */
    private final VmIsolate impl;

    /**
     * Constructor for the root isolate.
     * 
     * @param mainClass
     * @param args
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
    public Isolate(String mainClass, String[] args) {
        this(new StreamBindings(), new Properties(), mainClass, args);
    }

    /**
     * Initialize this instance.
     * 
     * @param mainClass
     * @param args
     * @param properties
     */
    public Isolate(Properties properties, String mainClass, String[] args) {
        this(new StreamBindings(), properties, mainClass, args);
    }

    /**
     * Initialize this instance.
     * 
     * @param mainClass
     * @param mainArgs
     * @param bindings
     * @param properties
     */
    public Isolate(StreamBindings bindings, Properties properties,
            String mainClass, String[] args) {
        this.impl = new VmIsolate(this, bindings.getBindings(), properties,
                mainClass, args);
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
     * Has this isolate reached the exited state.
     * 
     * @return
     */
    public boolean hasExited() {
        return impl.hasExited();
    }

    /**
     * Has this isolate reached the terminated state.
     * 
     * @return
     */
    public boolean hasTerminated() {
        return impl.hasTerminated();
    }

    /**
     * Has this isolate reached the started state.
     * 
     * @return
     */
    public boolean hasStarted() {
        return impl.hasStarted();
    }

    /**
     * Gets a new Link associated with this Isolate from which the current
     * isolate can receive events.
     * 
     * @return
     * @throws ClosedLinkException
     */
    public Link newEventLink() throws ClosedLinkException {
        // TODO implement me
        return null;
    }

    /**
     * Start this isolate.
     * 
     * @param messages
     * @throws IsolateStartupException
     */
    public void start(Link... links) throws IsolateStartupException {
        impl.start(this, links);
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

    /**
     * Gets the implementation instance.
     * 
     * @return
     */
    final VmIsolate getImpl() {
        return impl;
    }
}
