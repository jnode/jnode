/*
 * $Id$
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
 * @author Levente Sántha
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
