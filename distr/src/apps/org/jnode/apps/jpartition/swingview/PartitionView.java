package org.jnode.apps.jpartition.swingview;

import java.awt.Color;

import javax.swing.Action;
import javax.swing.JLabel;

import org.jnode.apps.jpartition.model.Partition;
import org.jnode.apps.jpartition.swingview.actions.FormatPartitionAction;

public class PartitionView extends DiskAreaView
{
	final private JLabel lblInfos = new JLabel();
	final private DeviceView deviceView;
	final private Partition partition;

	public PartitionView(DeviceView deviceView, Partition partition) {
		super(Color.BLUE);
		this.deviceView = deviceView;
		this.partition = partition;
		update();
	}

	public void update()
	{
		if(partition.isUsed())
		{
			lblInfos.setText("empty");
		}
		else
		{
			lblInfos.setText(partition.getFormat());
		}
	}

	@Override
	protected Action[] getActions() {
		return new Action[]{
				new FormatPartitionAction()
				};
	}
}
