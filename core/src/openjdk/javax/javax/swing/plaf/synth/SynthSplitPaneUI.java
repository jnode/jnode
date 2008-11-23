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
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import sun.swing.plaf.synth.SynthUI;


/**
 * Synth's SplitPaneUI.
 *
 * @author Scott Violet
 */
class SynthSplitPaneUI extends BasicSplitPaneUI implements
                                    PropertyChangeListener, SynthUI {
    /**
     * Keys to use for forward focus traversal when the JComponent is
     * managing focus.
     */
    private static Set managingFocusForwardTraversalKeys;

    /**
     * Keys to use for backward focus traversal when the JComponent is
     * managing focus.
     */
    private static Set managingFocusBackwardTraversalKeys;

    /**
     * Style for the JSplitPane.
     */
    private SynthStyle style;
    /**
     * Style for the divider.
     */
    private SynthStyle dividerStyle;


    /**
     * Creates a new SynthSplitPaneUI instance
     */
    public static ComponentUI createUI(JComponent x) {
        return new SynthSplitPaneUI();
    }

    /**
     * Installs the UI defaults.
     */
    protected void installDefaults() {
        updateStyle(splitPane);

        setOrientation(splitPane.getOrientation());
        setContinuousLayout(splitPane.isContinuousLayout());

        resetLayoutManager();

        /* Install the nonContinuousLayoutDivider here to avoid having to
        add/remove everything later. */
        if(nonContinuousLayoutDivider == null) {
            setNonContinuousLayoutDivider(
                                createDefaultNonContinuousLayoutDivider(),
                                true);
        } else {
            setNonContinuousLayoutDivider(nonContinuousLayoutDivider, true);
        }

	// focus forward traversal key
	if (managingFocusForwardTraversalKeys==null) {
	    managingFocusForwardTraversalKeys = new HashSet();
	    managingFocusForwardTraversalKeys.add(
		KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
	}
	splitPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
					managingFocusForwardTraversalKeys);
	// focus backward traversal key
	if (managingFocusBackwardTraversalKeys==null) {
	    managingFocusBackwardTraversalKeys = new HashSet();
	    managingFocusBackwardTraversalKeys.add(
		KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
	}
	splitPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
					managingFocusBackwardTraversalKeys);
    }

    private void updateStyle(JSplitPane splitPane) {
        SynthContext context = getContext(splitPane, Region.SPLIT_PANE_DIVIDER,
                                          ENABLED);
        SynthStyle oldDividerStyle = dividerStyle;
        dividerStyle = SynthLookAndFeel.updateStyle(context, this);
        context.dispose();

        context = getContext(splitPane, ENABLED);
        SynthStyle oldStyle = style;

        style = SynthLookAndFeel.updateStyle(context, this);

        if (style != oldStyle) {
            Object value = style.get(context, "SplitPane.size");
            if (value == null) {
                value = new Integer(6);
            }
            LookAndFeel.installProperty(splitPane, "dividerSize", value);

            value = style.get(context, "SplitPane.oneTouchExpandable");
            if (value != null) {
                LookAndFeel.installProperty(splitPane, "oneTouchExpandable", value);
            }

            if (divider != null) {
                splitPane.remove(divider);
                divider.setDividerSize(splitPane.getDividerSize());
            }
            if (oldStyle != null) {
                uninstallKeyboardActions();
                installKeyboardActions();
            }
        }
        if (style != oldStyle || dividerStyle != oldDividerStyle) {
            // Only way to force BasicSplitPaneDivider to reread the
            // necessary properties.
            if (divider != null) {
                splitPane.remove(divider);
            }
            divider = createDefaultDivider();
            divider.setBasicSplitPaneUI(this);
            splitPane.add(divider, JSplitPane.DIVIDER);
        }
        context.dispose();
    }

    /**
     * Installs the event listeners for the UI.
     */
    protected void installListeners() {
        super.installListeners();
        splitPane.addPropertyChangeListener(this);
    }

    /**
     * Uninstalls the UI defaults.
     */
    protected void uninstallDefaults() {
        SynthContext context = getContext(splitPane, ENABLED);

        style.uninstallDefaults(context);
        context.dispose();
        style = null;

        context = getContext(splitPane, Region.SPLIT_PANE_DIVIDER, ENABLED);
        dividerStyle.uninstallDefaults(context);
        context.dispose();
        dividerStyle = null;

        super.uninstallDefaults();
    }


    /**
     * Uninstalls the event listeners for the UI.
     */
    protected void uninstallListeners() {
        super.uninstallListeners();
        splitPane.removePropertyChangeListener(this);
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
        return SynthLookAndFeel.getComponentState(c);
    }

    SynthContext getContext(JComponent c, Region region) {
        return getContext(c, region, getComponentState(c, region));
    }

    private SynthContext getContext(JComponent c, Region region, int state) {
        if (region == Region.SPLIT_PANE_DIVIDER) {
            return SynthContext.getContext(SynthContext.class, c, region,
                                           dividerStyle, state);
        }
        return SynthContext.getContext(SynthContext.class, c, region,
                                       style, state);
    }

    private int getComponentState(JComponent c, Region subregion) {
        int state = SynthLookAndFeel.getComponentState(c);

        if (divider.isMouseOver()) {
            state |= MOUSE_OVER;
        }
        return state;
    }


    public void propertyChange(PropertyChangeEvent e) {
        if (SynthLookAndFeel.shouldUpdateStyle(e)) {
            updateStyle((JSplitPane)e.getSource());
        }
    }

    /**
     * Creates the default divider.
     */
    public BasicSplitPaneDivider createDefaultDivider() {
        SynthSplitPaneDivider divider = new SynthSplitPaneDivider(this);

        divider.setDividerSize(splitPane.getDividerSize());
        return divider;
    }

    protected Component createDefaultNonContinuousLayoutDivider() {
        return new Canvas() {
            public void paint(Graphics g) {
                paintDragDivider(g, 0, 0, getWidth(), getHeight());
            }
        };
    }

    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);

        SynthLookAndFeel.update(context, g);
        context.getPainter().paintSplitPaneBackground(context,
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
        // This is done to update package private variables in
        // BasicSplitPaneUI
        super.paint(g, splitPane);
    }


    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintSplitPaneBorder(context, g, x, y, w, h);
    }

    private void paintDragDivider(Graphics g, int x, int y, int w, int h) {
        SynthContext context = getContext(splitPane,Region.SPLIT_PANE_DIVIDER);
        context.setComponentState(((context.getComponentState() | MOUSE_OVER) ^
                                   MOUSE_OVER) | PRESSED);
        Shape oldClip = g.getClip();
        g.clipRect(x, y, w, h);
        context.getPainter().paintSplitPaneDragDivider(context, g, x, y, w, h,
                                           splitPane.getOrientation());
        g.setClip(oldClip);
        context.dispose();
    }

    public void finishedPaintingChildren(JSplitPane jc, Graphics g) {
        if(jc == splitPane && getLastDragLocation() != -1 &&
                              !isContinuousLayout() && !draggingHW) {
            if(jc.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                paintDragDivider(g, getLastDragLocation(), 0, dividerSize - 1,
                                 splitPane.getHeight() - 1);
            } else {
                paintDragDivider(g, 0, getLastDragLocation(),
                                 splitPane.getWidth() - 1, dividerSize - 1);
            }
        }
    }
}
