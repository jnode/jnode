package org.jnode.apps.jpartition.swingview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.model.Bounded;

import java.awt.Rectangle;

abstract public class DiskAreaView<T extends Bounded> extends JComponent
{
	final private int DEFAULT_PIXELS_PER_BYTE = 1;

	final protected int borderWidth = 5;
	final protected ErrorReporter errorReporter;

	protected T bounded;
	protected double pixelsPerByte = DEFAULT_PIXELS_PER_BYTE;

	public DiskAreaView(ErrorReporter errorReporter)
	{
		this.errorReporter = errorReporter;
		setLayout(null);
		setOpaque(true);

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

		update();
	}

	abstract protected Color getColor(T bounded);

	protected void setInfos(String infos)
	{
		setToolTipText(infos);
	}

	public void update()
	{
		if(bounded == null)
		{
			pixelsPerByte = DEFAULT_PIXELS_PER_BYTE;
		}
		else
		{
			double size = (bounded.getEnd() - bounded.getStart() + 1);
			pixelsPerByte = (double) getWidth() / size;
		}

		Color borderColor = getColor(bounded);
		Border line = BorderFactory.createLineBorder(borderColor, borderWidth);
		//Border space = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		//setBorder(BorderFactory.createCompoundBorder(space, line));
		setBorder(line);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Rectangle r = g.getClipBounds();
		g.setColor(getBackground());
		g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
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
