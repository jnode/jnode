/* BasicButtonUI.java
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

package javax.swing.plaf.basic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;

public class BasicButtonUI extends ButtonUI
{
  /** A constant used to pad out elements in the button's layout and
      preferred size calculations. */
  int defaultTextIconGap = 3;

  /** A constant added to the defaultTextIconGap to adjust the text
      within this particular button. */
  int defaultTextShiftOffset = 0;

  /**
   * Factory method to create an instance of BasicButtonUI for a given
   * {@link JComponent}, which should be an {@link AbstractButton}.
   *
   * @param c The component to create a UI got
   *
   * @return A new UI capable of drawing the component
   */
  public static ComponentUI createUI(final JComponent c) 
  {
    return new BasicButtonUI();
  }

  public int getDefaultTextIconGap(AbstractButton b)
  {
    return defaultTextIconGap;
  }

  protected void installDefaults(AbstractButton b)
  {
    UIDefaults defaults = UIManager.getLookAndFeelDefaults();
    b.setForeground(defaults.getColor("Button.foreground"));
    b.setBackground(defaults.getColor("Button.background"));
    b.setMargin(defaults.getInsets("Button.margin"));
    b.setBorder(defaults.getBorder("Button.border"));
    b.setOpaque(true);
  }

  protected void uninstallDefaults(AbstractButton b)
  {
    b.setForeground(null);
    b.setBackground(null);
    b.setBorder(null);
    b.setMargin(null);
  }

  protected BasicButtonListener listener;

  protected BasicButtonListener createButtonListener(AbstractButton b)
  {
    return new BasicButtonListener();
  }

  public void installListeners(AbstractButton b)
  {
    listener = createButtonListener(b);
    b.addChangeListener(listener);
    b.addPropertyChangeListener(listener);
    b.addFocusListener(listener);    
    b.addMouseListener(listener);
    b.addMouseMotionListener(listener);
  }

  public void uninstallListeners(AbstractButton b)
  {
    b.removeChangeListener(listener);
    b.removePropertyChangeListener(listener);
    b.removeFocusListener(listener);    
    b.removeMouseListener(listener);
    b.removeMouseMotionListener(listener);
  }

  protected void installKeyboardActions(AbstractButton b)
  {
  }

  protected void uninstallKeyboardActions(AbstractButton b)
  {
  }

  /**
   * Install the BasicButtonUI as the UI for a particular component.
   * This means registering all the UI's listeners with the component,
   * and setting any properties of the button which are particular to 
   * this look and feel.
   *
   * @param c The component to install the UI into
   */
  public void installUI(final JComponent c) 
  {
    super.installUI(c);
    if (c instanceof AbstractButton)
      {
        AbstractButton b = (AbstractButton) c;
        installDefaults(b);
        installListeners(b);
        installKeyboardActions(b);
      }
  }

  /**
   * Calculate the preferred size of this component, by delegating to
   * {@link BasicGraphicsUtils.getPreferredButtonSize}.
   *
   * @param c The component to measure
   *
   * @return The preferred dimensions of the component
   */
  public Dimension getPreferredSize(JComponent c) 
  {
    AbstractButton b = (AbstractButton)c;
    Dimension d = 
      BasicGraphicsUtils.getPreferredButtonSize
      (b, defaultTextIconGap + defaultTextShiftOffset);
    return d;
  }

  static private Icon currentIcon(AbstractButton b)
  {
    Icon i = b.getIcon();
    ButtonModel model = b.getModel();

    if (model.isPressed() && b.getPressedIcon() != null)
      i = b.getPressedIcon();

    else if (model.isRollover())
      {
        if (b.isSelected() && b.getRolloverSelectedIcon() != null)
          i = b.getRolloverSelectedIcon();
        else if (b.getRolloverIcon() != null)
          i = b.getRolloverIcon();
      }    

    else if (b.isSelected())
      {
        if (b.isEnabled() && b.getSelectedIcon() != null)
          i = b.getSelectedIcon();
        else if (b.getDisabledSelectedIcon() != null)
          i = b.getDisabledSelectedIcon();
      }

    else if (! b.isEnabled() && b.getDisabledIcon() != null)
      i = b.getDisabledIcon();

    return i;
  }

  /**
   * Paint the component, which is an {@link AbstractButton}, according to 
   * its current state.
   *
   * @param g The graphics context to paint with
   * @param c The component to paint the state of
   */
  public void paint(Graphics g, JComponent c)
  {      
    AbstractButton b = (AbstractButton) c;

    Rectangle tr = new Rectangle();
    Rectangle ir = new Rectangle();
    Rectangle vr = new Rectangle();
    Rectangle br = new Rectangle();

    Font f = c.getFont();

    g.setFont(f);

    SwingUtilities.calculateInnerArea(b, br);
    SwingUtilities.calculateInsetArea(br, b.getMargin(), vr);    
    String text = SwingUtilities.layoutCompoundLabel(c, g.getFontMetrics(f), 
                                                     b.getText(),
                                                     currentIcon(b),
                                                     b.getVerticalAlignment(), 
                                                     b.getHorizontalAlignment(),
                                                     b.getVerticalTextPosition(), 
                                                     b.getHorizontalTextPosition(),
                                                     vr, ir, tr, 
                                                     defaultTextIconGap 
                                                     + defaultTextShiftOffset);
    
    if ((b.getModel().isArmed() && b.getModel().isPressed()) 
        || b.isSelected())
      paintButtonPressed(g, br, c);
    else
      paintButtonNormal(g, br, c);
	
    paintIcon(g, c, ir);
    if (text != null)
      paintText(g, c, tr, b.getText());
    paintFocus(g, c, vr, tr, ir);
  }

  /**
   * Paint any focus decoration this {@link JComponent} might have.  The
   * component, which in this case will be an {@link AbstractButton},
   * should only have focus decoration painted if it has the focus, and its
   * "focusPainted" property is <code>true</code>.
   *
   * @param g Graphics context to paint with
   * @param c Component to paint the focus of
   * @param vr Visible rectangle, the area in which to paint
   * @param tr Text rectangle, contained in visible rectangle
   * @param ir Icon rectangle, contained in visible rectangle
   *
   * @see AbstractButton.isFocusPainted()
   * @see JComponent.hasFocus()
   */
  protected void paintFocus(Graphics g, JComponent c, Rectangle vr,
                            Rectangle tr, Rectangle ir)
  {
    AbstractButton b = (AbstractButton) c;
    if (b.hasFocus() && b.isFocusPainted())
      {
        Graphics2D g2 = (Graphics2D) g;
        Stroke saved_stroke = g2.getStroke();
        Color saved_color = g2.getColor();
        float dashes[] = new float[] {1.0f, 1.0f};        
        BasicStroke s = new BasicStroke(1.0f, 
                                        BasicStroke.CAP_SQUARE, 
                                        BasicStroke.JOIN_MITER,
                                        10, dashes, 0.0f);
        g2.setStroke(s);
        g2.setColor(Color.BLACK);
        g2.drawRect(vr.x + 2, 
                    vr.y + 2, 
                    vr.width - 4,
                    vr.height - 4);
        g2.setStroke(saved_stroke);
        g2.setColor(saved_color);
      }
  }

  /**
   * Paint the icon for this component. Depending on the state of the
   * component and the availability of the button's various icon
   * properties, this might mean painting one of several different icons.
   *
   * @param g Graphics context to paint with
   * @param c Component to paint the icon of
   * @param iconRect Rectangle in which the icon should be painted
   */
  protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect)
  {
    AbstractButton b = (AbstractButton) c;
    Icon i = currentIcon(b);

    if (i != null)
      {
        int x = iconRect.x;
        int y = iconRect.y;
        i.paintIcon(c, g, x, y);
      }
  }

  /**
   * Paints the background area of an {@link AbstractButton} in the pressed
   * state.  This means filling the supplied area with the {@link
   * pressedBackgroundColor}.
   *
   * @param g The graphics context to paint with
   * @param area The area in which to paint
   * @param b The component to paint the state of
   */
  protected void paintButtonPressed(Graphics g, Rectangle area, JComponent b)
  {
    if (((AbstractButton)b).isContentAreaFilled())
      {
        g.setColor(b.getBackground().darker());
        g.fillRect(area.x, area.y, area.width, area.height);
      }
  }
    
  /**
   * Paints the background area of an {@link AbstractButton} in the normal,
   * non-pressed state.  This means filling the supplied area with the
   * {@link normalBackgroundColor}.
   *
   * @param g The graphics context to paint with
   * @param area The area in which to paint
   * @param b The component to paint the state of
   */
  protected void paintButtonNormal(Graphics g, Rectangle area, JComponent b)
  {
    if (((AbstractButton)b).isContentAreaFilled())
      {
        g.setColor(b.getBackground());
        g.fillRect(area.x, area.y, area.width, area.height);
      }
  }
    
  /**
   * Paints the "text" property of an {@link AbstractButton}, using the
   * {@link textColor} color.
   *
   * @param g The graphics context to paint with
   * @param c The component to paint the state of
   * @param textRect The area in which to paint the text
   * @param text The text to paint
   */
  protected void paintText(Graphics g, JComponent c, Rectangle textRect,
                           String text) 
  {	
    Font f = c.getFont();
    g.setFont(f);
    FontMetrics fm = g.getFontMetrics(f);
    g.setColor(c.getForeground());
    BasicGraphicsUtils.drawString(g, text, 0,
                                  textRect.x, 
                                  textRect.y + fm.getAscent());
  } 
}
