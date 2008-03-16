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

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.VMAwtAPI;
import java.awt.Window;
import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Base class for peer implementation that subclass {@link java.awt.Window}.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingBaseWindow<awtT extends Window, swingPeerT extends SwingBaseWindow<awtT, swingPeerT>>
        extends JInternalFrame implements ISwingPeer<awtT> {

    /** The AWT component this is a peer for */
    protected final awtT target;

    /** The swing peer implementation */
    private SwingBaseWindowPeer<awtT, swingPeerT> swingPeer;

    public SwingBaseWindow(awtT target) {
        this.target = target;
    }

    /**
     * Initialize this instance.
     * 
     * @param target
     */
    public SwingBaseWindow(awtT target, String title) {
        super(title, true, true, true, true);
        this.target = target;
    }

    /**
     * @see javax.swing.JInternalFrame#reshape(int, int, int, int)
     */
    public void reshape(int x, int y, int width, int height) {
        VMAwtAPI.setBoundsCallback(target, x, y, width, height);
        VMAwtAPI.invalidateTree(target);
        super.reshape(x, y, width, height);
        validate();
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public final awtT getAWTComponent() {
        return target;
    }

    /**
     * Pass an event onto the AWT component.
     * 
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        target.dispatchEvent(SwingToolkit.convertEvent(event,
                target));
    }

    /**
     * Process an event within this swingpeer
     * 
     * @param event
     */
    public final void processAWTEvent(AWTEvent event) {
        super.processEvent(event);
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#validatePeerOnly()
     */
    public final void validatePeerOnly() {
        super.validate();
        doLayout();
        getRootPane().doLayout();
    }

    /**
     * @see javax.swing.JInternalFrame#createRootPane()
     */
    protected final JRootPane createRootPane() {
        return new RootPane();
    }

    /**
     * Gets the peer implementation.
     */
    final SwingBaseWindowPeer<awtT, swingPeerT> getSwingPeer() {
        return swingPeer;
    }

    final void initialize(SwingBaseWindowPeer<awtT, swingPeerT> swingPeer) {
        this.swingPeer = swingPeer;
        //((ContentPane) getContentPane()).initialize(target,swingPeer);
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    @Override
    public void update(Graphics g) {
        super.update(g);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * @see java.awt.Component#invalidate()
     */
    public final void invalidate() {
        super.invalidate();
        if (target != null) {
            target.invalidate();
        }
    }

    /**
     * @see java.awt.Component#validate()
     */
    public final void validate() {
        super.validate();
        if (target != null) {
            target.validate();
        }
    }

    public void updateUI() {
        super.updateUI();
        //todo review it
        //when the laf is changed starting from the peer component
        //we enforce the UI update of the target componet
        //this can be dangerous, find a better solution
        if(target != null)
            SwingUtilities.updateComponentTreeUI(target);
    }

    private final class ContentPane extends JComponent {

        private awtT target;

        private SwingBaseWindowPeer swingPeer;

        public void initialize(awtT target, SwingBaseWindowPeer<awtT, swingPeerT> swingPeer) {
            this.target = target;
            this.swingPeer = swingPeer;
            target.invalidate();
        }

        /**
         * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
         */
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            final Insets insets = swingPeer.getInsets();
            //SwingToolkit.paintLightWeightChildren(target, g, insets.left,insets.top);
        }

        @SuppressWarnings("deprecation")
        public void reshape(int x, int y, int width, int height) {
            super.reshape(x, y, width, height);
            if (!swingPeer.isReshapeInProgress) {
                Point p = target.isShowing() ? target.getLocationOnScreen()
                        : new Point();
                // Point p = awtFrame.getLocationOnScreen();
                Insets ins = swingPeer.getInsets();
                target.reshape(p.x + x, p.y, width + ins.left + ins.right,
                        height + ins.bottom + ins.top);
            }
        }
    }

    private final class RootPane extends JRootPane {

        /**
         * @see javax.swing.JRootPane#createContentPane()
         */
        protected Container createContentPane() {
            return super.createContentPane();
            /*
            ContentPane p = new ContentPane();
            p.setName(this.getName() + ".contentPane");
            p.setLayout(new BorderLayout());
            return p;
            */
        }
    }
}
