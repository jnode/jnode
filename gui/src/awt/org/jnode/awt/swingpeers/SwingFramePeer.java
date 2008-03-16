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

import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.Rectangle;
import java.awt.peer.FramePeer;
import java.beans.PropertyVetoException;

final class SwingFrame extends SwingBaseWindow<Frame, SwingFrame> {

    public SwingFrame(Frame awtFrame, String title) {
        super(awtFrame, title);
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
        super(toolkit, target, new SwingFrame(target, target.getTitle()));
        setResizable(target.isResizable());
        peerComponent.setIconifiable(true);
        peerComponent.setMaximizable(true);
        peerComponent.setClosable(true);
        try {
            peerComponent.setIcon(target.getState() == Frame.ICONIFIED);
        } catch (PropertyVetoException x) {
        }
        setState(target.getState());
        peerComponent.setTitle(target.getTitle());
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
        int ret = Frame.NORMAL;
        if(peerComponent.isIcon())
            ret = ret | Frame.ICONIFIED;
        if(peerComponent.isMaximum())
            ret = ret | Frame.MAXIMIZED_BOTH;
        return ret;
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
                peerComponent
                        .setJMenuBar(((SwingMenuBarPeer) mb.getPeer()).jComponent);
                targetComponent.invalidate();
            }
        });
    }
    /**
     * @see java.awt.peer.FramePeer#setState(int)
     */
    public void setState(int state) {
        if((state & Frame.ICONIFIED) == Frame.ICONIFIED) {
            try {
                peerComponent.setIcon(true);
            } catch (PropertyVetoException x){
                log.warn(x);
            }
        }

        if((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            try {
                peerComponent.setMaximum(true);
            } catch (PropertyVetoException x){
                log.warn(x);
            }
        }

        if(state == Frame.NORMAL) {
            try {
                peerComponent.setMaximum(false);
                peerComponent.setIcon(false);
            } catch (PropertyVetoException x){
                log.warn(x);
            }
        }
    }

    public void setBoundsPrivate(int x, int y, int width, int height) {

    }


    //jnode openjdk
    public Rectangle getBoundsPrivate() {
        //TODO implement it
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
