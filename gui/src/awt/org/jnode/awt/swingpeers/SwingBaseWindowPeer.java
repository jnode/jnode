/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

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

    public final void dispose() {
        jComponent.dispose();
        toolkit.onDisposeFrame(this);
    }

    public void toBack() {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                jComponent.toBack();                
            }
        });
    }

    public void toFront() {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                jComponent.toFront();                
            }
        });
    }
}
