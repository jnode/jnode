/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import org.jnode.awt.JNodeGraphics;
import org.jnode.awt.JNodeToolkit;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Component;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.event.PaintEvent;
import java.awt.peer.PanelPeer;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * AWT panel peer implemented as a {@link javax.swing.JPanel}.
 */

class SwingPanelPeer extends SwingComponentPeer implements PanelPeer, SwingContainerPeer,
		SwingPeer {

	private final Panel panel;

	//
	// Construction
	//

	public SwingPanelPeer(SwingToolkit toolkit, Panel panel) {
        super(toolkit, panel);
        JPanel jPanel = new JPanel();
        jComponent = jPanel;
		this.panel = panel;
		SwingToolkit.add(panel, jPanel);
		SwingToolkit.copyAwtProperties(panel, jPanel);
	}

	/**
	 * @see org.jnode.awt.swingpeers.SwingContainerPeer#addAWTComponent(java.awt.Component,
	 *      javax.swing.JComponent)
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer) {
		((JPanel)jComponent).add(peer);
	}

	public void beginLayout() {
	}

	//
	// ContainerPeer
	//

	public void beginValidate() {
	}


	public void endLayout() {
	}

	public void endValidate() {
	}
	/**
	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return panel;
	}

	public boolean isPaintPending() {
		return false;
	}

    public Insets getInsets() {
        return ((JPanel)jComponent).getInsets();
    }

    public Insets insets() {
        return getInsets();
    }
}