/**
 * $Id$  
 */
package org.jnode.awt.swingpeers;

import org.jnode.awt.JNodeToolkit;

import javax.swing.JComponent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.peer.LightweightPeer;

/**
 * @author Levente Sántha
 */
public class SwingLightweightContainerPeer extends SwingContainerPeer implements LightweightPeer , ISwingPeer {
    private Insets containerInsets;

    public SwingLightweightContainerPeer(JNodeToolkit toolkit, Container component) {
        super(toolkit, component, new JLightweightContainer());
    }

    public Component getAWTComponent() {
        return (Component) component;
    }

    /**
     * @see java.awt.peer.ContainerPeer#getInsets()
     */
    public Insets getInsets() {
        if (containerInsets == null) {
            containerInsets = new Insets(0, 0, 0, 0);
        }
        return containerInsets;
    }

	private static class JLightweightContainer extends JComponent {

	}
}
