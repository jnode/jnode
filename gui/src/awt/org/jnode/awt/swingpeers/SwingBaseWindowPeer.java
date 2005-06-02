/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.Window;
import java.awt.peer.WindowPeer;

import javax.swing.JInternalFrame;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingBaseWindowPeer<awtT extends Window, peerT extends JInternalFrame>
        extends SwingContainerPeer<awtT, peerT> implements WindowPeer {

    public SwingBaseWindowPeer(SwingToolkit toolkit, awtT window, peerT jComponent) {
        super(toolkit, window, jComponent);
    }

    public void handleEvent(AWTEvent e) {
    }

    public void dispose() {
        jComponent.dispose();
        ((SwingToolkit)toolkit).onDisposeFrame();
    }

    public void toBack() {
        jComponent.toBack();
    }

    public void toFront() {
        jComponent.toFront();
    }
}
