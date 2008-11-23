/*
 * Copyright 1998-2001 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.swing.plaf.metal;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicSeparatorUI;


/**
 * A Metal L&F implementation of SeparatorUI.  This implementation 
 * is a "combined" view/controller.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @author Jeff Shapiro
 */

public class MetalSeparatorUI extends BasicSeparatorUI
{
    public static ComponentUI createUI( JComponent c )
    {
        return new MetalSeparatorUI();
    }

    protected void installDefaults( JSeparator s )
    {
        LookAndFeel.installColors( s, "Separator.background", "Separator.foreground" );
    }

    public void paint( Graphics g, JComponent c )
    {
        Dimension s = c.getSize();

	if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL )
	{
	  g.setColor( c.getForeground() );
	  g.drawLine( 0, 0, 0, s.height );

	  g.setColor( c.getBackground() );
	  g.drawLine( 1, 0, 1, s.height );
	}
	else  // HORIZONTAL
	{
	  g.setColor( c.getForeground() );
	  g.drawLine( 0, 0, s.width, 0 );

	  g.setColor( c.getBackground() );
	  g.drawLine( 0, 1, s.width, 1 );
	}
    }

    public Dimension getPreferredSize( JComponent c )
    { 
	if ( ((JSeparator)c).getOrientation() == JSeparator.VERTICAL )
	    return new Dimension( 2, 0 );
	else
	    return new Dimension( 0, 2 );
    }
}
