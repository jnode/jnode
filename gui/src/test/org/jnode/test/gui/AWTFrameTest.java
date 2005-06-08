package org.jnode.test.gui;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Button;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: Jun 8, 2005
 * Time: 10:19:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class AWTFrameTest {
    public static void main(String[] argv){
        Frame f = new Frame("Frame test");
        f.setSize(200,200);
        f.setLocation(50,50);
        final Frame f2 = new Frame("Test");
        f2.setSize(100,100);
        f2.setLocation(350, 200);
        f.setLayout(new GridLayout(5,1));
        Button show = new Button("show");
        show.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.setVisible(true);
            }
        });
        f.add(show);
        Button hide = new Button("hide");
        hide.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.setVisible(false);
            }
        });
        f.add(hide);
        Button back = new Button("back");
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.toBack();
            }
        });
        f.add(back);
        Button front = new Button("front");
        front.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.toFront();
            }
        });
        f.add(front);
        f.validate();
        f.setVisible(true);
    }
}
