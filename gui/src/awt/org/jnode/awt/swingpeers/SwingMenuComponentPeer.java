/**
 * $Id$  
 */
package org.jnode.awt.swingpeers;

import org.jnode.awt.JNodeGenericPeer;
import org.jnode.awt.JNodeToolkit;

import javax.swing.JComponent;
import java.awt.peer.MenuComponentPeer;

/**
 * @author Levente Sántha
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
