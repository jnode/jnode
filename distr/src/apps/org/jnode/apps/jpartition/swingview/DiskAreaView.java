package org.jnode.apps.jpartition.swingview;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jnode.apps.jpartition.controller.MainController;

public class DiskAreaView extends JPanel 
{
	final protected MainController controller;
	
	final protected JLabel lblInfos = new JLabel();

	public DiskAreaView(MainController controller)
	{
		this.controller = controller;
		
		setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));
		lblInfos.setBackground(Color.CYAN);
		add(lblInfos);
		
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent event) {
				showMenu(event);
			}
			
			@Override
			public void mouseReleased(MouseEvent event) {
				showMenu(event);
			}
		});
	}
/*	
	final private CommandProcessor commandProcessor;
	final private Device device; 
	
	public DiskAreaView(CommandProcessor commandProcessor, Device device)
	{
		this.commandProcessor = commandProcessor;
		this.device = device;
		
		setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));
		lblInfos.setBackground(Color.CYAN);
		add(lblInfos);
		
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent event) {
				showMenu(event);
			}
			
			@Override
			public void mouseReleased(MouseEvent event) {
				showMenu(event);
			}
		});
	}
	
	public void setLabel(String label)
	{
		lblInfos.setText(label);
	}	
*/
	
	protected void showMenu(MouseEvent event)
	{
		//TODO add popup menu
/*		
		if(event.isPopupTrigger())
		{
			JPopupMenu menu = new JPopupMenu("context menu");
			menu.add(new JMenuItem(new InitMbrAction((IDEDevice) device, commandProcessor)));
			menu.show(event.getComponent(), event.getX(), event.getY());
		}
*/		
	}
}
