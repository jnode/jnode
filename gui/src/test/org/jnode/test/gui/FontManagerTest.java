/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * @author Valentin Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FontManagerTest {

    public static void main(String[] args) {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        // foants loaded

        System.out.println("" + fonts.length);

        for (int i = 0; i < fonts.length; i++) {
            System.out.println(fonts[i].getName());
        }
    }
}
