/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.AWTKeyStroke;

import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SwingTest {

	public static void main(String[] args) {
		try {
			//UIDefaults defs = UIManager.getDefaults();
			//System.out.println("Defs=" + defs);
			
			KeyStroke.getKeyStroke(' ');
			AWTKeyStroke ks = AWTKeyStroke.getAWTKeyStroke("SPACE");
			System.out.println("ks=" + ks);
			JPanel pan = new JPanel();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
