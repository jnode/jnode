/**
 * $Id$  
 */
package org.jnode.test.gui;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import java.awt.Frame;
import java.awt.BorderLayout;

/**
 * @author Levente S?ntha
 */
public class JDPTest {
    public static void main(String[] args) {
        Frame wnd = new Frame();
        try {
            wnd.setSize(600, 400);
            JDesktopPane jdtp = new JDesktopPane();
            JInternalFrame jif = new JInternalFrame("Test");
            jif.setSize(100, 100);
            jif.setLocation(10,10);
            jif.setResizable(true);
            jif.setClosable(true);
            jif.setIconifiable(true);
            jif.setMaximizable(true);
            jif.setSelected(true);
            jdtp.add(jif);
            jif.setVisible(true);
            wnd.add(jdtp, BorderLayout.CENTER);

            wnd.show();

            Thread.sleep(5000);

            wnd.hide();
        }catch(Throwable t){
            t.printStackTrace();
        } finally {
            wnd.dispose();
        }
    }
}
