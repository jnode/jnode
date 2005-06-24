/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.Menu;
import java.awt.MenuBar;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AWTMenuTest extends AWTTest {

    /**
     * @param title
     */
    public AWTMenuTest(String title) {
        super(title);
        MenuBar mb = new MenuBar();
        mb.add(new Menu("Window"));
        setMenuBar(mb);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            final AWTMenuTest wnd = new AWTMenuTest("AWTTest");
            wnd.show();
        }catch(Throwable t){
            t.printStackTrace();
        } finally {
//            wnd.dispose();
        }
    }
}
