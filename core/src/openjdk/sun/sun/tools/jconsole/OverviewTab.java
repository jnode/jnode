/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;

import static sun.tools.jconsole.JConsole.*;
import static sun.tools.jconsole.Resources.*;


@SuppressWarnings("serial")
class OverviewTab extends Tab {
    JPanel gridPanel;
    TimeComboBox timeComboBox;

    public static String getTabName() {
        return getText("Overview");
    }

    public OverviewTab(VMPanel vmPanel) {
        super(vmPanel, getTabName());

        setBorder(new EmptyBorder(4, 4, 3, 4));
        setLayout(new BorderLayout());

        JPanel topPanel     = new JPanel(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        topPanel.add(controlPanel, BorderLayout.CENTER);

        timeComboBox = new TimeComboBox();
        LabeledComponent lc = new LabeledComponent(Resources.getText("Time Range:"),
                                                   getMnemonicInt("Time Range:"),
                                                   timeComboBox);
        controlPanel.add(lc);

        gridPanel = new JPanel(new AutoGridLayout(10, 6));
        gridPanel.setBorder(null);
        JScrollPane sp = new JScrollPane(gridPanel);
        sp.setBorder(null);
        sp.setViewportBorder(null);
        add(sp, BorderLayout.CENTER);

        // Note that panels are added on first update
    }


    public SwingWorker<?, ?> newSwingWorker() {
        return new SwingWorker<Object, Object>() {
            public Object doInBackground() {
                return null;
            }

            protected void done() {
                if (gridPanel.getComponentCount() == 0) {
                    final ArrayList<Plotter> plotters = new ArrayList<Plotter>();
                    for (Tab tab : vmPanel.getTabs()) {
                        OverviewPanel[] ops = tab.getOverviewPanels();
                        if (ops != null) {
                            for (OverviewPanel op : ops) {
                                gridPanel.add(op);
                                Plotter plotter = op.getPlotter();
                                if (plotter != null) {
                                    plotters.add(plotter);
                                    timeComboBox.addPlotter(plotter);
                                }
                            }
                        }
                    }
                    if (plotters.size() > 0) {
                        workerAdd(new Runnable() {
                            public void run() {
                                ProxyClient proxyClient = vmPanel.getProxyClient();
                                for (Plotter plotter : plotters) {
                                    proxyClient.addWeakPropertyChangeListener(plotter);
                                }
                            }
                        });
                    }
                    if (getParent() instanceof JTabbedPane) {
                        Utilities.updateTransparency((JTabbedPane)getParent());
                    }
                }
            }
        };
    }



    private class AutoGridLayout extends GridLayout {
        public AutoGridLayout(int hGap, int vGap) {
            super(0, 1, hGap, vGap);
        }

        public Dimension preferredLayoutSize(Container parent) {
            return minimumLayoutSize(parent);
        }

        public Dimension minimumLayoutSize(Container parent) {
            updateColumns(parent);
            return super.minimumLayoutSize(parent);
        }

        private void updateColumns(Container parent) {
            // Use the outer panel width, not the scrolling gridPanel
            int parentWidth = OverviewTab.this.getWidth();

            int columnWidth = 1;

            for (Component c : parent.getComponents()) {
                columnWidth = Math.max(columnWidth, c.getPreferredSize().width);
            }

            int n = parent.getComponentCount();
            int maxCols = Math.min(n, parentWidth / columnWidth);

            for (int columns = maxCols; columns >= 1; columns--) {
                if (columns == 1) {
                    setColumns(maxCols);
                } else if ((n % columns) == 0) {
                    setColumns(columns);
                    break;
                }
            }
        }
    }
}
