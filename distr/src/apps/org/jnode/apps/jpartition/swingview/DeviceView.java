package org.jnode.apps.jpartition.swingview;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.Action;
import javax.swing.JLabel;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.Partition;
import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.util.BinaryPrefix;
import org.jnode.util.NumberUtils;

public class DeviceView extends DiskAreaView<Device>
{
	private static final long serialVersionUID = 4961328945650444476L;

	public DeviceView(ErrorReporter errorReporter)
	{
		super(errorReporter);
		setLayout(null);
		update();

		addComponentListener(new ComponentListener(){

			public void componentHidden(ComponentEvent e) {
				update();
			}

			public void componentMoved(ComponentEvent e) {
				update();
			}

			public void componentResized(ComponentEvent e) {
				update();
			}

			public void componentShown(ComponentEvent e) {
				update();
			}});
	}

	public void update() {
		removeAll();
		this.bounded = UserFacade.getInstance().getSelectedDevice();
		super.update();

		if(bounded == null)
		{
			add(new JLabel("no partition"));
		}
		else
		{
			//final int space = 10;
			final int space = 1;
			double x = 0;
			for(Partition partition : bounded.getPartitions())
			{
				PartitionView p = new PartitionView(errorReporter, this, partition);

				double size = pixelsPerByte * partition.getSize();
				p.setBounds((int) x+space, 0+space, (int) size-2*space, getHeight()-2*space);
				add(p);
				p.update();

				x += size;
			}
			repaint();
		}

		if(bounded == null)
		{
			setInfos(null);
		}
		else
		{
			setInfos(bounded.getName() + " " + NumberUtils.toBinaryByte(bounded.getSize()));
		}
	}

	@Override
	protected Action[] getActions() {
		return null;
	}

	@Override
	protected Color getColor(Device bounded) {
		return Color.GREEN;
	}
}
