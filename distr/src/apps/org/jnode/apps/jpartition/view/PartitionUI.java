package org.jnode.apps.jpartition.view;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jnode.apps.jpartition.model.Partition;

public class PartitionUI extends JPanel 
{
	private Partition partition = null;
	
	private JLabel lblInfos = new JLabel();
	
	public PartitionUI(Partition partition)
	{
		setPartition(partition);
		
		
		setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));
		lblInfos.setBackground(Color.CYAN);
		add(lblInfos);
	}

	public Partition getPartition() {
		return partition;
	}

	public void setPartition(Partition partition) {
		if(this.partition != partition)
		{
			this.partition = partition;
			refreshFromModel();
		}
	}
	
	public void refreshFromModel()
	{
		StringBuilder sb = new StringBuilder();
				
		String name = partition.getType().getName();
		if(name.length() > 10)
		{
			name = name.substring(0, 10);
		}
		String boot = partition.isBootable() ? "(B)" : "(-)";
		sb.append(boot).append(name);
		if(!partition.isEmpty())
		{
			sb.append("-").append(partition.getStart());
			sb.append("-").append(partition.getSize());
		}
		lblInfos.setText(sb.toString());
	}
}
