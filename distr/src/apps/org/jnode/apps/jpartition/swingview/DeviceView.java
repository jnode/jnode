package org.jnode.apps.jpartition.swingview;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.Action;
import javax.swing.JLabel;

import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.Partition;
import org.jnode.apps.jpartition.model.UserFacade;

public class DeviceView extends DiskAreaView
{
	public DeviceView()
	{
		super(Color.GREEN);
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
		Device device = UserFacade.getInstance().getSelectedDevice();
		if(device == null)
		{
			add(new JLabel("no partition"));
		}
		else
		{
			final int space = 10;
			double x = 0;
			double pixelsPerByte = (double) getWidth() / (double) device.getSize();
			for(Partition partition : device.getPartitions())
			{
				PartitionView p = new PartitionView(this, partition);
				//JLabel p = new JLabel("test");

				double size = pixelsPerByte * partition.getSize();
				p.setBounds((int) x+space, 0+space, (int) size-2*space, getHeight()-2*space);
				add(p);
				p.update();

				x += size;
			}
			repaint();
		}
	}

	@Override
	protected Action[] getActions() {
		//return new Action[]{
		//		new AddPartitionAction()
		//		};
		return null;
	}
}
