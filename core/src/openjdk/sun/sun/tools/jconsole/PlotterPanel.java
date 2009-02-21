/*
 * Copyright 2004-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.tools.jconsole;

import java.awt.*;
import java.awt.event.*;

import javax.accessibility.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class PlotterPanel extends BorderedComponent {
    Plotter plotter;

    public PlotterPanel(String labelStr, Plotter.Unit unit, boolean collapsible) {
        super(labelStr, new Plotter(unit), collapsible);

        this.plotter = (Plotter)comp;

        init();
    }

    public PlotterPanel(String labelStr) {
        super(labelStr, null);

        init();
    }

    public Plotter getPlotter() {
        return this.plotter;
    }

    public void setPlotter(Plotter plotter) {
        this.plotter = plotter;
        setComponent(plotter);
    }

    private void init() {
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    public JPopupMenu getComponentPopupMenu() {
        return (getPlotter() != null)? getPlotter().getComponentPopupMenu() : null;
    }

    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessiblePlotterPanel();
        }
        return accessibleContext;
    }

    protected class AccessiblePlotterPanel extends AccessibleJComponent {
        public String getAccessibleName() {
            String name = null;
            if (getPlotter() != null) {
                name = getPlotter().getAccessibleContext().getAccessibleName();
            }
            if (name == null) {
                name = super.getAccessibleName();
            }
            return name;
        }
    }
}
