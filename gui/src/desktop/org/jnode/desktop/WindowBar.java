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
 
package org.jnode.desktop;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class WindowBar extends JPanel {

	private final static Logger log = Logger.getLogger(WindowBar.class);

	private final JList list;

	private final ArrayList<JInternalFrame> frames;

	public WindowBar() {
		this.list = new JList();
		this.frames = new ArrayList<JInternalFrame>();
		list.setModel(new WindowListModel());

		add(list, BorderLayout.CENTER);
	}

	public void addFrame(JInternalFrame frame) {
		log.debug("addFrame " + frame.getTitle());
		frames.add(frame);
		revalidate();
		forceRepaint();
	}

	public void removeFrame(JInternalFrame frame) {
		log.debug("removeFrame " + frame.getTitle());
		frames.remove(frame);
		revalidate();
		forceRepaint();
	}

	public void setActiveFrame(JInternalFrame frame) {
		log.debug("setActiveFrame " + frame.getTitle());
	}

	private void forceRepaint() {
		final Container p = getParent();
		if (p != null) {
			p.repaint();
		}
	}

	private class WindowListModel extends AbstractListModel {

		/**
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		public Object getElementAt(int index) {
			final JInternalFrame frame = (JInternalFrame)frames.get(index);
			return frame.getTitle();
		}

		/**
		 * @see javax.swing.ListModel#getSize()
		 */
		public int getSize() {
			return frames.size();
		}
	}
}
