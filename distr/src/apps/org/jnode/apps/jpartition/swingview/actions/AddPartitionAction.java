package org.jnode.apps.jpartition.swingview.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.UserFacade;

public class AddPartitionAction extends AbstractAction {

	public AddPartitionAction() {
		super("add a partition");
	}

	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run() {
				final Device dev = UserFacade.getInstance().getSelectedDevice();

				JDialog dlg = new JDialog((Dialog) null, "create new partition", true);
				final SpinnerNumberModel spn = new SpinnerNumberModel(10, 1, dev.getSize(), 10);
				dlg.getContentPane().setLayout(new FlowLayout());
				dlg.getContentPane().add(new JLabel("size : "));
				dlg.getContentPane().add(new JSpinner(spn));
				JButton btn = new JButton("Add partition");
				btn.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						long size = spn.getNumber().longValue();
						try {
							UserFacade.getInstance().createPartition(dev.getStart(), size);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});
				dlg.getContentPane().add(btn);
				dlg.setBounds(200, 200, 300, 100);
				dlg.setVisible(true);
			}
		});
	}
}
