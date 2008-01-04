package org.jnode.apps.jpartition.swingview.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.UserFacade;

public class FormatPartitionAction extends AbstractAction {

	public FormatPartitionAction() {
		super("format the partition");
	}

	public void actionPerformed(ActionEvent e) {
		final Component parent = (Component) e.getSource();
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run() {
				Object[] options = UserFacade.getInstance().getFormatters();
				String option = (String)JOptionPane.showInputDialog(
                    parent, "Select a format",
                    "Format selection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

				if((option != null) && (option.length() > 0))
				{
					UserFacade.getInstance().selectFormatter(option);
					Device dev = UserFacade.getInstance().getSelectedDevice();
					try {
						UserFacade.getInstance().formatPartition(dev.getStart());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
	}
}
