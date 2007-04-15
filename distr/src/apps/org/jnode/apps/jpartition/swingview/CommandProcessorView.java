package org.jnode.apps.jpartition.swingview;

import it.battlehorse.stamps.annotations.ModelDependent;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.controller.MainController;

public class CommandProcessorView extends JPanel
{
	private static final Logger log = Logger.getLogger(CommandProcessorView.class);
		
	private MainController controller;
	
	private DefaultListModel  commands = new DefaultListModel();
	private JList commandsUI = new JList(commands);
	private JButton btnApply = new JButton("Apply");
	
	public CommandProcessorView(MainController controller)
	{
		this.controller = controller;
				
		setBorder(BorderFactory.createTitledBorder("pending commands"));
		
		JPanel btnPanel = new JPanel(new BorderLayout());
		btnPanel.add(btnApply, BorderLayout.NORTH);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(btnPanel, BorderLayout.EAST);
		panel.add(new JScrollPane(commandsUI), BorderLayout.CENTER);
		add(panel);
		
		btnApply.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae_) {
				CommandProcessorView.this.setEnabled(false);
				CommandProcessorView.this.controller.userProcessCommands();
			}
		});
	}
	
    @ModelDependent(modelKey ="CommandProcessorModel" , propertyKey = "commandsProcessed")
    public void commandsProcessed(Object command) {
		setEnabled(true);
    }		

    @ModelDependent(modelKey ="CommandProcessorModel" , propertyKey = "commandAdded")
    public void commandAdded(Object command) {
        commands.addElement(command);
    }		

    @ModelDependent(modelKey ="CommandProcessorModel" , propertyKey = "commandStarted")
    public void commandStarted(Object command) {
    	refreshCommand(command);
    }		

    @ModelDependent(modelKey ="CommandProcessorModel" , propertyKey = "commandFinished")
    public void commandFinished(Object command) {
    	refreshCommand(command);
    }		

    @ModelDependent(modelKey ="CommandProcessorModel" , propertyKey = "commandRemoved")
    public void commandRemoved(Object command) {
    	commands.removeElement(command);
    }		

    protected void refreshCommand(Object command)
    {
    	int index = commands.indexOf(command);
    	commands.set(index, command);
    }
}
