/*
 * Copyright 2002-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.border.*;
import java.util.Arrays;
import java.util.ArrayList;
import sun.swing.plaf.synth.SynthUI;


/**
 * Synth's MenuUI.
 *
 * @author Georges Saab
 * @author David Karlton
 * @author Arnaud Weber
 */
class SynthMenuUI extends BasicMenuUI implements PropertyChangeListener,
                               SynthUI {
    private SynthStyle style;
    private SynthStyle accStyle;

    private String acceleratorDelimiter;

    public static ComponentUI createUI(JComponent x) {
	return new SynthMenuUI();
    }

    protected void installDefaults() {
        updateStyle(menuItem);
    }

    protected void installListeners() {
        super.installListeners();
        menuItem.addPropertyChangeListener(this);
    }

    private void updateStyle(JMenuItem mi) {
        SynthStyle oldStyle = style;
        SynthContext context = getContext(mi, ENABLED);

        style = SynthLookAndFeel.updateStyle(context, this);
        if (oldStyle != style) {
            String prefix = getPropertyPrefix();
            defaultTextIconGap = style.getInt(
                           context, prefix + ".textIconGap", 4);
            if (menuItem.getMargin() == null || 
                         (menuItem.getMargin() instanceof UIResource)) {
                Insets insets = (Insets)style.get(context, prefix + ".margin");

                if (insets == null) {
                    // Some places assume margins are non-null.
                    insets = SynthLookAndFeel.EMPTY_UIRESOURCE_INSETS;
                }
                menuItem.setMargin(insets);
            }
            acceleratorDelimiter = style.getString(context, prefix +
                                            ".acceleratorDelimiter", "+");

            if (useCheckAndArrow()) {
                checkIcon = style.getIcon(context, prefix + ".checkIcon");
                arrowIcon = style.getIcon(context, prefix + ".arrowIcon");
            } else {
                // Not needed in this case
                checkIcon = null;
                arrowIcon = null;
            }

            ((JMenu)menuItem).setDelay(style.getInt(context, prefix +
                                                    ".delay", 200));
            if (oldStyle != null) {
                uninstallKeyboardActions();
                installKeyboardActions();
            }
        }
        context.dispose();

        SynthContext accContext = getContext(mi, Region.MENU_ITEM_ACCELERATOR,
                                             ENABLED);

        accStyle = SynthLookAndFeel.updateStyle(accContext, this);
        accContext.dispose();
    }

    protected void uninstallDefaults() {
        SynthContext context = getContext(menuItem, ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;

        SynthContext accContext = getContext(menuItem,
                                     Region.MENU_ITEM_ACCELERATOR, ENABLED);
        accStyle.uninstallDefaults(accContext);
        accContext.dispose();
        accStyle = null;

        super.uninstallDefaults();
    }

    protected void uninstallListeners() {
	super.uninstallListeners();
        menuItem.removePropertyChangeListener(this);
    }

    public SynthContext getContext(JComponent c) {
        return getContext(c, getComponentState(c));
    }

    SynthContext getContext(JComponent c, int state) {
        Region region = getRegion(c);
        return SynthContext.getContext(SynthContext.class, c, region,
                                       style, state);
    }

    public SynthContext getContext(JComponent c, Region region) {
        return getContext(c, region, getComponentState(c, region));
    }

    private SynthContext getContext(JComponent c, Region region, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                                       region, accStyle, state);
    }

    private Region getRegion(JComponent c) {
        return SynthLookAndFeel.getRegion(c);
    }

    private int getComponentState(JComponent c) {
        int state;

        if (!c.isEnabled()) {
            return DISABLED;
        }
        if (menuItem.isArmed()) {
            state = MOUSE_OVER;
        }
        else {
            state = SynthLookAndFeel.getComponentState(c);
        }
        if (menuItem.isSelected()) {
            state |= SELECTED;
        }
        return state;
    }

    private int getComponentState(JComponent c, Region region) {
        return getComponentState(c);
    }

    protected Dimension getPreferredMenuItemSize(JComponent c,
                                                     Icon checkIcon,
                                                     Icon arrowIcon,
                                                     int defaultTextIconGap) {
        SynthContext context = getContext(c);
        SynthContext accContext = getContext(c, Region.MENU_ITEM_ACCELERATOR);
        Dimension value = SynthMenuItemUI.getPreferredMenuItemSize(
                  context, accContext, c, checkIcon, arrowIcon,
                  defaultTextIconGap, acceleratorDelimiter);
        context.dispose();
        accContext.dispose();
        return value;
    }


    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);

        SynthLookAndFeel.update(context, g);
        context.getPainter().paintMenuBackground(context,
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
        SynthContext accContext = getContext(menuItem,
                                             Region.MENU_ITEM_ACCELERATOR);
        SynthStyle style = context.getStyle();
        Icon checkIcon;
        Icon arrowIcon;
        if (useCheckAndArrow()) {
            // Refetch the appropriate icons for the current state
            String prefix = getPropertyPrefix();
            checkIcon = style.getIcon(context, prefix + ".checkIcon");
            arrowIcon = style.getIcon(context, prefix + ".arrowIcon");
        } else {
            // Not needed in this case
            checkIcon = null;
            arrowIcon = null;
        }
        SynthMenuItemUI.paint(context, accContext, g, checkIcon, arrowIcon,
                              acceleratorDelimiter, defaultTextIconGap);
        accContext.dispose();
    }

    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintMenuBorder(context, g, x, y, w, h);
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (SynthLookAndFeel.shouldUpdateStyle(e)) {
            updateStyle((JMenu)e.getSource());
        }
    }

    private boolean useCheckAndArrow() {
        return !((JMenu)menuItem).isTopLevelMenu();
    }
}
