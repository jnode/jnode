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

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeAwtContext;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class SwingRepaintManager extends RepaintManager {

    private static final Logger log = Logger.getLogger(SwingRepaintManager.class);

    /** The AWT context */
    private final JNodeAwtContext context;
    
    /** Should we stop */
    private boolean shutdown = false;
    
    /** The actual dirty region (on awtRoot coordinate space) */
    private Rectangle dirtyRegion = new Rectangle(0, 0, 0, 0);
    
    /**
     * Initialize this instance.
     * @param context
     */
    public SwingRepaintManager(JNodeAwtContext context) {
        this.context = context;
    }
    
    /** 
     * Stop this repaint manager.
     */
    final void shutdown() {
        this.shutdown = true;
        log.debug("shutdown");
    }
    
    /**
     * @see javax.swing.RepaintManager#addDirtyRegion(javax.swing.JComponent, int, int, int, int)
     */
    public void addDirtyRegion(JComponent component, int x, int y, int w, int h) {
        if ((!shutdown) && (w > 0) && (h > 0)) {
            checkThread();
            final JComponent root = context.getAwtRoot();
            if (root != null) {
                final Point p = SwingUtilities.convertPoint(component, x, y, root);
                dirtyRegion.add(p);
                dirtyRegion.add(p.x + w, p.y + h);
            } else {
                dirtyRegion.add(x, y);
                dirtyRegion.add(x + w, y + h);
            }
            super.addDirtyRegion(component, x, y, w, h);
        }
    }

    /**
     * @see javax.swing.RepaintManager#addInvalidComponent(javax.swing.JComponent)
     */
    public void addInvalidComponent(JComponent component) {
        if (!shutdown) {
            checkThread();
            super.addInvalidComponent(component);
        }
    }

    /**
     * @see javax.swing.RepaintManager#paintDirtyRegions()
     */
    public void paintDirtyRegions() {
        if ((!shutdown) && !dirtyRegion.isEmpty()) {
            final JComponent root = context.getAwtRoot();
            if (root != null) {
                Rectangle r = new Rectangle(dirtyRegion);
                dirtyRegion.setRect(0, 0, 0, 0);
                root.paintImmediately(r);
            }
        }
    }

    /**
     * @see javax.swing.RepaintManager#validateInvalidComponents()
     */
    public void validateInvalidComponents() {
        if (!shutdown) {
            super.validateInvalidComponents();
        }
    }

    /**
     * @see javax.swing.RepaintManager#setDoubleBufferingEnabled(boolean)
     */
    public void setDoubleBufferingEnabled(boolean buffer) {
//        log.info("setDoubleBufferingEnabled " + buffer);
        super.setDoubleBufferingEnabled(buffer);
    }

    private void checkThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            log.debug("Wrong Thread: " + Thread.currentThread(), new Exception("Stacktrace"));
        }
    }
}
