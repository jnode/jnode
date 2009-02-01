/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class LinkMessageDispatcher implements Runnable {

    /** Has shutdown been invoked */
    private boolean shutdown = false;

    /**
     * Adds the supplied link to the set of links for which this dispatcher is
     * dispatching messages and associates it with the supplied listener.
     * 
     * @param link
     * @param listener
     */
    public void add(Link link, LinkMessageHandler listener) {
        // TODO implement me
    }

    /**
     * Removes the supplied link from the set of links for which this dispatcher
     * dispatches messages.
     * 
     * @param link
     */
    public void remove(Link link) {
        // TODO implement me
    }

    /**
     * Shutdown this dispatcher.
     */
    public void shutdown() {
        this.shutdown = true;
    }
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (!shutdown) {
            // TODO implement me
        }
    }

}
