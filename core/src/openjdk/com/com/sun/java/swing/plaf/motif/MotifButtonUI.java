/*
 * Copyright 1997-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
 
package com.sun.java.swing.plaf.motif;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.*;

/**
 * MotifButton implementation
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 *
 * @author Rich Schiavi
 */
public class MotifButtonUI extends BasicButtonUI {

    private final static MotifButtonUI motifButtonUI = new MotifButtonUI();

    protected Color selectColor; 

    private boolean defaults_initialized = false;
    
    // ********************************
    //          Create PLAF
    // ********************************
    public static ComponentUI createUI(JComponent c){
	return motifButtonUI;
    }
    
    // ********************************
    //         Create Listeners
    // ********************************
    protected BasicButtonListener createButtonListener(AbstractButton b){
	return new MotifButtonListener(b);
    }

    // ********************************
    //          Install Defaults
    // ********************************
    public void installDefaults(AbstractButton b) {
	super.installDefaults(b);
	if(!defaults_initialized) {
	    selectColor = UIManager.getColor(getPropertyPrefix() + "select");
	    defaults_initialized = true;
	}
        LookAndFeel.installProperty(b, "opaque", Boolean.FALSE);
    }

    protected void uninstallDefaults(AbstractButton b) {
	super.uninstallDefaults(b);
	defaults_initialized = false;
    }
    
    // ********************************
    //          Default Accessors
    // ********************************

    protected Color getSelectColor() {
	return selectColor;
    }
    
    // ********************************
    //          Paint Methods
    // ********************************
    public void paint(Graphics g, JComponent c) {
        fillContentArea( g, (AbstractButton)c , c.getBackground() );   
	super.paint(g,c);
    }

    // Overridden to ensure we don't paint icon over button borders.
    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
        Shape oldClip = g.getClip();
        Rectangle newClip =
            AbstractBorder.getInteriorRectangle(c, c.getBorder(), 0, 0,
                                                c.getWidth(), c.getHeight());

        Rectangle r = oldClip.getBounds();
        newClip =
            SwingUtilities.computeIntersection(r.x, r.y, r.width, r.height,
                                               newClip);
        g.setClip(newClip);
        super.paintIcon(g, c, iconRect);
        g.setClip(oldClip);
    }

    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect){
	// focus painting is handled by the border
    }
    
    protected void paintButtonPressed(Graphics g, AbstractButton b) {

        fillContentArea( g, b , selectColor );

    }

    protected void fillContentArea( Graphics g, AbstractButton b, Color fillColor) {

        if (b.isContentAreaFilled()) {
	    Insets margin = b.getMargin();
	    Insets insets = b.getInsets();
	    Dimension size = b.getSize();
	    g.setColor(fillColor);
	    g.fillRect(insets.left - margin.left,
		       insets.top - margin.top, 
		       size.width - (insets.left-margin.left) - (insets.right - margin.right),
		       size.height - (insets.top-margin.top) - (insets.bottom - margin.bottom));
	}
    }
}
