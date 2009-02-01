/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.desktop;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class WindowBar extends JPanel {

    private static final Logger log = Logger.getLogger(WindowBar.class);

    private final JList list;
    private final DefaultListModel model;
    private final Map<JInternalFrame, FrameWrapper> wrappers;

    public WindowBar() {
        this.list = new JList();
        this.model = new DefaultListModel();
        this.wrappers = new HashMap<JInternalFrame, FrameWrapper>();
        list.setModel(model);
        list.addListSelectionListener(new SelectionListener());

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
                revalidate();
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
                    revalidate();
                    repaint();
                }
            });
        }
    }

    public void setActiveFrame(JInternalFrame frame) {
        FrameWrapper w = wrappers.get(frame);
        if (w != null) {
            list.setSelectedValue(w, true);
        }

    }

    private class SelectionListener implements ListSelectionListener {

        /**
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent event) {
            final FrameWrapper sel = (FrameWrapper) list.getSelectedValue();
            if ((sel != null) && !sel.getFrame().isSelected()) {
                final JInternalFrame frame = sel.getFrame();
                final JDesktopPane desktop = frame.getDesktopPane();
                desktop.setSelectedFrame(frame);
            }
        }
    }

    private static class FrameWrapper {
        private final JInternalFrame frame;

        /**
         * @param frame
         */
        public FrameWrapper(JInternalFrame frame) {
            this.frame = frame;
        }

        public final JInternalFrame getFrame() {
            return frame;
        }

        public String toString() {
            return frame.getTitle();
        }
    }
}
