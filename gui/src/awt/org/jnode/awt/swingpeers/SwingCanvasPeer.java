/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.BufferCapabilities;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.PaintEvent;
import java.awt.peer.CanvasPeer;

import javax.swing.JComponent;

/**
 * AWT canvas peer implemented as a {@link javax.swing.JComponent}.
 */

class SwingCanvasPeer extends SwingComponentPeer implements CanvasPeer, SwingPeer {

	private final Canvas canvas;

	//
	// Construction
	//

	public SwingCanvasPeer(SwingToolkit toolkit, Canvas canvas) {
        super(toolkit, canvas);
		this.canvas = canvas;
        jComponent = new JComponent() {};
		SwingToolkit.add(canvas, jComponent);
		SwingToolkit.copyAwtProperties(canvas, jComponent);
	}

    public Component getAWTComponent() {
        return canvas;
    }
}