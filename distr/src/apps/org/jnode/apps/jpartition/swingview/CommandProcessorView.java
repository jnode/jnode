package org.jnode.apps.jpartition.swingview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jnode.apps.jpartition.commands.framework.Command;
import org.jnode.apps.jpartition.commands.framework.CommandProcessor;
import org.jnode.apps.jpartition.commands.framework.CommandProcessorListener;
import org.jnode.apps.jpartition.model.UserFacade;

public class CommandProcessorView extends JPanel implements CommandProcessorListener {
    /**
     * 
     */
    private static final long serialVersionUID = 4411987954528000167L;

    private DefaultListModel commands = new DefaultListModel();
    private JList commandsUI = new JList(commands);
    private JButton btnApply = new JButton("Apply");

    public CommandProcessorView() {
        setBorder(BorderFactory.createTitledBorder("pending commands"));

        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.add(btnApply, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(btnPanel, BorderLayout.EAST);
        panel.add(new JScrollPane(commandsUI), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        btnApply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae_) {
                CommandProcessorView.this.setEnabled(false);
                UserFacade.getInstance().applyChanges();
            }
        });

        UserFacade.getInstance().addCommandProcessorListener(this);
    }

    protected void refreshCommand(Object command) {
        int index = commands.indexOf(command);
        commands.set(index, command);
    }

    public void commandAdded(CommandProcessor processor, Command command) {
        commands.addElement(command);
    }

    public void commandFinished(CommandProcessor processor, Command command) {
        refreshCommand(command);
    }

    public void commandRemoved(CommandProcessor processor, Command command) {
        commands.removeElement(command);
    }

    public void commandStarted(CommandProcessor processor, Command command) {
        refreshCommand(command);
    }
}
