/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Component;
import java.awt.Scrollbar;
import java.awt.peer.ScrollbarPeer;

import javax.swing.JScrollBar;

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