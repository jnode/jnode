/*
 * $Id$
 */
package org.jnode.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;

import org.jnode.awt.JNodeAwtContext;
import org.jnode.awt.JNodeToolkit;
import org.jnode.vm.VmSystem;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Desktop implements Runnable {

	private final JPanel statusBar = new JPanel(new FlowLayout());
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		final JNodeToolkit tk = JNodeToolkit.getJNodeToolkit();
		final JNodeAwtContext ctx = tk.getAwtContext();
		
		statusBar.setBackground(Color.DARK_GRAY);
		final JButton haltCmd = new JButton("Halt");
		haltCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JNodeToolkit.stopGui();
			}
		});
		statusBar.add(haltCmd);
		
		final JButton rebootCmd = new JButton("Reboot");
		rebootCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JNodeToolkit.stopGui();
				VmSystem.halt(true);
			}
		});
		statusBar.add(rebootCmd);

		final JDesktopPane desktop = ctx.getDesktop();
		final Container awtRoot = ctx.getAwtRoot();
		awtRoot.removeAll();
		awtRoot.setLayout(new BorderLayout());
		awtRoot.add(desktop, BorderLayout.CENTER);
		awtRoot.add(statusBar, BorderLayout.EAST);
		awtRoot.invalidate();
		
		
		Frame test = new Frame("Test");
		test.setSize(100, 200);
		test.show();
		// TODO Auto-generated method stub

	}
}
