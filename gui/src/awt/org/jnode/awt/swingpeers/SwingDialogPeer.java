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

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.peer.DialogPeer;

/**
 * AWT dialog peer implemented as a {@link javax.swing.JInternalFrame}.
 * @author Levente S\u00e1ntha
 */

class SwingDialogPeer extends SwingWindowPeer implements DialogPeer,
		ISwingContainerPeer {

	public SwingDialogPeer(SwingToolkit toolkit, Dialog dialog) {
        super(toolkit, dialog, new JInternalFrame());
		((JInternalFrame)jComponent).setTitle(dialog.getTitle());
		((JInternalFrame)jComponent).getContentPane().setLayout(null);
	}

	/**
	 * @see org.jnode.awt.swingpeers.ISwingContainerPeer#addAWTComponent(java.awt.Component,
	 *      javax.swing.JComponent)
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer) {
		((JInternalFrame)jComponent).getContentPane().add(peer);
	}

	public void dispose() {
        ((JInternalFrame)jComponent).dispose();
		((SwingToolkit)toolkit).onDisposeFrame();
	}

	public void handleEvent(AWTEvent e) {
	}

    public void setResizable(boolean resizeable) {
        ((JInternalFrame)jComponent).setResizable(resizeable);
    }

    public void setTitle(String title) {
        ((JInternalFrame)jComponent).setTitle(title);
    }
}
