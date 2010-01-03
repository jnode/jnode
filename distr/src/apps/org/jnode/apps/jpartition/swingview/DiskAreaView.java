/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.apps.jpartition.swingview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

import org.jnode.apps.jpartition.Context;
import org.jnode.apps.jpartition.model.Bounded;

public abstract class DiskAreaView<T extends Bounded> extends JComponent {
    private static final long serialVersionUID = -506634580666065291L;

    private final int DEFAULT_PIXELS_PER_BYTE = 1;

    protected final int borderWidth = 5;
    protected final Context context;

    protected T bounded;
    protected double pixelsPerByte = DEFAULT_PIXELS_PER_BYTE;

    public DiskAreaView(Context context) {
        this.context = context;
        setLayout(null);
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                showMenu(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                showMenu(event);
            }
        });

        update();
    }

    protected abstract Color getColor(T bounded);

    protected void setInfos(String infos) {
        setToolTipText(infos);
    }

    public void update() {
        if (bounded == null) {
            pixelsPerByte = DEFAULT_PIXELS_PER_BYTE;
        } else {
            double size = (bounded.getEnd() - bounded.getStart() + 1);
            pixelsPerByte = (double) getWidth() / size;
        }

        Color borderColor = getColor(bounded);
        Border line = BorderFactory.createLineBorder(borderColor, borderWidth);
        // Border space = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        // setBorder(BorderFactory.createCompoundBorder(space, line));
        setBorder(line);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Rectangle r = g.getClipBounds();
        g.setColor(getBackground());
        g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
    }

    protected void showMenu(MouseEvent event) {
        if (event.isPopupTrigger()) {
            Action[] actions = getActions();
            if ((actions != null) && (actions.length != 0)) {
                JPopupMenu menu = new JPopupMenu("context menu");
                for (Action action : actions) {
                    if (action == null) {
                        menu.addSeparator();
                    } else {
                        menu.add(new JMenuItem(action));
                    }
                }

                menu.show(event.getComponent(), event.getX(), event.getY());
            }
        }
    }

    protected abstract Action[] getActions();
}
