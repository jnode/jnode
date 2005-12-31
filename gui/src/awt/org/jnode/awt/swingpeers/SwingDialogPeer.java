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

import java.awt.Dialog;
import java.awt.peer.DialogPeer;

/**
 * AWT dialog peer implemented as a {@link javax.swing.JInternalFrame}.
 * @author Levente S\u00e1ntha
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class SwingDialogPeer extends SwingBaseWindowPeer<Dialog, SwingDialog>
        implements DialogPeer, ISwingContainerPeer {

    public SwingDialogPeer(SwingToolkit toolkit, Dialog target) {
        super(toolkit, target, new SwingDialog(target));
        setTitle(target.getTitle());
        setResizable(target.isResizable());
        peerComponent.setIconifiable(true);
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
}

final class SwingDialog extends SwingBaseWindow<Dialog, SwingDialog> {
    
    public SwingDialog(Dialog target) {
        super(target);
    }
}

