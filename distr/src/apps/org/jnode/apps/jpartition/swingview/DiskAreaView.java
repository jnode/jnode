package org.jnode.apps.jpartition.swingview;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

abstract public class DiskAreaView extends JComponent
{
	public DiskAreaView(Color borderColor)
	{
		setLayout(null);
		setBorder(BorderFactory.createLineBorder(borderColor, 2));

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

	protected void showMenu(MouseEvent event)
	{
		if(event.isPopupTrigger())
		{
			Action[] actions = getActions();
			if((actions != null) && (actions.length != 0))
			{
				JPopupMenu menu = new JPopupMenu("context menu");
				for(Action action : actions)
				{
					if(action == null)
					{
						menu.addSeparator();
					}
					else
					{
						menu.add(new JMenuItem(action));
					}
				}

				menu.show(event.getComponent(), event.getX(), event.getY());
			}
		}
	}

	abstract protected Action[] getActions();
}
