/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.apps.debug;

import charva.awt.Dimension;
import charva.awt.event.KeyAdapter;
import charva.awt.event.KeyEvent;
import charvax.swing.DefaultListModel;
import charvax.swing.JList;
import charvax.swing.JPanel;
import charvax.swing.JScrollPane;
import charvax.swing.ListSelectionModel;
import charvax.swing.event.ListSelectionEvent;
import charvax.swing.event.ListSelectionListener;
import java.util.Vector;

/**
 * @author blind
 */
abstract class ListPanel extends JPanel {
    //public static final char TOGGLE_SHOW_SUPER_FIELDS='p';    //show the fields of superclasses

    Vector list;
    JList jlist;
    JScrollPane scrollPane;
    int rows, cols;

    public ListPanel(int cols, int rows) {
        super();
        this.rows = rows;
        this.cols = cols;
        this.setSize(new Dimension(cols, rows));

        jlist = new JList();
        jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jlist.setModel(new DefaultListModel());
        jlist.setVisibleRowCount(rows - 2);

        jlist.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                //Object o = list.elementAt(evt.getFirstIndex());
                int selectedIndex = jlist.getSelectedIndex();
                if ((selectedIndex >= 0) && (selectedIndex < list.size())) {    //need this check because
                    //something is screwed: sometimes strange values are returned by getSelectedIndex()
                    Object o = list.elementAt(jlist.getSelectedIndex());
                    elementSelected(((ListElement) o).getValue());
                }
            }
        });

        this.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                keyEntered(ke.getKeyChar());
            }
        });

        jlist.setColumns(cols - 2);

        scrollPane = new JScrollPane(jlist);
        this.add(scrollPane);
    }

    public void positionToLastRow() {
        jlist.ensureIndexIsVisible(list.size());
    }

    /*
     public DefaultListModel getListModel() {
         return (DefaultListModel)jlist.getModel();
     }
     */

    public String getElementLabel(Object o) {
        return o.getClass().getName() + " [" + o.toString() + "]";
    }

    /**
     * @param list a Vector of ListElements
     */
    public void setList(final Vector list) {
        this.list = list;

        ((DefaultListModel) jlist.getModel()).removeAllElements();
        for (int i = 0; i < list.size(); i++)
            ((DefaultListModel) jlist.getModel()).addElement(list.elementAt(i));

        scrollPane.invalidate();
        validate();
    }

    public Dimension minimumSize() {
        return new Dimension(cols, rows);
    }

    public Vector getList() {
        return list;
    }

    public abstract void elementSelected(Object o);

    public void keyEntered(char c) {
    }

}
