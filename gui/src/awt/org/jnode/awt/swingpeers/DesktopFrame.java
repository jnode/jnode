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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;

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
    private static final Color DESKTOP_BACKGROUND_COLOR = new Color(70, 130, 180);
	private final JDesktopPane desktop;
	private static final Logger log = Logger.getLogger(DesktopFrame.class);
	
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
	 */
	public DesktopFrame(Dimension screenSize) {
		super("");
		setSize(screenSize);
        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new DefaultFocusTraversalPolicy());
        desktop = new JDesktopPane();
        desktop.setBackground(DESKTOP_BACKGROUND_COLOR);
        getContentPane().add(desktop);
	}

    /**
	 * @return Returns the desktop.
	 */
	public final JDesktopPane getDesktop() {
		return desktop;
	}
	
	public final JComponent getAwtRoot() {
		return (JComponent)getContentPane();
	}

    public void adjustDesktopSize(int width, int height) {
        setSize(width, height);
    }

    /**
	 * @see javax.swing.JFrame#frameInit()
	 */
	protected void frameInit() {
		super.setLayout(new BorderLayout());
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		getRootPane(); // will do set/create
	}

    public void dispose() {
        for(JInternalFrame f : desktop.getAllFrames()){
            try{
                if(f instanceof SwingBaseWindow){
                    ((SwingBaseWindow)f).target.dispose();
                } else {
                    f.setClosed(true);
                }
            } catch(PropertyVetoException e){
                log.warn("Failed closing frame: " + f.getTitle(), e);
            }
        }
        super.dispose();
    }
}
