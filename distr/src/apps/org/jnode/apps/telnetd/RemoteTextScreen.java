package org.jnode.apps.telnetd;

import java.io.IOException;

import net.wimpi.telnetd.io.BasicTerminalIO;

import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.x86.AbstractPcBufferTextScreen;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class RemoteTextScreen extends AbstractPcBufferTextScreen implements TextScreen {
	private final BasicTerminalIO terminalIO;
	public RemoteTextScreen(BasicTerminalIO terminalIO)
	{
		super(terminalIO.getColumns(), terminalIO.getRows());
		this.terminalIO = terminalIO;
	}

	@Override
	protected void setParentCursor(int x, int y) {
		try {
			terminalIO.setCursor(x, y);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sync() {
		
	}
}
