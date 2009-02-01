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

import java.awt.Dialog;
import java.awt.Window;
import java.awt.peer.DialogPeer;
import java.util.List;

/**
 * AWT dialog peer implemented as a {@link javax.swing.JInternalFrame}.
 *
 * @author Levente S\u00e1ntha
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class SwingDialogPeer extends SwingBaseWindowPeer<Dialog, SwingDialog>
    implements DialogPeer, ISwingContainerPeer {

    public SwingDialogPeer(SwingToolkit toolkit, Dialog target) {
        super(toolkit, target, new SwingDialog(target, target.getTitle()));
        setTitle(target.getTitle());
        setResizable(target.isResizable());
        peerComponent.setIconifiable(false);
        peerComponent.setMaximizable(true);
        peerComponent.setClosable(true);
        peerComponent.setTitle(target.getTitle());
        addToDesktop();
    }

    /**
     * @see org.jnode.awt.swingpeers.SwingBaseWindowPeer#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
        if (!targetComponent.isUndecorated()) {
            super.setTitle(title);
        }
    }

    public void blockWindows(List<Window> windows) {
        //TODO implement it
    }
}

final class SwingDialog extends SwingBaseWindow<Dialog, SwingDialog> {

    public SwingDialog(Dialog target, String title) {
        super(target, title);
    }
}

