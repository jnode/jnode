/* BasicCheckBoxUI.java
   Copyright (C) 2002 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.plaf.*;
import java.awt.*;


public class BasicCheckBoxUI extends BasicRadioButtonUI
{  
    public static ComponentUI createUI(final JComponent c)  {
	return new BasicCheckBoxUI();
    }

    
    public void installUI(final JComponent c)  {
	super.installUI(c);
    }
    
    public Dimension getPreferredSize(JComponent c) 
    {
	AbstractButton b = (AbstractButton)c;
	Dimension d = BasicGraphicsUtils.getPreferredSize(b, 
							  gap,
							  b.getText(),
							  b.getIcon(),
							  b.getVerticalAlignment(),
							  b.getHorizontalAlignment(),
							  b.getHorizontalTextPosition(),
							  b.getVerticalTextPosition());
							  
	//System.out.println("^^^^^^^^^^^^^^^^^^^^^^   BASIC-PREF="+d + ",T="+b.text);
	return d;
    }
    
    protected void paintFocus(Graphics g, 
			      JComponent c,
			      Rectangle vr,
			      Rectangle tr,
			      Rectangle ir)
    {
    }

    protected void paintIcon(Graphics g, 
			     JComponent c, 
			     Rectangle iconRect)
    {
    }

    protected void paintButtonPressed(Graphics g,
				      JComponent b)
    {
	Dimension size = b.getSize();
	
	g.setColor(pressedBackgroundColor);
	g.fillRect(1,1,size.width-2, size.height-2);                

    }
    
    protected void paintButtonNormal(Graphics g,
				     JComponent b)
    {
	Dimension size = b.getSize();
	
	g.setColor(normalBackgroundColor);
	g.fillRect(1,1,size.width-2, size.height-2);                

    }
    protected void paintText(Graphics g,
			     JComponent c,
			     Rectangle textRect,
			     String text) 
    {
	//        AbstractButton b = (AbstractButton) c;
	
	//	System.out.println("drawing string: " + text + ", at:" + textRect);
	
	g.setColor(textColor);
	BasicGraphicsUtils.drawString(g,
				      text, 
				      0,
				      textRect.x, 
				      textRect.y);
    } 
}




