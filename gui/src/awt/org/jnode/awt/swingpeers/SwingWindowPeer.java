/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JInternalFrame;
import java.awt.AWTEvent;
import java.awt.Window;
import java.awt.peer.WindowPeer;

/**
 * AWT window peer implemented as a {@link javax.swing.JInternalFrame}.
 * @author Levente Sántha
 */

class SwingWindowPeer extends SwingContainerPeer implements WindowPeer {

    public SwingWindowPeer(SwingToolkit toolkit, Window window) {
        super(toolkit, window, new JInternalFrame());
        SwingToolkit.copyAwtProperties(window, jComponent);
    }
    public SwingWindowPeer(SwingToolkit toolkit, Window window, JInternalFrame jComponent) {
        super(toolkit, window, jComponent);
    }

    public void handleEvent(AWTEvent e) {
    }

    public void dispose() {
        ((JInternalFrame)jComponent).dispose();
		((SwingToolkit)toolkit).onDisposeFrame();
    }

    public void toBack() {
        ((JInternalFrame)jComponent).toBack();
    }

    public void toFront() {
        ((JInternalFrame)jComponent).toFront();
    }
}