/*
 * $Id$
 */
package org.jnode.test.gui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SwingMenuTest extends SwingTest {

    /**
     * @param title
     */
    public SwingMenuTest(String title) {
        super(title);
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("JMenu test");
        JMenuItem mi = new JMenuItem("JMenuItem test");
        mb.add(menu);
        menu.add(mi);
        setJMenuBar(mb);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            SwingMenuTest frame = new SwingMenuTest("JFrame test");
            frame.validate();
            frame.show();
            frame.dumpInfo();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
