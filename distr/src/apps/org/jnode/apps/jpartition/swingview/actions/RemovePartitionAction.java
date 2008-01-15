package org.jnode.apps.jpartition.swingview.actions;

import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.apps.jpartition.swingview.PartitionView;

public class RemovePartitionAction extends AbstractAction<PartitionView> {
	public RemovePartitionAction(ErrorReporter errorReporter, PartitionView view) {
		super("remove partition", errorReporter, view);
	}

	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run() {
				final Device dev = UserFacade.getInstance().getSelectedDevice();
				try {
					UserFacade.getInstance().removePartition(view.getPartition().getStart()+1);
					view.getDeviceView().update();
				} catch (Exception e) {
					errorReporter.reportError(log, this, e);
				}
			}
		});
	}
}
