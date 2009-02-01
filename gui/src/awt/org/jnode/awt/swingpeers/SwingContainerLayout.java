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
 
package org.jnode.awt.swingpeers;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class SwingContainerLayout implements LayoutManager {

    private final Container awtContainer;
    private final SwingContainerPeer<?, ?> containerPeer;
    private boolean layoutBusy = false;

    public SwingContainerLayout(Container awtContainer, SwingContainerPeer containerPeer) {
        this.awtContainer = awtContainer;
        this.containerPeer = containerPeer;
    }

    /**
     * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String,
     *      java.awt.Component)
     */
    public void addLayoutComponent(String name, Component component) {
    }

    /**
     * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
     */
    public void layoutContainer(Container parent) {
        if (!layoutBusy) {
            layoutBusy = true;
            try {
                doLayout(parent);
            } finally {
                layoutBusy = false;
            }
        }
    }

    private final void doLayout(Container parent) {
        awtContainer.doLayout();

        final int cnt = parent.getComponentCount();
        final Insets insets = containerPeer.getInsets();
        final int dx = insets.left;
        final int dy = insets.top;
        for (int i = 0; i < cnt; i++) {
            final Component child = parent.getComponent(i);
            if (child instanceof ISwingPeer) {
                final Component awtComp = ((ISwingPeer<?>) child)
                    .getAWTComponent();
                child.setLocation(awtComp.getX() - dx, awtComp.getY() - dy);
            }
        }
    }

    /**
     * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
     */
    public Dimension minimumLayoutSize(Container parent) {
        return parent.getSize();
    }

    /**
     * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
     */
    public Dimension preferredLayoutSize(Container parent) {
        return parent.getSize();
    }

    /**
     * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
     */
    public void removeLayoutComponent(Component component) {
    }

}
