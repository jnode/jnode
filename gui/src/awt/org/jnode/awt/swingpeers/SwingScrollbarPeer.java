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

import javax.swing.JScrollBar;
import java.awt.Component;
import java.awt.Scrollbar;
import java.awt.peer.ScrollbarPeer;

/**
 * AWT scrollbar peer implemented as a {@link javax.swing.JScrollBar}.
 */

final class SwingScrollbarPeer extends SwingComponentPeer implements
		ScrollbarPeer {

	//
	// Construction
	//

	public SwingScrollbarPeer(SwingToolkit toolkit, Scrollbar sb) {
		super(toolkit, sb, new SwingScrollbar(sb));
		SwingToolkit.add(sb, jComponent);
		SwingToolkit.copyAwtProperties(sb, jComponent);
		final SwingScrollbar jsb = (SwingScrollbar)jComponent;
		jsb.setOrientation(sb.getOrientation());
		jsb.setBlockIncrement(sb.getBlockIncrement());
		jsb.setUnitIncrement(sb.getUnitIncrement());
		setValues(sb.getValue(), sb.getVisibleAmount(), sb.getMinimum(), sb
				.getMaximum());
	}

	/**
	 * @see java.awt.peer.ScrollbarPeer#setLineIncrement(int)
	 */
	public void setLineIncrement(int inc) {
		((JScrollBar)jComponent).setUnitIncrement(inc);
	}

	/**
	 * @see java.awt.peer.ScrollbarPeer#setPageIncrement(int)
	 */
	public void setPageIncrement(int inc) {
		((JScrollBar)jComponent).setBlockIncrement(inc);
	}

	/**
	 * @see java.awt.peer.ScrollbarPeer#setValues(int, int, int, int)
	 */
	public void setValues(int value, int visible, int min, int max) {
		((JScrollBar)jComponent).setValues(value, visible, min, max);
	}

	private static class SwingScrollbar extends JScrollBar implements ISwingPeer {
		private final Scrollbar awtComponent;

		public SwingScrollbar(Scrollbar awtComponent) {
			this.awtComponent = awtComponent;
		}

		/**
		 * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
		 */
		public Component getAWTComponent() {
			return awtComponent;
		}
	}
}
