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
 
package org.jnode.awt.swingpeers;

import org.jnode.awt.JNodeGenericPeer;
import org.jnode.awt.JNodeToolkit;

import javax.swing.JComponent;
import java.awt.peer.MenuComponentPeer;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class SwingMenuComponentPeer extends JNodeGenericPeer implements MenuComponentPeer{
    protected JComponent jComponent;
    public SwingMenuComponentPeer(JNodeToolkit toolkit, Object component, JComponent peer) {
        super(toolkit, component);
        this.jComponent = peer;
    }

    public void dispose() {
        super.dispose();
    }
}
