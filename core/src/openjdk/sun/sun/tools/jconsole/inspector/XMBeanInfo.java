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

package sun.tools.jconsole.inspector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.*;
import javax.management.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import sun.tools.jconsole.JConsole;
import sun.tools.jconsole.Resources;
import sun.tools.jconsole.inspector.XNodeInfo.Type;

import static sun.tools.jconsole.Utilities.*;

@SuppressWarnings("serial")
public class XMBeanInfo extends JPanel {

    private static final Color lightYellow = new Color(255, 255, 128);

    private final int NAME_COLUMN = 0;
    private final int VALUE_COLUMN = 1;

    private final String[] columnNames = {
        Resources.getText("Name"),
        Resources.getText("Value")
    };

    private JTable infoTable = new JTable();
    private JTable descTable = new JTable();
    private JPanel infoBorderPanel = new JPanel(new BorderLayout());
    private JPanel descBorderPanel = new JPanel(new BorderLayout());

    private static class ReadOnlyDefaultTableModel extends DefaultTableModel {
        public void setValueAt(Object value, int row, int col) {
        }
    }

    private static class TableRowDivider {

        private String tableRowDividerText;

        public TableRowDivider(String tableRowDividerText) {
            this.tableRowDividerText = tableRowDividerText;
        }

        public String toString() {
            return tableRowDividerText;
        }
    }

    private static MBeanInfoTableCellRenderer renderer =
            new MBeanInfoTableCellRenderer();

    private static class MBeanInfoTableCellRenderer
            extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (value instanceof TableRowDivider) {
                JLabel label = new JLabel(value.toString());
                label.setBackground(ensureContrast(lightYellow,
                                                   label.getForeground()));
                label.setOpaque(true);
                return label;
            }
            return comp;
        }
    }

    private static TableCellEditor editor =
            new MBeanInfoTableCellEditor(new JTextField());

    private static class MBeanInfoTableCellEditor
            extends Utils.ReadOnlyTableCellEditor {
        public MBeanInfoTableCellEditor(JTextField tf) {
            super(tf);
        }
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int column) {
            Component comp = super.getTableCellEditorComponent(
                    table, value, isSelected, row, column);
            if (value instanceof TableRowDivider) {
                JLabel label = new JLabel(value.toString());
                label.setBackground(ensureContrast(lightYellow,
                                                   label.getForeground()));
                label.setOpaque(true);
                return label;
            }
            return comp;
        }
    }

    public XMBeanInfo() {
        // Use the grid layout to display the two tables
        //
        super(new GridLayout(2, 1));
        // MBean*Info table
        //
        infoTable.setModel(new ReadOnlyDefaultTableModel());
        infoTable.setRowSelectionAllowed(false);
        infoTable.setColumnSelectionAllowed(false);
        infoTable.getTableHeader().setReorderingAllowed(false);
        ((DefaultTableModel) infoTable.getModel()).setColumnIdentifiers(columnNames);
        infoTable.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(140);
        infoTable.getColumnModel().getColumn(NAME_COLUMN).setMaxWidth(140);
        infoTable.getColumnModel().getColumn(NAME_COLUMN).setCellRenderer(renderer);
        infoTable.getColumnModel().getColumn(VALUE_COLUMN).setCellRenderer(renderer);
        infoTable.getColumnModel().getColumn(NAME_COLUMN).setCellEditor(editor);
        infoTable.getColumnModel().getColumn(VALUE_COLUMN).setCellEditor(editor);
        infoTable.addKeyListener(new Utils.CopyKeyAdapter());
        infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        JScrollPane infoTableScrollPane = new JScrollPane(infoTable);
        infoBorderPanel.setBorder(
                BorderFactory.createTitledBorder("MBeanInfoPlaceHolder"));
        infoBorderPanel.add(infoTableScrollPane);
        // Descriptor table
        //
        descTable.setModel(new ReadOnlyDefaultTableModel());
        descTable.setRowSelectionAllowed(false);
        descTable.setColumnSelectionAllowed(false);
        descTable.getTableHeader().setReorderingAllowed(false);
        ((DefaultTableModel) descTable.getModel()).setColumnIdentifiers(columnNames);
        descTable.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(140);
        descTable.getColumnModel().getColumn(NAME_COLUMN).setMaxWidth(140);
        descTable.getColumnModel().getColumn(NAME_COLUMN).setCellRenderer(renderer);
        descTable.getColumnModel().getColumn(VALUE_COLUMN).setCellRenderer(renderer);
        descTable.getColumnModel().getColumn(NAME_COLUMN).setCellEditor(editor);
        descTable.getColumnModel().getColumn(VALUE_COLUMN).setCellEditor(editor);
        descTable.addKeyListener(new Utils.CopyKeyAdapter());
        descTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        JScrollPane descTableScrollPane = new JScrollPane(descTable);
        descBorderPanel.setBorder(
                BorderFactory.createTitledBorder(Resources.getText("Descriptor")));
        descBorderPanel.add(descTableScrollPane);
        // Add the two tables to the grid
        //
        add(infoBorderPanel);
        add(descBorderPanel);
    }

    public void emptyInfoTable() {
        DefaultTableModel tableModel = (DefaultTableModel) infoTable.getModel();
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
    }

    public void emptyDescTable() {
        DefaultTableModel tableModel = (DefaultTableModel) descTable.getModel();
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
    }

    private void addDescriptor(Descriptor desc, String text) {
        if (desc != null && desc.getFieldNames().length > 0) {
            DefaultTableModel tableModel = (DefaultTableModel) descTable.getModel();
            Object rowData[] = new Object[2];
            rowData[0] = new TableRowDivider(text);
            rowData[1] = new TableRowDivider("");
            tableModel.addRow(rowData);
            for (String fieldName : desc.getFieldNames()) {
                rowData[0] = fieldName;
                Object fieldValue = desc.getFieldValue(fieldName);
                if (fieldValue instanceof boolean[]) {
                    rowData[1] = Arrays.toString((boolean[]) fieldValue);
                } else if (fieldValue instanceof byte[]) {
                    rowData[1] = Arrays.toString((byte[]) fieldValue);
                } else if (fieldValue instanceof char[]) {
                    rowData[1] = Arrays.toString((char[]) fieldValue);
                } else if (fieldValue instanceof double[]) {
                    rowData[1] = Arrays.toString((double[]) fieldValue);
                } else if (fieldValue instanceof float[]) {
                    rowData[1] = Arrays.toString((float[]) fieldValue);
                } else if (fieldValue instanceof int[]) {
                    rowData[1] = Arrays.toString((int[]) fieldValue);
                } else if (fieldValue instanceof long[]) {
                    rowData[1] = Arrays.toString((long[]) fieldValue);
                } else if (fieldValue instanceof short[]) {
                    rowData[1] = Arrays.toString((short[]) fieldValue);
                } else if (fieldValue instanceof Object[]) {
                    rowData[1] = Arrays.toString((Object[]) fieldValue);
                } else {
                    rowData[1] = fieldValue;
                }
                tableModel.addRow(rowData);
            }
            tableModel.newDataAvailable(new TableModelEvent(tableModel));
        }
    }

    public void addMBeanInfo(XMBean mbean, MBeanInfo mbeanInfo) {
        emptyInfoTable();
        emptyDescTable();
        ((TitledBorder) infoBorderPanel.getBorder()).setTitle(
                Resources.getText("MBeanInfo"));
        String text = Resources.getText("Info") + ":";
        DefaultTableModel tableModel = (DefaultTableModel) infoTable.getModel();
        Object rowData[] = new Object[2];
        rowData[0] = new TableRowDivider(text);
        rowData[1] = new TableRowDivider("");
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("ObjectName");
        rowData[1] = mbean.getObjectName();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("ClassName");
        rowData[1] = mbeanInfo.getClassName();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Description");
        rowData[1] = mbeanInfo.getDescription();
        tableModel.addRow(rowData);
        addDescriptor(mbeanInfo.getDescriptor(), text);
        // MBeanConstructorInfo
        //
        int i = 0;
        for (MBeanConstructorInfo mbci : mbeanInfo.getConstructors()) {
            addMBeanConstructorInfo(mbci,
                    Resources.getText("Constructor") + "-" + i + ":");
            // MBeanParameterInfo
            //
            int j = 0;
            for (MBeanParameterInfo mbpi : mbci.getSignature()) {
                addMBeanParameterInfo(mbpi,
                        Resources.getText("Parameter") + "-" + i + "-" + j + ":");
                j++;
            }
            i++;
        }
        tableModel.newDataAvailable(new TableModelEvent(tableModel));
    }

    public void addMBeanAttributeInfo(MBeanAttributeInfo mbai) {
        emptyInfoTable();
        emptyDescTable();
        ((TitledBorder) infoBorderPanel.getBorder()).setTitle(
                Resources.getText("MBeanAttributeInfo"));
        String text = Resources.getText("Attribute") + ":";
        DefaultTableModel tableModel = (DefaultTableModel) infoTable.getModel();
        Object rowData[] = new Object[2];
        rowData[0] = new TableRowDivider(text);
        rowData[1] = new TableRowDivider("");
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Name");
        rowData[1] = mbai.getName();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Description");
        rowData[1] = mbai.getDescription();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Readable");
        rowData[1] = mbai.isReadable();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Writable");
        rowData[1] = mbai.isWritable();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Is");
        rowData[1] = mbai.isIs();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Type");
        rowData[1] = mbai.getType();
        tableModel.addRow(rowData);
        addDescriptor(mbai.getDescriptor(), text);
        tableModel.newDataAvailable(new TableModelEvent(tableModel));
    }

    public void addMBeanOperationInfo(MBeanOperationInfo mboi) {
        emptyInfoTable();
        emptyDescTable();
        ((TitledBorder) infoBorderPanel.getBorder()).setTitle(
                Resources.getText("MBeanOperationInfo"));
        String text = Resources.getText("Operation") + ":";
        DefaultTableModel tableModel = (DefaultTableModel) infoTable.getModel();
        Object rowData[] = new Object[2];
        rowData[0] = new TableRowDivider(text);
        rowData[1] = new TableRowDivider("");
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Name");
        rowData[1] = mboi.getName();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Description");
        rowData[1] = mboi.getDescription();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Impact");
        switch (mboi.getImpact()) {
            case MBeanOperationInfo.INFO:
                rowData[1] = Resources.getText("INFO");
                break;
            case MBeanOperationInfo.ACTION:
                rowData[1] = Resources.getText("ACTION");
                break;
            case MBeanOperationInfo.ACTION_INFO:
                rowData[1] = Resources.getText("ACTION_INFO");
                break;
            case MBeanOperationInfo.UNKNOWN:
                rowData[1] = Resources.getText("UNKNOWN");
                break;
        }
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("ReturnType");
        rowData[1] = mboi.getReturnType();
        tableModel.addRow(rowData);
        addDescriptor(mboi.getDescriptor(), text);
        // MBeanParameterInfo
        //
        int i = 0;
        for (MBeanParameterInfo mbpi : mboi.getSignature()) {
            addMBeanParameterInfo(mbpi,
                    Resources.getText("Parameter") + "-" + i++ + ":");
        }
        tableModel.newDataAvailable(new TableModelEvent(tableModel));
    }

    public void addMBeanNotificationInfo(MBeanNotificationInfo mbni) {
        emptyInfoTable();
        emptyDescTable();
        ((TitledBorder) infoBorderPanel.getBorder()).setTitle(
                Resources.getText("MBeanNotificationInfo"));
        String text = Resources.getText("Notification") + ":";
        DefaultTableModel tableModel = (DefaultTableModel) infoTable.getModel();
        Object rowData[] = new Object[2];
        rowData[0] = new TableRowDivider(text);
        rowData[1] = new TableRowDivider("");
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Name");
        rowData[1] = mbni.getName();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Description");
        rowData[1] = mbni.getDescription();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("NotifTypes");
        rowData[1] = Arrays.toString(mbni.getNotifTypes());
        tableModel.addRow(rowData);
        addDescriptor(mbni.getDescriptor(), text);
        tableModel.newDataAvailable(new TableModelEvent(tableModel));
    }

    private void addMBeanConstructorInfo(MBeanConstructorInfo mbci, String text) {
        DefaultTableModel tableModel = (DefaultTableModel) infoTable.getModel();
        Object rowData[] = new Object[2];
        rowData[0] = new TableRowDivider(text);
        rowData[1] = new TableRowDivider("");
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Name");
        rowData[1] = mbci.getName();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Description");
        rowData[1] = mbci.getDescription();
        tableModel.addRow(rowData);
        addDescriptor(mbci.getDescriptor(), text);
        tableModel.newDataAvailable(new TableModelEvent(tableModel));
    }

    private void addMBeanParameterInfo(MBeanParameterInfo mbpi, String text) {
        DefaultTableModel tableModel = (DefaultTableModel) infoTable.getModel();
        Object rowData[] = new Object[2];
        rowData[0] = new TableRowDivider(text);
        rowData[1] = new TableRowDivider("");
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Name");
        rowData[1] = mbpi.getName();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Description");
        rowData[1] = mbpi.getDescription();
        tableModel.addRow(rowData);
        rowData[0] = Resources.getText("Type");
        rowData[1] = mbpi.getType();
        tableModel.addRow(rowData);
        addDescriptor(mbpi.getDescriptor(), text);
        tableModel.newDataAvailable(new TableModelEvent(tableModel));
    }

    public static void loadInfo(DefaultMutableTreeNode root) {
        // Retrieve XMBean from XNodeInfo
        //
        XMBean mbean = (XMBean) ((XNodeInfo) root.getUserObject()).getData();
        // Initialize MBean*Info
        //
        final MBeanInfo mbeanInfo;
        try {
            mbeanInfo = mbean.getMBeanInfo();
        } catch (Exception e) {
            if (JConsole.isDebug()) {
                e.printStackTrace();
            }
            return;
        }
        MBeanAttributeInfo[] ai = mbeanInfo.getAttributes();
        MBeanOperationInfo[] oi = mbeanInfo.getOperations();
        MBeanNotificationInfo[] ni = mbeanInfo.getNotifications();
        // MBeanAttributeInfo node
        //
        if (ai != null && ai.length > 0) {
            DefaultMutableTreeNode attributes = new DefaultMutableTreeNode();
            XNodeInfo attributesUO = new XNodeInfo(Type.ATTRIBUTES, mbean,
                    Resources.getText("Attributes"), null);
            attributes.setUserObject(attributesUO);
            root.add(attributes);
            for (MBeanAttributeInfo mbai : ai) {
                DefaultMutableTreeNode attribute = new DefaultMutableTreeNode();
                XNodeInfo attributeUO = new XNodeInfo(Type.ATTRIBUTE,
                        new Object[] {mbean, mbai}, mbai.getName(), null);
                attribute.setUserObject(attributeUO);
                attributes.add(attribute);
            }
        }
        // MBeanOperationInfo node
        //
        if (oi != null && oi.length > 0) {
            DefaultMutableTreeNode operations = new DefaultMutableTreeNode();
            XNodeInfo operationsUO = new XNodeInfo(Type.OPERATIONS, mbean,
                    Resources.getText("Operations"), null);
            operations.setUserObject(operationsUO);
            root.add(operations);
            for (MBeanOperationInfo mboi : oi) {
                // Compute the operation's tool tip text:
                // "operationname(param1type,param2type,...)"
                //
                StringBuilder sb = new StringBuilder();
                for (MBeanParameterInfo mbpi : mboi.getSignature()) {
                    sb.append(mbpi.getType() + ",");
                }
                String signature = sb.toString();
                if (signature.length() > 0) {
                    // Remove the trailing ','
                    //
                    signature = signature.substring(0, signature.length() - 1);
                }
                String toolTipText = mboi.getName() + "(" + signature + ")";
                // Create operation node
                //
                DefaultMutableTreeNode operation = new DefaultMutableTreeNode();
                XNodeInfo operationUO = new XNodeInfo(Type.OPERATION,
                        new Object[] {mbean, mboi}, mboi.getName(), toolTipText);
                operation.setUserObject(operationUO);
                operations.add(operation);
            }
        }
        // MBeanNotificationInfo node
        //
        if (mbean.isBroadcaster()) {
            DefaultMutableTreeNode notifications = new DefaultMutableTreeNode();
            XNodeInfo notificationsUO = new XNodeInfo(Type.NOTIFICATIONS, mbean,
                    Resources.getText("Notifications"), null);
            notifications.setUserObject(notificationsUO);
            root.add(notifications);
            if (ni != null && ni.length > 0) {
                for (MBeanNotificationInfo mbni : ni) {
                    DefaultMutableTreeNode notification =
                            new DefaultMutableTreeNode();
                    XNodeInfo notificationUO = new XNodeInfo(Type.NOTIFICATION,
                            mbni, mbni.getName(), null);
                    notification.setUserObject(notificationUO);
                    notifications.add(notification);
                }
            }
        }
    }
}
