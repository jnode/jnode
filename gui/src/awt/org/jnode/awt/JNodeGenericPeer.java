/*
 * $Id$
 *
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
 
package org.jnode.awt;

import java.awt.Toolkit;

/**
 * @author epr
 */
public abstract class JNodeGenericPeer<toolkitT extends JNodeToolkit, compT> {

    protected final toolkitT toolkit;
    protected final compT targetComponent;

    public JNodeGenericPeer(toolkitT toolkit, compT target) {
        this.toolkit = toolkit;
        this.targetComponent = target;
    }

    /**
     * @return
     */
    public final compT getTargetComponent() {
        return this.targetComponent;
    }

    /**
     * @return The toolkit
     * @see java.awt.peer.ComponentPeer#getToolkit()
     */
    public final Toolkit getToolkit() {
        return toolkit;
    }

    /**
     * Gets the implementation toolkit
     *
     * @return The toolkit
     */
    public final toolkitT getToolkitImpl() {
        return toolkit;
    }

    /**
     * Destroy the peer and release all resource
     */
    public void dispose() {
        // Nothing to do
    }
}
