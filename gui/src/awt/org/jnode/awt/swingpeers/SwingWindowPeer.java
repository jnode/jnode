/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.peer.WindowPeer;

import javax.swing.JComponent;

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
    }

    /**
     * @see javax.swing.JComponent#updateUI()
     */
    public void updateUI() {
        setMinimumSize(null);
        setBorder(null);
        super.updateUI();
        setMinimumSize(null);

        final JComponent rootpane = getRootPane();
        setBorder(SwingToolkit.EMPTY_BORDER);
        removeAll();
        setLayout(new BorderLayout());
        add(rootpane, BorderLayout.CENTER);
        getLayout().layoutContainer(this);

        this.setOpaque(false);
        rootpane.setOpaque(false);
        getLayeredPane().setOpaque(false);
    }
}
