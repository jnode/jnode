/*
 * Copyright 2002-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import sun.swing.plaf.synth.SynthUI;

/**
 * Synth's LabelUI.
 *
 * @author Scott Violet
 */
class SynthLabelUI extends BasicLabelUI implements SynthUI {
    private SynthStyle style;

    /**
     * Returns the LabelUI implementation used for the skins look and feel.
     */
    public static ComponentUI createUI(JComponent c){
        return new SynthLabelUI();
    }


    protected void installDefaults(JLabel c) {
        updateStyle(c);
    }

    void updateStyle(JLabel c) {
        SynthContext context = getContext(c, ENABLED);
        style = SynthLookAndFeel.updateStyle(context, this);
        context.dispose();
    }

    protected void uninstallDefaults(JLabel c){
        SynthContext context = getContext(c, ENABLED);

        style.uninstallDefaults(context);
        context.dispose();
        style = null;
    }

    public SynthContext getContext(JComponent c) {
        return getContext(c, getComponentState(c));
    }

    private SynthContext getContext(JComponent c, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                    SynthLookAndFeel.getRegion(c), style, state);
    }

    private Region getRegion(JComponent c) {
        return SynthLookAndFeel.getRegion(c);
    }

    private int getComponentState(JComponent c) {
        int state = SynthLookAndFeel.getComponentState(c);
        if (SynthLookAndFeel.selectedUI == this &&
                        state == SynthConstants.ENABLED) {
            state = SynthLookAndFeel.selectedUIState | SynthConstants.ENABLED;
        }
        return state;
    }

    public int getBaseline(JComponent c, int width, int height) {
        if (c == null) {
            throw new NullPointerException("Component must be non-null");
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException(
                    "Width and height must be >= 0");
        }
        JLabel label = (JLabel)c;
        String text = label.getText();
        if (text == null || "".equals(text)) {
            return -1;
        }
        Insets i = label.getInsets();
        Rectangle viewRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        Rectangle iconRect = new Rectangle();
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = width - (i.right + viewRect.x);
        viewRect.height = height - (i.bottom + viewRect.y);

        // layout the text and icon
        SynthContext context = getContext(label);
        FontMetrics fm = context.getComponent().getFontMetrics(
            context.getStyle().getFont(context));
        context.getStyle().getGraphicsUtils(context).layoutText(
            context, fm, label.getText(), label.getIcon(), 
            label.getHorizontalAlignment(), label.getVerticalAlignment(),
            label.getHorizontalTextPosition(), label.getVerticalTextPosition(),
            viewRect, iconRect, textRect, label.getIconTextGap());
        View view = (View)label.getClientProperty(BasicHTML.propertyKey);
        int baseline;
        if (view != null) {
            baseline = BasicHTML.getHTMLBaseline(view, textRect.width,
                                                 textRect.height);
            if (baseline >= 0) {
                baseline += textRect.y;
            }
        }
        else {
            baseline = textRect.y + fm.getAscent();
        }
        context.dispose();
        return baseline;
    }

    /**
     * Notifies this UI delegate that it's time to paint the specified
     * component.  This method is invoked by <code>JComponent</code> 
     * when the specified component is being painted. 
     */
    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);

        SynthLookAndFeel.update(context, g);
        context.getPainter().paintLabelBackground(context,
                          g, 0, 0, c.getWidth(), c.getHeight());
        paint(context, g);
        context.dispose();
    }

    public void paint(Graphics g, JComponent c) {
        SynthContext context = getContext(c);

        paint(context, g);
        context.dispose();
    }

    protected void paint(SynthContext context, Graphics g) {
        JLabel label = (JLabel)context.getComponent();
        Icon icon = (label.isEnabled()) ? label.getIcon() :
                                          label.getDisabledIcon();

        g.setColor(context.getStyle().getColor(context,
                                               ColorType.TEXT_FOREGROUND));
        g.setFont(style.getFont(context));
        context.getStyle().getGraphicsUtils(context).paintText(
            context, g, label.getText(), icon,
            label.getHorizontalAlignment(), label.getVerticalAlignment(),
            label.getHorizontalTextPosition(), label.getVerticalTextPosition(),
            label.getIconTextGap(), label.getDisplayedMnemonicIndex(), 0);
    }

    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintLabelBorder(context, g, x, y, w, h);
    }

    public Dimension getPreferredSize(JComponent c) {
        JLabel label = (JLabel)c;
        Icon icon = (label.isEnabled()) ? label.getIcon() :
                                          label.getDisabledIcon();
        SynthContext context = getContext(c);
        Dimension size = context.getStyle().getGraphicsUtils(context).
            getPreferredSize(
               context, context.getStyle().getFont(context), label.getText(),
               icon, label.getHorizontalAlignment(),
               label.getVerticalAlignment(), label.getHorizontalTextPosition(),
               label.getVerticalTextPosition(), label.getIconTextGap(),
               label.getDisplayedMnemonicIndex());

        context.dispose();
        return size;
    }


    public Dimension getMinimumSize(JComponent c) {
        JLabel label = (JLabel)c;
        Icon icon = (label.isEnabled()) ? label.getIcon() :
                                          label.getDisabledIcon();
        SynthContext context = getContext(c);
        Dimension size = context.getStyle().getGraphicsUtils(context).
            getMinimumSize(
               context, context.getStyle().getFont(context), label.getText(),
               icon, label.getHorizontalAlignment(),
               label.getVerticalAlignment(), label.getHorizontalTextPosition(),
               label.getVerticalTextPosition(), label.getIconTextGap(),
               label.getDisplayedMnemonicIndex());

        context.dispose();
        return size;
    }

    public Dimension getMaximumSize(JComponent c) {
        JLabel label = (JLabel)c;
        Icon icon = (label.isEnabled()) ? label.getIcon() :
                                          label.getDisabledIcon();
        SynthContext context = getContext(c);
        Dimension size = context.getStyle().getGraphicsUtils(context).
               getMaximumSize(
               context, context.getStyle().getFont(context), label.getText(),
               icon, label.getHorizontalAlignment(),
               label.getVerticalAlignment(), label.getHorizontalTextPosition(),
               label.getVerticalTextPosition(), label.getIconTextGap(),
               label.getDisplayedMnemonicIndex());

        context.dispose();
        return size;
    }


    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        if (SynthLookAndFeel.shouldUpdateStyle(e)) {
            updateStyle((JLabel)e.getSource());
        }
    }
}
