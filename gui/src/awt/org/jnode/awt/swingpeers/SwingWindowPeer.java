/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.awt.swingpeers;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.peer.WindowPeer;
import javax.swing.JRootPane;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;

/**
 * AWT window peer implemented as a {@link javax.swing.JInternalFrame}.
 *
 * @author Levente S\u00e1ntha
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class SwingWindowPeer extends SwingBaseWindowPeer<Window, SwingWindow>
    implements WindowPeer {

    public SwingWindowPeer(SwingToolkit toolkit, Window window) {
        super(toolkit, window, new SwingWindow(window));
        addToDesktop();
    }
}

final class SwingWindow extends SwingBaseWindow<Window, SwingWindow> {

    public SwingWindow(Window target) {
        super(target);
        setResizable(false);
        setIconifiable(false);
        setMaximizable(false);
        setClosable(false);
        InternalFrameUI ui1 = getUI();
        if (ui1 instanceof BasicInternalFrameUI) {
            //removing upper decoration
            ((BasicInternalFrameUI) ui1).setNorthPane(null);
        } else {
            throw new RuntimeException("Unknown UI: " + ui1);
        }
    }

    /**
     * @see javax.swing.JComponent#updateUI()
     */
    public void updateUI() {
        setMinimumSize(null);
        setBorder(null);
        super.updateUI();
        setMinimumSize(null);

        final JRootPane rootpane = getRootPane();
        setBorder(SwingToolkit.EMPTY_BORDER);
        removeAll();
        setLayout(new BorderLayout());
        //add(rootpane, BorderLayout.CENTER);
        setRootPane(rootpane);
        getLayout().layoutContainer(this);

        this.setOpaque(false);
        rootpane.setOpaque(false);
        getLayeredPane().setOpaque(false);
    }
}
