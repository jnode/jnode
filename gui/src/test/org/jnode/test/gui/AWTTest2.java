/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

/**
 * @author epr
 */
public class AWTTest2 {

    public static void main(String[] args) throws InterruptedException {
        Frame wnd = new Frame();
        try {
            wnd.setSize(600, 400);
            BoxWorld bw = new BoxWorld();
            bw.init();
            wnd.add(bw, BorderLayout.CENTER);
            wnd.show();

			Thread.sleep(5000);

			wnd.hide();
        } finally {
            wnd.dispose();
        }
    }
}
