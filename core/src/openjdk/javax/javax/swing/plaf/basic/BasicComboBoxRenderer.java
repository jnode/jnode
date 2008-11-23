/*
 * Copyright 1997-2005 Sun Microsystems, Inc.  All Rights Reserved.
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
package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.awt.*;

import java.io.Serializable;


/**
 * ComboBox renderer
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @author Arnaud Weber
 */
public class BasicComboBoxRenderer extends JLabel
implements ListCellRenderer, Serializable {

   /**
    * An empty <code>Border</code>. This field might not be used. To change the
    * <code>Border</code> used by this renderer directly set it using
    * the <code>setBorder</code> method.
    */
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    private final static Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    public BasicComboBoxRenderer() {
        super();
        setOpaque(true);
        setBorder(getNoFocusBorder());
    }
    
    private static Border getNoFocusBorder() {
        if (System.getSecurityManager() != null) {
            return SAFE_NO_FOCUS_BORDER;
        } else {
            return noFocusBorder;
        }
    }
    
    public Dimension getPreferredSize() {
        Dimension size;
        
        if ((this.getText() == null) || (this.getText().equals( "" ))) {
            setText( " " );
            size = super.getPreferredSize();
            setText( "" );
        }
        else {
            size = super.getPreferredSize();
        }
        
        return size;
    }

    public Component getListCellRendererComponent(
                                                 JList list, 
                                                 Object value,
                                                 int index, 
                                                 boolean isSelected, 
                                                 boolean cellHasFocus)
    {

        /**if (isSelected) {
            setBackground(UIManager.getColor("ComboBox.selectionBackground"));
            setForeground(UIManager.getColor("ComboBox.selectionForeground"));
        } else {
            setBackground(UIManager.getColor("ComboBox.background"));
            setForeground(UIManager.getColor("ComboBox.foreground"));
        }**/

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setFont(list.getFont());

        if (value instanceof Icon) {
            setIcon((Icon)value);
        }
        else {
            setText((value == null) ? "" : value.toString());
        }
        return this;
    }


    /**
     * A subclass of BasicComboBoxRenderer that implements UIResource.
     * BasicComboBoxRenderer doesn't implement UIResource
     * directly so that applications can safely override the
     * cellRenderer property with BasicListCellRenderer subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource extends BasicComboBoxRenderer implements javax.swing.plaf.UIResource {
    }
}
