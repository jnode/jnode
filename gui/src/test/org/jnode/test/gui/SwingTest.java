/*
 * $Id$
 */
package org.jnode.test.gui;

import javax.swing.JPanel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SwingTest {

	public static void main(String[] args) {
		try {
			UIDefaults defs = UIManager.getDefaults();
			//System.out.println("Defs=" + defs);
			
			JPanel pan = new JPanel();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
