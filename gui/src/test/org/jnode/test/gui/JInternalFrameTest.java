package org.jnode.test.gui;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import java.awt.BorderLayout;

/**
 * @author Levente S\u00e1ntha
 */
public class JInternalFrameTest {
    public static void main(String[] argv){
        JFrame f = new JFrame("Test");
        JDesktopPane dt = new JDesktopPane();
        f.getContentPane().add(dt, BorderLayout.CENTER);
        JInternalFrame ifr = new JInternalFrame("IF1");
        ifr.setLocation(0,0);
        ifr.setSize(150,150);
        ifr.setResizable(true);
        ifr.setClosable(true);
        ifr.setMaximizable(true);
        dt.add(ifr);
        ifr.setVisible(true);

        JInternalFrame ifr2 = new JInternalFrame("IF2");
        ifr2.setLocation(20,20);
        ifr2.setSize(150,150);
        ifr2.setResizable(true);
        ifr2.setClosable(true);
        ifr2.setMaximizable(true);
        dt.add(ifr2);
        ifr2.setVisible(true);

        JInternalFrame ifr3 = new JInternalFrame("IF3");
        ifr3.setLocation(40,40);
        ifr3.setSize(150,150);
        ifr3.setResizable(true);
        ifr3.setClosable(true);
        ifr3.setMaximizable(true);
        dt.add(ifr3);
        ifr3.setVisible(true);

        f.setLocation(0,0);
        f.setSize(300,300);
        f.setVisible(true);
    }
}
