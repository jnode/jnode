/*
 * $Id$
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

	private final ArrayList frames;

	public WindowBar() {
		this.list = new JList();
		this.frames = new ArrayList();
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
