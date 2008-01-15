package org.jnode.apps.jpartition.swingview;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Partition;
import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.apps.jpartition.swingview.actions.AddPartitionAction;
import org.jnode.apps.jpartition.swingview.actions.FormatPartitionAction;
import org.jnode.apps.jpartition.swingview.actions.RemovePartitionAction;
import org.jnode.util.BinaryPrefix;

public class PartitionView extends DiskAreaView<Partition>
{
	final private DeviceView deviceView;

	private PartitionState state = new IdleState();

	public PartitionView(ErrorReporter errorReporter, DeviceView deviceView, Partition partition) {
		super(errorReporter);
		this.deviceView = deviceView;
		this.bounded = partition;
		update();

		if(partition.isUsed())
		{
			addMouseMotionListener(new MouseAdapter()
			{
				@Override
				public void mouseMoved(MouseEvent e) {
					int cursor = Cursor.DEFAULT_CURSOR;
					state = new IdleState();

					/*if(e.getX() < borderWidth)
					{
						cursor = Cursor.W_RESIZE_CURSOR;
					}
					else */ if(e.getX() >= (getWidth() - borderWidth))
					{
						cursor = Cursor.W_RESIZE_CURSOR;
						state = new ResizeState();
					}
//					else
//					{
//						cursor = Cursor.MOVE_CURSOR;
//					}
					setCursor(Cursor.getPredefinedCursor(cursor));
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					state.mouseDragged(PartitionView.this, e);
				}
			});
		}
	}

	protected Color getColor(Partition partition)
	{
		Color color = Constants.UNALLOCATED;
		if((partition != null) && partition.isUsed())
		{
			if("JFAT".equals(partition.getFormat()))
			{
				color = Constants.FAT32;
			}
			else if("FAT".equals(partition.getFormat()))
			{
				color = Constants.FAT16;
			}
			else if("EXT2".equals(partition.getFormat()))
			{
				color = Constants.EXT2;
			}
			else if("NTFS".equals(partition.getFormat()))
			{
				color = Constants.NTFS;
			}
		}

		return color;
	}

	public void update()
	{
		super.update();

		if(bounded != null)
		{
			setInfos(bounded.getFormat() + " " + BinaryPrefix.apply(bounded.getSize()));

			Color color = getColor(bounded);
			setBackground(color);

			if(!bounded.isUsed())
			{
				setBorder(BorderFactory.createEmptyBorder());
			}
		}
	}

	final public Partition getPartition()
	{
		return bounded;
	}

	@Override
	protected Action[] getActions() {
		Action[] actions;
		if(bounded.isUsed())
		{
			actions = new Action[]
			{
				new FormatPartitionAction(errorReporter, this),
				new RemovePartitionAction(errorReporter, this),
			};
		}
		else
		{
			actions = new Action[]
			{
				new AddPartitionAction(errorReporter, this),
			};
		}
		return actions;
	}

	public DeviceView getDeviceView() {
		return deviceView;
	}

	abstract private static class PartitionState
	{

		abstract protected void mouseDragged(PartitionView partitionView, MouseEvent e);
	}

	private static class IdleState extends PartitionState
	{
		@Override
		protected void mouseDragged(PartitionView partitionView, MouseEvent e) {
			// do nothing
		}
	}

	private static class ResizeState extends PartitionState
	{
		@Override
		protected void mouseDragged(PartitionView partitionView, MouseEvent e) {
			long size = (long) ((double) e.getX() / partitionView.pixelsPerByte);
			try {
				UserFacade.getInstance().resizePartition(partitionView.bounded.getStart()+1,  size);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
