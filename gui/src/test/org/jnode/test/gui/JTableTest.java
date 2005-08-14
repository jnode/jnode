package org.jnode.test.gui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * @author Levente S\u00e1ntha
 */
public class JTableTest {
    public static void main(String[] argv) {
        JFrame f = new JFrame();
        f.setSize(400, 400);
        f.add(new JScrollPane(new JTable(new DefaultTableModel(
                new Object[][]{
                        {1, 2, 3, 4},
                        {'a', 'b', 'c', 'd'},
                        {5, 6, 7, 8},
                        {11, 22, 33, 44},
                        {55, 66, 66, 88},
                },
                new Object[]{'A', 'B', 'C', 'D'}))));
        f.setVisible(true);
    }
}
