/* DefaultCaret.java -- 
   Copyright (C) 2002, 2004 Free Software Foundation, Inc.

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

package javax.swing.text;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;
import java.util.Vector;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;


public class DefaultCaret extends Rectangle implements Caret, FocusListener, MouseListener, MouseMotionListener
{
    Color color = new Color(0,0,0);
    JTextComponent parent;
    
    public void mouseDragged(java.awt.event.MouseEvent  evt)
    {
    }

    public void mouseMoved(java.awt.event.MouseEvent  evt)
    {
    }

    public void mouseClicked(java.awt.event.MouseEvent  evt)
    {
    }

    public void mouseEntered(java.awt.event.MouseEvent  evt)
    {
    }

    public void mouseExited(java.awt.event.MouseEvent  evt)
    {
    }

    public void mousePressed(java.awt.event.MouseEvent  evt)
    {
    }

    public void mouseReleased(java.awt.event.MouseEvent  evt)
    {
    }

    public void focusGained(java.awt.event.FocusEvent  evt)
    {
    }

    public void focusLost(java.awt.event.FocusEvent  evt)
    {
    }

    // caret methods:

    public void deinstall(JTextComponent c)
    {
	parent.removeFocusListener(this);
	parent.removeMouseListener(this);

	parent = null;    
    }
    public void install(JTextComponent c)
    {
	parent.addFocusListener(this);
	parent.addMouseListener(this);
	parent = c;
	repaint();
    }
    
    Point magic = null;
    public void setMagicCaretPosition(Point p)
    {	magic = p;    }
    public Point getMagicCaretPosition()
    {	return magic;    }

    
    int mark = 0;
    public int getMark()
    {	return mark;    }

    boolean vis_sel = true;
    public void setSelectionVisible(boolean v)
    {  vis_sel = v;  repaint();  }
    public boolean isSelectionVisible()
    {  return vis_sel;    }

    private void repaint()
    {	
	if (parent != null)
	    {
		parent.repaint();
	    }
    }

    public void paint(Graphics g)
    {
	g.setColor(color);
	g.drawLine(x,y,
		   x,y+height);
    }

    
    Vector changes = new Vector();
    public void addChangeListener(ChangeListener l)
    {	changes.addElement(l);    }
    public void removeChangeListener(ChangeListener l)
    {   changes.removeElement(l);    }


    int blink = 500;
    public int getBlinkRate()
    { return blink;    }
    public void setBlinkRate(int rate)
    { blink = rate;    }

    int dot = 0;
    public int getDot()
    {  return dot;     }
    public void moveDot(int dot)
    {   setDot(dot);    }
    public void setDot(int dot)
    {
	this.dot = dot;   
	repaint();
    }

    boolean vis = true;
    public boolean isVisible()
    {	return vis;    }
    public void setVisible(boolean v)
    {
	vis = v; 
	repaint();
    }
}
