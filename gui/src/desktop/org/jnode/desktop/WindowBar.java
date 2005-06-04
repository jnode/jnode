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

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class WindowBar extends JPanel {

	private final static Logger log = Logger.getLogger(WindowBar.class);

	private final JList list;
    private final DefaultListModel model;
    private final Map<JInternalFrame, FrameWrapper> wrappers;

	public WindowBar() {
		this.list = new JList();
        this.model = new DefaultListModel();
        this.wrappers = new HashMap<JInternalFrame, FrameWrapper>();
		list.setModel(model);

        setLayout(new GridLayout(1, 1));
        list.setBorder(new BevelBorder(BevelBorder.LOWERED));
		add(list);
	}

	public void addFrame(final JInternalFrame frame) {
		log.debug("addFrame " + frame.getTitle());
        final FrameWrapper wrapper = new FrameWrapper(frame);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                wrappers.put(frame, wrapper);
                model.addElement(wrapper);
                repaint();
            }            
        });
	}

    public void removeFrame(final JInternalFrame frame) {
		log.debug("removeFrame " + frame.getTitle());
        final FrameWrapper wrapper = wrappers.get(frame);
        if (wrapper != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    model.removeElement(wrapper);
                    wrappers.remove(frame);
                    repaint();
                }            
            });
        }
	}

	public void setActiveFrame(JInternalFrame frame) {
		log.debug("setActiveFrame " + frame.getTitle());
	}
    
    private static class FrameWrapper {
        private final JInternalFrame frame;

        /**
         * @param frame
         */
        public FrameWrapper(JInternalFrame frame) {
            this.frame = frame;
        }
        
        public String toString() {
            return frame.getTitle();
        }
    }
}
