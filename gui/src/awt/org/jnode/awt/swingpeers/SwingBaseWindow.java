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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.beans.PropertyVetoException;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

/**
 * Base class for peer implementation that subclass {@link java.awt.Window}.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingBaseWindow<awtT extends Window, swingPeerT extends SwingBaseWindow<awtT, swingPeerT>>
    extends JInternalFrame implements ISwingPeer<awtT> {

    /**
     * The AWT component this is a peer for
     */
    protected final awtT target;

    /**
     * The swing peer implementation
     */
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
        super.reshape(x, y, width, height);
        target.reshape(x, y, width, height);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (target instanceof RootPaneContainer && isVisible()) {
            //target.paint(g.create());
            swingPeer.postPaintEvent();
            //JNodeToolkit.postToTarget(new PaintEvent(target, PaintEvent.UPDATE, target.getBounds()), target);
        }
    }

    @Override
    public void setMaximum(boolean b) throws PropertyVetoException {
        super.setMaximum(b);
        target.setBounds(this.getBounds());
    }

    @Override
    protected void validateTree() {
        super.validateTree();
        if (target instanceof JFrame)
            ((JFrame) target).getRootPane().validate();
        else if (target instanceof JDialog)
            ((JDialog) target).getRootPane().validate();
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
        AppContext ac = SunToolkit.targetToAppContext(target);
        if (ac == null) {
            target.dispatchEvent(SwingToolkit.convertEvent(event, target));
        } else {
            EventQueue eq = (EventQueue) ac.get(AppContext.EVENT_QUEUE_KEY);
            if (eq == null) {
                target.dispatchEvent(SwingToolkit.convertEvent(event, target));
            } else {
                eq.postEvent(SwingToolkit.convertEvent(event, target));
            }
        }
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
    protected JRootPane createRootPane() {
        return new NoContentRootPane();
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
        super.update(g);
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
        if (target != null)
            SwingUtilities.updateComponentTreeUI(target);
    }

    final class NullContentPane extends JComponent {
        @Override
        public void update(Graphics g) {
            //org.jnode.vm.Unsafe.debug("NullContantPane.update()\n");
        }

        @Override
        public void paint(Graphics g) {
            if (target instanceof Frame && !(target instanceof JFrame)) {
                SwingBaseWindow sf = SwingBaseWindow.this;
                Frame f = (Frame) target;

                Color bg = f.getBackground();
                if (bg == null) bg = UIManager.getColor("window");
                if (bg == null) bg = UIManager.getColor("control");
                if (bg == null) bg = Color.GRAY;

                g.setColor(bg);
                g.fillRect(0, 0, getWidth(), getHeight());

                Point f_loc = sf.getLocationOnScreen();
                Point p_loc = this.getLocationOnScreen();

                int dx = p_loc.x - f_loc.x;
                int dy = p_loc.y - f_loc.y;


                for (Component c : f.getComponents()) {
                    Graphics cg = g.create(c.getX() - dx, c.getY() - dy, c.getWidth(), c.getHeight());
                    c.paintAll(cg);
                    cg.dispose();
                }
            }
        }

        @Override
        public void repaint() {

        }

        @Override
        public void repaint(long tm, int x, int y, int width, int height) {

        }
    }

    final class NoContentRootPane extends JRootPane {
        /**
         * @see javax.swing.JRootPane#createContentPane()
         */
        protected Container createContentPane() {
            return new NullContentPane();
        }
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
                Point p = target.isShowing() ? target.getLocationOnScreen() : new Point();
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
