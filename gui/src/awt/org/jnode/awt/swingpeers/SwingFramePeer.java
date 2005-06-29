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

import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.Rectangle;
import java.awt.peer.FramePeer;
import java.beans.PropertyVetoException;

final class SwingFrame extends SwingBaseWindow<Frame, SwingFrame> {

    public SwingFrame(Frame awtFrame) {
        super(awtFrame);
    }

}

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
final class SwingFramePeer extends SwingBaseWindowPeer<Frame, SwingFrame>
        implements FramePeer, ISwingContainerPeer {

    /**
     * Initialize this instance.
     */
    public SwingFramePeer(SwingToolkit toolkit, Frame target) {
        super(toolkit, target, new SwingFrame(target));
        setResizable(target.isResizable());
        jComponent.setIconifiable(true);
        jComponent.setMaximizable(true);
        jComponent.setClosable(true);
        try {
            jComponent.setIcon(target.getState() == Frame.ICONIFIED);
        } catch (PropertyVetoException x) {
        }
        setState(target.getState());
        jComponent.setTitle(target.getTitle());
        // frame.setIconImage(awtFrame.getIconImage());
        MenuBar mb = target.getMenuBar();
        if (mb != null) {
            setMenuBar(mb);
        }

        addToDesktop();
    }

    /**
     * @see java.awt.peer.FramePeer#getState()
     */
    public int getState() {
        return -1;
    }

    /**
     * @see java.awt.peer.FramePeer#setIconImage(java.awt.Image)
     */
    public void setIconImage(Image image) {
    }

    /**
     * @see java.awt.peer.FramePeer#setMaximizedBounds(java.awt.Rectangle)
     */
    public void setMaximizedBounds(Rectangle r) {
    }

    /**
     * @see java.awt.peer.FramePeer#setMenuBar(java.awt.MenuBar)
     */
    @SuppressWarnings("deprecation")
    public void setMenuBar(final MenuBar mb) {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                mb.addNotify();
                jComponent
                        .setJMenuBar(((SwingMenuBarPeer) mb.getPeer()).jComponent);
                target.invalidate();
            }
        });
    }
    /**
     * @see java.awt.peer.FramePeer#setState(int)
     */
    public void setState(int state) {
    }
}
