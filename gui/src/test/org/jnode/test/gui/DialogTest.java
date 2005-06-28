/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogTest extends Dialog {

    
    /**
     * @param title
     */
    public DialogTest(String title) {
        super(new Frame(), title);
        setModal(true);
        final Button ok = new Button("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                DialogTest.this.dispose();
            }
            
        });
        add(new TextField(), BorderLayout.CENTER);
        add(ok, BorderLayout.EAST);
        
        setSize(200, 100);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        DialogTest dlg = new DialogTest("Dialog test");
        dlg.show();
        System.exit(0);
    }

}
