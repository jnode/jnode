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
        int sleep = 5;
        try{
            sleep = Integer.parseInt(args[0]);
        }catch(Exception e){
            //ignore
        }
        
        Frame wnd = new Frame();
        try {
            wnd.setSize(600, 400);
            BoxWorld bw = new BoxWorld();
            bw.init();
            wnd.add(bw, BorderLayout.CENTER);
            wnd.show();
            bw.requestFocus();
			Thread.sleep(sleep * 1000);

			wnd.hide();
        } finally {
            wnd.dispose();
        }
    }
}
