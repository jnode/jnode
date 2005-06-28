/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.TextField;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TextFieldTest extends Frame {

    public TextFieldTest() {
        super("TextFieldTest");
        final TextField tf = new TextField();
        final Button b = new Button();
        add(tf, BorderLayout.CENTER);
        add(b, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        try {
            final TextFieldTest wnd = new TextFieldTest();
            wnd.setSize(200, 100);
            wnd.show();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
