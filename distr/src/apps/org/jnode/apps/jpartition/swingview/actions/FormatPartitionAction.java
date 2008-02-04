package org.jnode.apps.jpartition.swingview.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.apps.jpartition.swingview.PartitionView;

public class FormatPartitionAction extends AbstractAction<PartitionView> {
	private static final long serialVersionUID = 4100228292937417784L;

	public FormatPartitionAction(ErrorReporter errorReporter, PartitionView view) {
		super("format the partition", errorReporter, view);
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
						errorReporter.reportError(log, FormatPartitionAction.this, e1);
					}
					view.update();
				}
			}
		});
	}
}
