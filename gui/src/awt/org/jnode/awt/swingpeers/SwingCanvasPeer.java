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
import java.awt.Canvas;
import java.awt.Component;
import java.awt.peer.CanvasPeer;

/**
 * AWT canvas peer implemented as a {@link javax.swing.JComponent}.
 */

final class SwingCanvasPeer extends SwingComponentPeer<Canvas, SwingCanvas> implements CanvasPeer {

	public SwingCanvasPeer(SwingToolkit toolkit, Canvas canvas) {
        super(toolkit, canvas, new SwingCanvas(canvas));
		SwingToolkit.add(canvas, jComponent);
		SwingToolkit.copyAwtProperties(canvas, jComponent);
	}

}

final class SwingCanvas extends JComponent implements ISwingPeer {
    private final Canvas awtComponent;

    public SwingCanvas(Canvas awtComponent) {
        this.awtComponent = awtComponent;
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Component getAWTComponent() {
        return awtComponent;
    }
}
