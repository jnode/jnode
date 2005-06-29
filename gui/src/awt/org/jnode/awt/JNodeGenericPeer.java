/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.awt;

import java.awt.Toolkit;

/**
 * @author epr
 */
public abstract class JNodeGenericPeer<toolkitT extends JNodeToolkit, compT> {

	protected final toolkitT toolkit;
	protected final compT target;

	public JNodeGenericPeer(toolkitT toolkit, compT target) {
		this.toolkit = toolkit;
		this.target = target;
	}

    /**
     * @return
     */
    public final compT getTarget() {
        return this.target;
    }

	/**
	 * @see java.awt.peer.ComponentPeer#getToolkit()
	 * @return The toolkit
	 */
	public final Toolkit getToolkit() {
		return toolkit;
	}

	/**
	 * Gets the implementation toolkit
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
