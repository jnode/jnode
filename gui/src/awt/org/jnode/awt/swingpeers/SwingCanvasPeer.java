/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Canvas;
import java.awt.Component;
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
        super(toolkit, canvas, new JCanvas());
		this.canvas = canvas;
		SwingToolkit.add(canvas, jComponent);
		SwingToolkit.copyAwtProperties(canvas, jComponent);
	}

    public Component getAWTComponent() {
        return canvas;
    }
    
    private static class JCanvas extends JComponent {
    	
    }
}