/*
 * $Id$
 *
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.VMAwtAPI;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import org.apache.log4j.Logger;
import org.jnode.awt.JNodeAwtContext;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class DesktopFrame extends JFrame implements JNodeAwtContext {
    private static final String DESKTOP_BACKGROUND = "desktop.background";
    private static final Color DESKTOP_BACKGROUND_COLOR = new Color(110, 190, 235);
    private final JDesktopPane desktop;
    private static final Logger log = Logger.getLogger(DesktopFrame.class);
    private BufferedImage backgroundImage;

    /**
     * @see java.awt.Component#repaint(long, int, int, int, int)
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        // TODO Auto-generated method stub
        //log.info("repaint (" + tm + ", " + x + ", " + y + ", " + width + ", "
        //      + height);
        super.repaint(tm, x, y, width, height);
    }

    /**
     * Initialize this instance.
     *
     * @param screenSize the desktop size
     */
    public DesktopFrame(Dimension screenSize) {
        super("");
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        setSize(screenSize);
        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new DefaultFocusTraversalPolicy());
        desktop = new JDesktopPane() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    Dimension ds = desktop.getSize();
                    /* cnetered
                    int iw = backgroundImage.getWidth();
                    int ih = backgroundImage.getHeight();
                    g.drawImage(backgroundImage, (ds.width - iw) / 2, (ds.height - ih) / 2, desktop);
                    */

                    // bottom right
                    int iw = backgroundImage.getWidth();
                    int ih = backgroundImage.getHeight();
                    g.drawImage(backgroundImage, ds.width - iw - 30, ds.height - ih - 20, desktop);                    
                }
            }
        };
        setBgColor();
        getContentPane().add(desktop);
    }

    private void setBgColor() {
        Preferences prefs = Preferences.userNodeForPackage(DesktopFrame.class);
        int color = prefs.getInt(DESKTOP_BACKGROUND, DESKTOP_BACKGROUND_COLOR.getRGB());
        desktop.setBackground(new Color(color));
    }

    private void saveBgColor() {
        Color color = desktop.getBackground();
        if (color != null) {
            Preferences prefs = Preferences.userNodeForPackage(DesktopFrame.class);
            prefs.putInt(DESKTOP_BACKGROUND, color.getRGB());
        }
    }

    /**
     * @return Returns the desktop.
     */
    public final JDesktopPane getDesktop() {
        return desktop;
    }

    public final JComponent getAwtRoot() {
        return (JComponent) getContentPane();
    }

    public void adjustDesktopSize(int width, int height) {
        setSize(width, height);
        VMAwtAPI.invalidateTree(this);
        validateTree();
    }

    /**
     * @see javax.swing.JFrame#frameInit()
     */
    protected void frameInit() {
        super.setLayout(new BorderLayout());
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        setRootPane(createRootPane());
    }

    public void dispose() {
        for (JInternalFrame f : desktop.getAllFrames()) {
            try {
                if (f instanceof SwingBaseWindow) {
                    ((SwingBaseWindow) f).target.dispose();
                } else {
                    f.setClosed(true);
                }
            } catch (PropertyVetoException e) {
                log.warn("Failed closing frame: " + f.getTitle(), e);
            }
        }
        saveBgColor();
        super.dispose();
    }

    public void setBackgroundImage(BufferedImage backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public Component getTopLevelRootComponent() {
        return this;
    }

    protected void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);
    }
}
